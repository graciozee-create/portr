package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.config.GirlsConfig;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

/**
 * Heals a tamed girl from food she carries in her own inventory.
 *
 * <p>Deliberately declares no {@link Flag}s: eating is instant, so healing must never preempt
 * movement, combat or the look goals. The goal runs alongside everything else and only gates on
 * her own state, her health threshold and a cooldown.</p>
 */
public class GirlSelfHealGoal extends Goal {
    private final TameableGirlEntity girl;
    private int cooldown = 0;

    public GirlSelfHealGoal(TameableGirlEntity girl) {
        this.girl = girl;
        // No flags on purpose.
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (!GirlsConfig.selfHealEnabled()) return false;
        if (girl.isDowned() || girl.isSceneActive() || girl.isPassenger()) return false;
        if (girl.getHealth() >= girl.getMaxHealth() * GirlsConfig.selfHealBelowPercent()) return false;
        return findFoodSlot() >= 0;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        int slot = findFoodSlot();
        if (slot < 0) return;
        ItemStack food = girl.getInventory().removeItem(slot, 1);
        if (food.isEmpty()) return;
        FoodProperties properties = food.get(DataComponents.FOOD);
        if (properties == null) {
            // Lost its food component between the check and the take; put it back.
            girl.getInventory().setItem(slot, food);
            return;
        }
        // Same healing rate as hand-feeding (mobInteract): 2 HP per nutrition point.
        girl.heal(2.0F * properties.nutrition());
        girl.playSound(SoundEvents.GENERIC_EAT, 0.8F, 1.0F);
        cooldown = Math.max(1, GirlsConfig.selfHealIntervalTicks());
    }

    private int findFoodSlot() {
        var inv = girl.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.get(DataComponents.FOOD) != null) {
                return i;
            }
        }
        return -1;
    }
}
