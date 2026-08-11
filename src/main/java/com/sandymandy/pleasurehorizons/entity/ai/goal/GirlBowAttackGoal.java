package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;

import java.util.EnumSet;

public class GirlBowAttackGoal extends Goal {
    private final SettlementGirlEntityAI girl;
    private final double moveSpeed;
    private final double minRangeSq;
    private final double maxRangeSq;

    private int shootCooldown = 0;

    /**
     * @param girl the entity
     * @param speed movement speed
     * @param minRange minimum distance to keep from target in defensive mode
     * @param maxRange maximum distance to keep from target in defensive mode
     * @param cooldownTicks bow cooldown
     */
    public GirlBowAttackGoal(SettlementGirlEntityAI girl, double speed, float minRange, float maxRange, int cooldownTicks) {
        this.girl = girl;
        this.moveSpeed = speed;
        this.minRangeSq = minRange * minRange;
        this.maxRangeSq = maxRange * maxRange;
        this.shootCooldown = cooldownTicks;

        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    private boolean hasBow() {
        return girl.isHolding(Items.BOW);
    }

    @Override
    public boolean canStart() {
        return girl.getTarget() != null && hasBow();
    }

    @Override
    public boolean shouldContinue() {
        return girl.getTarget() != null && hasBow();
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void start() {
        girl.setAttacking(true);
    }

    @Override
    public void stop() {
        girl.setAttacking(false);
        girl.clearActiveItem();
        girl.setSprinting(false);
    }

    private void lowHpMovement(double distSq, LivingEntity target){
        // Low HP → defensive: stay between minRange and maxRange
        girl.setSprinting(false);
        if (distSq < minRangeSq) {
            // Too close → back off
            girl.getNavigation().startMovingTo(
                    girl.getX() - (target.getX() - girl.getX()),
                    girl.getY(),
                    girl.getZ() - (target.getZ() - girl.getZ()),
                    moveSpeed
            );
        } else if (distSq > maxRangeSq) {
            // Too far → approach
            girl.getNavigation().startMovingTo(target, moveSpeed);
        } else {
            // Safe zone → stop
            girl.getNavigation().stop();
        }
    }

    @Override
    public void tick() {
        LivingEntity target = girl.getTarget();
        if (target == null) return;

        double distSq = girl.squaredDistanceTo(target);
        double healthRatio = girl.getHealth() / girl.getMaxHealth();

        // === AIM ===
        girl.getLookControl().lookAt(target.getX(), target.getEyeY(), target.getZ(), 35f, 35f);

        // === MOVEMENT ===
        if(this.girl.getMainHandStack().isEmpty()){
            lowHpMovement(distSq, target);
        }
        else {
            if (healthRatio > 0.5) {
                // High HP → aggressive: get close to
                girl.setSprinting(true);
                girl.getNavigation().startMovingTo(target, moveSpeed);
            } else {
                lowHpMovement(distSq, target);
            }
        }

        // === SHOOTING ===
        if (girl.isUsingItem()) {
            int useTime = girl.getItemUseTime();

            if (useTime >= 20) {
                float pull = BowItem.getPullProgress(useTime);
                girl.shootAt(target, pull);
                girl.clearActiveItem();
                shootCooldown = 20;
            }
        } else if (shootCooldown <= 0) {
            // Begin drawing bow
            girl.setCurrentHand(ProjectileUtil.getHandPossiblyHolding(girl, Items.BOW));
        }

        if (shootCooldown > 0) shootCooldown--;
    }
}
