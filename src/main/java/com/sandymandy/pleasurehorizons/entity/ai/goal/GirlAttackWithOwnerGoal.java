package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;

import java.util.EnumSet;

public class GirlAttackWithOwnerGoal extends TrackTargetGoal {
    private final TameableGirlEntity tameable;
    private LivingEntity attacking;
    private int lastAttackTime;
    private final Class<?>[] doNotTarget;

    public GirlAttackWithOwnerGoal(TameableGirlEntity tameable, Class<?>... doNotTarget) {
        super(tameable, false);
        this.tameable = tameable;
        this.doNotTarget = doNotTarget;
        this.setControls(EnumSet.of(Goal.Control.TARGET));
    }

    @Override
    public boolean canStart() {
        if (this.tameable.isTamed() && !this.tameable.isSitting()) {
            LivingEntity owner = this.tameable.getOwner();
            if (owner == null) {
                return false;
            }

            this.attacking = owner.getAttacking();
            int i = owner.getLastAttackTime();

            if (i != this.lastAttackTime && this.attacking != null) {
                for (Class<?> clazz : this.doNotTarget) {
                    if (clazz.isAssignableFrom(this.attacking.getClass())) {
                        return false;
                    }
                }

                return this.canTrack(this.attacking, TargetPredicate.DEFAULT)
                        && this.tameable.canAttackWithOwner(this.attacking, owner);
            }
        }

        return false;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacking);
        LivingEntity owner = this.tameable.getOwner();
        if (owner != null) {
            this.lastAttackTime = owner.getLastAttackTime();
        }
        super.start();
    }
}
