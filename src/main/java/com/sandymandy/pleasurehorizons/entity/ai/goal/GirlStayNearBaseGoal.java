package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class GirlStayNearBaseGoal extends Goal {
    private final TameableGirlEntity girl;
    private final double speed;
    private final float minDist;
    private final float maxDist;

    public GirlStayNearBaseGoal(TameableGirlEntity girl, double speed, float minDist, float maxDist) {
        this.girl = girl;
        this.speed = speed;
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!girl.isStayNearBaseEnabled()) return false;
        if (girl.isSitting() || girl.isFollowing() || girl.isSceneActive() || girl.isDowned() || girl.isPassenger()) return false;
        BlockPos base = girl.getBasePos();
        if (BlockPos.ZERO.equals(base)) return false;
        return girl.distanceToSqr(base.getX() + 0.5, base.getY(), base.getZ() + 0.5) > (maxDist * maxDist);
    }

    @Override
    public boolean canContinueToUse() {
        if (!girl.isStayNearBaseEnabled()) return false;
        if (girl.isSitting() || girl.isFollowing() || girl.isSceneActive()
                || girl.isDowned() || girl.isPassenger()) return false;
        BlockPos base = girl.getBasePos();
        if (BlockPos.ZERO.equals(base)) return false;
        return girl.distanceToSqr(base.getX() + 0.5, base.getY(), base.getZ() + 0.5) > (minDist * minDist);
    }

    @Override
    public void start() {
        BlockPos base = girl.getBasePos();
        if (!BlockPos.ZERO.equals(base)) {
            girl.getNavigation().moveTo(base.getX() + 0.5, base.getY(), base.getZ() + 0.5, speed);
        }
    }

    @Override
    public void stop() {
        girl.getNavigation().stop();
    }
}
