#!/usr/bin/env python3
"""Regenerate the small item/texture sprites as 16x16 pixel art.

The original textures were solid 15x15 colour swatches with a white band on top, which looked
unfinished in inventories. This script paints simple, readable pixel-art icons on a transparent
background and writes them as RGBA PNGs in src/main/resources/assets/pleasurehorizons/textures.

No external dependencies are needed: the PNG is written directly through zlib/struct.
"""
import os
import struct
import zlib

ROOT = os.path.join(os.path.dirname(__file__), "..",
                    "src/main/resources/assets/pleasurehorizons/textures")


def new(w=16, h=16, color=(0, 0, 0, 0)):
    return [[color for _ in range(w)] for _ in range(h)]


def px(img, x, y, color):
    h = len(img)
    w = len(img[0])
    if 0 <= x < w and 0 <= y < h:
        img[y][x] = color


def rect(img, x0, y0, x1, y1, color):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px(img, x, y, color)


def outline(img, x0, y0, x1, y1, color):
    for x in range(x0, x1 + 1):
        px(img, x, y0, color)
        px(img, x, y1, color)
    for y in range(y0, y1 + 1):
        px(img, x0, y, color)
        px(img, x1, y, color)


def disc(img, cx, cy, r, color, fill=True):
    for y in range(int(cy - r), int(cy + r) + 1):
        for x in range(int(cx - r), int(cx + r) + 1):
            if (x - cx) ** 2 + (y - cy) ** 2 <= r * r:
                px(img, x, y, color)
            elif fill is False and (x - cx) ** 2 + (y - cy) ** 2 <= (r + 1) ** 2:
                px(img, x, y, color if (x - cx) ** 2 + (y - cy) ** 2 > (r - 0.5) ** 2 else None)


def ring(img, cx, cy, r, color, thickness=2):
    for y in range(int(cy - r - 1), int(cy + r + 1) + 1):
        for x in range(int(cx - r - 1), int(cx + r + 1) + 1):
            d2 = (x - cx) ** 2 + (y - cy) ** 2
            if (r - thickness) ** 2 <= d2 <= r * r:
                px(img, x, y, color)


def line(img, x0, y0, x1, y1, color):
    dx = abs(x1 - x0)
    dy = -abs(y1 - y0)
    sx = 1 if x0 < x1 else -1
    sy = 1 if y0 < y1 else -1
    err = dx + dy
    while True:
        px(img, x0, y0, color)
        if x0 == x1 and y0 == y1:
            break
        e2 = 2 * err
        if e2 >= dy:
            err += dy
            x0 += sx
        if e2 <= dx:
            err += dx
            y0 += sy


def diamond(img, cx, cy, r, color):
    for y in range(cy - r, cy + r + 1):
        for x in range(cx - r, cx + r + 1):
            if abs(x - cx) + abs(y - cy) <= r:
                px(img, x, y, color)


def shade_top(img, x0, y0, x1, y1, color, amount=0x30):
    for x in range(x0, x1 + 1):
        px(img, x, y0, color)


def save(img, name):
    if not name.endswith(".png"):
        name += ".png"
    path = os.path.join(ROOT, "item", name)
    w = len(img[0])
    h = len(img)
    raw = b"".join(b"\x00" + b"".join(bytes(v) for v in row) for row in img)

    def chunk(tag, data):
        c = struct.pack(">I", len(data)) + tag + data
        return c + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)
    print(f"wrote {os.path.relpath(path, ROOT)}")


