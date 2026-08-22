#!/usr/bin/env python3
"""Minimal NBT dumper for inspecting structure entity NBT files."""
import gzip
import struct
import sys


class NBT:
    def __init__(self, data):
        self.data = data
        self.pos = 0

    def u1(self):
        v = self.data[self.pos]
        self.pos += 1
        return v

    def payload(self, t):
        d = self.data
        if t == 1:
            v = struct.unpack('>b', d[self.pos:self.pos + 1])[0]; self.pos += 1; return v
        if t == 2:
            v = struct.unpack('>h', d[self.pos:self.pos + 2])[0]; self.pos += 2; return v
        if t == 3:
            v = struct.unpack('>i', d[self.pos:self.pos + 4])[0]; self.pos += 4; return v
        if t == 4:
            v = struct.unpack('>q', d[self.pos:self.pos + 8])[0]; self.pos += 8; return v
        if t == 5:
            v = struct.unpack('>f', d[self.pos:self.pos + 4])[0]; self.pos += 4; return round(v, 4)
        if t == 6:
            v = struct.unpack('>d', d[self.pos:self.pos + 8])[0]; self.pos += 8; return round(v, 4)
        if t == 7:
            n = struct.unpack('>i', d[self.pos:self.pos + 4])[0]; self.pos += 4
            v = d[self.pos:self.pos + n]; self.pos += n; return v
        if t == 8:
            n = struct.unpack('>H', d[self.pos:self.pos + 2])[0]; self.pos += 2
            v = d[self.pos:self.pos + n].decode('utf-8', 'replace'); self.pos += n; return v
        if t == 9:
            it = self.u1()
            n = struct.unpack('>i', d[self.pos:self.pos + 4])[0]; self.pos += 4
            return [self.payload(it) for _ in range(n)]
        if t == 10:
            out = {}
            while True:
                t2 = self.u1()
                if t2 == 0:
                    return out
                name_len = struct.unpack('>H', d[self.pos:self.pos + 2])[0]; self.pos += 2
                name = d[self.pos:self.pos + name_len].decode('utf-8', 'replace'); self.pos += name_len
                out[name] = self.payload(t2)
        if t == 11:
            n = struct.unpack('>i', d[self.pos:self.pos + 4])[0]; self.pos += 4
            v = list(struct.unpack('>' + 'i' * n, d[self.pos:self.pos + 4 * n])); self.pos += 4 * n; return v
        if t == 12:
            n = struct.unpack('>i', d[self.pos:self.pos + 4])[0]; self.pos += 4
            v = list(struct.unpack('>' + 'q' * n, d[self.pos:self.pos + 8 * n])); self.pos += 8 * n; return v
        raise ValueError('unknown tag %d at %d' % (t, self.pos))

    def parse(self):
        t = self.u1()
        assert t == 10, 'root must be compound'
        name_len = struct.unpack('>H', self.data[self.pos:self.pos + 2])[0]; self.pos += 2
        name = self.data[self.pos:self.pos + name_len].decode('utf-8', 'replace'); self.pos += name_len
        return self.payload(10)


def load(path):
    raw = gzip.open(path).read() if path.endswith(('.nbt', '.mca')) else open(path, 'rb').read()
    try:
        return NBT(raw).parse()
    except OSError:
        pass
    return NBT(gzip.open(path).read()).parse()


if __name__ == '__main__':
    for path in sys.argv[1:]:
        print('==== %s' % path)
        root = load(path)
        # structure template: entities: [{pos, nbt}]
        for ent in root.get('entities', []):
            nbt = ent.get('nbt', {})
            interesting = {}
            for k in ('id', 'Health', 'PersistenceRequired', 'attributes', 'Invulnerable',
                      'Pos', 'Tags', 'CustomName'):
                if k in nbt:
                    interesting[k] = nbt[k]
            print(interesting)
