package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.Items;

import java.util.EnumSet;

public class GirlAttackSwitchGoal extends Goal {

    private final SettlementGirlEntityAI girl;
    private final GirlMeleeAttackGoal meleeGoal;
    private final GirlBowAttackGoal bowGoal;

    private final double switchDistanceSq;

    private Goal activeGoal = null;

    public GirlAttackSwitchGoal(SettlementGirlEntityAI girl, double speed, float switchDistance, float minBowRange, float maxBowRange) {
        this.girl = girl;

        this.meleeGoal = new GirlMeleeAttackGoal(girl, speed, false);
        this.bowGoal = new GirlBowAttackGoal(girl, speed, minBowRange, maxBowRange, 5);

        this.switchDistanceSq = switchDistance * switchDistance;

        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    private boolean hasBow() {
        return girl.isHolding(Items.BOW);
    }

    @Override
    public boolean canStart() {
        return girl.getTarget() != null;
    }

    @Override
    public boolean shouldContinue() {
        return girl.getTarget() != null;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    private void swapTo(Goal newGoal) {
        if (activeGoal == newGoal) return;

        // stop old
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

        double distSq = girl.squaredDistanceTo(target);
        boolean canUseBow = hasBow();
        boolean mainHandEmpty = this.girl.getMainHandStack().isEmpty();
        double healthRatio = girl.getHealth() / girl.getMaxHealth();

        // Decide what to use:
        if (canUseBow) {
            if(mainHandEmpty){
                swapTo(bowGoal);
            }
            else {
                if (healthRatio > 0.5) {
                    // Aggressive → still switch to melee if in range
                    if (distSq > switchDistanceSq) {
                        swapTo(bowGoal);
                    } else {
                        swapTo(meleeGoal);
                    }
                } else {
                    // Defensive → never switch to melee
                    swapTo(bowGoal);
                }
            }
        } else {
            // No bow → always melee
            swapTo(meleeGoal);
        }

        // Let the active goal handle the real attacking
        if (activeGoal != null) activeGoal.tick();
    }

}