C = {
    "black": (24, 18, 24, 255),
    "outline": (30, 22, 34, 255),
    "white": (238, 234, 240, 255),
    "red": (206, 44, 66, 255),
    "red_hi": (236, 92, 110, 255),
    "green": (60, 132, 62, 255),
    "green_hi": (104, 178, 88, 255),
    "brown": (124, 66, 32, 255),
    "brown_hi": (170, 104, 60, 255),
    "brown_dark": (78, 40, 24, 255),
    "gold": (218, 168, 62, 255),
    "gold_hi": (246, 214, 118, 255),
    "gold_dark": (150, 104, 34, 255),
    "copper": (190, 106, 50, 255),
    "copper_hi": (224, 146, 86, 255),
    "silver": (188, 194, 210, 255),
    "silver_hi": (224, 229, 240, 255),
    "silver_dark": (120, 126, 142, 255),
    "cyan": (84, 190, 208, 255),
    "cyan_hi": (150, 228, 238, 255),
    "cyan_dark": (42, 110, 132, 255),
    "purple": (142, 82, 178, 255),
    "purple_hi": (180, 128, 214, 255),
    "purple_dark": (86, 44, 116, 255),
    "pink": (236, 118, 170, 255),
    "pink_hi": (250, 170, 204, 255),
    "pink_dark": (176, 70, 126, 255),
    "cream": (240, 226, 190, 255),
    "cream_dark": (204, 184, 138, 255),
    "teal": (52, 148, 138, 255),
    "teal_hi": (110, 192, 178, 255),
    "blue": (76, 128, 218, 255),
    "blue_hi": (128, 176, 236, 255),
    "blue_dark": (42, 76, 138, 255),
    "lime": (132, 198, 70, 255),
}

transparent = (0, 0, 0, 0)


def gift_red_rose():
    img = new()
    line(img, 8, 9, 7, 14, C["green"])
    px(img, 6, 12, C["green"])
    px(img, 5, 12, C["green_hi"])
    px(img, 6, 13, C["green"]) if False else None
    px(img, 9, 11, C["green"])
    px(img, 10, 11, C["green_hi"])
    # bud
    disc(img, 8, 6, 2, C["red"])
    px(img, 7, 5, C["red_hi"])
    px(img, 8, 5, C["red_hi"])
    px(img, 6, 7, C["red_dark"] if False else C["red"])
    return img


def gift_chocolate_box():
    img = new()
    rect(img, 1, 5, 14, 12, C["brown"])
    rect(img, 2, 4, 13, 6, C["brown_dark"])
    outline(img, 1, 4, 14, 6, C["brown_dark"])
    # chocolates
    rect(img, 3, 8, 6, 11, C["brown_dark"])
    rect(img, 8, 8, 11, 11, C["brown_dark"])
    px(img, 4, 8, C["gold_dark"])
    px(img, 7, 8, C["gold"])
    px(img, 9, 8, C["gold_dark"])
    px(img, 11, 8, C["gold"])
    return img


def gift_diamond_ring():
    img = new()
    ring(img, 8, 10, 4, C["gold"], 1)
    px(img, 7, 7, C["gold_hi"])
    disc(img, 8, 6, 2, C["cyan"])
    px(img, 7, 5, C["cyan_hi"])
    px(img, 8, 5, C["cyan_hi"])
    px(img, 6, 6, C["cyan_dark"])
    return img


def gift_teddy_bear():
    img = new()
    disc(img, 8, 10, 4, C["brown"])
    disc(img, 8, 5, 3, C["brown"])
    disc(img, 4, 5, 2, C["brown"])
    disc(img, 12, 5, 2, C["brown"])
    px(img, 4, 4, C["brown_dark"])
    px(img, 12, 4, C["brown_dark"])
    px(img, 7, 5, C["outline"])
    px(img, 9, 5, C["outline"])
    px(img, 8, 6, C["brown_dark"])
    px(img, 6, 8, C["brown_hi"])
    px(img, 7, 9, C["brown_hi"])
    return img


def gift_love_letter():
    img = new()
    rect(img, 2, 4, 13, 12, C["cream"])
    outline(img, 2, 4, 13, 12, C["cream_dark"])
    line(img, 2, 4, 8, 8, C["cream_dark"])
    line(img, 13, 4, 8, 8, C["cream_dark"])
    disc(img, 8, 7, 2, C["red"])
    px(img, 8, 5, C["red_hi"])
    return img


def gift_ancient_coin():
    img = new()
    disc(img, 8, 8, 6, C["gold"])
    ring(img, 8, 8, 4, C["gold_dark"], 1)
    rect(img, 7, 5, 9, 11, C["gold_hi"])
    px(img, 6, 6, C["gold_dark"])
    px(img, 10, 6, C["gold_dark"])
    px(img, 8, 4, C["gold_hi"])
    return img


