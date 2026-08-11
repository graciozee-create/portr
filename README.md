# Pleasure Horizons — NeoForge 1.21.1

An in-progress NeoForge port of the Fabric mod *Pleasure Horizons* (adult-themed, 18+).

> **Status: playable preview, not feature-complete.**
> The girls spawn, render, animate, can be tamed and befriended, and the village
> generates. The deeper systems (scenes, the girl inventory GUI, appearance
> customisation, settlements, freecam) are **not** ported yet. See
> [What works / what doesn't](#what-works--what-doesnt).

---

## Installing (for players)

1. Install **Minecraft 1.21.1** and **NeoForge 21.1.80** or newer.
2. Download **GeckoLib 4.x for NeoForge 1.21.1** (required — the mod will not
   load without it): <https://www.curseforge.com/minecraft/mc-mods/geckolib>
3. Drop both jars into your `mods` folder:
   - `geckolib-neoforge-1.21.1-<version>.jar`
   - `pleasure-horizons-<version>.jar`
4. Launch.

GeckoLib is the **only** required dependency.

### Multiplayer

Install the mod **and** GeckoLib on both the server and every client.
The mod is safe to load on a dedicated server.

---

## Getting started in game

Girls do **not** spawn naturally in the wild — same as the original mod. You get
them in one of three ways:

1. **Creative tab** — "Pleasure Horizons" contains all six spawn eggs.
2. **Crafting** — most spawn eggs are a shapeless recipe of one egg
   (`c:eggs`) plus that girl's favourite item:

   | Girl   | Favourite item | Egg craftable? |
   |--------|----------------|----------------|
   | Lucy   | Allium         | yes            |
   | Mika   | Poppy          | yes            |
   | Momo   | Poppy          | yes            |
   | Slime  | Slime Ball     | yes            |
   | Kobold | Raw Iron       | yes            |
   | Coppie | Copper Ingot   | no — creative tab only |

   (Coppie has no recipe upstream either; the favourite item still works for
   taming and gifts.)

3. **Girl villages** — a jigsaw structure that generates in plains, meadows,
   taiga and similar biomes, pre-populated with Lucy, Mika and Momo.
   Find one with `/locate structure pleasurehorizons:girl_village`.

### Interacting

- **Give her favourite item** to an untamed girl → tames her.
- **Give her favourite item** to your tamed girl → raises the relationship
  level (up to the per-girl maximum).
- **Sneak + right-click** → toggle sit / stand.
- **Right-click** → toggle follow / stay.
- **Meat** (anything in the `minecraft:wolf_food` tag) → heals her when hurt.

Taming, relationship level, sitting, following, pregnancy state and her
inventory all persist across world reloads.

---

## What works / what doesn't

### Working
- All 6 girls + the custom-girl entity: spawning, rendering, animation
  (idle / walk / run / sit / attack / blink)
- Spawn eggs, creative tab, crafting recipes
- Taming, relationship levels, gifts, sitting, following
- Girl village worldgen
- Blocks: Settlement Hub, House Tag, Carved Girl Pumpkin
- Sounds, advancement, loot tables, persistence
- Dedicated-server safe

### Not ported yet
- Scenes and all adult interactions (the partner rig is hidden outside scenes)
- The girl inventory / equipment GUI — right-click currently toggles following
  instead of opening it
- Appearance customisation (bone texture / colour / size overrides, jiggle physics)
- Held items are not rendered in her hand
- Settlement management (the Hub block exists but its logic is a stub)
- Freecam
- Config GUI — `ModConfig` is an in-memory stub with no persistence

Roughly 27% of the original's Java (4.8k of 17.5k lines) is ported so far.

---

## Building from source

```bash
./gradlew build
```

The jar lands in `build/libs/`. Requires JDK 21.

Note: `gradle/wrapper/gradle-wrapper.jar` is excluded by `.gitignore`, so a
fresh clone needs `gradle wrapper` (or an existing Gradle install) first.

---

## Credits

Original mod by **SandyMandy**. This is an unofficial port.
Licensed CC0-1.0, following the upstream project.
