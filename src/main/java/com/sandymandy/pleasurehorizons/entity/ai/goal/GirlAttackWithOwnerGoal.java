package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Girl attacks whatever her owner is attacking.
 *
 * <p>Yarn to Mojang: {@code TrackTargetGoal} → {@link TargetGoal}, {@code canStart} → {@code canUse},
 * {@code setControls} → {@code setFlags}, {@code owner.getAttacking()} → {@code owner.getLastHurtMob()},
 * {@code owner.getLastAttackTime()} → {@code owner.getLastHurtMobTimestamp()},
 * {@code canTrack} → {@code canAttack}, {@code TargetPredicate.DEFAULT} →
 * {@link TargetingConditions#DEFAULT}.</p>
 */
public class GirlAttackWithOwnerGoal extends TargetGoal {
    private final TameableGirlEntity girl;
    @Nullable
    private LivingEntity attacking;
    private int lastAttackTime;
    private final Class<?>[] doNotTarget;

    public GirlAttackWithOwnerGoal(TameableGirlEntity girl, Class<?>... doNotTarget) {
        super(girl, false);
        this.girl = girl;
        this.doNotTarget = doNotTarget;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!girl.isTamed() || girl.isSitting() || girl.isDowned()
                || girl.isSceneActive() || girl.isPassenger()) {
            return false;
        }

        LivingEntity owner = girl.getOwner();
        if (owner == null) {
            return false;
        }

        this.attacking = owner.getLastHurtMob();
        int timestamp = owner.getLastHurtMobTimestamp();

        if (timestamp == this.lastAttackTime || this.attacking == null) {
            return false;
        }

        for (Class<?> clazz : this.doNotTarget) {
            if (clazz.isAssignableFrom(this.attacking.getClass())) {
                return false;
            }
        }

        // Never turn on her own owner.
        if (this.attacking == owner) {
            return false;
        }

        // Only join REAL fights. The owner's last-hurt target can be anything: a fish
        // hit with a sword, a turtle, a villager bopped on the head. Following that made
        // the girls chase and attack passive underwater mobs. Restrict to natural
        // hostiles: Enemy covers every Monster plus Phantom and friends.
        if (!(this.attacking instanceof net.minecraft.world.entity.monster.Enemy)) {
            return false;
        }

        return this.canAttack(this.attacking, TargetingConditions.DEFAULT);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacking);
        LivingEntity owner = girl.getOwner();
        if (owner != null) {
            this.lastAttackTime = owner.getLastHurtMobTimestamp();
        }
        super.start();
    }
}
