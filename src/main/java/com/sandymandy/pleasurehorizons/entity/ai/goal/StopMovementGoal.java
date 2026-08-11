package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

public class StopMovementGoal extends Goal {
    private final GirlEntity entity;
    public float bodyYaw;

    public StopMovementGoal(GirlEntity entity) {
        this.entity = entity;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.JUMP));
    }

    @Override
    public boolean canStart() {
        return entity.isMovementLocked();
    }

    @Override
    public boolean shouldContinue() {
        return entity.isMovementLocked();
    }

    @Override
    public void start() {
        bodyYaw = entity.getBodyYaw();
        haltMovement();
    }

    @Override
    public void tick() {
        haltMovement();
    }

    @Override
    public void stop() {
        // when unfreezing, clear any move control and let AI re-path
        entity.getNavigation().stop();
    }

    private void haltMovement() {
        entity.getNavigation().stop();
        entity.setVelocity(0, entity.getVelocity().y > 0 ? 0 : entity.getVelocity().y, 0); // stops lateral motion
        entity.setJumping(false);
        entity.bodyYaw = bodyYaw;

        MoveControl control = entity.getMoveControl();
        if (control != null) {
            control.moveTo(entity.getX(), entity.getY(), entity.getZ(), 0); // keep position
        }
    }


}
