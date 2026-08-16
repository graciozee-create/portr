# Инструкция для агента Cursor: полный аудит порта Pleasure Horizons

> Репозиторий: `graciozee-create/portr` — порт `colorgarden/Pleasure-Horizons-I18n`
> (ветка `reworked-girls`) с Fabric 1.21.6 на **NeoForge 1.21.1** (NeoForge `21.1.80`,
> GeckoLib `4.9.2`, маппинги Mojang).
> Ветка: `arena/01a000a8-portr` (PR #6). Отвечай по-русски.

---

## 0. Ограничения среды и что от тебя требуется

1. **Локальной сборки нет.** Нет `javac`/`gradle`, нет Maven Central / maven.neoforged.net.
   Единственная проверка компиляции — GitHub Actions, а у тебя, вероятно, нет доступа к
   Actions. Поэтому это **СТАТИЧЕСКИЙ аудит**: читаешь код и JSON, сверяешь утверждения с
   реальным содержимым файлов и с исходниками vanilla/NeoForge/GeckoLib (если есть сеть —
   см. §7).
2. **Не заявляй визуальный/рантайм-успех.** «Поза красивая», «занимает 50% экрана»,
   «звук играет один раз» — это доказывается только в запущенной игре. Без запуска честно
   пиши «runtime-only, нужен игровой тест».
3. **Не меняй код ради стиля.** Правки — только при **доказанном** дефекте (конкретный
   файл, строка, достижимый сценарий). Рабочий код не переписываем.
4. **Не коммить/не пушь сам**, если у тебя нет доступа к GitHub. Если доступ есть — работай
   только в ветке `arena/01a000a8-portr` и **никогда не делай force-push**. Лучший выход —
   отдай отчёт (и, опционально, `.patch`-файл), а не коммит.
5. **Отчёт** пиши в файл `CURSOR_AUDIT_REPORT.md` в корне репозитория (формат — §8).
   Не удаляй существующие `AGENT_INSTRUCTIONS_FOR_NEXT.md` / `HANDOFF_NEXT_AGENT.md`.

---

## 1. Карта репозитория

```
src/main/java/com/sandymandy/pleasurehorizons/
  entity/base/           GirlEntity (состояние+кость-оверрайды), GirlSceneEntity (сцены+анимации)
  entity/base/tamable/   TameableGirlEntity (carry, тогглы выживания), SettlementGirlEntityAI (бой)
  entity/base/wild/      WildGirlEntity
  entity/girls/          Mika/Lucy/Kobold/Momo/Coppie/Slime/CustomGirlEntity
  entity/ai/goal/        BedGoal, MoveToPlayerGoal, StationaryContactGoal, StopMovementGoal,
                         StripGoal, GirlSitGoal, GirlFollow/Gather/Harvest/Guard*/ChopTrees/
                         FeedOwner/Cook/Hunt/BowAttack/MeleeAttack/AttackSwitch/TrackOwner...
  client/render/         GirlRenderer (carry-поза, партнёр, jiggle, head-tracking), GirlModel
  client/rendering/layers/ BoneOverrideRenderLayer (кожа партнёра, слои 1-3)
  client/gui/screen/     GirlInventoryScreen (вкладки Main/Survival), GirlSceneScreen,
                         hud/GirlStatusOverlay, hud/SceneProgressOverlay, customize/*
  client/networking/     ClientPacketHandlers
  networking/C2S/, networking/S2C/, networking/PleasureHorizonsPackets.java
  command/               GirlsCommand (/girls reload|spawn|role)
  util/variables/        Scene, ScenePhase, SceneType, GirlRole
  util/                  GirlStatusCache, SceneKeyframeEventReloader, JigglePhysics, OffsetVertexConsumer

src/main/resources/assets/pleasurehorizons/
  animations/            <girl>.animation.json (7: mika,lucy,kobold,momo,coppie,slime,default)
  geo/dressed/ geo/nude/ <girl>.geo.json (14 файлов)
  keyframe_events/       lucy.json, mika.json, momo.json, example.json
  lang/                  en_us.json, ru_ru.json
```

Риги: `mika, lucy, kobold, momo, coppie, slime` + `default` (кастомные девушки,
`CustomGirlEntity`). Каждый риг существует в вариантах `dressed` и `nude`.

---

## 2. Жёсткие правила (нарушение = ошибка аудита)

- Клиент **не пишет** synched entity data (tracked data). Исключение принято осознанно:
  `setCurrentSexAnim` в `GirlSceneEntity` (клиентский выбор анимации, наследие оригинала).
- Кости GeckoLib **общие** между сущностями одного рига. Всё, что меняешь в `preRender`/
  `renderRecursively`, нужно **запомнить и восстановить** (см. `GirlRenderer.renderRecursively`
  + `postRender` + `BoneRenderState`).
- S2C-пакеты **не должны импортировать** `net.minecraft.client.*` (иначе падение на dedicated
  server) — через `ClientPacketHandlers` рефлексией.
- `StreamCodec.composite` — **максимум 6 полей**; для 7+ — ручной кодек (см. `Scene`).
- Новая ИИ-цель обязана проверять `!isPassenger() && !isDowned() && !isSceneActive()`
  (и, где уместно, `!isSitting() && !isFollowing()`).
- Любой новый ключ локализации — **одновременно** в `en_us.json` и `ru_ru.json` (паритет).
- Не оставляй заглушек, молча возвращающих `false`/пустоту, если функция заявлена как
  портированная. Честный «shift-click не поддержан» (`quickMoveStack` → `ItemStack.EMPTY`)
  — это НЕ заглушка, это явное «не реализовано».
- Не меняй `.github/workflows/*`.
- Не удаляй функционал только потому, что «не нашёл отправителя grep-ом» — проверь весь путь.

---

## 3. Ground truth (проверено, НЕ перепроверять как «баг»)

Чтобы не тратить время и не плодить ложные срабатывания, прими как факт:

- **Сторона переноски:** девушка на ПРАВОЙ руке носильщика. Боковой вектор =
  `(-cos(yaw), -sin(yaw))`. Константы в `TameableGirlEntity`: `CARRY_RIGHT_OFFSET=0.30`,
  `CARRY_FORWARD_OFFSET=0.10`, `CARRY_VERTICAL_OFFSET=-0.12`. Carry-поза в `GirlRenderer`:
  lean `-8°`, sway `1.5°`, бедро `-82°`, голень `+72°` (градусы Blockbench).
- **Замах атаки:** vanilla 1.21.1 сбрасывает `swinging` ТОЛЬКО в `Player#serverAiStep()`,
  поэтому мод использует одноразовый `attackAnimationPending` (НЕ `swinging`).
- **Резервирование сцен:** `activeScenes` = `Map<игрок → девушка>` (per-player); `usedBeds` =
  `Map<девушка → кровать>` (блокирует кровать для всех). `PlayerLoggedOutEvent` снимает оба.
- **Пассажир на игроке:** `ChunkMap.TrackedEntity#updatePlayer` начинается с
  `if (player != entity)` — игрок не получает трекинг о себе; `syncCarryState()` явно шлёт
  `ClientboundSetPassengersPacket`. Клиент позиционирует пассажира каждый кадр через
  `ClientLevel#tickPassenger → rideTick → positionRider`. `snapToCarrier` **не нужен** и
  возвращать его нельзя.
- **Сцены:** в `ON_PLAYER`/bed-сценах **игрок катает девушку** (`player.startRiding(girl)`),
  то есть девушка — vehicle, а не passenger. Carry-поза (`getVehicle() instanceof Player`) в
  сцене недостижима. Не «чини» это guard-ом — это не баг.
- **Слои текстур:** `boneTextureOverridesLayer3` объявлена и читается рендер-слоем, но
  **нигде не записывается** — всегда пуста. «Не очищается layer3» — НЕ баг.
- **Прогресс сцены идёт двумя путями** (keyframe `thrust` + tick-fallback при удержании
  клавиши) — намеренно; это не «случайное двойное ускорение», а принятый дизайн (сервер не
  знает текущую клиентскую анимацию).
- **Client-originated timing:** `AnimationFinish`/`SoundEventSync` принимаются от авторитетного
  участника его собственной сцены с rate-limit (finish ≤ 1 раз / 5 тиков, keyframe ≤ 1 payload
  / серверный тик, regex `[a-z0-9_, \-]{1,128}`). Это осознанный gameplay-trust, НЕ ужесточай
  без конкретного доказанного вреда.
- **`kobold` не имеет `strip`** (есть `hasStripAnim()=false`), **`default` не имеет `blink`**
  (`hasBlinkAnimation()=false`) — намеренно.
- **HUD-панель статуса** выключена по умолчанию, клавиша **N**. Смена ролей — клавиша **B**,
  плюс `/girls role <idle|worker|guard|cook>`.
- **Принятые решения, которые НЕ откатывать:** global attack/clothing broadcasts →
  tracker-scoped (`44fa38e`); render-time `snapToCarrier` удалён; нет общего scale модели для
  первого лица; нет generic Mika-carry как физической позы; клиент не пишет slim/wide;
  non-freecam `ModConfig` = in-memory defaults; `Vec3dInputSection` = кнопки ±0.1;
  `TamedGirlManager.cleanupDeadGirls` отключён (выгруженная ≠ мёртвая).

---

## 4. Чек-листы по системам

### 4.1 Анимации
- [ ] Каждый id, который возвращает Java-код или `Scene`, существует в соответствующем
      `<girl>.animation.json` (проверь **скриптом**, §6.1).
- [ ] Базовые id для каждого рига: `idle`, `walk`, `run`, `attack0/1/2`, `sit`, `downed`,
      `strip` (кроме kobold), `blink` (кроме default). Сверь, что выбор в `handleAnimations`
      и `handleAttackAnimations` соответствует реальным именам.
- [ ] Контроллер не зависает после: конца сцены, снятия с рук, смерти/downed, прерывания
      сцены, исчезновения партнёра, смены измерения / disconnect игрока.
- [ ] `AnimationFinishC2SPacket` не продвигает чужую сцену и не вызывает повторных/пропущенных
      переходов фаз (см. `acceptsAnimationFinishFrom`, `animationFinished`).
- [ ] `SoundEventSyncC2SPacket` + keyframe-обработка корректно разбирают combined payload
      (запятые/дефисы) — `SceneKeyframeEventRegistry.tokenize`.
- [ ] Один keyframe не проигрывает звук дважды для участника сцены (участник — локально,
      наблюдатели — через relay `RunAnimEventsS2CPacket`).
- [ ] `PlayAttackAnimationS2CPacket` отправляется только трекерам и реально запускает swing
      (`triggerSwing` → `attackAnimationPending` → PLAY_ONCE).
- [ ] Серверные animation queues очищаются в правильный момент и не теряют событие
      (`animationEventQueueServer.clear()` в конце tick).
- [ ] Скорость/прогресс сцены нельзя случайно ускорить сильнее задуманного (оба пути —
      намеренные, см. §3).
- [ ] Клиент не пишет synched data (кроме принятого `setCurrentSexAnim`).

**GeckoLib + общие кости:**
- [ ] Carry-поза применяется ПОСЛЕ анимации контроллера (в `renderRecursively`), не до.
- [ ] Состояние каждой изменяемой кости снимается и восстанавливается (snapshot в основном
      проходе, restore в `postRender`; override-слой с `isReRender=true` snapshot не трогает).
- [ ] Snapshot не создаётся/восстанавливается повторно из secondary render layer.
- [ ] Состояние одной девушки не протекает в другую (тот же риг).
- [ ] visibility/rotation/scale/position/color/UV/texture-оверрайды сбрасываются для сущностей,
      к которым не относятся.
- [ ] Дерево `steve` скрыто в обычном проходе, открывается только в override-проходе.
- [ ] wide/slim поддеревья рук взаимоисключаются (по серверному `PLAYER_MODEL_SLIM`).
- [ ] Скин партнёра не остаётся на следующей девушке; `hidePlayer=true` исключает партнёра;
      партнёра нет в фазах `NONE/BED_IDLE/LAYING_DOWN/DIALOG`.
- [ ] Carry-кости `legL/R, shinL/R, armL/R, lowerArmL/R` есть во ВСЕХ 14 geo-ригах (скрипт §6.2).
- [ ] FIRST_PERSON-сдвиг применяется только локальному носильщику в `CameraType.FIRST_PERSON`,
      не во freecam; third-person и другие игроки его не получают.

### 4.2 Сцены (серверный state machine)
- [ ] Полный жизненный цикл: открытие списка → выбор → проверка владельца/wild → relationship
      → резервирование → снятие одежды → движение к игроку/кровати → ожидание контакта →
      intro → stationary loop → active loop → thrust → cum → завершение → полная очистка.
- [ ] `StartSceneC2SPacket` резолвит полное определение сцены ТОЛЬКО на сервере
      (`girl.findScene(displayName)`); клиент не может прислать animation id / стоимость /
      relationship / параметры.
- [ ] tameable: запрос связан с точным открытым `GirlInventoryScreenHandler` + девушкой +
      владельцем. wild: существует реальный server-issued flow (`WildGirlEntity.mobInteract`).
- [ ] Один игрок не может подготовить/запустить несколько сцен одновременно (`activeScenes`).
- [ ] Резервирование существует уже на pending-этапах strip/path/contact (`startScene` кладёт
      в `activeScenes` до `beginScene`).
- [ ] При любом отказе/исключении reservation освобождается (`stopScene` из `stop()` целей).
- [ ] Disconnect, смена измерения, смерть, разрушение кровати, потеря passenger/vehicle,
      удаление сущности не оставляют: `activeScenes`, `CURRENT_SCENE_PLAYER`, frozen/noGravity,
      скрытого игрока, зависшей GUI-сессии, старой phase.
- [ ] Беременность и оплата сцены применяются строго один раз (`sceneCostPaid`, cum → stage+1).
- [ ] `Scene.hidePlayer()` влияет согласованно и на обычный player renderer, и на `steve`.
- [ ] Все scene definitions каждой встроенной девушки сопоставлены с существующими animation
      JSON (скрипт §6.1).
- [ ] `CustomGirlEntity`: отсутствие профиля / client-side scene list → fallback, без мутации
      synched data на клиенте. Максимум отношений считается из server-side scenes, fallback ≥ 4
      (`maxRelationshipLevel()`).

### 4.3 Carry (физическое держание)
- [ ] `getVehicleAttachmentPoint` учитывает размеры игрока и девушки ровно один раз; vanilla
      вычитает passenger attachment point (`positionRider`); нет двойного вертикального/
      горизонтального смещения.
- [ ] Переноска следует `player.yBodyRot`, а не `getYRot()`/мыши.
- [ ] Синхронно обновляются текущие И предыдущие yaw-поля (иначе дрожание).
- [ ] Look-goals не вращают переносимую девушку; навигация и остаточная скорость остановлены.
- [ ] `noGravity` всегда сбрасывается: после обычного снятия, после неудачного `startRiding`,
      при смерти/удалении носильщика, при остановке сцены, после загрузки / аварийного разрыва.
- [ ] Переносимая девушка не толкает носильщика (`canBeCollidedWith=false`).
- [ ] Нельзя начать carry, если девушка уже пассажир / downed в недопустимом flow / в сцене;
      нельзя одновременно сцену и carry.
- [ ] Проверь снятие, teleport, смену измерения, смерть, disconnect, повторное взятие.
- [ ] Рендер: не уменьшать модель; не использовать `mika.carry_slow1` как физическую позу
      (carry накладывается на `idle`); наклон после ориентации модели к entity yaw; направление
      сгиба колен/локтей соответствует конвенции (§3, §5.1).

### 4.4 Сеть (пакеты)
Сверь таблицу «пакет → sender → receiver → authorization → rate limit» по фактическому коду:
- **C2S (12):** `InventoryButton`, `StartScene`, `ThrustKeybind`, `CumKeybind`,
  `StopSceneOnServer`, `AnimationFinish`, `SoundEventSync`, `GirlCustomize`, `KoboldCustomize`,
  `RemovePreviewEntity`, `SetGUIOpenState`, `ShiftRoles`.
- **S2C (8):** `ClothingArmorVisibility`, `SceneOptions`, `PlayCumHudAnimation`,
  `OpenCustomizeScreen`, `OpenKoboldCustomizeScreen`, `PlayAttackAnimation`, `RunAnimEvents`,
  `GirlStatus`.
- [ ] У каждого есть реальный sender и непустой receiver (нет «зарегистрирован, но никто не
      шлёт»).
- [ ] C2S: `ThrustKeybind`/`CumKeybind` берут девушку из `ctx.player().getVehicle()`, а не из
      присланного id.
- [ ] Preview (`GirlCustomize`/`KoboldCustomize`/`RemovePreviewEntity`): владелец + активная
      серверная сессия, source/requester/preview UUID связаны, значения в диапазонах, clone
      самоудаляется при disconnect/исчезновении source.
- [ ] `SetGUIOpenState`: клиент может только закрыть exact GUI, `true` отвергается.
- [ ] S2C заходят в client-only GUI/HUD через `ClientPacketHandlers` рефлексией.

### 4.5 ИИ / выживание (новые функции)
- [ ] `GirlChopTreesGoal`: рубит только `BlockTags.LOGS` с кроной `BlockTags.LEAVES` сверху
      (не дома); drop подбирает gather.
- [ ] `GirlFeedOwnerGoal`: кормит голодного владельца едой из своего инвентаря через
      `FoodData#eat(FoodProperties)`; не трогает броню.
- [ ] `GirlCookGoal`: сырое — через `RecipeType.SMELTING` + `SingleRecipeInput` +
      `DataComponents.FOOD`; топливо — через `AbstractFurnaceBlockEntity.isFuel`, НЕ еда и НЕ
      `isDamageableItem()`; результат — в рюкзак или дроп у печи; совместно с harvest.
- [ ] `GirlHuntGoal`: только взрослый скот (Cow/Pig/Sheep/Chicken/Rabbit/Goat; MushroomCow
      через Cow); НЕ детёныши/приручаемые/жители/девушки; приоритет ниже guard/hurt; бросок
      цели дальше `GIVE_UP_DISTANCE_SQ`.
- [ ] Роли (`GirlRole`): пресеты поверх тогглов; роль — только метка (synched-строка),
      авторитет — сами тогглы; сохраняются в NBT.
- [ ] `GirlStatusS2CPacket` — change-driven (раз в 20 тиков) + `sendPairingData`;
      `GirlStatusCache` — common-код без client-импортов.
- [ ] Каждая новая цель соблюдает §2 (запрещённые состояния).

### 4.6 Прочее
- [ ] Локализация: паритет en/ru (скрипт §6.3); ни одного `translatable` без ключа.
- [ ] NBT: новые тогглы (`ChopTrees`, `FeedOwner`, `Cook`, `Hunt`, `Role`) сохраняются и
      читаются с обратной совместимостью (`tag.contains(...)`).
- [ ] Нет клиентской записи synched data.
- [ ] Freecam, settlement, `/girls`, custom girls — поверхностная проверка на заглушки.
- [ ] Скан `TODO/FIXME`, пустых тел методов, постоянных `null/false/EMPTY` (§6.4) — каждую
      находку проверь на реальную достижимость, прежде чем называть багом.

---

## 5. Конвенции GeckoLib 4.9.2 (для проверки векторов)

Проверено по исходникам (`BakedAnimationsAdapter`, `AnimationProcessor`, `RenderUtil`):

- **Rotation** (JSON, градусы Blockbench): адаптер делает `X=-X, Y=-Y, Z=+Z`, переводит в
  радианы; применяется **аддитивно** к rest-позе: `bone.setRotX(lerp + initialSnapshot.getRotX())`.
  Порядок рендера: Z → Y → X.
- **Position** (JSON): без изменений (без инверсии, без радиан); применяется как **офсет
  поверх pivot**: `translate(-posX/16, +posY/16, +posZ/16)` — X инвертируется на рендере,
  Y/Z как есть; `1/16` = единица Blockbench. Сброс — лерп к rest-офсету (0).
- **Scale** (JSON): как есть, абсолютный множитель; сброс к 1.0.

**Эмпирические знаки (ground truth):**
- бедро `leg` X **−** = колено вперёд (sit `-120`, carry `-82`);
- голень `shin` X **+** = икра назад (sit `+55`, carry `+72`);
- плечо `arm` X **−** = рука вперёд (hug `-60`, carry `-87`);
- локоть `lowerArm` X **−** = сгиб вперёд (sit `-65`, attack `-125`);
- `torso`/`upperBody` X **−** = наклон вперёд, **+** = назад.

Проверь `setCarryBoneRotation` (совпадает с конвенцией) и новые `cook/chop/harvest`
(в `mika.animation.json` и остальных) на соответствие этим знакам.

---

## 6. Скрипты для проверки (Python, без сборки)

### 6.1 Анимации из Java/Scene против JSON
Прочитай `getScenes()` в `entity/girls/*.java` и сравни каждое имя (`intro/slow/fast/cum/
layOnBed/bedIdle/stationary*`) с ключами `<girl>.animation.json`, добавив префикс
`animation.<girlID>.`. Плюс базовые: `idle, walk, run, sit, downed, attack0/1/2, strip, blink`.

### 6.2 Carry-кости во всех ригах
Для каждого `geo/{dressed,nude}/*.geo.json` рекурсивно собери имена костей и проверь наличие
`legL, legR, shinL, shinR, armL, armR, lowerArmL, lowerArmR` (+ `steve`, `torso`, `upperBody`).

### 6.3 Паритет локализации
Сравни множества ключей `en_us.json` и `ru_ru.json`; различия — ошибка (кроме явно
документированных).

### 6.4 Скан заглушек
- `grep -rn "TODO\|FIXME" src/main/java`
- методы с пустым телом: `grep -rn "{ }"` и `return false;/return null;/return 0;` — каждую
  находку проверь на реальную достижимость.

---

## 7. Проверка API по исходникам (если есть сеть)

Vanilla 1.21.1 (Mojang-маппинги):
```bash
gh api "repos/Yeet-Masta/MCP-1.21/contents/src/main/java/net/minecraft/<path>.java" --jq '.content' | base64 -d
```
NeoForge 1.21.1: `gh api "repos/neoforged/NeoForge/contents/src/main/java/net/neoforged/<path>.java?ref=1.21.1"`.
GeckoLib 4.9.2: `gh api "repos/bernie-g/geckolib/contents/common/src/main/java/software/bernie/geckolib/<path>.java?ref=1.21.1"`.

Полезные файлы для сверки: `Entity.java` (startRiding/positionRider/getVehicleAttachmentPoint/
EntityAttachments), `Player.java` (DEFAULT_VEHICLE_ATTACHMENT), `ServerEntity.java`,
`ChunkMap.java` (TrackedEntity.updatePlayer), `ClientLevel.java` (tickPassenger),
`ClientPacketListener.java` (handleSetEntityPassengersPacket), `LivingEntity.java`
(updateSwingTime — только у Player), GeckoLib `BakedAnimationsAdapter`, `AnimationProcessor`,
`RenderUtil`, `AnimationController`, `GeoBone`.

---

## 8. Формат отчёта (`CURSOR_AUDIT_REPORT.md`)

1. **Таблицы** (обязательно):
   - все типы девушек → animation JSON → отсутствующие/существующие id;
   - carry-кости по каждому geo-ригу;
   - каждая фаза сцены → вход → выход → cleanup → серверная проверка;
   - пакеты → sender → receiver → authorization → rate limit;
   - carry flow (начало → passenger sync → client tick/render → позиция → поворот → снятие →
     аварийный cleanup).
2. **Найденные проблемы** с уровнем: `critical` / `functional` / `visual-runtime-only` /
   `false-positive / принятое решение`.
3. Для каждого дефекта — файл, строка, достижимый сценарий, предложенный минимальный фикс
   (в тексте или `.patch`).

**Главное правило:** если доказанных дефектов нет — не создавай искусственный рефакторинг.
Просто напиши, что перепроверил и что сошлось.
