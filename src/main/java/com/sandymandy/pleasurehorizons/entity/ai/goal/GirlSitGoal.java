package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Keeps a sitting or otherwise immobilised girl in place. */
public class GirlSitGoal extends Goal {
    private final TameableGirlEntity girl;

    public GirlSitGoal(TameableGirlEntity girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.girl.isSitting()
                || this.girl.isFrozenInPlace()
                || this.girl.isMovementLocked()
                || this.girl.isSceneActive();
    }

    @Override
    public void start() {
        this.girl.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.girl.getNavigation().stop();
    }
}
