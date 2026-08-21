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
                if (friendlyInLineOfFire(target)) {
                    // A sister or the owner is between her and the target - hold fire and
                    // reposition instead of putting an arrow into a squad mate's back.
                    girl.stopUsingItem();
                    shootCooldown = 15;
                } else {
                    girl.performRangedAttack(target, BowItem.getPowerForTime(useTime));
                    girl.stopUsingItem();
                    shootCooldown = 20;
                }
            }
        } else if (shootCooldown <= 0) {
            girl.startUsingItem(ProjectileUtil.getWeaponHoldingHand(girl, item -> item instanceof BowItem));
        }

        if (shootCooldown > 0) shootCooldown--;
    }

    /**
     * True when another tamed girl of any owner or her own owner stands in the corridor between
     * her eyes and the target. Girl-vs-girl arrows already deal no damage, but holding fire
     * keeps the squad from blocking each other's shots constantly.
     */
    private boolean friendlyInLineOfFire(LivingEntity target) {
        net.minecraft.world.phys.Vec3 from = girl.getEyePosition();
        net.minecraft.world.phys.Vec3 to = target.getEyePosition();
        net.minecraft.world.phys.Vec3 dir = to.subtract(from);
        double length = dir.length();
        if (length < 1.0E-4D) {
            return false;
        }
        dir = dir.normalize();

        net.minecraft.world.phys.AABB corridor = girl.getBoundingBox().expandTowards(
                target.getX() - girl.getX(),
                target.getY() - girl.getY(),
                target.getZ() - girl.getZ()).inflate(1.0D);

        for (net.minecraft.world.entity.Entity entity : girl.level().getEntities(girl, corridor)) {
            if (entity == target || !entity.isAlive()) continue;
            boolean isSister = entity instanceof com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
            boolean isOwner = girl.isOwner(entity instanceof LivingEntity living ? living : null);
            if (!isSister && !isOwner) continue;

            net.minecraft.world.phys.Vec3 rel = entity.getBoundingBox().getCenter().subtract(from);
            double along = rel.dot(dir);
            if (along <= 0.5D || along >= length - 0.5D) continue;
            double sideways = rel.subtract(dir.scale(along)).length();
            if (sideways < entity.getBbWidth() * 0.6D + 0.3D) {
                return true;
            }
        }
        return false;
    }
}
