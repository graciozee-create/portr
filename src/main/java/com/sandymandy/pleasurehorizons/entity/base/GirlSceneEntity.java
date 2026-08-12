package com.sandymandy.pleasurehorizons.entity.base;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.networking.S2C.PlayCumHudAnimationS2CPacket;
import com.sandymandy.pleasurehorizons.networking.S2C.RunAnimEventsS2CPacket;
import com.sandymandy.pleasurehorizons.registries.SceneKeyframeEventRegistry;
import com.sandymandy.pleasurehorizons.util.Utils;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import com.sandymandy.pleasurehorizons.util.variables.ScenePhase;
import com.sandymandy.pleasurehorizons.util.variables.SceneType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

/**
 * Scene playback and GeckoLib animation support on top of {@link GirlEntity}.
 *
 * <p>The Fabric original targets GeckoLib 5.x, whose animation API differs substantially
 * from the 4.x line that is the only option on 1.21.1:</p>
 * <ul>
 *     <li>{@code AnimationTest<T>} → {@link AnimationState}</li>
 *     <li>{@code AnimationController(name, ticks, handler)} → {@code AnimationController(animatable, name, ticks, handler)}</li>
 *     <li>{@code software.bernie.geckolib.animatable.processing.*} → {@code software.bernie.geckolib.animation.*}</li>
 * </ul>
 *
 * <p>Scene state differs from upstream in one deliberate way: upstream synced the whole
 * {@code Scene} object through a custom tracked-data serialiser. Scenes are static per girl, so
 * this port syncs only the scene's display name and resolves it against {@link #getScenes()} on
 * both sides. That keeps the entity data simple and prevents a client from injecting a
 * hand-crafted scene definition.</p>
 */
public abstract class GirlSceneEntity extends GirlEntity implements GeoEntity {
    private static final EntityDataAccessor<String> CURRENT_SCENE_NAME =
            SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> CURRENT_SCENE_PHASE =
            SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> CURRENT_SEX_ANIM =
            SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> SCENE_PROGRESS =
            SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CUM_THRESHOLD =
            SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> STATIONARY_LOOP =
            SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STATIONARY_LOOP_THRESHOLD =
            SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> INTRO_INDEX =
            SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STATIONARY_INDEX =
            SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PREGNANCY_TICKS =
            SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> THRUSTING =
            SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> CURRENT_SCENE_PLAYER =
            SynchedEntityData.defineId(GirlSceneEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final int PREGNANCY_MAX_TICKS = (int) (20 * 60 * 2.5);
    private static final float PROGRESS_SPEED = 0.1f;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /** Keyframe events observed this tick. Cleared at the end of every tick, like upstream. */
    public final Queue<String> animationEventQueueClient = new LinkedList<>();
    public final Queue<String> animationEventQueueServer = new LinkedList<>();

    protected String lastSceneAnim = "";
    private long lastAnimationFinishInputTick = -1L;
    private long lastAnimationEventInputTick = -1L;
    public String passengerBoneName = "boyCam";
    public Scene stripOptions = Scene.EMPTY;
    @Nullable
    public BlockPos targetBedPos;
    @Nullable
    private BlockPos bedPos;

    private boolean requestStrip = false;
    private boolean requestMoveToBed = false;
    private boolean requestMoveToPlayer = false;
    private boolean requestWaitForPlayer = false;

    protected GirlSceneEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CURRENT_SCENE_NAME, "");
        builder.define(CURRENT_SCENE_PHASE, ScenePhase.NONE.ordinal());
        builder.define(CURRENT_SEX_ANIM, "");
        builder.define(SCENE_PROGRESS, 0f);
        builder.define(CUM_THRESHOLD, 5f);
        builder.define(STATIONARY_LOOP, 0);
        builder.define(STATIONARY_LOOP_THRESHOLD, 0);
        builder.define(INTRO_INDEX, 0);
        builder.define(STATIONARY_INDEX, 0);
        builder.define(PREGNANCY_TICKS, 0);
        builder.define(THRUSTING, false);
        builder.define(CURRENT_SCENE_PLAYER, Optional.empty());
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ------------------------------------------------------------- scene state

    public void setCurrentScene(Scene scene) {
        this.entityData.set(CURRENT_SCENE_NAME, scene == null ? "" : scene.displayName());
    }

    public Scene getCurrentScene() {
        String name = this.entityData.get(CURRENT_SCENE_NAME);
        if (name.isEmpty()) return Scene.EMPTY;
        return findScene(name);
    }

