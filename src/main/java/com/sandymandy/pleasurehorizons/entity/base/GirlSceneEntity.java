package com.sandymandy.pleasurehorizons.entity.base;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Adds GeckoLib animation support on top of {@link GirlEntity}.
 *
 * <p>The Fabric original targets GeckoLib 5.x, whose animation API differs substantially
 * from the 4.x line that is the only option on 1.21.1:</p>
 * <ul>
 *     <li>{@code AnimationTest<T>} → {@link AnimationState}</li>
 *     <li>{@code AnimationController(name, ticks, handler)} → {@code AnimationController(animatable, name, ticks, handler)}</li>
 *     <li>{@code software.bernie.geckolib.animatable.processing.*} → {@code software.bernie.geckolib.animation.*}</li>
 * </ul>
 */
public abstract class GirlSceneEntity extends GirlEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected String lastSceneAnim = "";

    protected GirlSceneEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * Animations live in one file per girl, so every animation name is prefixed
     * with the girl id, matching the shipped {@code *.animation.json} assets.
     */
    public String getAnimationPath(String animation) {
        return "animation." + getGirlID() + "." + animation;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "girl_animations", 4, this::handleAnimations));
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

    private PlayState handleAttackAnimations(AnimationState<GirlSceneEntity> state) {
        AnimationController<?> controller = state.getController();

        if (this.swinging && controller.getAnimationState() == AnimationController.State.STOPPED) {
            controller.forceAnimationReset();
            return state.setAndContinue(RawAnimation.begin()
                    .then(getAnimationPath("attack" + RANDOM.nextInt(3)), Animation.LoopType.PLAY_ONCE));
        }

        return PlayState.CONTINUE;
    }

    private PlayState handleAnimations(AnimationState<GirlSceneEntity> state) {
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

        // Scene animations come next.
        String sceneAnim = getSceneAnim();
        if (isSceneActive() && !sceneAnim.isEmpty()) {
            this.lastSceneAnim = sceneAnim;
            return state.setAndContinue(
                    RawAnimation.begin().then(getAnimationPath(sceneAnim), Animation.LoopType.LOOP));
        }

        if (isSitting()) {
            return state.setAndContinue(
                    RawAnimation.begin().then(getAnimationPath("sit"), Animation.LoopType.LOOP));
        }

        if (state.isMoving()) {
            String walkAnim = this.isSprinting() ? "run" : "walk";
            return state.setAndContinue(
                    RawAnimation.begin().then(getAnimationPath(walkAnim), Animation.LoopType.LOOP));
        }

        return state.setAndContinue(
                RawAnimation.begin().then(getAnimationPath("idle"), Animation.LoopType.LOOP));
    }

    /**
     * Set by the "Strip" inventory button; consumed once by {@link #shouldStrip()}
     * on the next tick so the request survives the packet-to-tick hop exactly once.
     */
    private boolean requestStrip = false;

    public void requestStrip() {
        this.requestStrip = true;
    }

    /** Returns true at most once per {@link #requestStrip()} call. */
    public boolean shouldStrip() {
        if (this.requestStrip) {
            this.requestStrip = false;
            return true;
        }
        return false;
    }

    public void animationFinished() {}
    public void stopScene() {}
    public void playPhase(int phase) {}
    public void handleAnimationEventServer(String event) {}
    public void startScene(net.minecraft.world.entity.player.Player player, String scene) {}
    public void tryTriggerCum() {}
    public void setThrusting(boolean held) {}
    public java.util.Queue<String> animationKeyFrameEvent = new java.util.LinkedList<>();
}
