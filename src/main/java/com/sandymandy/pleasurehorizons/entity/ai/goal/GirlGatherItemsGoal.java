package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.util.inventory.GirlInventory;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class GirlGatherItemsGoal extends Goal {
    private static final int GIVE_UP_TICKS = 600;
    private static final int GIVE_UP_COOLDOWN = 200;

    private final TameableGirlEntity girl;
    private ItemEntity targetItem;
    private int elapsed = 0;
    private int cooldown = 0;
    private boolean gaveUp = false;

    public GirlGatherItemsGoal(TameableGirlEntity girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (!girl.isGatherEnabled()) return false;
        if (girl.isSitting() || girl.isSceneActive() || girl.isDowned() || girl.isPassenger()) {
            return false;
        }
        double range = 8.0D * girl.workRadiusScale();
        List<ItemEntity> items = girl.level().getEntitiesOfClass(ItemEntity.class,
                girl.getBoundingBox().inflate(range, 3.0D, range),
                item -> item.isAlive() && !item.hasPickUpDelay() && !item.getItem().isEmpty()
                        && canFit(item.getItem()));
        if (items.isEmpty()) {
            return false;
        }
        // Nearest first - without this she could stomp past an item at her feet towards one
        // that was merely found first in the entity list.
        items.sort(Comparator.comparingDouble(girl::distanceToSqr));
        targetItem = items.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return girl.isGatherEnabled() && targetItem != null && targetItem.isAlive() && !targetItem.getItem().isEmpty()
                && !girl.isSitting() && !girl.isSceneActive() && !girl.isDowned() && !girl.isPassenger();
    }

    @Override
    public void start() {
        this.elapsed = 0;
        if (targetItem != null) {
            girl.getNavigation().moveTo(targetItem, girl.workSpeedModifier());
        }
    }

    @Override
    public void stop() {
        if (gaveUp) {
            cooldown = GIVE_UP_COOLDOWN;
            gaveUp = false;
        }
        targetItem = null;
        girl.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (targetItem == null) return;

        if (++elapsed > GIVE_UP_TICKS) {
            // Item unreachable (fell into water or a pit) - stop chasing it.
            gaveUp = true;
            targetItem = null;
            return;
        }

        girl.getLookControl().setLookAt(targetItem, 30.0F, 30.0F);
        if (girl.distanceToSqr(targetItem) < 3.0D) {
            if (!pickup(targetItem)) {
                // No room after all (someone filled the backpack in between) - bail out so
                // she does not stand on the item forever.
                targetItem = null;
            }
        } else if (girl.getNavigation().isDone()) {
            girl.getNavigation().moveTo(targetItem, girl.workSpeedModifier());
        }
    }

    /** Tries to store the item entity's stack in the backpack; true when it was fully stored. */
    private boolean pickup(ItemEntity targetItem) {
        ItemStack stack = targetItem.getItem();
        GirlInventory inv = girl.getInventory();
        for (int i = GirlInventory.BACKPACK_START; i <= GirlInventory.BACKPACK_END; i++) {
            ItemStack inSlot = inv.getItem(i);
            if (inSlot.isEmpty()) {
                inv.setItem(i, stack.copy());
                targetItem.discard();
                return true;
            } else if (ItemStack.isSameItemSameComponents(inSlot, stack) && inSlot.getCount() < inSlot.getMaxStackSize()) {
                int space = inSlot.getMaxStackSize() - inSlot.getCount();
                int toAdd = Math.min(space, stack.getCount());
                inSlot.grow(toAdd);
                stack.shrink(toAdd);
                if (stack.isEmpty()) {
                    targetItem.discard();
                    return true;
                }
            }
        }
        return stack.isEmpty();
    }

    /** True when the stack would fit into the backpack (empty slot or stackable space). */
    private boolean canFit(ItemStack stack) {
        GirlInventory inv = girl.getInventory();
        for (int i = GirlInventory.BACKPACK_START; i <= GirlInventory.BACKPACK_END; i++) {
            ItemStack inSlot = inv.getItem(i);
            if (inSlot.isEmpty()) {
                return true;
            }
            if (ItemStack.isSameItemSameComponents(inSlot, stack) && inSlot.getCount() < inSlot.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }
}
