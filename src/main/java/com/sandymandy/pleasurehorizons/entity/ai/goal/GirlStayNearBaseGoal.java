package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class GirlStayNearBaseGoal extends Goal {
    private static final int GIVE_UP_TICKS = 600;
    private static final int GIVE_UP_COOLDOWN = 200;

    private final TameableGirlEntity girl;
    private final double speed;
    private int repath = 0;
    private int elapsed = 0;
    private int cooldown = 0;
    private boolean givenUp = false;

    public GirlStayNearBaseGoal(TameableGirlEntity girl, double speed, float minDist, float maxDist) {
        this.girl = girl;
        this.speed = speed;
        // minDist/maxDist now come from the per-girl "stay radius" setting; the constructor
        // values are kept for signature compatibility but unused.
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (!girl.isStayNearBaseEnabled()) return false;
        if (girl.isSitting() || girl.isSceneActive() || girl.isDowned() || girl.isPassenger()) return false;
        BlockPos base = girl.getBasePos();
        if (BlockPos.ZERO.equals(base)) return false;
        double maxDist = girl.stayNearBaseMaxDistance();
        return girl.distanceToSqr(base.getX() + 0.5, base.getY(), base.getZ() + 0.5) > (maxDist * maxDist);
    }

    @Override
    public boolean canContinueToUse() {
        if (!girl.isStayNearBaseEnabled() || givenUp) return false;
        if (girl.isSitting() || girl.isSceneActive()
                || girl.isDowned() || girl.isPassenger()) return false;
        BlockPos base = girl.getBasePos();
        if (BlockPos.ZERO.equals(base)) return false;
        double minDist = girl.stayNearBaseMinDistance();
        return girl.distanceToSqr(base.getX() + 0.5, base.getY(), base.getZ() + 0.5) > (minDist * minDist);
    }

    @Override
    public void start() {
        this.repath = 0;
        this.elapsed = 0;
        BlockPos base = girl.getBasePos();
        if (!BlockPos.ZERO.equals(base)) {
            girl.getNavigation().moveTo(base.getX() + 0.5, base.getY(), base.getZ() + 0.5, speed);
        }
    }

    @Override
    public void tick() {
        if (++elapsed > GIVE_UP_TICKS) {
            // Base unreachable (walled off, floating island) - stop blocking other goals and
            // retry later instead of standing still outside the radius forever.
            givenUp = true;
            return;
        }
        BlockPos base = girl.getBasePos();
        if (BlockPos.ZERO.equals(base)) return;
        double x = base.getX() + 0.5;
        double y = base.getY();
        double z = base.getZ() + 0.5;
        double minDist = girl.stayNearBaseMinDistance();
        if (girl.distanceToSqr(x, y, z) > minDist * minDist && --repath <= 0) {
            repath = this.adjustedTickDelay(10);
            girl.getNavigation().moveTo(x, y, z, speed);
        }
    }

    @Override
    public void stop() {
        if (givenUp) {
            cooldown = GIVE_UP_COOLDOWN;
            givenUp = false;
        }
        girl.getNavigation().stop();
    }
}
