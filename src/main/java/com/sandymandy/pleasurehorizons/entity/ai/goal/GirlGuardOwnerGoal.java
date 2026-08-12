package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

/**
 * Advanced AI: Guard owner within radius - attacks monsters near owner.
 * Controlled via AI toggle button.
 */
public class GirlGuardOwnerGoal extends TargetGoal {
    private final TameableGirlEntity girl;

    public GirlGuardOwnerGoal(TameableGirlEntity girl) {
        super(girl, false);
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!girl.isGuardOwnerEnabled()) return false;
        if (girl.isSitting() || girl.isSceneActive() || girl.isDowned()) return false;
        if (girl.isPassenger()) return false;

        LivingEntity owner = girl.getOwner();
        if (owner == null) return false;

        AABB box = new AABB(owner.blockPosition()).inflate(12.0D, 6.0D, 12.0D);
        Monster found = girl.level().getNearestEntity(Monster.class,
                TargetingConditions.forCombat().range(12.0D), girl,
                owner.getX(), owner.getY(), owner.getZ(), box);
        if (found != null && found.isAlive()) {
            girl.setTarget(found);
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return girl.isGuardOwnerEnabled()
                && !girl.isSitting() && !girl.isSceneActive() && !girl.isDowned() && !girl.isPassenger()
                && girl.getTarget() != null && girl.getTarget().isAlive()
                && girl.getOwner() != null && girl.distanceToSqr(girl.getOwner()) < 400; // within 20 blocks of owner
    }
}