def gift_copper_gear():
    img = new()
    disc(img, 8, 8, 5, C["copper"])
    for a in range(8):
        import math
        x = int(8 + 5.6 * math.cos(a * math.pi / 4))
        y = int(8 + 5.6 * math.sin(a * math.pi / 4))
        px(img, x, y, C["copper_hi"])
    disc(img, 8, 8, 2, C["copper_dark"] if False else C["gold_dark"])
    px(img, 7, 7, C["copper_hi"])
    px(img, 9, 9, C["copper_hi"])
    return img


def gift_crystal_slime():
    img = new()
    disc(img, 8, 9, 5, C["lime"])
    disc(img, 8, 8, 5, C["lime"])
    px(img, 6, 7, C["white"])
    px(img, 7, 7, C["white"])
    px(img, 9, 7, C["green_hi"])
    px(img, 5, 11, C["green_hi"])
    px(img, 11, 11, C["green_hi"])
    px(img, 6, 9, C["green"])
    px(img, 9, 9, C["green"])
    return img


def gift_dragon_scale():
    img = new()
    diamond(img, 8, 8, 5, C["teal"])
    diamond(img, 8, 8, 4, C["teal_hi"])
    diamond(img, 8, 8, 2, C["cyan"])
    px(img, 5, 8, C["blue_dark"])
    px(img, 11, 8, C["blue_dark"])
    px(img, 8, 3, C["blue_dark"])
    return img


def gift_enchanted_quill():
    img = new()
    line(img, 4, 12, 11, 4, C["purple"])
    line(img, 5, 12, 11, 5, C["purple_hi"])
    px(img, 6, 11, C["purple"])
    px(img, 7, 10, C["purple_hi"])
    px(img, 8, 9, C["purple_dark"])
    px(img, 9, 8, C["purple_hi"])
    px(img, 10, 7, C["purple_dark"])
    px(img, 13, 3, C["pink_hi"])
    disc(img, 12, 3, 1, C["pink"])
    return img


def gift_golden_honeycomb():
    img = new()
    for cy, cx in [(5, 6), (8, 4), (8, 8), (11, 6), (5, 9), (11, 9)]:
        disc(img, cx, cy, 2, C["gold"])
        disc(img, cx, cy, 2, C["gold_hi"])
        px(img, cx, cy, C["gold_dark"])
    px(img, 3, 8, C["gold_hi"])
    px(img, 13, 8, C["gold_hi"])
    return img


def gift_moonlight_lily():
    img = new()
    line(img, 8, 13, 8, 8, C["green"])
    for a in range(5):
        import math
        angle = a * 2 * math.pi / 5 - math.pi / 2
        x0 = int(8 + 2.5 * math.cos(angle))
        y0 = int(7 + 2.5 * math.sin(angle))
        x1 = int(8 + 5.2 * math.cos(angle))
        y1 = int(7 + 5.2 * math.sin(angle))
        line(img, x0, y0, x1, y1, C["white"])
        px(img, x1, y1, C["pink_hi"])
    disc(img, 8, 7, 2, C["gold"])
    px(img, 8, 6, C["gold_hi"])
    return img


def gift_mystic_herb():
    img = new()
    line(img, 8, 14, 8, 6, C["green"])
    line(img, 8, 10, 4, 6, C["green_hi"])
    line(img, 8, 9, 12, 5, C["green_hi"])
    disc(img, 4, 5, 1, C["lime"])
    disc(img, 12, 4, 1, C["lime"])
    disc(img, 8, 4, 1, C["lime"])
    return img


def gift_silver_bell():
    img = new()
    for y in range(5, 12):
        w = max(2, 12 - y)
        x0 = 8 - w // 2
        rect(img, x0, y, x0 + w, y, C["silver"])
    px(img, 8, 4, C["silver_dark"])
    px(img, 7, 5, C["silver_hi"])
    px(img, 9, 6, C["silver_hi"])
    px(img, 8, 13, C["silver_dark"])
    return img


def guide_book():
    img = new()
    rect(img, 2, 2, 13, 13, C["purple"])
    rect(img, 3, 3, 12, 12, C["purple_hi"])
    rect(img, 5, 4, 11, 11, C["cream"])
    px(img, 4, 5, C["gold"])
    px(img, 5, 6, C["pink_hi"])
    px(img, 8, 6, C["gold"])
    px(img, 5, 9, C["gold"])
    px(img, 8, 9, C["gold"])
    return img


