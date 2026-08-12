package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Pins a movement-locked girl in place.
 *
 * <p>Yarn to Mojang: {@code getBodyYaw}/{@code bodyYaw} → {@code getYBodyRot}/{@code setYBodyRot},
 * {@code setVelocity} → {@code setDeltaMovement}, {@code setJumping} → {@code setJumping} (unchanged),
 * {@code MoveControl.moveTo} → {@code MoveControl.setWantedPosition}.</p>
 */
public class StopMovementGoal extends Goal {
    private final GirlEntity entity;
    private float bodyYaw;

    public StopMovementGoal(GirlEntity entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return entity.isMovementLocked();
    }

    @Override
    public boolean canContinueToUse() {
        return entity.isMovementLocked();
    }

    @Override
    public void start() {
        bodyYaw = entity.getYBodyRot();
        haltMovement();
    }

    @Override
    public void tick() {
        haltMovement();
    }

    @Override
    public void stop() {
        entity.getNavigation().stop();
    }

    private void haltMovement() {
        entity.getNavigation().stop();
        Vec3 velocity = entity.getDeltaMovement();
        entity.setDeltaMovement(0, velocity.y > 0 ? 0 : velocity.y, 0);
        entity.setJumping(false);
        entity.setYBodyRot(bodyYaw);

        MoveControl control = entity.getMoveControl();
        if (control != null) {
            control.setWantedPosition(entity.getX(), entity.getY(), entity.getZ(), 0);
        }
    }
}
