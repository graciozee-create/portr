# Инструкция для следующего агента — порт Pleasure Horizons Fabric 1.21.6 → NeoForge 1.21.1

## 0. Контекст репозитория

- **Репозиторий:** `graciozee-create/portr`
- **Ветка текущей сессии:** `arena/019ff5e7-portr` (PR #4). Arena трекает сессию по имени ветки —
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
- Инвентарь девушки (18 слотов), рендер предмета в руке, 24 пакета, система downed,
  перенос на руках (Shift+ПКМ), гардероб, ИИ Вариант 2 (gather/harvest/guard),
  кнопки ИИ в инвентаре, фикс Strip-freeze, русификация.

### Добавлено в этой сессии (PR #4)
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

> Всё ниже собрано и проверено CI (последний зелёный прогон — `31612418298`).

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
- `getVehicleAttachmentPoint` = `(0, 0.7, 0)` — `positionRider` **вычитает** этот вектор,
  поэтому положительный Y опускает её: 1.8 − 0.7 ≈ уровень груди;
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

## 5a. Что ещё осталось

- **UV-сдвиг брони по материалу** — единственное, что осталось от текстурных оверрайдов.
  Скин партнёра и слои `boneTextureOverrides` работают, брони это пока не касается.
- `Vec3dInputSection` — кнопки ±0.1 вместо `EditBox` с парсингом double.
- `RegisterCustomGirl*C2SPacket` — пустые; в оригинале они тоже никем не отправлялись,
  можно удалить целиком.
- `TamedGirlManager` — очистка мёртвых девушек отключена (`onServerTick` закомментирован).
- `AbstractGirlModel`/`client/rendering/renderers/*` — дублирующая иерархия рендереров из
  оригинала; в порту всё рисует единый `GirlRenderer`, эти классы можно удалить.
- Остальной `ModConfig` (не-freecam часть) — всё ещё хардкод-дефолты в памяти.

## 6. Правила работы

- После каждого логического этапа: `git add -A && git commit && git push origin <ветка>`,
  затем дождись `gh run list --branch <ветка> --limit 1` → `success`.
  Сборка занимает ~1.5–2 минуты, ждать через `sleep 155` перед проверкой.
- Не оставляй заглушек, молча возвращающих `false`/пустоту, если функция заявлена как
  портированная — лучше не трогать, чем сделать вид, что работает.
- Кнопок в `GirlInventoryScreen` максимум 7 слева и 6 справа, иначе вылезают за экран.
- Любая новая цель ИИ обязана проверять `!isPassenger()`, `!isDowned()`, `!isSceneActive()`.