    /** Resolves a scene by display name against this girl's own scene list. */
    public Scene findScene(String displayName) {
        for (Scene scene : getScenes()) {
            if (scene.displayName().equals(displayName)) {
                return scene;
            }
        }
        return Scene.EMPTY;
    }

    public void setCurrentScenePhase(ScenePhase phase) {
        this.entityData.set(CURRENT_SCENE_PHASE, phase.ordinal());
    }

    public ScenePhase getCurrentScenePhase() {
        int ordinal = this.entityData.get(CURRENT_SCENE_PHASE);
        ScenePhase[] values = ScenePhase.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : ScenePhase.NONE;
    }

    public void setCurrentSexAnim(String anim) { this.entityData.set(CURRENT_SEX_ANIM, anim); }
    public String getCurrentSexAnim() { return this.entityData.get(CURRENT_SEX_ANIM); }

    public void setSceneProgress(float progress) { this.entityData.set(SCENE_PROGRESS, progress); }
    public float getSceneProgress() { return this.entityData.get(SCENE_PROGRESS); }

    public void setCumThreshold(float threshold) { this.entityData.set(CUM_THRESHOLD, threshold); }
    public float getCumThreshold() { return this.entityData.get(CUM_THRESHOLD); }

    public void setStationaryLoop(int value) { this.entityData.set(STATIONARY_LOOP, value); }
    public int getStationaryLoop() { return this.entityData.get(STATIONARY_LOOP); }

    public void setStationaryLoopThreshold(int value) { this.entityData.set(STATIONARY_LOOP_THRESHOLD, value); }
    public int getStationaryLoopThreshold() { return this.entityData.get(STATIONARY_LOOP_THRESHOLD); }

    public void setIntroIndex(int value) { this.entityData.set(INTRO_INDEX, value); }
    public int getIntroIndex() { return this.entityData.get(INTRO_INDEX); }

    public void setStationaryIndex(int value) { this.entityData.set(STATIONARY_INDEX, value); }
    public int getStationaryIndex() { return this.entityData.get(STATIONARY_INDEX); }

    public void setPregnancyTicks(int value) { this.entityData.set(PREGNANCY_TICKS, value); }
    public int getPregnancyTicks() { return this.entityData.get(PREGNANCY_TICKS); }

    public void setThrusting(boolean thrust) { this.entityData.set(THRUSTING, thrust); }
    public boolean isThrusting() { return this.entityData.get(THRUSTING); }

    public Queue<String> getAnimationKeyFrameEvent() {
        return this.level().isClientSide() ? this.animationEventQueueClient : this.animationEventQueueServer;
    }

    public void setScenePlayer(@Nullable Player player) {
        this.entityData.set(CURRENT_SCENE_PLAYER,
                player == null ? Optional.empty() : Optional.of(player.getUUID()));
    }

    @Nullable
    public Player getScenePlayer() {
        Optional<UUID> uuid = this.entityData.get(CURRENT_SCENE_PLAYER);
        if (uuid.isEmpty()) return null;
        return this.level().getPlayerByUUID(uuid.get());
    }

    @Nullable
    public ServerPlayer getScenePlayerServer() {
        return getScenePlayer() instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }

    public boolean isCurrentScenePlayer(Player player) {
        Player scenePlayer = getScenePlayer();
        return scenePlayer != null && scenePlayer.getUUID().equals(player.getUUID());
    }

    /** Server-side authorization shared by every packet that advances or changes a scene. */
    public boolean acceptsSceneInputFrom(Player player) {
        return !this.level().isClientSide()
                && this.isSceneActive()
                && this.isCurrentScenePlayer(player);
    }

    public boolean acceptsAnimationFinishFrom(Player player) {
        if (!acceptsSceneInputFrom(player)) return false;

        long now = this.level().getGameTime();
        if (this.lastAnimationFinishInputTick >= 0L
                && now - this.lastAnimationFinishInputTick < 5L) {
            return false;
        }
        this.lastAnimationFinishInputTick = now;
        return true;
    }

