package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

public class GirlSitGoal extends Goal {
    private final GirlEntity tameable;

    public GirlSitGoal(GirlEntity tameable) {
        this.tameable = tameable;
        this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
    }

    @Override
    public boolean shouldContinue() {
        return this.tameable.isSitting();
    }

    @Override
    public boolean canStart() {
        if (this.tameable.isTouchingWater()) {
            return false;
        } else if (!this.tameable.isOnGround()) {
            return false;
        } else {
                return this.tameable.isSitting();
        }
    }

    @Override
    public void start() {
        this.tameable.getNavigation().stop();
        this.tameable.setSitting(true);
    }

    @Override
    public void stop() {
        this.tameable.setSitting(false);
    }
}
