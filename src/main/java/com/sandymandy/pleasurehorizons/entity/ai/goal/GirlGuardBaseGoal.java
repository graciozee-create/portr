package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

public class GirlGuardBaseGoal extends TargetGoal {
    private final TameableGirlEntity girl;

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
        BlockPos center = girl.getBasePos();
        if (center.equals(BlockPos.ZERO)) {
            center = girl.blockPosition();
        }
        AABB box = new AABB(center).inflate(16.0D, 6.0D, 16.0D);
        java.util.List<Monster> candidates = girl.level().getEntitiesOfClass(Monster.class, box,
                monster -> monster.isAlive() && !girl.isAvoidCreepersEnabled(monster));
        Monster found = girl.level().getNearestEntity(candidates,
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
