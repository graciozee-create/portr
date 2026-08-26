# Pleasure Horizons — Полный гайд для AI-агента по анализу кода

## 1. Что это за проект

**Pleasure Horizons** — мод для Minecraft NeoForge 1.21.1 + GeckoLib 4.9.2, порт с Fabric 1.21.6. Добавляет девушек-компаньонов с продвинутым AI, сценами, бронёй, поселениями и выживанием.

**Репозиторий:** `graciozee-create/portr`
**Ветка:** `arena/01a02ef4-portr`
**CI:** GitHub Actions `.github/workflows/build.yml`, триггер на PR в `main`

**Стек:**
- Java 21
- NeoForge 21.1.242
- GeckoLib 4.9.2
- Mojang маппинги

## 2. Архитектура

### 2.1 Иерархия Entity (сверху вниз)

```
GirlEntity (базовый класс: модель, инвентарь, анимации)
  └── GirlSceneEntity (сцены, relationship, override anims, downed state)
       └── TameableGirlEntity (приручение, владелец, carry, AI-голы, анти-застревание)
            └── SettlementGirlEntityAI (боевой AI, лук, settlements)
                 ├── LucyEntity, MikaEntity, MomoEntity (оригинальные PH)
                 ├── SlimeEntity, KoboldEntity, CoppieEntity (оригинальные PH)
                 └── AllieEntity, BiaEntity, GalathEntity, GoblinEntity,
                     JennyEntity, ManglelieEntity (из Jenny Mod)
```

**Важно:** Все девушки используют ОДИН `GirlModel` и ОДИН `GirlRenderer`, которые выбирают ассеты по `girl.getGirlID()`:
- Модель: `geo/dressed/<id>.geo.json` или `geo/nude/<id>.geo.json`
- Текстура: `textures/entities/<id>.png`
- Анимация: `animations/<id>.animation.json`

### 2.2 Ключевые директории

```
src/main/java/com/sandymandy/pleasurehorizons/
├── PleasureHorizons.java          — главный класс мода, регистрация событий
├── entity/
│   ├── base/
│   │   ├── GirlEntity.java        — базовый: модель, инвентарь (18 слотов + рюкзак), анимации
│   │   ├── GirlSceneEntity.java   — сцены, relationship levels, strip, downed
│   │   └── wild/WildGirlEntity.java — дикие (не приручаемые) девушки
│   ├── base/tamable/
│   │   ├── TameableGirlEntity.java — ★ ГЛАВНЫЙ ФАЙЛ: приручение, владелец, carry,
│   │   │                            anti-stuck, water escape, combat mobility, tank/taunt,
│   │   │                            follow teleport, preemptive aggro (~1800 строк)
│   │   └── SettlementGirlEntityAI.java — боевой AI (лук, settlement)
│   ├── girls/                     — конкретные девушки (LucyEntity, AllieEntity, etc.)
│   └── ai/goal/                   — AI-голы (20+ штук)
│       ├── GirlFollowOwnerGoal.java
│       ├── GirlGuardOwnerGoal.java — защита владельца (Mob+Enemy, не только Monster)
│       ├── GirlHarvestCropsGoal.java
│       ├── GirlChopTreesGoal.java
│       ├── GirlCookGoal.java
│       ├── GirlHuntGoal.java
│       ├── GirlSelfHealGoal.java
│       ├── GirlDeliverLootGoal.java
│       ├── GirlOpenDoorGoal.java
│       └── StripGoal.java
├── client/
│   ├── render/
│   │   ├── GirlModel.java         — выбирает geo/texture/animation по girlID
│   │   └── GirlRenderer.java      — рендеринг: head tracking, jiggle physics, carry pose,
│   │                                bone overrides, partner skin (steve tree), armor UV shift
│   ├── rendering/layers/
│   │   └── BoneOverrideRenderLayer.java — override слой для steve/брони
│   └── gui/screen/                — GUI: инвентарь, HUD, сцены, поселения
├── networking/
│   ├── C2S/                       — клиент→сервер пакеты (11 штук)
│   └── S2C/                       — сервер→клиент пакеты (7 штук)
├── registries/
│   ├── GirlRegistry.java          — entity types + attributes
│   └── InventoryButtonRegistry.java — кнопки инвентаря
├── item/
│   ├── PleasureHorizonsSpawnEggs.java — spawn eggs (DeferredSpawnEggItem)
│   ├── PleasureHorizonsItems.java     — предметы (lamp, coin, staff, tribe egg)
│   └── items/                     — классы предметов
├── util/
│   ├── managers/TamedGirlRegistry.java + TamedGirlSavedData.java — персистентный реестр
│   ├── variables/Scene.java, GirlRole.java, CustomGirlProfile.java
│   └── json/CustomGirlLoader.java — загрузка JSON профилей кастомных девушек
├── command/GirlsCommand.java      — /girls summon|spawn|call|role|reload
└── config/FreecamConfig.java, GirlsConfig.java — конфиги

src/main/resources/
├── assets/pleasurehorizons/
│   ├── geo/dressed/ + geo/nude/   — GeckoLib модели (.geo.json)
│   ├── animations/                — GeckoLib анимации (.animation.json)
│   ├── textures/entities/         — текстуры девушек
│   ├── textures/item/             — текстуры предметов
│   ├── sounds/                    — звуки (OGG файлы)
│   ├── lang/en_us.json + ru_ru.json — локализация
│   └── models/item/               — JSON модели предметов
├── data/pleasurehorizons/
│   ├── neoforge/biome_modifier/   — естественный спавн в Overworld
│   ├── recipe/                    — рецепты крафта
│   └── worldgen/                  — структуры деревни
```

