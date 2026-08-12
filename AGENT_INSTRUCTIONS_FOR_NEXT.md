# Инструкция для следующего агента — порт Pleasure Horizons Fabric 1.21.6 → NeoForge 1.21.1

## 0. Контекст репозитория

- **Репозиторий:** `graciozee-create/portr`
- **Ветка текущей сессии:** `arena/019ff6de-portr` (PR #5). Arena трекает сессию по имени ветки —
  работай только на своей ветке сессии, пуш только в неё.
- **CI:** локально нет `javac`/`gradle` и нет доступа к Maven Central / maven.neoforged.net.
  Единственная сборка — GitHub Actions `.github/workflows/build.yml`, триггер на PR в `main`.
  Проверка: `gh run list --branch <ветка> --limit 1`. Нужен `success`.
- **Маппинги:** Mojang, NeoForge 1.21.1, GeckoLib 4.9.2.
- **Оригинал:** `https://github.com/colorgarden/Pleasure-Horizons-I18n`, ветка `reworked-girls`.

## 1. Как читать ошибки компиляции (важно!)

Сырые логи Actions лежат на blob-хранилище, которое из песочницы **недоступно**
(`gh run view --log` падает с `EOF`, `gh run download` тоже). Поэтому в `build.gradle` добавлена
задача `reportCompileDiagnostics`: она повторно прогоняет javac через Compiler API и печатает
`::error file=...,line=...::` — GitHub превращает это в аннотации check-run, которые читаются
обычным REST API:

```bash
ID=$(gh api repos/graciozee-create/portr/actions/runs/<RUN_ID>/jobs --jq '.jobs[0].id')
gh api repos/graciozee-create/portr/check-runs/$ID/annotations \
  --jq '.[]|select(.path|endswith(".java"))|"\(.path):\(.start_line): \(.message)"'
```

Не пытайся менять `.github/workflows/*` — у GitHub App нет права `workflows`, пуш будет отклонён.

## 2. Грабли NeoForge 1.21.1, проверенные на практике

- **`StreamCodec.composite` максимум 6 пар полей**, не 7. Для 7+ пиши кодек руками
  (`new StreamCodec<>() { decode/encode }`) — см. `Scene.SceneOptions`, `Scene.SceneAnimations`,
  `KoboldCustomizeC2SPacket`.
- `ByteBufCodecs.collection(...)` возвращает кодек конкретного типа коллекции. Для поля
  `List<X>` нужны явные типовые аргументы: `ByteBufCodecs.<Buf, X, List<X>>collection(...)`.
- Для record из одного поля используй `.map(Factory, Getter)`, не `composite`.
- `LivingEntity` имеет `setYBodyRot`, но **геттера нет** — читай публичное поле `yBodyRot`.
- S2C-пакеты не должны импортировать `net.minecraft.client.*` — иди через
  `client/networking/ClientPacketHandlers` рефлексией (`Class.forName(...).getMethod(...)`),
  иначе выделенный сервер падает при загрузке класса.
- Меню: у NeoForge нет `ExtendedScreenHandlerFactory`. Данные на клиент передавай через
  `player.openMenu(provider, buf -> ...)` + декодирование в `IMenuTypeExtension.create`.
  Пример: `SettlementSnapshot` для `SettlementHubScreenHandler`.
- Блоки: `onUse` → `useWithoutItem(state, level, pos, player, hit)`; `onPlaced` → `setPlacedBy`;
  `getBaseDimensions(EntityPose)` → `getDefaultDimensions(Pose)` + `EntityDimensions.scalable`.
- Экраны: `HandledScreen` → `AbstractContainerScreen`, `handledScreenTick` → `containerTick`,
  `drawTooltip` → `renderTooltip`, `context.getMatrices().translate(x,y)` → `pose().translate(x,y,0)`.
  На клиенте используй `this.title`, не `getTitle()`.
- Клиентские регистрации: клавиши — `RegisterKeyMappingsEvent`, HUD — `RegisterGuiLayersEvent`,
  экраны — `RegisterMenuScreensEvent` (всё на MOD-шине, `Dist.CLIENT`).
  Тик клиента — `ClientTickEvent.Post` на **игровой** шине.
- Датапак-папки в единственном числе: `recipe/`, `loot_table/`, `advancement/`.

## 3. Что портировано и работает (не ломать)

### Уже было до этой сессии
- Инвентарь девушки (18 слотов), рендер предмета в руке, сетевой слой, система downed,
  перенос на руках (Shift+ПКМ), гардероб, ИИ Вариант 2 (gather/harvest/guard),
  кнопки ИИ в инвентаре, фикс Strip-freeze, русификация.

### Ранние коммиты этой работы (начаты в PR #4, входят в текущий PR #5)
- **Поселения:** `SettlementSnapshot` (иммутабельная копия для клиента), `SettlementHubScreen`
  + `SettlementTab`/`SettlementTabWidget`/`SettlementTabType` + `ResourcesTab`/`SettlersTab`
  (показывают реальные данные, а не заглушки из оригинала). Блок хаба открывает GUI
  (`useWithoutItem`) и создаёт поселение при установке (`setPlacedBy`).
- **Система сцен:** `Scene`/`SceneAnimations`/`SceneOptions` с кодеками, `getScenes()` для
  Mika/Lucy/Kobold/Momo/Coppie/Slime по определениям из оригинала, полный стейт-машин в
  `GirlSceneEntity` (фазы, intro-последовательность, cum, беременность, keyframe-события),
  `GirlSceneScreen`, `SceneProgressOverlay`, цели `BedGoal`/`MoveToPlayerGoal`/
  `StationaryContactGoal`/`StopMovementGoal`. Кнопка Talk открывает список сцен.
  Клавиши Z (thrust) / V (cum) и HUD прогресса зарегистрированы.
  **Отличие от оригинала:** сцена синкается по имени (`displayName`), а не объектом — сервер
  резолвит её через `girl.getScenes()`, поэтому клиент не может подсунуть свою сцену.
- **Кобольд:** хитбокс от размера тела (лерп 1.0–1.75), видимость рогов, окраска
  primary/secondary/iris, размер и Z-смещение груди, `randomizeAppearance`, пресеты цветов.
- **Оверрайды костей:** `setBoneVisibility`/`overrideBoneColor`/`setBoneSize`/`setBonePos`
  в `GirlEntity` + применение в `GirlRenderer` (масштаб, позиция, цвет).
  **Важно:** кости — это общий стейт запечённой модели, поэтому рендерер каждый кадр сбрасывает
  всё, что трогал ранее. Не убирай этот сброс, иначе настройки одной девушки протекут на всех.
- **Защита владельца:** `GirlAttackWithOwnerGoal` и `GirlTrackOwnerAttackerGoal` (были заглушками
  `return false`), `PlayAttackAnimationS2CPacket` теперь несёт id и проигрывает замах.
- Локализация: 292 ключа, полный паритет en/ru, ни одного `translatable` без ключа.

## 4. Добавлено во второй половине сессии

- **Keyframe-события сцен:** `SceneKeyframeEventRegistry` (восстановлено разбиение на токены —
  GeckoLib шлёт payload списком через запятую, поэтому старый точный lookup никогда не совпадал),
  `SceneKeyframeEventLoader` читает `assets/*/keyframe_events/*.json`, регистрация через
  `RegisterClientReloadListenersEvent`. Реплики, стоны и шаги во время сцен теперь работают,
  звук ретранслируется остальным игрокам через `RunAnimEventsS2CPacket`.
- **Сканер зданий:** flood-fill комнаты, захват пола/стен/крыши, проверка требований
  (кровати/наковальня/сундуки), многоблочные объекты считаются один раз.
  Установка таблички рядом с дверью запускает скан. Счётчик зданий в GUI хаба больше не 0.
- **Кастомные девушки:** `CustomGirlProfile`/`CustomGirlParser`/`CustomGirlLoader` +
  `CustomGirlEntity` реально применяет профиль (HP, скорость, урон, хитбокс, GUI, предмет
  приручения, сцены), сохраняет id профиля, поддерживает смену профиля через Shift+ПКМ.
  **Важно:** профили грузятся на `ServerStartingEvent`, а не в конструкторе мода — до заполнения
  реестров `tame_item` резолвился бы в воздух.
- **Команда `/girls`:** `reload` и `spawn <id> [pos]` с автодополнением id профилей.
- **Лучный бой:** `SettlementGirlEntityAI implements RangedAttackMob`, `GirlBowAttackGoal`
  (натягивает, стреляет, кайтит при HP < 50%), `GirlAttackSwitchGoal` переключает ближний/дальний.
  Раньше девушка с луком просто подходила и била рукой.
- `PleasureHorizonsMessages` — методы были пустыми и глотали сообщения.

## 5. Добавлено в третьей части сессии

> Последний коммит с изменением Java-кода `44fa38e2e010cfc92abd996e6d35448725c59d82`
> собран точно: прогон `31634341592`, job `94240775818`, `SUCCESS`; `headSha` проверен.

### 5.1. Починен баг «девушка застывает» при взятии на руки

Симптом: нажимаешь взаимодействие — девушка замирает на месте вместо того, чтобы
оказаться на руках. Причина **не** в `startRiding`, а в ванильном трекинге сущностей:

- `ChunkMap.TrackedEntity#updatePlayer` начинается с `if (player != this.entity)` —
  игроку **никогда** не шлют трекинг-пакеты о нём самом. Значит, когда транспортом
  становится игрок, `ClientboundSetPassengersPacket` до клиента носильщика не доходит.
- `ServerEntity#sendChanges` при этом перестаёт слать позицию для пассажира.

Итог: у носильщика девушка остаётся на последней синхронизированной позиции — «застыла».
Другие игроки видели её на руках корректно.

Что сделано в `TameableGirlEntity`:
- добавлен `syncCarryState()` — явно шлёт `ClientboundSetPassengersPacket` носильщику;
- обе ветки (обычная и «спасение раненой») сведены в один `toggleCarry()`;
- неудачный `startRiding` больше не оставляет `setNoGravity(true)` — раньше это и давало
  зависание в воздухе;
- первоначальная вертикальная точка `(0, 0.7, 0)` больше не используется; текущий
  `getVehicleAttachmentPoint` рассчитывает полную тесную посадку из размеров обеих сущностей
  и констант `CARRY_*_OFFSET`, подробно описанных в §5.4;
- `canBeCollidedWith` = false во время переноски;
- в `tick()` убран мусорный код, добавлен сброс `noGravity`.

### 5.2. Freecam портирован

Оригинал построен на 18 миксинах и фейковом `ClientPlayerEntity` с самодельным
`ClientPlayNetworkHandler`. В порту вместо этого:

- `Minecraft#setCameraEntity` + лёгкая сущность `FreeCamera` на `EntityType.MARKER`
  (у него нет рендерера и хитбокса). `GameRenderer#renderLevel` передаёт
  `getCameraEntity()` прямо в `Camera#setup` — миксин в камеру не нужен;
- `ViewportEvent.ComputeCameraAngles` — вместо `CameraMixin` и
  `EntityMixin#changeLookDirection`. Мышь уже повернула игрока, поэтому дельта
  переносится на камеру, а игрок «отматывается» назад;
- `MovementInputUpdateEvent` — вместо подмены `player.input`;
- `RenderPlayerEvent.Pre` / `RenderHandEvent` / `InteractionKeyMappingTriggered` /
  `LivingDamageEvent.Post` / `LevelEvent.Unload` — вместо остальных миксинов;
- `FreecamConfig` на `ModConfigSpec` вместо AutoConfig/Cloth (Fabric-only),
  файл `config/pleasurehorizons-freecam.toml`;
- клавиши: F4 — вкл/выкл, плюс две несвязанные (`player_control`, `tripod_reset`).
  F4 + цифра 1–9 — штативы, как в оригинале;
- исправлена ошибка оригинала: в `TripodSlot.valueOf` условие было инвертировано.

Удалены больше не нужные заглушки `config/keys/*`, `config/gui/*`, `ModBindings`.

### 5.3. Модели: физика, поворот головы и скин партнёра

- `JigglePhysics` был заглушкой (`update()` пустой, геттеры возвращали `Vec3.ZERO`) —
  портирован дословно, это чистая векторная математика;
- добавлена запись `JiggleBoneConfig`;
- в порту вообще не было аналога `AbstractGirlModel#setCustomAnimations`, поэтому голова
  была намертво зафиксирована. Теперь в `GirlRenderer#preRender`:
  - поворот головы по yaw/pitch относительно корпуса (в сценах отключён);
  - тряска `cheekL`/`cheekR`/`belly` + `boobs` (одетая) или `boobL`/`boobR` (раздетая),
    фиксированный шаг 25 Гц с интерполяцией, как в оригинале;
  - опорный поворот кости запоминается один раз, а не читается каждый кадр — иначе
    смещение накапливалось бы (кости в GeckoLib общие и переиспользуются).
- **Скин партнёра в сценах.** Кость `steve` (полноценный второй скелет, вшитый в каждый риг)
  не имеет своей текстуры, поэтому рисовалась текстурой самой девушки — «каша» из UV.
  В оригинале это `applySkinToBone` + `BoneOverrideRenderLayer`; в порту не было ни того,
  ни другого (слой был заглушкой на 5 строк). Теперь слой реализован под API GeckoLib 4.9.2
  (в оригинале — API render-state из GeckoLib 5, которого здесь нет), в `GirlEntity`
  добавлены три карты `boneTextureOverrides*`, а скин обновляется каждый кадр во время
  сцены, потому что скины подгружаются асинхронно.
  Флаг slim/wide намеренно не пишется из рендерера — это synched data сервера.
- Финальный фикс партнёра (`5a81da4`): дерево `steve` теперь всегда скрыто в основном
  проходе с текстурой девушки и временно открывается только в override-слое. Партнёр не
  рисуется в `NONE/BED_IDLE/LAYING_DOWN/DIALOG` и в сценах с `hidePlayer=true`; wide/slim
  поддеревья рук взаимоисключаются по серверному флагу. Это устраняет двойной проход,
  неправильные UV вне активной фазы и одновременные руки Alex/Steve.

### 5.4. Переноска: полноразмерная тесная поза с объятием

Старая запись про миниатюрную посадку на плечо больше не актуальна. После игровых тестов
масштаб `0.62`, рендерные локальные смещения и `snapToCarrier` удалены. Положение пассажира
теперь задаётся только серверно/ванильно через
`TameableGirlEntity#getVehicleAttachmentPoint`, поэтому клиент носильщика и наблюдатели
получают одну и ту же авторитетную позицию.

Текущее устройство:

- `CARRY_RIGHT_OFFSET = 0.36`, `CARRY_FORWARD_OFFSET = 0.03`,
  `CARRY_VERTICAL_OFFSET = -0.12`: полноразмерная модель прижата к правой/передней стороне
  корпуса и немного опущена до уровня поддержки на бедре, без видимого воздушного зазора;
- переносимая девушка всегда проигрывает нейтральный `idle`; клипы сцен (включая
  `mika.carry_slow1`) не используются как физическая переноска;
- `GirlRenderer` применяет позу после вычисления контроллеров GeckoLib и восстанавливает
  точные значения общих костей после всех проходов. Задействованы все восемь костей:
  `legL/R`, `shinL/R`, `armL/R`, `lowerArmL/R`;
- причина «коленей в обратную сторону» найдена точно: `BakedAnimationsAdapter` при загрузке
  Blockbench JSON меняет знак X и Y, но не Z. Прежний код записывал JSON-подобные углы прямо
  в `GeoBone`, то есть фактически поворачивал бёдра и голени наоборот. Новый
  `setCarryBoneRotation` принимает градусы Blockbench, выполняет то же преобразование
  `X=-X, Y=-Y, Z=Z` и прибавляет исходный поворот конкретного рига;
- бёдра/голени используют анатомическую согнутую позу (`-82/+72` в координатах
  Blockbench), а поднятые плечи и сильно согнутые локти (`lowerArmL/R`) дают видимое
  объятие носильщика;
- наклон применяется в `applyRotations` **после** поворота модели к yaw сущности. В старом
  `preRender` это был поворот вокруг мировой Z, из-за чего направление наклона зависело от
  стороны света. Покачивание при ходьбе уменьшено до `1.5°`;
- в первом лице модель не уменьшается. Только для локального носильщика и только при
  `CameraType.FIRST_PERSON` добавляется камерное кадрирование по basis-векторам камеры:
  наружу `0.32`, вперёд `0.24`, вниз `0.24`. Это держит полноразмерную девушку у нижнего
  края и примерно в пределах половины экрана; третье лицо и другие игроки видят тесную
  авторитетную посадку без этого визуального сдвига. Во freecam сдвиг не применяется,
  потому что camera entity уже не носильщик.

Синхронизация `ClientboundSetPassengersPacket`, блокировка look-goals во время переноски,
фиксация всех текущих/предыдущих yaw и сброс `noGravity` после снятия остаются обязательными.

Коммит переноски: `d9b22723551d302f7e5a5069621cabd93e7d5cfc`.
Точный CI: run `31633437292`, job `94237712414`, `SUCCESS`.
Внешний вид нельзя проверить локально — нужен следующий игровой тест именно этого артефакта;
после него подбирать только перечисленные константы, не возвращать масштаб или snap в рендерер.

### 5.5. Броня на девушках

Девушки не используют ванильные модели брони: в каждый риг вшита своя геометрия брони,
а в атласе на каждый материал отведён свой столбец — то есть выбор материала это сдвиг UV
по горизонтали (шаг `0.017578125` = 9/512, столбцы: 0 без брони, далее diamond, gold, iron,
copper, chain, leather, turtle). Не работало сразу три звена:

- `OffsetVertexConsumer` компилировался, но **ничего не сдвигал** и использовал имена
  методов до 1.21 (`vertex`/`texture`/`overlay`/`light`). Переписан под интерфейс 1.21.1
  (`addVertex`/`setUv`/`setUv1`/`setUv2` — всего 6 абстрактных методов).
- `GirlEntity#applyClothingAndArmor` был **пустым методом**, поэтому надетая броня вообще
  никак не отображалась. Теперь показывает/прячет кости брони, прячет `vagina` под поножами,
  выбирает столбец материала и красит крашеную кожу (`DyedItemColor`, фолбэк `0xA06540`).
- `ClothingArmorVisibilityS2CPacket` был зарегистрирован, но **никто его не отправлял**,
  так что даже правильный клиент не узнал бы, что надето. Добавлена серверная половина
  `updateClothingAndArmor()`; опрос раз в секунду в `tick` и отправка только при изменении,
  потому что ни контейнер девушки, ни флаг `stripped` не генерируют событий.
- Коммит `44fa38e` ограничил эти обновления реальными трекерами сущности. Текущий state
  дополнительно входит в NeoForge `sendPairingData`/initial tracking bundle, иначе игрок,
  подошедший к давно загруженной девушке, мог навсегда получить неверное состояние — особенно
  начальное all-false, которое не вызывает change-driven отправку.

Важно: броня физически лежит в собственном контейнере девушки (`GirlInventory`, слоты 1–4).
`getArmorStack(EquipmentSlot)` читает эти слоты напрямую, а переопределённые
`getItemBySlot`/`setItemSlot` и iterable-методы мостят их в ванильную экипировку для боя,
синхронизации предметов и GeckoLib held-item layer.

Ещё: `renderRecursively` теперь пропускает кости с текстурным оверрайдом в базовом проходе,
иначе они рисуются дважды и z-fight'ят со слоем оверрайда.

### 5.6. Итог аудита сети и подозрительных заглушек

В `PleasureHorizonsPackets` сейчас зарегистрировано ровно **18 payload: 11 C2S и 7 S2C**.
Для каждого найден реальный sender и непустой receiver; зарегистрированных пакетов, которые
только делают вид, что работают, больше нет.

**C2S:**

- `InventoryButton` допускает действие только для владельца, точной девушки из реально
  открытого `GirlInventoryScreenHandler`, в допустимом состоянии и на расстоянии ≤ 8 блоков.
  Сервер сам выбирает доступные сцены и создаёт preview.
- `StartScene` повторно резолвит сцену по серверному `getScenes()`, проверяет live/state,
  расстояние, relationship и глобальную reservation игрока. Для tameable нужна точная открытая
  inventory-сессия и владелец; для wild — ранее выданный сервером wild-flow.
- `ThrustKeybind`, `CumKeybind`, `StopSceneOnServer`, `AnimationFinish` и `SoundEventSync`
  принимаются только от авторитетного текущего участника. Для thrust/cum девушка берётся из
  текущего vehicle отправителя, а не из присланного id. Finish ограничен одним событием за
  5 тиков, keyframe — одним combined payload за серверный тик.
- `GirlCustomize`, `KoboldCustomize` и `RemovePreviewEntity` требуют владельца и активную
  серверную preview-сессию. Source/requester UUID, server-owned preview UUID/id и оба заявленных
  id связаны между собой; значения ограничены диапазонами. Clone сам удаляется при disconnect,
  исчезновении source или потере этой точной связи.
- `SetGUIOpenState` с клиента может только закрыть exact GUI, который сервер связал с этим
  игроком; присланное `true` отвергается.

**S2C:**

- `SceneOptions`, оба `Open*CustomizeScreen` и `PlayCumHudAnimation` имеют реальные серверные
  sender-path и заходят в client-only GUI/HUD через `ClientPacketHandlers` рефлексией, не
  загружая `net.minecraft.client.*` на dedicated server.
- `RunAnimEvents`, `PlayAttackAnimation` и `ClothingArmorVisibility` несут id/state и реально
  применяются на клиенте. Attack и change-driven clothing после `44fa38e` отправляются только
  трекерам сущности; clothing также вложен в initial pairing bundle каждого нового трекера.
- Пустой encoder `PlayCumHudAnimationS2CPacket` корректен: это настоящий сигнал без полей,
  а не заглушка.

**Решение по client-originated animation timing:** оставить текущую модель. Сервер 1.21.1 не
знает фактический момент GeckoLib keyframe без дублирования клиентского animation runtime,
поэтому finish/keyframe по назначению сообщаются клиентом, но только участником его собственной
активной или pending сцены и с rate limit. `SoundEventSync` дополнительно принимает лишь
`[a-z0-9_, \-]{1,128}`. Синтаксически допустимый произвольный ключ максимум добавит прогресс
собственной сцены через `thrust`, вызовет шаг или уже зарегистрированный звук; он не выбирает
произвольный registry sound, сущность или другого игрока. Это ограниченный gameplay trust, а не
неавторизованное изменение мира; whitelist мог бы ломать кастомные keyframe assets. Если модель
угроз изменится, hardening надо делать по токенам server-loaded registry, не по полному payload.

Широкий scan `TODO/FIXME`, коротких классов, пустых тел и постоянных `null/false/EMPTY` не нашёл
другой достижимой fake-функции. Пустые override у `FreeCamera` — обязательные no-state методы
абстрактной `Entity`. `GirlInventoryScreenHandler#quickMoveStack` честно возвращает
`ItemStack.EMPTY`: shift-click сейчас **не поддержан**, а не симулирует перенос. Реализовывать
его следует только как отдельную принятую UX-функцию с правилами для 18 girl/player slots.

### 5.7. Максимум отношений

`GirlEntity#maxRelationshipLevel()` теперь чисто вычисляет максимум
`requiredRelationshipLevel` из серверных сцен и не пишет synched data из getter. Для кастомного
профиля `CustomGirlEntity` записывает fallback только на сервере после загрузки профиля; если
клиент не имеет profile scenes или профиль повреждён, используется tracked fallback минимум 4.
Не возвращать Fabric-подход с mutation во время чтения — экран вызывает getter и на клиенте.

## 5a. Что ещё осталось

- Нужен игровой тест артефакта с `44fa38e`: переноска в первом/третьем лице, все типы
  девушек, поворот носильщика на 360°, ходьба и снятие с рук. Если кадрирование всё ещё
  занимает больше ~50% экрана, менять только `FIRST_PERSON_*`; если есть зазор в третьем
  лице — только `CARRY_*_OFFSET` и `CARRY_INWARD_LEAN`.
- В том же запуске проверить партнёра сцены: он отсутствует до активной фазы и при
  `hidePlayer=true`, появляется с правильным скином и только одним вариантом wide/slim рук.
- Проверить initial clothing sync: снять/надеть броню или раздеть девушку, уйти из tracking
  range вторым клиентом и подойти снова; в том числе проверить девушку с all-false state,
  которая была загружена до входа второго клиента.
- Если на **текущем** артефакте снова воспроизведутся Customize/preview/Talk, исследовать новый
  лог и точный flow. Старый отчёт был сделан по прежнему запуску; в новом кнопки уже работали,
  поэтому не ослаблять серверную session authorization на основании старого результата.
- Дальнейший аудит вести по реально достижимым пользовательским функциям. Текущий полный
  packet audit закрыт (§5.6); отсутствие очевидного `new Packet(...)` само по себе не доказывает
  мёртвый функционал.
- `TamedGirlManager.cleanupDeadGirls` оставлять отключённым: выгруженная сущность не равна
  мёртвой, старый cleanup терял живых девушек.
- Не переделывать уже принятые решения без нового воспроизведения: non-freecam `ModConfig`
  остаётся набором in-memory defaults; `Vec3dInputSection` использует кнопки ±0.1;
  texture override брони ограничен `steve`; slim/wide не пишется клиентом.

## 6. Правила работы

- После каждого логического этапа: `git add -A && git commit && git push origin <ветка>`,
  затем дождись GitHub Actions. Обязательно сравни `gh run view <id> --json headSha` с
  локальным `git rev-parse HEAD`; одного статуса последнего запуска недостаточно.
- Не оставляй заглушек, молча возвращающих `false`/пустоту, если функция заявлена как
  портированная — лучше не трогать, чем сделать вид, что работает.
- Кнопок в `GirlInventoryScreen` максимум 7 слева и 6 справа, иначе вылезают за экран.
- Любая новая цель ИИ обязана проверять `!isPassenger()`, `!isDowned()`, `!isSceneActive()`.
