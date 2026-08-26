package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Girl retaliates against whoever hurt her owner.
 *
 * <p>Yarn to Mojang: {@code owner.getAttacker()} → {@code owner.getLastHurtByMob()},
 * {@code owner.getLastAttackedTime()} → {@code owner.getLastHurtByMobTimestamp()}.</p>
 */
public class GirlTrackOwnerAttackerGoal extends TargetGoal {
    private final TameableGirlEntity girl;
    @Nullable
    private LivingEntity attacker;
    private int lastAttackedTime;

    public GirlTrackOwnerAttackerGoal(TameableGirlEntity girl) {
        super(girl, false);
        this.girl = girl;
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

        this.attacker = owner.getLastHurtByMob();
        int timestamp = owner.getLastHurtByMobTimestamp();

        if (timestamp == this.lastAttackedTime || this.attacker == null) {
            return false;
        }

        if (this.attacker == owner || this.attacker == girl) {
            return false;
        }

        // A sister who accidentally clipped the owner in a heated fight is never a target.
        if (this.attacker instanceof TameableGirlEntity) {
            return false;
        }

        return this.canAttack(this.attacker, TargetingConditions.DEFAULT);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacker);
        LivingEntity owner = girl.getOwner();
        if (owner != null) {
            this.lastAttackedTime = owner.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