### 2.3 Нейминг ассетов (критично!)

Кости в `.geo.json` файлах имеют стандартные имена, которые рендерер ожидает:
- **Обязательные:** `torso`, `head`, `armL`, `armR`, `lowerArmL`, `lowerArmR`
- **Для carry:** `legL`, `legR`, `shinL`, `shinR` (если нет — carry pose для ног не работает)
- **Для jiggle:** `boobs` или `boobL`+`boobR`, `belly`, `cheekL`, `cheekR`
- **Партнёр:** `steve` (полный скелет игрока, скрыт в обычном проходе)
- **Аксессуары (скрывать!):** `coin`, `energyBallL/R`, `offhand`, `weapon`, `customHandL/R`, `blocks`

## 3. Ключевые системы

### 3.1 Anti-stuck (TameableGirlEntity.tick())

5-ступенчатая система выхода из застревания. Срабатывает ТОЛЬКО когда `!getNavigation().isDone()` (девушка активно pathing):
1. 1с: прыжок
2. 2с: поворот 90° + прыжок
3. 3с: пересчёт пути
4. 4с: прыжок + рывок вбок
5. 6с: телепорт к владельцу

**НЕ срабатывает** когда девушка idle, sitting, guarding — это правильно.

### 3.2 Water escape (TameableGirlEntity.tick())

Проактивная система:
- +30% скорости в воде
- Каждые 0.5с: прыжок + плыть к ближайшему берегу (`findNearestShore()`)
- Если под водой: поиск воздуха в 4 направлениях
- Через 4с: телепорт к владельцу или force-jump

### 3.3 Combat mobility (TameableGirlEntity.tick())

- При target: убрать water malus, +40% скорость
- Combat stuck: если цель рядом но путь длинный → прыгать

### 3.4 Preemptive aggro (TameableGirlEntity.tick())

Каждые 10 тиков сканирует мобов в 8 блоках от ВЛАДЕЛЬЦА:
- Моб атакует владельца → переключить на девушку
- Моб без цели и ближе к владельцу → переключить на девушку

### 3.5 Tank/taunt (doHurtTarget)

Когда девушка бьёт моба, который атакует владельца → переключить цель моба на девушку.

### 3.6 Carry (toggleCarry/putDown/getVehicleAttachmentPoint)

Девушка骑上 игрока (passenger). Серверный carry-sync через ClientboundSetPassengersPacket.
Кости carry pose: `legL/R`, `shinL/R`, `armL/R`, `lowerArmL/R`.
First-person framing для локального носильщика.

### 3.7 Сцены (GirlSceneEntity + Scene.java)

Сцены определяются в `getScenes()` каждой девушки. Типы: `stationary`, `onPlayer`, `onBed`.
Каждая сцена имеет `requiredRelationshipLevel` (0-10).
Strip — отдельная анимация (`animation.<id>.strip`), если нет — `hasStripAnim()` = false.

### 3.8 Сеть (18 payload: 11 C2S + 7 S2C)

S2C пакеты НЕ импортируют `net.minecraft.client.*` — идут через рефлексию в `ClientPacketHandlers`.

## 4. Известные ограничения и принятые решения

