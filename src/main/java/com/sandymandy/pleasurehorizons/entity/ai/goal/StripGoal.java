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
    }

    @Override
    public void tick() {
        if (!girl.hasStripAnim()) return;
        if (started) {
            if (!girl.isFrozenInPlace()) girl.setFreeze(true);
            if (isStringInQueue(girl.animationKeyFrameEvent, "becomeNude".toLowerCase()) && !stripTrigged) {
                girl.setStripped(!girl.isStripped());
                stripTrigged = true;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (!girl.hasStripAnim()) return false;
        return !stripTrigged || !girl.getOverrideAnim().isEmpty();
    }

    @Override
    public void stop() {
        if (girl.hasStripAnim()) {
            girl.setFreeze(false);
        } else {
            girl.setStripped(!girl.isStripped());
        }
        started = false;
    }
}
