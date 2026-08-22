package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;

import java.util.EnumSet;

/**
 * Bow combat: aggressive while healthy, kiting once below half health.
 *
 * <p>Yarn to Mojang: {@code canStart} → {@code canUse}, {@code shouldContinue} → {@code canContinueToUse},
 * {@code shouldRunEveryTick} → {@code requiresUpdateEveryTick}, {@code setAttacking} → {@code setAggressive},
 * {@code clearActiveItem} → {@code stopUsingItem}, {@code setCurrentHand} → {@code startUsingItem},
 * {@code getItemUseTime} → {@code getTicksUsingItem}, {@code isUsingItem} → {@code isUsingItem},
 * {@code shootAt} → {@code performRangedAttack}, {@code BowItem.getPullProgress} →
 * {@code BowItem.getPowerForTime}, {@code ProjectileUtil.getHandPossiblyHolding} →
 * {@code ProjectileUtil.getWeaponHoldingHand}, {@code startMovingTo} → {@code moveTo},
 * {@code lookAt(x,y,z,yawStep,pitchStep)} → {@code setLookAt(...)}.</p>
 */
public class GirlBowAttackGoal extends Goal {
    private final SettlementGirlEntityAI girl;
    private final double moveSpeed;
    private final double minRangeSq;
    private final double maxRangeSq;

    private int shootCooldown;

    public GirlBowAttackGoal(SettlementGirlEntityAI girl, double speed,
                             float minRange, float maxRange, int cooldownTicks) {
        this.girl = girl;
        this.moveSpeed = speed;
        this.minRangeSq = minRange * minRange;
        this.maxRangeSq = maxRange * maxRange;
        this.shootCooldown = cooldownTicks;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean hasBow() {
        return girl.isHolding(Items.BOW);
    }

    private boolean isBusy() {
        return girl.isSceneActive() || girl.isDowned() || girl.isPassenger() || girl.isSitting();
    }

    @Override
    public boolean canUse() {
        return girl.getTarget() != null && hasBow() && !isBusy();
    }

    @Override
    public boolean canContinueToUse() {
        return girl.getTarget() != null && hasBow() && !isBusy();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        girl.setAggressive(true);
    }

    @Override
    public void stop() {
        girl.setAggressive(false);
        girl.stopUsingItem();
        girl.setSprinting(false);
        girl.getNavigation().stop();
    }

    /** Keeps her in the comfortable band between min and max range. */
    private void kite(double distSq, LivingEntity target) {
        girl.setSprinting(false);
        if (distSq < minRangeSq) {
            girl.getNavigation().moveTo(
                    girl.getX() - (target.getX() - girl.getX()),
                    girl.getY(),
                    girl.getZ() - (target.getZ() - girl.getZ()),
                    moveSpeed);
        } else if (distSq > maxRangeSq) {
            girl.getNavigation().moveTo(target, moveSpeed);
        } else {
            girl.getNavigation().stop();
        }
    }

    @Override
    public void tick() {
        LivingEntity target = girl.getTarget();
        if (target == null) return;

        double distSq = girl.distanceToSqr(target);
        double healthRatio = girl.getHealth() / girl.getMaxHealth();

        girl.getLookControl().setLookAt(target.getX(), target.getEyeY(), target.getZ(), 35f, 35f);

        if (healthRatio > 0.5 && !girl.getMainHandItem().isEmpty()) {
            girl.setSprinting(true);
            girl.getNavigation().moveTo(target, moveSpeed);
        } else {
            kite(distSq, target);
        }

        if (girl.isUsingItem()) {
            int useTime = girl.getTicksUsingItem();
            if (useTime >= 20) {
                girl.performRangedAttack(target, BowItem.getPowerForTime(useTime));
                girl.stopUsingItem();
                shootCooldown = 20;
            }
        } else if (shootCooldown <= 0) {
            girl.startUsingItem(ProjectileUtil.getWeaponHoldingHand(girl, item -> item instanceof BowItem));
        }

        if (shootCooldown > 0) shootCooldown--;
    }
}