def girl_wand():
    img = new()
    line(img, 8, 14, 8, 8, C["gold"])
    line(img, 7, 13, 8, 14, C["gold_dark"])
    disc(img, 8, 5, 3, C["pink"])
    px(img, 7, 4, C["pink_hi"])
    px(img, 8, 3, C["pink_hi"])
    disc(img, 8, 5, 1, C["gold_hi"])
    px(img, 6, 8, C["gold_dark"])
    px(img, 10, 8, C["gold_dark"])
    return img


def healing_charm():
    img = new()
    # heart charm with a small green gem
    line(img, 9, 3, 12, 3, C["silver"])
    disc(img, 11, 3, 1, C["silver"])
    disc(img, 6, 8, 3, C["red"])
    disc(img, 10, 8, 3, C["red"])
    line(img, 8, 5, 8, 5, C["red"])
    for y in range(5, 13):
        half = None
    disc(img, 8, 11, 3, C["red"])
    px(img, 7, 7, C["red_hi"])
    px(img, 10, 7, C["red_hi"])
    disc(img, 8, 11, 1, C["green_hi"])
    return img


def horny_potion():
    img = new()
    rect(img, 6, 3, 9, 3, C["silver_dark"])
    line(img, 7, 4, 9, 4, C["cream"])
    rect(img, 5, 5, 10, 6, C["cream_dark"])
    rect(img, 4, 6, 11, 12, C["cyan_dark"])
    rect(img, 5, 7, 10, 11, C["pink"])
    px(img, 6, 8, C["pink_hi"])
    px(img, 9, 7, C["pink_hi"])
    return img


def memory_crystal():
    img = new()
    diamond(img, 8, 9, 4, C["blue"])
    diamond(img, 8, 9, 3, C["blue_hi"])
    diamond(img, 8, 9, 1, C["cyan_hi"])
    px(img, 6, 7, C["cyan"])
    px(img, 9, 7, C["cyan"])
    px(img, 8, 3, C["blue_hi"])
    px(img, 8, 4, C["blue_hi"])
    return img


def milk_jug_full():
    img = new()
    rect(img, 4, 3, 11, 3, C["silver_dark"])
    line(img, 10, 4, 12, 3, C["cream_dark"])
    rect(img, 4, 4, 11, 12, C["white"])
    rect(img, 5, 5, 10, 11, C["cream"])
    px(img, 5, 6, C["white"])
    px(img, 6, 6, C["white"])
    rect(img, 5, 8, 8, 11, C["cyan_hi"])
    return img


def milk_jug_half():
    img = milk_jug_full()
    rect(img, 6, 8, 10, 11, C["cream"])
    px(img, 6, 9, C["cyan_hi"])
    px(img, 7, 9, C["cyan_hi"])
    return img


def milk_jug_empty():
    img = milk_jug_full()
    rect(img, 5, 7, 10, 12, C["cream_dark"])
    px(img, 6, 8, C["white"])
    px(img, 7, 8, C["white"])
    return img


def summoning_whistle():
    img = new()
    disc(img, 8, 10, 4, C["gold"])
    rect(img, 4, 9, 12, 12, C["gold"])
    px(img, 4, 9, C["gold_hi"])
    px(img, 5, 10, C["gold_hi"])
    disc(img, 4, 10, 2, C["gold_dark"])
    disc(img, 8, 10, 1, C["gold_dark"])
    px(img, 12, 10, C["gold_dark"])
    return img


def settlement_recruit_contract():
    img = new()
    rect(img, 3, 3, 12, 13, C["cream"])
    rect(img, 4, 4, 11, 12, C["white"])
    line(img, 5, 6, 10, 6, C["cream_dark"])
    line(img, 5, 8, 10, 8, C["cream_dark"])
    px(img, 5, 10, C["red"])
    px(img, 6, 10, C["red"])
    px(img, 13, 3, C["gold"])
    px(img, 13, 4, C["gold"])
    return img


