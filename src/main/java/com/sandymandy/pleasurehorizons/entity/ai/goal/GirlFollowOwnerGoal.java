package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

import java.util.EnumSet;

/**
 * Follows the owner while "following" is enabled. Modelled on vanilla FollowOwnerGoal, but
 * written by hand because the girl is a PathfinderMob rather than a TamableAnimal.
 *
 * <p>Distances come from the per-girl "follow distance" setting, and the optional follow-teleport
 * reproduces vanilla tamed-pet behaviour: when she is more than 12 blocks away and cannot path
 * to the owner (or is hopelessly far at 32+), she teleports next to them instead of being
 * permanently lost behind terrain, water or a cliff.</p>
 */
public class GirlFollowOwnerGoal extends Goal {
    private static final double TELEPORT_MIN_DISTANCE_SQ = 12.0D * 12.0D;
    private static final double TELEPORT_FAR_DISTANCE_SQ = 32.0D * 32.0D;

    private final TameableGirlEntity girl;
    private final double speedModifier;
    private final PathNavigation navigation;

    private LivingEntity owner;
    private int timeToRecalcPath;
    private int teleportCooldown;
    private int failedRecalcs;
    private float oldWaterCost;

    public GirlFollowOwnerGoal(TameableGirlEntity girl, double speedModifier,
                               float startDistance, float stopDistance) {
        this.girl = girl;
        this.speedModifier = speedModifier;
        this.navigation = girl.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.girl.isTamed() || !this.girl.isFollowing()) {
            return false;
        }
        if (this.girl.isSitting() || this.girl.isSceneActive() || this.girl.isMovementLocked()
                || this.girl.isDowned() || this.girl.isPassenger()) {
            return false;
        }
        LivingEntity owner = this.girl.getOwner();
        if (owner == null || owner.isSpectator()) {
            return false;
        }
        float startDistance = this.girl.getFollowDistance();
        if (this.girl.distanceToSqr(owner) < (double) (startDistance * startDistance)) {
            return false;
        }
        this.owner = owner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.navigation.isDone()) {
            return false;
        }
        if (this.girl.isSitting() || this.girl.isSceneActive() || !this.girl.isFollowing()
                || this.girl.isDowned() || this.girl.isPassenger()) {
            return false;
        }
        float stopDistance = Math.max(1, this.girl.getFollowDistance() - 2);
        return this.girl.distanceToSqr(this.owner) > (double) (stopDistance * stopDistance);
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.teleportCooldown = 0;
        this.failedRecalcs = 0;
        this.oldWaterCost = this.girl.getPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER);
        this.girl.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.girl.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        this.girl.getLookControl().setLookAt(this.owner, 10.0F, this.girl.getMaxHeadXRot());
        if (this.teleportCooldown > 0) {
            this.teleportCooldown--;
        }
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            boolean pathStarted = this.navigation.moveTo(this.owner, this.speedModifier);
            if (!pathStarted) {
                this.failedRecalcs++;
            } else {
                this.failedRecalcs = 0;
            }
        }

        // Teleport rules. The old shape ("distance > 12 && navigation.isDone()") almost never
        // fired: canContinueToUse() stops the goal the moment the path completes, so tick()
        // rarely ran with isDone() - she just walk-stall-walk-stalled behind walls. Now a
        // failed/partial route is detected via consecutive failed moveTo calls, and a
        // >32-block gap teleports unconditionally (the entity-level failsafe also covers the
        // case where another goal is holding the MOVE flag and this goal never runs).
        if (this.girl.isFollowTeleportEnabled() && this.teleportCooldown == 0
                && this.owner.level() == this.girl.level() && !this.owner.isSpectator()
                && !this.owner.isFallFlying()
                && this.owner instanceof net.minecraft.world.entity.player.Player player) {
            double distanceSqr = this.girl.distanceToSqr(this.owner);
            boolean blockedRoute = distanceSqr > TELEPORT_MIN_DISTANCE_SQ
                    && this.failedRecalcs >= 3;
            if (distanceSqr > TELEPORT_FAR_DISTANCE_SQ || blockedRoute) {
                if (this.girl.teleportNear(player)) {
                    this.teleportCooldown = this.adjustedTickDelay(20);
                    this.failedRecalcs = 0;
                }
            }
        }
    }
}
