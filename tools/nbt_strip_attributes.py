#!/usr/bin/env python3
"""Byte-surgical removal of the stale ``attributes`` list from girl_village entity NBTs.

Why: 1.21.1 ``LivingEntity#readAdditionalSaveData`` restores attribute base values from
entity NBT (verified against MCP-1.21 sources). The three girl_village entity templates
were captured from the ORIGINAL Fabric mod, so they pin stale bases
(movement_speed 0.2/0.15/0.2, follow_range 16) that override this port's buffed builders
(0.3/0.28/0.3, follow_range 100) and the server-config multipliers for every
structure-spawned village girl.

The fix removes the whole ``attributes`` list entry from ``entities[0].nbt`` at the byte
level and re-gzips. Everything else in the serialized NBT stays byte-identical, so no
format drift is possible. Idempotent: re-running on a patched file is a no-op.
"""
import gzip
import struct
import sys

TAG_END = 0
TAG_BYTE = 1
TAG_SHORT = 2
TAG_INT = 3
TAG_LONG = 4
TAG_FLOAT = 5
TAG_DOUBLE = 6
TAG_BYTE_ARRAY = 7
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10
TAG_INT_ARRAY = 11
TAG_LONG_ARRAY = 12


class SpanReader:
    """NBT reader that records the exact byte span of each compound child entry."""

    def __init__(self, data):
        self.data = data
        self.pos = 0

    def skip_payload(self, tag):
        d = self.data
        if tag == TAG_BYTE:
            self.pos += 1
        elif tag == TAG_SHORT:
            self.pos += 2
        elif tag == TAG_INT:
            self.pos += 4
        elif tag == TAG_LONG:
            self.pos += 8
        elif tag == TAG_FLOAT:
            self.pos += 4
        elif tag == TAG_DOUBLE:
            self.pos += 8
        elif tag in (TAG_BYTE_ARRAY, TAG_INT_ARRAY, TAG_LONG_ARRAY):
            width = {TAG_BYTE_ARRAY: 1, TAG_INT_ARRAY: 4, TAG_LONG_ARRAY: 8}[tag]
            n = struct.unpack('>i', d[self.pos:self.pos + 4])[0]
            self.pos += 4 + n * width
        elif tag == TAG_STRING:
            n = struct.unpack('>H', d[self.pos:self.pos + 2])[0]
            self.pos += 2 + n
        elif tag == TAG_LIST:
            elem = d[self.pos]
            self.pos += 1
            n = struct.unpack('>i', d[self.pos:self.pos + 4])[0]
            self.pos += 4
            for _ in range(n):
                self.skip_payload(elem)
        elif tag == TAG_COMPOUND:
            while True:
                start = self.pos
                child_type = self.read_named_type()
                if child_type == TAG_END:
                    break
                self.skip_name()
                self.skip_payload(child_type)
                del start
        else:
            raise ValueError('unknown tag %d at %d' % (tag, self.pos))

    def read_named_type(self):
        t = self.data[self.pos]
        self.pos += 1
        return t

    def skip_name(self):
        n = struct.unpack('>H', self.data[self.pos:self.pos + 2])[0]
        self.pos += 2 + n

    def read_name(self):
        n = struct.unpack('>H', self.data[self.pos:self.pos + 2])[0]
        self.pos += 2
        name = self.data[self.pos:self.pos + n].decode('utf-8')
        self.pos += n
        return name

    def compound_children(self):
        """Read a compound body; yield (name, tag_type, entry_start, entry_end)."""
        children = []
        while True:
            entry_start = self.pos
            child_type = self.read_named_type()
            if child_type == TAG_END:
                break
            name = self.read_name()
            self.skip_payload(child_type)
            children.append((name, child_type, entry_start, self.pos))
        return children


def entry_payload_pos(r, start):
    """Position the reader at the payload of a compound child entry starting at ``start``."""
    r.pos = start
    r.read_named_type()
    r.skip_name()
    return r.pos


def find_attributes_span(data):
    """Return (start, end) of the entities[0].nbt 'attributes' entry, or None."""
    r = SpanReader(data)
    assert r.read_named_type() == TAG_COMPOUND, 'root must be a compound'
    r.skip_name()
    root_children = r.compound_children()
    entities = next(c for c in root_children if c[0] == 'entities')
    assert entities[1] == TAG_LIST, 'entities must be a list'
    # Walk into the first (and only) entity compound.
    r.pos = entry_payload_pos(r, entities[2])
    elem_type = r.read_named_type()
    assert elem_type == TAG_COMPOUND
    count = struct.unpack('>i', data[r.pos:r.pos + 4])[0]
    r.pos += 4
    assert count == 1, 'expected exactly one entity, got %d' % count
    entity_children = r.compound_children()
    nbt = next(c for c in entity_children if c[0] == 'nbt')
    assert nbt[1] == TAG_COMPOUND
    r.pos = entry_payload_pos(r, nbt[2])
    nbt_children = r.compound_children()
    for name, tag_type, start, end in nbt_children:
        if name == 'attributes':
            assert tag_type == TAG_LIST
            return start, end
    return None


def strip(path):
    raw = gzip.open(path, 'rb').read()
    span = find_attributes_span(raw)
    if span is None:
        print('%s: no attributes entry (already stripped)' % path)
        return False
    start, end = span
    new_raw = raw[:start] + raw[end:]
    # Re-parse fully to prove the spliced stream is still valid NBT.
    check = SpanReader(new_raw)
    check.read_named_type()
    check.skip_name()
    check.skip_payload(TAG_COMPOUND)
    assert check.pos == len(new_raw), 'trailing bytes after splice: %d != %d' % (
        check.pos, len(new_raw))
    with open(path, 'wb') as f:
        f.write(gzip.compress(new_raw, mtime=0))
    print('%s: removed %d bytes (offset %d..%d)' % (path, end - start, start, end))
    return True


if __name__ == '__main__':
    changed = [strip(p) for p in sys.argv[1:]]
    sys.exit(0 if changed else 1)
