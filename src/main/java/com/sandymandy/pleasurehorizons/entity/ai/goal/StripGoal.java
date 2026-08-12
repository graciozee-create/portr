package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

import static com.sandymandy.pleasurehorizons.util.Utils.isStringInQueue;

/**
 * Plays the strip/dress animation and flips the stripped flag.
 *
 * <p>Two differences from the Fabric original, both fixing a hard freeze:</p>
 * <ul>
 *     <li>{@link #stop()} always clears the freeze flag. Upstream only cleared it when the rig had
 *     a strip animation, so a girl without one stayed frozen forever.</li>
 *     <li>A tick-based fallback flips the flag if the {@code becomeNude} keyframe never arrives -
 *     the event only reaches the server when a client is rendering her.</li>
 * </ul>
 */
public class StripGoal extends Goal {
    private final GirlSceneEntity girl;
    private boolean stripTrigged = false;
    private boolean started = false;
    private Scene scene = Scene.EMPTY;
    private int ticksInGoal = 0;

    public StripGoal(GirlSceneEntity girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return girl.getOverrideAnim().isEmpty() && girl.shouldStrip();
    }

    @Override
    public void start() {
        girl.setFreeze(true);
        if (girl.hasStripAnim()) girl.setOverrideAnim("strip");

        // A scene that required stripping parked itself here; resume it in stop().
        if (girl.stripOptions != null && girl.stripOptions != Scene.EMPTY) {
            this.scene = girl.stripOptions;
            girl.stripOptions = Scene.EMPTY;
        }

        stripTrigged = false;
        started = true;
        ticksInGoal = 0;
    }

    @Override
    public void tick() {
        ticksInGoal++;

        if (!girl.hasStripAnim()) {
            // No animation - the goal ends immediately, toggle + unfreeze happen in stop().
            return;
        }

        if (started) {
            if (!girl.isFrozenInPlace()) girl.setFreeze(true);

            if (!stripTrigged
                    && isStringInQueue(girl.getAnimationKeyFrameEvent(), "becomeNude".toLowerCase())) {
                girl.setStripped(!girl.isStripped());
                stripTrigged = true;
            }

            // Fallback: no client rendering her means no keyframe ever arrives.
            if (!stripTrigged && ticksInGoal >= 10) {
                girl.setStripped(!girl.isStripped());
                stripTrigged = true;
            }

            if (stripTrigged && ticksInGoal >= 30 && !girl.getOverrideAnim().isEmpty()) {
                girl.setOverrideAnim("");
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (!girl.hasStripAnim()) return false;
        if (ticksInGoal > 60) return false; // hard timeout - never freeze forever
        return !stripTrigged || !girl.getOverrideAnim().isEmpty();
    }

    @Override
    public void stop() {
        // Always unfreeze - this is what fixes the permanent freeze after pressing Strip.
        girl.setFreeze(false);

        if (girl.hasStripAnim() && "strip".equals(girl.getOverrideAnim())) {
            girl.setOverrideAnim("");
        }
        if (!girl.hasStripAnim() && started) {
            girl.setStripped(!girl.isStripped());
        }

        started = false;
        ticksInGoal = 0;

        // Resume the scene that asked her to strip first.
        if (this.scene != null && this.scene != Scene.EMPTY) {
            Scene pending = this.scene;
            this.scene = Scene.EMPTY;
            girl.stripOptions = pending;
            girl.resumeAfterStrip();
        }
    }
}
