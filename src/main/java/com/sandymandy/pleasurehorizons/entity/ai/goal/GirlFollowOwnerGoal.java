package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class GirlFollowOwnerGoal extends Goal {
    private final TameableGirlEntity tameable;
    @Nullable
    private LivingEntity owner;
    private final double speed;
    private final EntityNavigation navigation;
    private int updateCountdownTicks;
    private final float maxDistance;
    private final float minDistance;
    private float oldWaterPathfindingPenalty;

    public GirlFollowOwnerGoal(TameableGirlEntity tameable, double speed, float minDistance, float maxDistance) {
        this.tameable = tameable;
        this.speed = speed;
        this.navigation = tameable.getNavigation();
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        if (!(tameable.getNavigation() instanceof MobNavigation) && !(tameable.getNavigation() instanceof BirdNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    @Override
    public boolean canStart() {
        LivingEntity livingEntity = this.tameable.getOwner();
        if (livingEntity == null) {
            return false;
        } else if (this.tameable.cannotFollowOwner()) {
            return false;
        } else if (this.tameable.squaredDistanceTo(livingEntity) < this.minDistance * this.minDistance) {
            return false;
        } else {
            this.owner = livingEntity;
            return true;
        }
    }

    @Override
    public boolean shouldContinue() {
        if (this.navigation.isIdle()) {
            return false;
        } else {
            return !this.tameable.cannotFollowOwner() && !(this.tameable.squaredDistanceTo(this.owner) <= this.maxDistance * this.maxDistance);
        }
    }

    @Override
    public void start() {
        this.updateCountdownTicks = 0;
        this.oldWaterPathfindingPenalty = this.tameable.getPathfindingPenalty(PathNodeType.WATER);
        this.tameable.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.tameable.setSprinting(false);
        this.owner = null;
        this.navigation.stop();
        this.tameable.setPathfindingPenalty(PathNodeType.WATER, this.oldWaterPathfindingPenalty);
    }

    @Override
    public void tick() {
        boolean bl = this.tameable.shouldTryTeleportToOwner();
        if (!bl) {
            this.tameable.getLookControl().lookAt(this.owner, 10.0F, this.tameable.getMaxLookPitchChange());
        }

        if (--this.updateCountdownTicks <= 0) {
            this.updateCountdownTicks = this.getTickCount(10);
            if (bl) {
                this.tameable.setSprinting(false);
                this.tameable.tryTeleportToOwner();
            } else {
                this.tameable.setSprinting(true);
                this.navigation.startMovingTo(this.owner, this.speed);
            }
        }
    }
}