    /**
     * Keyframes may arrive while a requested scene is still running its strip animation, before
     * {@link #isSceneActive()} becomes true. The selected scene player remains the sole authority.
     * At most one combined keyframe payload is accepted per server tick.
     */
    public boolean acceptsAnimationEventFrom(Player player) {
        boolean authorized = acceptsSceneInputFrom(player)
                || (!this.level().isClientSide()
                && this.isCurrentScenePlayer(player)
                && this.getCurrentScene() != Scene.EMPTY);
        if (!authorized) return false;

        long now = this.level().getGameTime();
        if (now == this.lastAnimationEventInputTick) return false;
        this.lastAnimationEventInputTick = now;
        return true;
    }

    private boolean hasLocalScenePlayer() {
        Player scenePlayer = getScenePlayer();
        return scenePlayer != null && scenePlayer.isLocalPlayer();
    }

    public float getPregnancyProgress() {
        if (!isPregnant()) return 0f;
        return 1f - (getPregnancyTicks() / (float) PREGNANCY_MAX_TICKS);
    }

    public float getBedOffset() {
        return getCurrentScene().bedAlignmentOffset();
    }

    public boolean isBedScene() {
        return getCurrentScene().sceneType() == SceneType.ON_BED;
    }

    // ------------------------------------------------------------ scene control

    /** Entry point used by {@code StartSceneC2SPacket}; resolves the name server-side. */
    public void startScene(Player player, String sceneName) {
        Scene scene = findScene(sceneName);
        if (scene == Scene.EMPTY) return;
        startScene(player, scene);
    }

    public void startScene(Player player, Scene option) {
        if (this.level().isClientSide()) return;
        if (this.isSceneActive() || this.getScenePlayer() != null) return;
        if (player == null) return;

        if (this.getCurrentRelationshipLevel() < option.requiredRelationshipLevel()) {
            player.displayClientMessage(Component.translatable(
                    "gui.pleasurehorizons.requires_relationship", option.requiredRelationshipLevel()), true);
            return;
        }

        this.setScenePlayer(player);

        if (isPregnant()) {
            player.displayClientMessage(Component.translatable("msg.pleasurehorizons.isPregnant"), true);
            this.setScenePlayer(null);
            return;
        }

        if (this.isSitting()) this.setSitting(false);
        if (this.isPassenger()) this.stopRiding();

        this.setCurrentScene(option);

        if (!this.isStripped() && option.needsToStrip()) {
            // Strip first; StripGoal resumes the scene through resumeAfterStrip().
            this.requestStrip(option);
            return;
        }

        beginScene(option);
    }

    /** Second half of {@link #startScene}, also used once a required strip has finished. */
    public void beginScene(Scene option) {
        if (this.level().isClientSide()) return;
        if (option == null || option == Scene.EMPTY) return;

        Player player = getScenePlayer();
        if (player == null) return;

        if (this.useUpRelationShipLevels()) {
            this.setCurrentRelationshipLevel(
                    Math.max(0, this.getCurrentRelationshipLevel() - option.requiredRelationshipLevel()));
        }

        switch (option.sceneType()) {
            case ON_BED -> {
                Utils.BlockInfo bedInfo = Utils.findNearbyBed(this.level(), this.blockPosition(), 15);
                if (bedInfo == null) {
                    player.displayClientMessage(
                            Component.translatable("msg.pleasurehorizons.noBedFound"), true);
                    this.setCurrentScene(Scene.EMPTY);
                    this.setScenePlayer(null);
                    return;
                }
                PleasureHorizons.usedBeds.put(this.getUUID(), bedInfo.pos());
                this.targetBedPos = bedInfo.pos();
                this.bedPos = bedInfo.pos();
                this.requestMoveToBed();
            }
            case ON_PLAYER -> this.requestMoveToPlayer();
            case STATIONARY_CONTACT -> this.requestWaitForPlayer();
            case STATIONARY_INTRO -> startStationaryIntro(option);
            default -> startStationaryLoop(option);
        }
    }

    /** Called by the girl once she is next to the player (or on the bed) and can mount them. */
    public void startRidingScene(Player player) {
        if (player == null) return;

        SceneType type = getCurrentScene().sceneType();
        if (type == SceneType.STATIONARY_INTRO || type == SceneType.STATIONARY) return;

        player.setInvisible(true);
        this.setSceneProgress(0f);
        this.setCumThreshold(getCurrentScene().cumThreshold());
        this.setThrusting(false);
        this.targetBedPos = null;
        player.startRiding(this, true);
        this.setIntroIndex(0);
        this.lastSceneAnim = "";
        this.setSceneState(true);
        playPhase(getCurrentScene().introAnim().isEmpty() ? ScenePhase.HAVING_SEX : ScenePhase.INTRO);
    }

