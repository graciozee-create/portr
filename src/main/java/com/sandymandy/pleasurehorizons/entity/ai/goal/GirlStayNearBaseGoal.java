package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

public class GirlStayNearBaseGoal extends Goal {
    private final TameableGirlEntity girl;
    private final double speed;
    private final EntityNavigation navigation;
    private Path path;

    private int updateCountdownTicks;

    private final float maxDistance;   // Max allowed distance from base before returning
    private final float minDistance;   // Distance considered "close enough" to base
    private final float breakOffPoint; // Distance beyond which this goal won't run

    private float oldWaterPathPenalty;

    public GirlStayNearBaseGoal(TameableGirlEntity girl, double speed, float minDistance, float maxDistance, float breakOffPoint) {
        this.girl = girl;
        this.speed = speed;
        this.navigation = girl.getNavigation();
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.breakOffPoint = breakOffPoint;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if(!girl.isTamed()) return false    ;
        BlockPos basePos = girl.getBasePos();
        if (basePos == null || girl.isFollowing()) {
            return false;
        }

        double distToBase = girl.squaredDistanceTo(basePos.getX() + 0.5, basePos.getY(), basePos.getZ() + 0.5);

        // Don't activate if too far
        if (distToBase > breakOffPoint * breakOffPoint) {
            return false;
        }

        // Don’t need to move if already close enough
        if (distToBase <= maxDistance * maxDistance) {
            return false;
        }

        // Try to build a valid path
        this.path = this.navigation.findPathTo(basePos.getX() + 0.5, basePos.getY(), basePos.getZ() + 0.5, 0);
        return this.path != null;
    }

    @Override
    public boolean shouldContinue() {
        BlockPos basePos = girl.getBasePos();
        if (basePos == null || girl.isFollowing() || navigation.isIdle()) {
            return false;
        }

        double distToBase = girl.squaredDistanceTo(basePos.getX() + 0.5, basePos.getY(), basePos.getZ() + 0.5);

        // Continue while not within min distance and path still valid
        return distToBase > minDistance * minDistance && !navigation.isIdle();
    }

    @Override
    public void start() {
        updateCountdownTicks = 0;
        oldWaterPathPenalty = girl.getPathfindingPenalty(PathNodeType.WATER);
        girl.setPathfindingPenalty(PathNodeType.WATER, 0.0F);

        if (path != null) {
            navigation.startMovingAlong(path, speed);
        }
    }

    @Override
    public void stop() {
        navigation.stop();
        girl.setPathfindingPenalty(PathNodeType.WATER, oldWaterPathPenalty);
        path = null;
    }

    @Override
    public void tick() {
        BlockPos basePos = girl.getBasePos();
        if (basePos == null) return;

        // Always look at the base
        girl.getLookControl().lookAt(basePos.getX() + 0.5, basePos.getY(), basePos.getZ() + 0.5, 10.0F, girl.getMaxLookPitchChange());

        // Recalculate path periodically if idle or lost
        if (--updateCountdownTicks <= 0) {
            updateCountdownTicks = this.getTickCount(40); // recalc roughly every 2 seconds

            if (navigation.isIdle() || (path != null && path.isFinished())) {
                path = this.navigation.findPathTo(basePos.getX() + 0.5, basePos.getY(), basePos.getZ() + 0.5, 0);
                if (path != null) {
                    navigation.startMovingAlong(path, speed);
                }
            }
        }
    }
}