def bond_bracelet():
    img = new()
    ring(img, 8, 8, 5, C["gold"], 1)
    disc(img, 8, 8, 2, C["pink"])
    disc(img, 8, 8, 1, C["pink_hi"])
    px(img, 5, 8, C["gold_hi"])
    px(img, 11, 8, C["gold_hi"])
    px(img, 8, 4, C["gold_hi"])
    return img


def dragon_staff():
    img = new()
    line(img, 5, 14, 10, 3, C["brown_dark"])
    line(img, 6, 14, 11, 3, C["brown"])
    disc(img, 10, 3, 2, C["cyan"])
    px(img, 9, 2, C["cyan_hi"])
    px(img, 11, 4, C["cyan_dark"])
    line(img, 9, 5, 7, 4, C["teal"])
    line(img, 11, 6, 13, 5, C["teal"])
    return img


def allies_lamp():
    img = new()
    rect(img, 7, 2, 8, 13, C["gold_dark"])
    rect(img, 5, 3, 10, 7, C["gold"])
    px(img, 6, 4, C["gold_hi"])
    px(img, 9, 4, C["gold_hi"])
    disc(img, 8, 5, 1, C["pink"])
    rect(img, 6, 8, 9, 9, C["silver"])
    rect(img, 5, 10, 10, 11, C["silver_dark"])
    px(img, 7, 12, C["gold_dark"])
    return img


def galath_coin():
    img = new()
    disc(img, 8, 8, 6, C["copper"])
    ring(img, 8, 8, 4, C["copper_hi"], 1)
    rect(img, 7, 5, 9, 11, C["copper_hi"])
    px(img, 8, 4, C["copper_hi"])
    px(img, 6, 6, C["copper_dark"] if False else C["copper"])
    px(img, 10, 10, C["copper"])
    return img


def spawn_egg():
    img = new()
    disc(img, 8, 9, 5, C["cream"])
    px(img, 6, 13, C["cream"])
    px(img, 7, 14, C["cream"])
    px(img, 9, 14, C["cream"])
    disc(img, 8, 9, 4, C["gold_hi"])
    px(img, 6, 7, C["white"])
    px(img, 9, 6, C["white"])
    px(img, 7, 10, C["gold_dark"])
    return img


def tribe_egg():
    img = new()
    disc(img, 8, 9, 5, C["purple"])
    px(img, 6, 13, C["purple"])
    px(img, 7, 14, C["purple"])
    px(img, 9, 14, C["purple"])
    disc(img, 8, 9, 4, C["purple_hi"])
    px(img, 6, 7, C["pink_hi"])
    px(img, 9, 7, C["pink_hi"])
    px(img, 8, 10, C["purple_dark"])
    return img


def main():
    icons = {
        "gift_red_rose": gift_red_rose,
        "gift_chocolate_box": gift_chocolate_box,
        "gift_diamond_ring": gift_diamond_ring,
        "gift_teddy_bear": gift_teddy_bear,
        "gift_love_letter": gift_love_letter,
        "gift_ancient_coin": gift_ancient_coin,
        "gift_copper_gear": gift_copper_gear,
        "gift_crystal_slime": gift_crystal_slime,
        "gift_dragon_scale": gift_dragon_scale,
        "gift_enchanted_quill": gift_enchanted_quill,
        "gift_golden_honeycomb": gift_golden_honeycomb,
        "gift_moonlight_lily": gift_moonlight_lily,
        "gift_mystic_herb": gift_mystic_herb,
        "gift_silver_bell": gift_silver_bell,
        "guide_book": guide_book,
        "girl_wand": girl_wand,
        "healing_charm": healing_charm,
        "horny_potion": horny_potion,
        "memory_crystal": memory_crystal,
        "milk_jug_full": milk_jug_full,
        "milk_jug_half": milk_jug_half,
        "milk_jug_empty": milk_jug_empty,
        "summoning_whistle": summoning_whistle,
        "settlement_recruit_contract": settlement_recruit_contract,
        "bond_bracelet": bond_bracelet,
        "dragon_staff": dragon_staff,
        "allies_lamp": allies_lamp,
        "galath_coin": galath_coin,
        "spawn_egg": spawn_egg,
        "tribe_egg": tribe_egg,
    }
    for name, fn in icons.items():
        save(fn(), name)


if __name__ == "__main__":
    main()
