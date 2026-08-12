package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Walks the girl over to the scene player for an {@code ON_PLAYER} scene, then mounts them.
 *
 * <p>Yarn to Mojang: {@code canStart} → {@code canUse}, {@code shouldContinue} → {@code canContinueToUse},
 * {@code setControls} → {@code setFlags}, {@code startMovingTo} → {@code moveTo},
 * {@code squaredDistanceTo} → {@code distanceToSqr}, {@code setVelocity} → {@code setDeltaMovement}.</p>
 */
public class MoveToPlayerGoal extends Goal {
    private final GirlSceneEntity girl;
    private final double speed;
    private boolean started = false;
    private boolean sceneStarted = false;
    private int ticksRunning = 0;

    public MoveToPlayerGoal(GirlSceneEntity girl, double speed) {
        this.girl = girl;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return girl.shouldMoveToPlayer() && girl.getScenePlayer() != null;
    }

    @Override
    public void start() {
        this.started = false;
        this.sceneStarted = false;
        this.ticksRunning = 0;
    }

    @Override
    public void tick() {
        ticksRunning++;
        Player player = girl.getScenePlayer();
        if (player == null) {
            started = true; // ends the goal
            return;
        }

        if (!started) {
            girl.getNavigation().moveTo(player, this.speed);
        }

        if (girl.distanceToSqr(player) <= 2.5D) {
            girl.setDeltaMovement(Vec3.ZERO);
            girl.getNavigation().stop();
            girl.startRidingScene(player);
            sceneStarted = girl.isSceneActive() && girl.isVehicle();
            started = true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        // Give up after 15 seconds so a girl that cannot path never sticks in this goal.
        return !started && ticksRunning < 300 && girl.getScenePlayer() != null;
    }

    @Override
    public void stop() {
        girl.getNavigation().stop();
        if (!this.sceneStarted) {
            // A failed path or vanished player must not leave the girl/player reserved forever.
            girl.stopScene();
        }
        this.started = false;
        this.sceneStarted = false;
        this.ticksRunning = 0;
    }
}
