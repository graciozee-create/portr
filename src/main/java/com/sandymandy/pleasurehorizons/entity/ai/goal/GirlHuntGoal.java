package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.goat.Goat;

import java.util.EnumSet;
import java.util.List;

/**
 * Hunts passive livestock while {@code isHuntEnabled()} is on.
 *
 * <p>This goal only marks a target; the actual chase and damage come from the existing combat
 * goal (melee or bow, depending on the girl). The kill drops are then collected by
 * {@code GirlGatherItemsGoal} and cooked by {@code GirlCookGoal}, closing the
 * hunt -&gt; gather -&gt; cook food chain.</p>
 *
 * <p>Only a fixed set of adult livestock is hunted - never babies, tamables, villagers or other
 * girls - and an escaped animal (further than 40 blocks) is given up so the girl does not chase
 * it across the world.</p>
 */
public class GirlHuntGoal extends Goal {
    private static final double SCAN_RANGE = 24.0D;
    private static final double SCAN_HEIGHT = 6.0D;
    private static final double GIVE_UP_DISTANCE_SQ = 40.0D * 40.0D;

    private final TameableGirlEntity girl;
    private LivingEntity target;
    private int scanCooldown = 0;

    public GirlHuntGoal(TameableGirlEntity girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!girl.isHuntEnabled()) return false;
        if (girl.isSitting() || girl.isFollowing() || girl.isSceneActive()
                || girl.isDowned() || girl.isPassenger()) {
            return false;
        }
        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }

        // Already fighting something (a hostile, for example) - leave it alone.
        LivingEntity current = girl.getTarget();
        if (current != null && current.isAlive()) return false;

        target = findLivestock();
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return girl.isHuntEnabled()
                && target != null && target.isAlive()
                && girl.getTarget() == target
                && girl.distanceToSqr(target) <= GIVE_UP_DISTANCE_SQ
                && !girl.isSitting() && !girl.isFollowing()
                && !girl.isSceneActive() && !girl.isDowned() && !girl.isPassenger();
    }

    private LivingEntity findLivestock() {
        List<Animal> animals = girl.level().getEntitiesOfClass(
                Animal.class,
                girl.getBoundingBox().inflate(SCAN_RANGE, SCAN_HEIGHT, SCAN_RANGE),
                this::isHuntable);
        return animals.isEmpty() ? null : animals.get(0);
    }

    private boolean isHuntable(Animal animal) {
        if (!animal.isAlive() || animal.isBaby()) return false;
        // MushroomCow extends Cow, so it is covered by the Cow check.
        return animal instanceof Cow
                || animal instanceof Pig
                || animal instanceof Sheep
                || animal instanceof Chicken
                || animal instanceof Rabbit
                || animal instanceof Goat;
    }

    @Override
    public void start() {
        this.scanCooldown = 0;
        girl.setTarget(target);
    }

    @Override
    public void tick() {
        // Reacquire a lost target on the next scan; the combat goal does the actual chasing.
        if (target != null && !target.isAlive()) {
            stop();
        }
    }

    @Override
    public void stop() {
        // Only clear the target if it is still the one we set - never a hostile picked by a
        // higher-priority target goal while we were hunting.
        if (girl.getTarget() == target) {
            girl.setTarget(null);
        }
        target = null;
        scanCooldown = 60;
    }
}
