package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

import java.util.EnumSet;

/**
 * Follows the owner while "following" is enabled. Modelled on vanilla FollowOwnerGoal, but
 * written by hand because the girl is a PathfinderMob rather than a TamableAnimal.
 */
public class GirlFollowOwnerGoal extends Goal {
    private final TameableGirlEntity girl;
    private final double speedModifier;
    private final float startDistance;
    private final float stopDistance;
    private final PathNavigation navigation;

    private LivingEntity owner;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public GirlFollowOwnerGoal(TameableGirlEntity girl, double speedModifier,
                               float startDistance, float stopDistance) {
        this.girl = girl;
        this.speedModifier = speedModifier;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.navigation = girl.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.girl.isTamed() || !this.girl.isFollowing()) {
            return false;
        }
        if (this.girl.isSitting() || this.girl.isSceneActive() || this.girl.isMovementLocked()) {
            return false;
        }
        LivingEntity owner = this.girl.getOwner();
        if (owner == null || owner.isSpectator()) {
            return false;
        }
        if (this.girl.distanceToSqr(owner) < (double) (this.startDistance * this.startDistance)) {
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
        if (this.girl.isSitting() || this.girl.isSceneActive() || !this.girl.isFollowing()) {
            return false;
        }
        return this.girl.distanceToSqr(this.owner) > (double) (this.stopDistance * this.stopDistance);
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
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
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            this.navigation.moveTo(this.owner, this.speedModifier);
        }
    }
}
