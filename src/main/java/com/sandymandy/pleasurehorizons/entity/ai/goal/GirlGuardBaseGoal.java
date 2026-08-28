package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

public class GirlGuardBaseGoal extends TargetGoal {
    private static final int SCAN_INTERVAL = 20;

    private final TameableGirlEntity girl;
    private int scanCooldown = 0;

    public GirlGuardBaseGoal(TameableGirlEntity girl) {
        super(girl, false);
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!girl.isGuardBaseEnabled()) return false;
        if (girl.isSitting() || girl.isFollowing() || girl.isSceneActive() || girl.isDowned() || girl.isPassenger()) {
            return false;
        }
        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }
        scanCooldown = SCAN_INTERVAL;
        BlockPos center = girl.getBasePos();
        if (center.equals(BlockPos.ZERO)) {
            center = girl.blockPosition();
        }
        AABB box = new AABB(center).inflate(16.0D, 6.0D, 16.0D);
        // Search Mob + Enemy (not just Monster) so Phantoms and other flying mobs are included.
        java.util.List<Mob> candidates = girl.level().getEntitiesOfClass(Mob.class, box,
                mob -> mob.isAlive() && mob instanceof Enemy
                        && !girl.isAvoidCreepersEnabled(mob));
        Mob found = girl.level().getNearestEntity(candidates,
                TargetingConditions.forCombat().range(16.0D), girl,
                girl.getX(), girl.getY(), girl.getZ());
        if (found != null && found.isAlive()) {
            girl.setTarget(found);
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return girl.isGuardBaseEnabled() && !girl.isSitting() && !girl.isFollowing()
                && !girl.isSceneActive() && !girl.isDowned() && !girl.isPassenger()
                && girl.getTarget() != null && girl.getTarget().isAlive();
    }
}
