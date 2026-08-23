package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.util.inventory.GirlInventory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

/**
 * Feeds the owner from her inventory while {@code isFeedOwnerEnabled()} is on and the owner is
 * hungry. Uses {@code FoodData#eat(FoodProperties)} directly, which applies nutrition and
 * saturation without the interactive right-click path the server cannot perform for a player.
 */
public class GirlFeedOwnerGoal extends Goal {
    private static final int FEED_INTERVAL_TICKS = 40; // one item every two seconds, not 20/sec
    private static final int GIVE_UP_TICKS = 600;
    private static final int GIVE_UP_COOLDOWN = 200;

    private final TameableGirlEntity girl;
    private Player owner;
    private int cooldown = 0;
    private int timeToRecalcPath = 0;
    private int feedCooldown = 0;
    private int elapsed = 0;
    private boolean gaveUp = false;

    public GirlFeedOwnerGoal(TameableGirlEntity girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!girl.isFeedOwnerEnabled()) return false;
        if (girl.isSitting() || girl.isSceneActive() || girl.isDowned() || girl.isPassenger()) {
            return false;
        }
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        LivingEntity living = girl.getOwner();
        if (!(living instanceof Player player) || player.isSpectator() || !player.isAlive()) {
            return false;
        }
        if (!player.getFoodData().needsFood()) return false;
        if (findFoodSlot() < 0) {
            cooldown = 60;
            return false;
        }
        this.owner = player;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!girl.isFeedOwnerEnabled() || owner == null || !owner.isAlive()) return false;
        if (girl.isSitting() || girl.isSceneActive() || girl.isDowned() || girl.isPassenger()) {
            return false;
        }
        return owner.getFoodData().needsFood() && findFoodSlot() >= 0;
    }

    /**
     * Only foods WITHOUT any effects qualify. Effect-carrying foods are either harmful
     * (rotten flesh, pufferfish, spider eye, poisonous potato) or far too precious to be
     * force-fed away (golden apples, enchanted golden apple, chorus fruit, honey bottle).
     */
    private int findFoodSlot() {
        GirlInventory inv = girl.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            FoodProperties properties = stack.get(DataComponents.FOOD);
            if (properties != null && properties.effects().isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.feedCooldown = 0;
        this.elapsed = 0;
        girl.getNavigation().moveTo(owner, girl.workSpeedModifier());
    }

    @Override
    public void tick() {
        girl.getLookControl().setLookAt(owner, 30.0F, girl.getMaxHeadXRot());

        if (++elapsed > GIVE_UP_TICKS) {
            // Owner unreachable (flying, across water) - stop chasing and retry later.
            gaveUp = true;
            owner = null;
            return;
        }

        if (girl.distanceToSqr(owner) > 9.0D) {
            if (--timeToRecalcPath <= 0) {
                timeToRecalcPath = this.adjustedTickDelay(10);
                girl.getNavigation().moveTo(owner, girl.workSpeedModifier());
            }
            return;
        }

        // Pace the feeding: one item per interval. Without this she stuffed the whole
        // backpack into the player within a second (one item per tick).
        if (feedCooldown > 0) {
            feedCooldown--;
            return;
        }

        int slot = findFoodSlot();
        if (slot < 0) return;

        ItemStack food = girl.getInventory().removeItem(slot, 1);
        if (food.isEmpty()) return;

        FoodProperties properties = food.get(DataComponents.FOOD);
        if (properties == null) {
            // Item lost its food component between the check and the take; put it back.
            girl.getInventory().setItem(slot, food);
            return;
        }

        owner.getFoodData().eat(properties);
        girl.playSound(SoundEvents.GENERIC_EAT, 0.8F, 1.0F);
        feedCooldown = this.adjustedTickDelay(FEED_INTERVAL_TICKS);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.timeToRecalcPath = 0;
        girl.getNavigation().stop();
        // stop() must not clobber the give-up cooldown set in tick().
        cooldown = gaveUp ? GIVE_UP_COOLDOWN : 20;
        gaveUp = false;
    }
}