    private void startStationaryIntro(Scene option) {
        if (option.stationaryIntroAnim().isEmpty()) {
            startStationaryLoop(option);
            return;
        }

        this.setSceneState(true);
        this.setSceneProgress(0f);
        this.setCurrentScenePhase(ScenePhase.STATIONARY_INTRO);
        this.lastSceneAnim = "";
        this.setStationaryIndex(0);
        this.setStationaryLoop(0);
        this.setStationaryLoopThreshold(option.amountOfLoops());
    }

    private void startStationaryLoop(Scene option) {
        this.setSceneState(true);
        this.setSceneProgress(0f);
        this.setCurrentScenePhase(ScenePhase.STATIONARY);
        this.lastSceneAnim = "";
        this.setStationaryLoop(0);
        this.setStationaryLoopThreshold(option.amountOfLoops());
    }

    public void stopScene() {
        if (!this.isSceneActive()) return;

        if (this.level().isClientSide()) {
            if (hasLocalScenePlayer()) {
                PacketDistributor.sendToServer(
                        new com.sandymandy.pleasurehorizons.networking.C2S.StopSceneOnServerC2SPacket(this.getId()));
            }
            return;
        }

        PleasureHorizons.usedBeds.remove(this.getUUID());
        Player scenePlayer = getScenePlayer();
        if (scenePlayer != null) {
            PleasureHorizons.activeScenes.remove(scenePlayer.getUUID());
            scenePlayer.setInvisible(false);
        }

        if (this.isVehicle()) {
            this.ejectPassengers();
        }

        this.setIntroIndex(0);
        this.setStationaryIndex(0);
        this.setSceneProgress(0f);
        this.setCurrentSexAnim("");
        this.setThrusting(false);
        this.setCurrentScenePhase(ScenePhase.NONE);
        this.setSceneState(false);
        this.setCurrentScene(Scene.EMPTY);
        this.setSceneAnim("");
        this.lastSceneAnim = "";
        this.bedPos = null;
        this.targetBedPos = null;
        this.getNavigation().stop();
        this.setScenePlayer(null);
    }

    public void playPhase(ScenePhase phase) {
        if (this.level().isClientSide()) return;
        setCurrentScenePhase(phase);
        this.lastSceneAnim = "";
        if (phase != ScenePhase.INTRO) setIntroIndex(0);
    }

    public void tryTriggerCum() {
        if (this.isSceneActive()
                && this.getSceneProgress() >= this.getCumThreshold()
                && getCurrentScenePhase() != ScenePhase.CUM) {
            playPhase(ScenePhase.CUM);

            if (!this.level().isClientSide() && this.getFirstPassenger() instanceof ServerPlayer rider) {
                PacketDistributor.sendToPlayer(rider, new PlayCumHudAnimationS2CPacket());
            }
        }
    }

    /** Advances the scene when the client reports that an animation finished. */
    public void animationFinished() {
        if (this.level().isClientSide()) return;
        if (!this.isSceneActive()) return;

        Scene options = getCurrentScene();

        switch (getCurrentScenePhase()) {
            case INTRO -> {
                List<String> intros = options.introAnim();
                if (getIntroIndex() < intros.size() - 1) {
                    setIntroIndex(getIntroIndex() + 1);
                } else {
                    playPhase(ScenePhase.HAVING_SEX);
                }
            }
            case CUM -> {
                if (options.countTowardsImpregnation() && this.canGetImpregnated()) {
                    this.setPregnancyStage(this.getPregnancyStage() + 1);
                }
                stopScene();
            }
            case LAYING_DOWN -> playPhase(ScenePhase.BED_IDLE);
            case STATIONARY_INTRO -> {
                if (getStationaryIndex() < options.stationaryIntroAnim().size() - 1) {
                    setStationaryIndex(getStationaryIndex() + 1);
                } else {
                    playPhase(ScenePhase.STATIONARY);
                }
            }
            case STATIONARY -> {
                int current = getStationaryLoop() + 1;
                setStationaryLoop(current);
                if (current >= getStationaryLoopThreshold()) {
                    stopScene();
                }
            }
            default -> {
                // Other phases loop and never report completion.
            }
        }
    }

    // -------------------------------------------------------- keyframe events