1. **GeckoLib 4.9.2 ≠ 5.x** — API render-state из GeckoLib 5 недоступен. Рендеринг через `renderRecursively`.
2. **`StreamCodec.composite` максимум 6 полей** — для 7+ пиши кодек руками.
3. **`LivingEntity` нет геттера `yBodyRot`** — читай публичное поле.
4. **Блоки:** `onUse` → `useWithoutItem`, `onPlaced` → `setPlacedBy`.
5. **Датапак-папки в единственном числе:** `recipe/`, `loot_table/`, `advancement/`.
6. **`Monster.class` не включает Phantom** — Phantom это `FlyingMob implements Enemy`. Guard-голы используют `Mob.class + instanceof Enemy`.
7. **`Path.getLength()`** не существует в 1.21.1 → используй `Path.getNodeCount()`.
8. **`navigation.moveTo(BlockPos, speed)`** не существует → используй `moveTo(x, y, z, speed)`.
9. **`DeferredSpawnEggItem`** — стандартный NeoForge spawn egg, не устанавливает tamed.
10. **Biome modifier спавн** — weight 1 (очень редко, как ведьма).
11. **Приручение:** 1/3 шанс с первого раза. Предмет определяется `isAttractedTo()`.

## 5. Что проверять при анализе

### 5.1 Производительность

- [ ] `tick()` не делает тяжёлых операций каждый тик (entity scan, getEntitiesOfClass)
- [ ] Preemptive aggro scan — каждые 10 тиков, радиус 8 от владельца (не от девушки)
- [ ] Anti-stuck — только при активном pathing
- [ ] Water escape — `findNearestShore()` spiral search до 12 блоков (может быть тяжёлым)
- [ ] `TamedGirlRegistry.update()` — каждый chunk crossing + каждые 40 тиков

### 5.2 Корректность

- [ ] Все `hasStripAnim()` соответствуют наличию анимации `strip` в `.animation.json`
- [ ] `isAttractedTo()` возвращает реальный предмет (не null)
- [ ] `getScenes()` — все animation IDs существуют в `.animation.json`
- [ ] Аксессуарные кости (`coin`, `energyBall`, `offhand`) скрыты в конструкторе
- [ ] `boneVisibility`/`boneColorOverrides`/`boneTextureOverrides` очищаются каждый кадр в рендерере
- [ ] S2C пакеты не импортируют `net.minecraft.client.*`

### 5.3 Безопасность сети

- [ ] C2S пакеты проверяют владельца, расстояние, состояние
- [ ] ThrustKeybind/CumKeybind принимаются только от участника сцены
- [ ] InventoryButtonC2SPacket проверяет exact menu + distance ≤ 8
- [ ] GirlCustomize/KoboldCustomize требуют владельца + активную preview-сессию

### 5.4 Утечки памяти

- [ ] `PENDING_CALLS` очищается при server restart
- [ ] `TamedGirlRegistry` удаляет записи при `remove(reason.shouldDestroy())`
- [ ] `boneTextureOverrides` не накапливаются между кадрами
- [ ] `combatSpeedBoosted` корректно сбрасывается

### 5.5 Локализация

- [ ] Паритет en_us.json = ru_ru.json (текущий: 504 = 504)
- [ ] Нет `translatable` ключей без соответствующего перевода
- [ ] `commands.pleasurehorizons.girls.unknown_type` содержит всех зарегистрированных девушек

## 6. Тестовые сценарии

### Ванильное выживание (без команд):
1. Найти девушку в мире (biome modifier, weight 1)
2. Приручить (дать нужный предмет, 1/3 шанс)
3. Открыть инвентарь (ПКМ)
4. Нажать Talk → список сцен
5. Нажать Strip → раздевание
6. Следование за игроком (Follow toggle)
7. Охрана (Guard toggle) → мобы атакуют девушку
8. Попадание в воду → выход на берег
9. Застревание за стеной → anti-stuck срабатывает
10. Телепорт при G → все девушки призываются

### Предметы Jenny Mod:
1. Лампа Элли → спавнит Allie (3 использования)
2. Монета Галата → toggle Galath
3. Посох Дракона → управление кобольдами
4. Яйцо племени → 4 кобольда

## 7. Как собирать

Локально нет javac/gradle. Единственная сборка — GitHub Actions.
После push: `gh run list --branch arena/01a02ef4-portr --limit 1`
Проверка ошибок:
```bash
ID=$(gh api repos/graciozee-create/portr/actions/runs/<RUN_ID>/jobs --jq '.jobs[0].id')
gh api repos/graciozee-create/portr/check-runs/$ID/annotations \
  --jq '.[]|select(.path|endswith(".java"))|"\(.path):\(.start_line): \(.message)"'
```

## 8. Правила коммита

1. После каждого логического этапа: `git add -A && git commit && git push origin arena/01a02ef4-portr`
2. Дождись CI: `gh run list --branch arena/01a02ef4-portr --limit 1 --json status,conclusion,headSha`
3. Сверь `headSha` с `git rev-parse HEAD`
4. Не трогай `.github/workflows/*` — нет права `workflows`
5. Кнопок в инвентаре: максимум 7 слева, 6 справа
6. Любая новая цель ИИ проверяет: `!isPassenger()`, `!isDowned()`, `!isSceneActive()`
