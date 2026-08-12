package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

import static com.sandymandy.pleasurehorizons.util.Utils.isStringInQueue;

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
        stripTrigged = false;
        started = true;
        ticksInGoal = 0;
    }

    @Override
    public void tick() {
        ticksInGoal++;

        if (!girl.hasStripAnim()) {
            // No animation - goal will stop immediately via canContinueToUse()==false,
            // the toggle+unfreeze happens in stop(). Nothing to do here.
            return;
        }

        if (started) {
            if (!girl.isFrozenInPlace()) girl.setFreeze(true);

            // Original logic: toggle when keyframe \"becomeNude\" fires
            if (!stripTrigged && isStringInQueue(girl.animationKeyFrameEvent, "becomeNude".toLowerCase())) {
                girl.setStripped(!girl.isStripped());
                stripTrigged = true;
            }

            // Fallback for NeoForge port where animation keyframe queue is not populated
            // and/or client never sends becomeNude: toggle after ~0.5s
            if (!stripTrigged && ticksInGoal >= 10) {
                girl.setStripped(!girl.isStripped());
                stripTrigged = true;
            }

            // After toggle, keep animation playing a bit then clear override so goal can finish.
            // Original Fabric cleared override via client packet when animation finished.
            if (stripTrigged && ticksInGoal >= 30) {
                // Clear override anim to let canContinueToUse() become false
                if (!girl.getOverrideAnim().isEmpty()) {
                    girl.setOverrideAnim("");
                }
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (!girl.hasStripAnim()) return false;
        // Keep running until stripped flag flipped and override anim cleared (or timed out)
        if (ticksInGoal > 60) return false; // hard timeout - never freeze forever
        return !stripTrigged || !girl.getOverrideAnim().isEmpty();
    }

    @Override
    public void stop() {
        // Always unfreeze - fixes permanent freeze when hasStripAnim() == false or animation never ends
        girl.setFreeze(false);
        // Ensure override anim is cleared if we were the ones who set it
        if (girl.hasStripAnim() && "strip".equals(girl.getOverrideAnim())) {
            girl.setOverrideAnim("");
        }
        if (!girl.hasStripAnim() && started) {
            // started==true ensures we only toggle once even if stop() called multiple times
            girl.setStripped(!girl.isStripped());
        }
        started = false;
        ticksInGoal = 0;

        // If original code had a pending scene to resume after stripping, handle is stubbed in port
        // (GirlSceneEntity no longer stores stripOptions). Keep hook for future.
        if (scene != null && !scene.equals(Scene.EMPTY)) {
            // In full port this would restart scene; in stub we just clear
            scene = Scene.EMPTY;
        }
    }
}
