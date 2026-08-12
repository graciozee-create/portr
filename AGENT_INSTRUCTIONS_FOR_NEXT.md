# Инструкция для следующего агента — порт Pleasure Horizons Fabric 1.21.6 → NeoForge 1.21.1

## 0. Контекст репозитория
- **Репозиторий:** `graciozee-create/portr`
- **Ветка сессии:** `arena/019ff520-portr` (наследница `arena/019ff20a-portr`, та — PR #2 в main). Работай ТОЛЬКО на `arena/019ff520-portr`, пуш только `git push origin arena/019ff520-portr`. Не переключайся на другие ветки — Arena трекает по этой.
- **CI:** Локально нет `javac/gradle`. Сборка только через GitHub Actions `.github/workflows/build.yml` — триггерится на PR в `main`. Создан PR #3 из `arena/019ff520-portr`. Проверка: `gh run list --branch arena/019ff520-portr`, `gh pr checks 3`. Должен быть **BUILD SUCCESSFUL**.
- **Маппинги:** Mojang, NeoForge 1.21.1, GeckoLib 4 (единственная для 1.21.1). Yarn → Mojang: `Text` → `Component`, `ButtonWidget` → `Button.builder`, `SliderWidget` → `AbstractSliderButton` (но мы используем ± кнопки чтобы избежать проблем), `TextWidget` → `StringWidget`, `Vec3d` → `Vec3`, `World` → `Level`, `PlayerEntity` → `Player` и т.д.
- **Оригинал:** `https://github.com/colorgarden/Pleasure-Horizons-I18n` ветка `reworked-girls`.

## 1. Что уже полностью портировано и НЕ ЛОМАТЬ
- Инвентарь девушки: `GirlInventoryScreen`, `GirlInventoryScreenHandler` (18 слотов: 12 рюкзак 4x3 + 2 руки + 4 броня + слоты игрока), 4 класса слотов с проверкой `PREVENT_ARMOR_CHANGE`. Открытие через `ServerPlayer.openMenu` в `TameableGirlEntity`.
- Рендер предмета в руке: `GirlRenderer` + `BlockAndItemGeoLayer` в кости `"weapon"`, поворот X, масштаб.
- 24 пакета C2S/S2C на `StreamCodec`. Важно: для record из 1 поля (`AnimationFinishC2SPacket` и т.д.) используй `.map(Factory, Getter)`, не `composite`. Внутри record поля через геттеры `this.entityId()`.
- ИИ Вариант 2: `GirlGatherItemsGoal` (сбор `ItemEntity` в радиусе 8, кладет в рюкзак), `GirlHarvestCropsGoal` (ищет `CropBlock.isMaxAge` в 6 блоках, `destroyBlock` с `true` → дроп, затем `setBlock(age 0)` — ничего из воздуха), `GirlGuardBaseGoal` (16 блоков от базы).
- Гардероб: `ClothingArmorVisibilityS2CPacket` + `isArmorVisible/setArmorVisible`.
- Система downed вместо смерти, 65% резист, перенос на руках — см. раздел 3.
- Локализация 236+ ключей в `en_us.json`/`ru_ru.json`.

## 2. Первоочередные баги — уже починены, но проверь

### Task №1 Strip freeze
**Симптом:** после кнопки Strip девушка застывает.
**Файлы:**
- `entity/ai/goal/StripGoal.java`
- `networking/C2S/InventoryButtonC2SPacket.java`
- `entity/base/GirlEntity.java` / `GirlSceneEntity.java` (`setFreeze`, `isFrozenInPlace`)

**Оригинал Fabric:** в `PleasureHorizonsPackets.registerC2SPackets` для `stripOrDressup` только `girl.requestStrip()` — тоггл делает `StripGoal` по ключевому кадру `becomeNude`.

**Было в порту:** делали `requestStrip()` + сразу `setStripped(!)`, двойной тоггл.

**Фикс:**
- `InventoryButtonC2SPacket` — только `requestStrip()`, если `!hasStripAnim()` сразу `setFreeze(false)` safety.
- `StripGoal`:
  - `start()`: `setFreeze(true)`, если `hasStripAnim` → `setOverrideAnim("strip")`, `ticksInGoal=0`, `stripTrigged=false`
  - `tick()`: `ticksInGoal++`, keep freeze, проверка `isStringInQueue(animationKeyFrameEvent,"becomeNude")` → тоггл, **fallback** если очередь пуста (в порту `handleAnimationEventServer` заглушка) → тоггл через 10 тиков, через 30 тиков очистить `overrideAnim=""`
  - `canContinueToUse()`: `if (!hasStripAnim) return false; if (ticks>60) return false; return !stripTrigged || !overrideAnim.isEmpty()`
  - `stop()`: **всегда** `setFreeze(false)`, если override=="strip" → очистить, если `!hasStripAnim && started` → тоггл, `started=false`

### Task №2 Русификация
Поиск: `grep -Rn "Component.literal" src/main/java`
Было: `CustomizeScreen` Confirm/Cancel, `GirlInventoryScreen` Requires Relationship, `TameableGirlEntity` 5 сообщений с `§a§l + girlName + русский текст`, `She ignores you...`, `You asked ... out and she said Yes`, `giftRepliesLike/Love` — английские строки.

**Фикс:**
- Заменить на `Component.translatable("msg.pleasurehorizons.<key>", displayName)`
- Добавить ключи в оба lang файла. Пример ключи: `girl_revived`, `girl_downed_need_food`, `girl_put_down`, `girl_picked_up`, `girl_heavily_wounded`, `girl_ignores`, `tame_success`, `requires_relationship`, `gift_like.1..3`, `gift_love.1..3`, `confirm/cancel`
- Для `giftReplies` теперь `List<Component>` с `translatable`

## 3. Механика ношения на руках — улучшена, но проверь

**Оригинал Fabric:** не было ношения, это кастом порта.

**Текущая реализация (должна работать):**
- `TameableGirlEntity.mobInteract`: `Shift + ПКМ пустой рукой`
  - Если уже пассажир этого игрока → `stopRiding()`, `moveTo` на 1.2 блока вперёд, `setNoGravity(false)`, звук `ITEM_PICKUP`
  - Иначе → `navigation.stop()`, `setTarget(null)`, `setSitting(false)`, `boolean started = startRiding(player,true)`, если ok → `setNoGravity(true)`, `setDeltaMovement(0)`, сердечки, звук. Если fail → сообщение `carry_failed`.
  - Разрешено даже для `isDowned()` — спасение раненой.
- `tick()`: если `isPassenger && vehicle instanceof Player` → `setNoGravity(true)`, `navigation.stop()`, `setTarget(null)`, `setYRot(player.getYRot())`, проверка смерти игрока → авто-стоп.
- `hurt()`: нет урона `fall`/`inWall` пока на руках.
- `canAddPassenger()`: если сама пассажир — не может везти других.

**Рендеринг:**
- `GirlRenderer.preRender` раньше `scale(0,0,0)` в 1-ом лице. Теперь:
  - 1-е лицо справа на руках: `rightOffset 0.6, forward 0.5, down -0.4`, `translate(right*cos+forward*(-sin), down, right*sin+forward*cos)`, `rotate Y 180-yaw+30`, `scale 0.5`, sway `sin(tick*0.1)*2°`
  - 3-е лицо: `forward 0.4, up 0.2, scale 0.85, X -15°` — принцесса-кэрри.
- Анимация: в `GirlEntity` добавлено `hasHugAnimation()`, `hasCarryAnimation()`, `getCarryAnimation()` — по умолчанию `sit`, у Мики `carry_slow1`/`hugidle`. В `GirlSceneEntity.handleAnimations` если `isPassenger && vehicle instanceof Player` → `getCarryAnimation()` LOOP.

**Если снова застывает:** проверь `GirlFollowOwnerGoal.canUse()` и `canContinueToUse()` — должны содержать `!isPassenger() && !isDowned()`. Добавлено. Также `Gather`, `Harvest`, `GuardBase`, `StayNearBase` уже проверяют `isPassenger`. Если добавишь новые цели — всегда добавляй проверку `!isPassenger()`.

## 4. Прокаченная ИИ система — уже добавлена

**Проблема пользователя:** "ресурсы из воздуха", нет кнопок охранять/собирать.

**Сделано:**
- В `GirlEntity` 5 новых трекеров: `AI_GUARD_BASE`, `AI_GUARD_OWNER`, `AI_GATHER`, `AI_HARVEST`, `AI_STAY_NEAR_BASE` (дефолт gather=true). Define, builder.define, геттеры/сеттеры `setGuardBaseEnabled` и т.д., save/load в NBT `AIGuardBase` и т.д.
- `GirlGatherItemsGoal.canUse()`: теперь `if (!isGatherEnabled) return false` + проверки `isDowned/isPassenger`
- `GirlHarvestCropsGoal` аналогично `isHarvestEnabled`
- `GirlGuardBaseGoal` — `isGuardBaseEnabled`
- `GirlStayNearBaseGoal` — был заглушкой `return false`, теперь реализован: если `isStayNearBaseEnabled` и дистанция до базы > maxDist → идти к базе.
- **Новый** `GirlGuardOwnerGoal` — охраняет владельца в 12 блоках, ищет `Monster` рядом с owner, `TargetGoal`.
- `TameableGirlEntity.registerGoals()`: добавлены `StayNearBase` и `GuardOwner` в targetSelector.
- `InventoryButtonRegistry`: новые кнопки `guardBase`, `guardOwner`, `stayNearBase` слева, `gather`, `harvest` справа. Всего теперь 7 слева, 6 справа — влазит в GUI (кнопки снаружи инвентаря).
- `InventoryButtonC2SPacket`: handle для 5 новых экшенов с тогглом и сообщением `msg.pleasurehorizons.guardBaseEnabled` и т.д.
- `GirlInventoryScreen`: динамические лейблы — если `isGuardBaseEnabled()` → `stopGuardBase` и т.д.
- Переводы 10 кнопок + 10 сообщений в оба lang.

**Механика урожая подробно:** `GirlHarvestCropsGoal` сканирует `basePos.offset(x,y,z)` x±6,z±6,y±2, ищет `CropBlock`, если `isMaxAge` → `targetCrop=pos`, идет к `pos+0.5`, если dist<4 → `destroyBlock(pos,true,girl)` (дроп падает, подбирается `GatherItemsGoal`), затем `setBlock(crop.getStateForAge(0),3)` — сажает обратно из своих семян (из дропа). Ничего не спавнит. Кулдаун 40 если не нашла, 20 после сбора.

## 5. Экраны кастомизации — частично портированы

**Статус до твоей сессии:** `CustomizeScreen` абстрактный уже был, но `GirlCustomizeScreen`, `KoboldCustomizeScreen` и секции `Slider`, `ButtonGrid`, `Vec3dInput`, `Button`, `Label` — заглушки `public class X {}`.

**Что сделано:**
- `CustomizeScreen`: добавлен публичный `addWidget(AbstractWidget)` wrapper вокруг protected `addRenderableWidget`, чтобы секции из другого пакета могли добавлять виджеты. Также фикс превью: в конструкторе `previewEntity.setInvisible(false)` и `setNoGravity(false)` — раньше клон создавался invisible на y=800 и в GUI не рендерился.
- Секции переписаны под Mojang и чтобы CI проходил:
  - `LabelSection` — `StringWidget` + `Component`
  - `ButtonSection` — `StringWidget` лейбл + `Button` toggle True/False, принимает `Component`
  - `SliderSection` — принимает `Component label`, показывает `label + \": \" + value`, две кнопки -/+ с лямбдами `valueGetter`/`valueSetter`, без `Math.clamp` в лямбде чтобы избежать проблем (используется if)
  - `ButtonGridSection` — title `Component`, сетка `createSelectableButton`, помечает выбранную `markAsSelected`
  - `Vec3dInputSection` — `Component label`, 3 пары кнопок X-/X+ и т.д., меняет `Vec3` на ±0.1
  - Все секции имеют перегрузки `(String)` и `(Component)` для совместимости.

- `GirlCustomizeScreen` — портирован: слайдер груди `breastSize` min/max из `entity`, `Vec3dInput` оффсет, toggle `canGetImpregnated`, кнопка Clear, `onConfirm` шлёт `GirlCustomizeC2SPacket`, `applyToPreview` меняет `previewEntity`.

- `KoboldEntity` — расширен: трекеры `BODY_SIZE`, `BREAST_SIZE`, `PRIMARY/SECONDARY/IRIS_COLOR`, `TOP/BOTTOM_HORN`, методы `getBodySize` и т.д., `addAdditionalSaveData/readAdditionalSaveData`, энам `PatternPresets`.

- `KoboldCustomizeScreen` — слайдеры body/breast, `ButtonGridSection` для пресетов цвета, ириса, рогов, randomize кнопка. Использует `Component.translatable` для тайтлов.

- `KoboldCustomizeC2SPacket` — 8 полей не влазит в `StreamCodec.composite` (лимит 7), переписан на `StreamCodec.of((buf,pkt)->{writeVarInt...}, buf->{new...})`.

- `ClientPacketHandlers` — клиент-онли класс с `handleOpenCustomizeScreen` и `handleOpenKoboldCustomizeScreen`, делает `Minecraft.getInstance().execute(() -> setScreen(...))`, помечен `@OnlyIn(CLIENT)`. S2C пакеты `OpenCustomizeScreenS2CPacket` и `OpenKoboldCustomizeScreenS2CPacket` делегируют через рефлексию `Class.forName(...).getMethod(...).invoke` чтобы не импортить клиентский класс напрямую (иначе dedicated server краш).

- `InventoryButtonC2SPacket` case `customize` — создаёт `createTempClone()` и шлёт соответствующий S2C пакет через `PacketDistributor.sendToPlayer`.

- Переводы для кастомизации: `gui.pleasurehorizons.customize.breastSize`, `breastOffset`, `canGetImpregnated`, `clear`, `bodySize`, `colorPattern`, `irisColor`, `topHorns`, `bottomHorns`, `randomize`, `titleGirl`, `titleKobold`.

**Что осталось доделать:**
- `SettlementHubScreen` + `tabs/ResourcesTab`, `SettlersTab` — сейчас заглушки. Нужно портировать логику из оригинала: `Settlement`, `SettlementManager`, ресурсы, жители. В оригинале вкладки показывают список ресурсов и поселенцев, кнопки назначить здание и т.д.
- `GirlSceneScreen` + `SceneProgressOverlay` — заглушки. Нужно портировать управление сценами: прогресс-бар `SCENE_PROGRESS`, `CUM_THRESHOLD`, кнопки фаз, free cam.
- `KoboldEntity` — полная логика цвета костей `overrideBoneColor`, `setBoneVisibility` для рогов, хитбокс от `bodySize` (`calculateHitboxHeight` linear lerp 1.0-1.75), рандомизация внешности — сейчас только трекеры, без `applyCustomizations` в `tick()`.
- `Vec3dInputSection` — сейчас ±0.1 кнопками, в оригинале были `TextFieldWidget` (EditBox) с парсингом double — можно вернуть EditBox если CI позволит.
- Проверить `GirlSceneEntity` — в оригинале много логики сцен, `playPhase`, `animationFinished`, `handleAnimationEventServer` (очередь ключевых кадров) — в порту заглушки.

**Важные правила NeoForge 1.21.1 чтобы CI оставался зелёным:**
- Датапак папки в единственном числе: `recipe/`, `loot_table/`, `advancement/`, тег яиц `c:eggs`.
- S2C пакеты не должны напрямую импортить `net.minecraft.client.Minecraft` в заголовке класса — делай обёртку через `ClientPacketHandlers` и рефлексию, как сделано.
- `StreamCodec.composite` до 7 полей, для 8+ используй ручной `StreamCodec.of`.
- В конструкторе экрана для поиска сущности по ID: `ClientLevel world = Minecraft.getInstance().level; world.getEntity(id)` — у `Level` нет `getEntity(id)` на клиенте.
- На клиенте не используй `this.getTitle()` — используй `this.title` или своё поле `screenTitle`.
- Кнопки в `GirlInventoryScreen` рисуются вне GUI, 7 слева + 6 справа — максимум, иначе вылезут за экран.
- Для `AbstractWidget` обёртка `addWidget` должна возвращать `AbstractWidget`, не generic с `Navigable` — иначе не компилится (проверено).
- `StringWidget` с `setMessage` и `setTooltip` ломал сборку в некоторых версиях — избегай `setMessage` на `StringWidget`, используй просто создание нового или `Component.literal(label.getString() + ...)`.

## 6. Финальная проверка
- После каждого логического этапа: `git add -A && git commit && git push origin arena/019ff520-portr`, затем `gh run list --branch arena/019ff520-portr` и жди `success`. Если `failure` — смотри `log.txt` в репе (там лежит старый лог) или пытайся угадать по файлам. Локально `javac` нет.
- Текущая ветка на момент этой инструкции: `cb5a75e Fix preview invisible...` + `c42ba94 Add customize translations` + `837abb2 Advanced AI...` + carry фиксы — **BUILD SUCCESSFUL**.

Удачи, следующий агент!
