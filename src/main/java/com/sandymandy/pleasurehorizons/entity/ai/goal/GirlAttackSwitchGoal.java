package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Items;

import java.util.EnumSet;

/**
 * Picks between melee and bow depending on weapon, range and remaining health.
 *
 * <p>Wraps the two combat goals rather than registering both, so only one of them owns
 * movement at a time. A wounded archer keeps her distance instead of charging in.</p>
 */
public class GirlAttackSwitchGoal extends Goal {

    private final SettlementGirlEntityAI girl;
    private final GirlMeleeAttackGoal meleeGoal;
    private final GirlBowAttackGoal bowGoal;
    private final double switchDistanceSq;

    private Goal activeGoal = null;

    public GirlAttackSwitchGoal(SettlementGirlEntityAI girl, double speed, float switchDistance,
                                float minBowRange, float maxBowRange) {
        this.girl = girl;
        this.meleeGoal = new GirlMeleeAttackGoal(girl, speed, false);
        this.bowGoal = new GirlBowAttackGoal(girl, speed, minBowRange, maxBowRange, 5);
        this.switchDistanceSq = switchDistance * switchDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean hasBow() {
        return girl.isHolding(Items.BOW);
    }

    @Override
    public boolean canUse() {
        return girl.getTarget() != null
                && !girl.isSceneActive() && !girl.isDowned()
                && !girl.isPassenger() && !girl.isSitting();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void swapTo(Goal newGoal) {
        if (activeGoal == newGoal) return;
        if (activeGoal != null) activeGoal.stop();
        activeGoal = newGoal;
        activeGoal.start();
    }

    @Override
    public void stop() {
        if (activeGoal != null) {
            activeGoal.stop();
            activeGoal = null;
        }
    }

    @Override
    public void tick() {
        LivingEntity target = girl.getTarget();
        if (target == null) {
            stop();
            return;
        }

        double distSq = girl.distanceToSqr(target);
        double healthRatio = girl.getHealth() / girl.getMaxHealth();

        if (hasBow()) {
            boolean healthyAndClose = healthRatio > 0.5 && distSq <= switchDistanceSq;
            swapTo(healthyAndClose ? meleeGoal : bowGoal);
        } else {
            swapTo(meleeGoal);
        }

        if (activeGoal != null) activeGoal.tick();
    }
}