    public void handleAnimationEventServer(String key) {
        if (this.level().isClientSide()) return;

        this.animationEventQueueServer.add(key.toLowerCase());
        handleSceneSpeed(key);
        handleSceneFootstepSounds(key);
        relayKeyframeSounds(key);
    }

    /**
     * Replays the keyframe for everyone except the scene player.
     *
     * <p>The scene player already handled it locally the instant the frame fired, which keeps it
     * tight to the animation; replaying it for them would double the sound.</p>
     */
    private void relayKeyframeSounds(String key) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (SceneKeyframeEventRegistry.getSound(getGirlID(), key).isEmpty()) return;

        ServerPlayer scenePlayer = getScenePlayerServer();
        RunAnimEventsS2CPacket packet = new RunAnimEventsS2CPacket(this.getId(), key);

        for (ServerPlayer player : serverLevel.players()) {
            if (player == scenePlayer) continue;
            if (player.distanceToSqr(this) > 32 * 32) continue;
            PacketDistributor.sendToPlayer(player, packet);
        }
    }

    /** Footstep sounds keyed off the block she is standing on. */
    private void handleSceneFootstepSounds(String key) {
        if (!key.toLowerCase().contains("startstep")) return;

        net.minecraft.world.level.block.state.BlockState below =
                this.level().getBlockState(this.blockPosition().below());
        this.playSound(below.getSoundType().getStepSound(), 1.0f, 1.0f);
    }

    public void handleAnimationEventClient(String key) {
        if (!this.level().isClientSide()) return;
        this.animationEventQueueClient.add(key.toLowerCase());

        // Scene dialogue and voice lines bound to this keyframe.
        for (String message : SceneKeyframeEventRegistry.getMessage(getGirlID(), key)) {
            sceneMessageAsGirl(message);
        }
        for (String message : SceneKeyframeEventRegistry.getPlayerMessage(key)) {
            sceneMessageAsPlayer(message);
        }
        for (SoundEvent sound : SceneKeyframeEventRegistry.getSound(getGirlID(), key)) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                    sound, this.getSoundSource(), 1.0f, 1.0f, false);
        }
    }

    /**
     * Prints a scene line in chat as if the girl said it.
     *
     * <p>Only shown to the player currently in the scene. Deliberately does not touch
     * {@code Minecraft.getInstance()}: this class is common code and must never load a
     * client-only type, even from a client-guarded branch.</p>
     */
    protected void sceneMessageAsGirl(String message) {
        Player target = getScenePlayer();
        if (target == null) return;
        target.displayClientMessage(Component.translatable(
                "chat.pleasurehorizons.girlSays", getSceneDisplayName(), message), false);
    }

    protected void sceneMessageAsPlayer(String message) {
        Player target = getScenePlayer();
        if (target == null) return;
        target.displayClientMessage(Component.translatable(
                "chat.pleasurehorizons.girlSays", target.getGameProfile().getName(), message), false);
    }

    /** Overridden by {@code TameableGirlEntity} to respect custom names. */
    public String getSceneDisplayName() {
        String id = getGirlID();
        return id.isEmpty() ? "Girl" : Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }

    private void handleSceneSpeed(String key) {
        if (getCurrentScenePhase() != ScenePhase.HAVING_SEX) return;

        if (key.toLowerCase().contains("thrust")) {
            this.setSceneProgress(this.getSceneProgress() + PROGRESS_SPEED);
        }

        this.setSceneProgress(Math.max(0f, Math.min(this.getSceneProgress(), this.getCumThreshold())));
    }

    // ------------------------------------------------------------------- tick

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            modelLogic();
        }

        if (!this.level().isClientSide()) {
            // Armour visibility depends on the girl's own container plus the stripped flag,
            // neither of which raises an event, so it is re-checked periodically and only
            // broadcast when it actually changes.
            if (this.tickCount % 20 == 0) {
                updateClothingAndArmorIfChanged();
            }

            boolean inSexPhases = switch (getCurrentScenePhase()) {
                case NONE, BED_IDLE, LAYING_DOWN, DIALOG -> false;
                default -> true;
            };
            this.setHavingSex(isSceneActive() && inSexPhases);

            // Scenes that ride the player end as soon as the rider leaves.
            boolean isRidingPhase = switch (getCurrentScenePhase()) {
                case NONE, BED_IDLE, LAYING_DOWN, DIALOG, STATIONARY, STATIONARY_INTRO -> false;
                default -> true;
            };
            if (this.isSceneActive() && isRidingPhase && !this.isVehicle()) {
                stopScene();
            }

            // Bed scenes stop if the bed is destroyed mid-scene.
            if (this.isSceneActive() && bedPos != null && isBedScene()
                    && !Utils.checkForBlockAt(this.level(), bedPos, null, net.minecraft.tags.BlockTags.BEDS)) {
                stopScene();
            }

            // Progress also ticks up while the thrust key is held, so scenes still complete on
            // rigs whose animations carry no "thrust" keyframes.
            if (this.isSceneActive() && getCurrentScenePhase() == ScenePhase.HAVING_SEX && isThrusting()
                    && this.tickCount % 5 == 0) {
                this.setSceneProgress(Math.min(this.getSceneProgress() + PROGRESS_SPEED, this.getCumThreshold()));
            }

            tickPregnancy();

            this.animationEventQueueServer.clear();
        } else {
            this.animationEventQueueClient.clear();
        }
    }

    private void tickPregnancy() {
        if (getPregnancyStage() >= maxPregnancyStage() && !isPregnant()) {
            this.setPregnantState(true);
            this.setPregnancyTicks(PREGNANCY_MAX_TICKS);
        }

        if (!canGetImpregnated()) {
            setPregnancyStage(0);
            setPregnantState(false);
            setPregnancyTicks(0);
            return;
        }

        if (isPregnant() && getPregnancyTicks() > 0) {
            setPregnancyTicks(getPregnancyTicks() - 1);
            if (getPregnancyTicks() <= 0) {
                pregnancyFinished();
            }
        }
    }

    /**
     * Client-side per-frame model state: breast size/offset from the customize screen and the
     * pregnancy belly. Without this the customize screen changed tracked data that nothing read,
     * so the sliders had no visible effect.
     */
    protected void modelLogic() {
        if (!this.level().isClientSide()) return;

        setBoneSize("boobs", getBreastSize(), getBreastMinSize(), getBreastMaxSize());

        Vec3 offset = getBreastOffset();
        if (offset.lengthSqr() > 0) {
            setBonePos("boobs", offset);
        }

        int bellySize = isPregnant()
                ? (int) Mth.lerp(getPregnancyProgress(), 100, getMaxBellySizeWhenPregnant())
                : 100;
        setBoneSize("belly", bellySize);
    }

    /** Overridden by girls that spawn offspring; the base just resets the pregnancy. */
    protected void pregnancyFinished() {
        setPregnantState(false);
        setPregnancyStage(0);
        setPregnancyTicks(0);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART,
                    this.getX(), this.getY() + 1.2D, this.getZ(), 8, 0.4D, 0.4D, 0.4D, 0.05D);
        }
    }

    // ------------------------------------------------------------- animations

    /**
     * Animations live in one file per girl, so every animation name is prefixed
     * with the girl id, matching the shipped {@code *.animation.json} assets.
     */
    public String getAnimationPath(String animation) {
        return "animation." + getGirlID() + "." + animation;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "girl_animations", 4, this::handleAnimations)
                .setSoundKeyframeHandler(event -> {
                    // Keyframe handlers only fire while a client renders her, but guard anyway:
                    // PacketDistributor.sendToServer would blow up if this ever ran server-side.
                    if (!this.level().isClientSide()) return;

                    String key = event.getKeyframeData().getSound().toLowerCase();
                    handleAnimationEventClient(key);
                    if (hasLocalScenePlayer()) {
                        PacketDistributor.sendToServer(
                                new com.sandymandy.pleasurehorizons.networking.C2S.SoundEventSyncC2SPacket(
                                        this.getId(), key));
                    }
                }));
        registrar.add(new AnimationController<>(this, "girl_attack", 4, this::handleAttackAnimations));
        registrar.add(new AnimationController<>(this, "girl_face", 4, this::handleFacialAnimations));
    }

    private PlayState handleFacialAnimations(AnimationState<GirlSceneEntity> state) {
        // The "default" (custom girl) rig has no blink animation - playing it would spam errors.
        if (!hasBlinkAnimation()) {
            return PlayState.STOP;
        }
        return state.setAndContinue(
                RawAnimation.begin().then(getAnimationPath("blink"), Animation.LoopType.LOOP));
    }

    /** Overridden by rigs that lack a blink track. */
    protected boolean hasBlinkAnimation() {
        return true;
    }

    /** Set by {@code PlayAttackAnimationS2CPacket} so remote clients see her swing too. */
    public void triggerSwing() {
        this.swinging = true;
        this.swingTime = 0;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);

        if (hit && this.level() instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players()) {
                PacketDistributor.sendToPlayer(player,
                        new com.sandymandy.pleasurehorizons.networking.S2C.PlayAttackAnimationS2CPacket(this.getId()));
            }
        }

        return hit;
    }

    private PlayState handleAttackAnimations(AnimationState<GirlSceneEntity> state) {
        AnimationController<?> controller = state.getController();

        if (this.swinging && controller.getAnimationState() == AnimationController.State.STOPPED) {
            controller.forceAnimationReset();
            return state.setAndContinue(RawAnimation.begin()
                    .then(getAnimationPath("attack" + RANDOM.nextInt(3)), Animation.LoopType.PLAY_ONCE));
        }

        return PlayState.CONTINUE;
    }

    private PlayState setSceneAnimIfChanged(AnimationState<GirlSceneEntity> state, String anim,
                                            Animation.LoopType loop) {
        if (anim == null || anim.isEmpty()) return PlayState.CONTINUE;

        if (!anim.equals(lastSceneAnim)) {
            state.getController().forceAnimationReset();
            lastSceneAnim = anim;
            return state.setAndContinue(RawAnimation.begin().then(getAnimationPath(anim), loop));
        }
        return PlayState.CONTINUE;
    }

    private PlayState handleAnimations(AnimationState<GirlSceneEntity> state) {
        if (isSceneActive() && getOverrideAnim().isEmpty()) {
            return handleSceneAnimations(state);
        }

        if (isDowned()) {
            return state.setAndContinue(
                    RawAnimation.begin().then(getAnimationPath("downed"), Animation.LoopType.LOOP));
        }

        // An explicitly requested animation always wins.
        String override = getOverrideAnim();
        if (!override.isEmpty()) {
            Animation.LoopType loop = getOverrideLoopState()
                    ? Animation.LoopType.LOOP
                    : (getOverrideHoldState() ? Animation.LoopType.HOLD_ON_LAST_FRAME : Animation.LoopType.PLAY_ONCE);
            return state.setAndContinue(RawAnimation.begin().then(getAnimationPath(override), loop));
        }

        if (isPassenger() && getVehicle() instanceof Player) {
            // getCarryAnimation() predates physical player carrying and names scene/sitting
            // clips (Mika's "carry_slow1" is a scene animation). Start from neutral idle for
            // every rig, then let GirlRenderer layer the dedicated carried pose on top.
            return state.setAndContinue(
                    RawAnimation.begin().then(getAnimationPath("idle"), Animation.LoopType.LOOP));
        }

        if (isSitting()) {
            return state.setAndContinue(
                    RawAnimation.begin().then(getAnimationPath(getCarryAnimation()), Animation.LoopType.LOOP));
        }

        if (state.isMoving()) {
            String walkAnim = this.isSprinting() ? "run" : "walk";
            return state.setAndContinue(
                    RawAnimation.begin().then(getAnimationPath(walkAnim), Animation.LoopType.LOOP));
        }

        return state.setAndContinue(
                RawAnimation.begin().then(getAnimationPath("idle"), Animation.LoopType.LOOP));
    }

    private PlayState handleSceneAnimations(AnimationState<GirlSceneEntity> state) {
        AnimationController<?> controller = state.getController();
        Scene options = getCurrentScene();

        // Tell the server when an animation has run its course (once per animation).
        if (this.level().isClientSide()
                && (controller.hasAnimationFinished() || controller.getAnimationState() == AnimationController.State.PAUSED)
                && !lastSceneAnim.isEmpty()) {
            if (hasLocalScenePlayer()) {
                PacketDistributor.sendToServer(
                        new com.sandymandy.pleasurehorizons.networking.C2S.AnimationFinishC2SPacket(this.getId()));
            }
            lastSceneAnim = "";
        }

        switch (getCurrentScenePhase()) {
            case LAYING_DOWN -> {
                return setSceneAnimIfChanged(state, options.layOnBed(), Animation.LoopType.HOLD_ON_LAST_FRAME);
            }
            case BED_IDLE -> {
                return setSceneAnimIfChanged(state, options.bedIdle(), Animation.LoopType.LOOP);
            }
            case INTRO -> {
                List<String> intros = options.introAnim();
                if (intros.isEmpty()) {
                    playPhase(ScenePhase.HAVING_SEX);
                    return PlayState.CONTINUE;
                }
                String current = intros.get(Math.min(getIntroIndex(), intros.size() - 1));
                return setSceneAnimIfChanged(state, current, Animation.LoopType.HOLD_ON_LAST_FRAME);
            }
            case HAVING_SEX -> {
                boolean thrusting = isThrusting();

                if (getCurrentSexAnim().isBlank()) {
                    setCurrentSexAnim(getRandomFromList(options.slowAnim()));
                }

                if (options.useKeyFrameEvents()) {
                    Queue<String> keys = getAnimationKeyFrameEvent();

                    if (Utils.isStringInQueue(keys, "switch") && thrusting) {
                        setCurrentSexAnim(getRandomFromList(options.fastAnim()));
                    }
                    if (Utils.isStringInQueue(keys, "reset")) {
                        setCurrentSexAnim(getRandomFromList(thrusting ? options.fastAnim() : options.slowAnim()));
                    }

                    return setSceneAnimIfChanged(state, getCurrentSexAnim(), Animation.LoopType.LOOP);
                }

                List<String> anims = thrusting ? options.fastAnim() : options.slowAnim();
                if (anims.isEmpty()) return PlayState.CONTINUE;
                return setSceneAnimIfChanged(state, anims.get(0), Animation.LoopType.LOOP);
            }
            case CUM -> {
                setCurrentSexAnim("");
                return setSceneAnimIfChanged(state, options.cumAnim(), Animation.LoopType.HOLD_ON_LAST_FRAME);
            }
            case STATIONARY_INTRO -> {
                List<String> sequence = options.stationaryIntroAnim();
                if (sequence.isEmpty()) {
                    playPhase(ScenePhase.STATIONARY);
                    return PlayState.CONTINUE;
                }
                String current = sequence.get(Math.min(getStationaryIndex(), sequence.size() - 1));
                return setSceneAnimIfChanged(state, current, Animation.LoopType.HOLD_ON_LAST_FRAME);
            }
            case STATIONARY -> {
                String loopAnim = options.stationaryLoopAnim();
                if (loopAnim == null || loopAnim.isEmpty()) {
                    stopScene();
                    return PlayState.STOP;
                }
                if (getStationaryLoop() >= getStationaryLoopThreshold()) {
                    stopScene();
                    return PlayState.STOP;
                }
                return setSceneAnimIfChanged(state, loopAnim, Animation.LoopType.HOLD_ON_LAST_FRAME);
            }
            default -> {
                return PlayState.STOP;
            }
        }
    }

    protected String getRandomFromList(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return list.get(RANDOM.nextInt(list.size()));
    }

    // ---------------------------------------------------------------- requests

    public void requestMoveToBed() { this.requestMoveToBed = true; }

    public boolean shouldMoveToBed() {
        if (requestMoveToBed) {
            requestMoveToBed = false;
            return true;
        }
        return false;
    }

    public void requestMoveToPlayer() { this.requestMoveToPlayer = true; }

    public boolean shouldMoveToPlayer() {
        if (requestMoveToPlayer) {
            requestMoveToPlayer = false;
            return true;
        }
        return false;
    }

    public void requestWaitForPlayer() { this.requestWaitForPlayer = true; }

    public boolean shouldWaitForPlayer() {
        if (requestWaitForPlayer) {
            requestWaitForPlayer = false;
            return true;
        }
        return false;
    }

    public void requestStrip() {
        this.requestStrip(null);
    }

    public void requestStrip(@Nullable Scene options) {
        this.requestStrip = true;
        if (options != null) {
            this.stripOptions = options;
        }
    }

    /** Returns true at most once per {@link #requestStrip()} call. */
    public boolean shouldStrip() {
        if (this.requestStrip) {
            this.requestStrip = false;
            return true;
        }
        return false;
    }

    /** Called by {@code StripGoal} once the strip finished, to resume a scene that required it. */
    public void resumeAfterStrip() {
        if (this.stripOptions == null || this.stripOptions == Scene.EMPTY) return;
        Scene pending = this.stripOptions;
        this.stripOptions = Scene.EMPTY;
        beginScene(pending);
    }

    // --------------------------------------------------------------- save data

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("PregnancyTicks", this.getPregnancyTicks());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setPregnancyTicks(compound.getInt("PregnancyTicks"));
    }
}
