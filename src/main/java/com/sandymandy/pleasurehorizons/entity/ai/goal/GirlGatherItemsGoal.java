package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.util.inventory.GirlInventory;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.List;

public class GirlGatherItemsGoal extends Goal {
    private final TameableGirlEntity girl;
    private ItemEntity targetItem;

    public GirlGatherItemsGoal(TameableGirlEntity girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!girl.isGatherEnabled()) return false;
        if (girl.isSitting() || girl.isSceneActive() || girl.isDowned() || girl.isPassenger()) {
            return false;
        }
        List<ItemEntity> items = girl.level().getEntitiesOfClass(ItemEntity.class, girl.getBoundingBox().inflate(8.0D, 3.0D, 8.0D),
                item -> item.isAlive() && !item.hasPickUpDelay() && !item.getItem().isEmpty());
        if (items.isEmpty()) {
            return false;
        }
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
        if (targetItem != null) {
            girl.getNavigation().moveTo(targetItem, 1.1D);
        }
    }

    @Override
    public void stop() {
        targetItem = null;
        girl.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (targetItem != null && targetItem.isAlive()) {
            girl.getLookControl().setLookAt(targetItem, 30.0F, 30.0F);
            if (girl.distanceToSqr(targetItem) < 3.0D) {
                ItemStack stack = targetItem.getItem();
                GirlInventory inv = girl.getInventory();
                for (int i = GirlInventory.BACKPACK_START; i <= GirlInventory.BACKPACK_END; i++) {
                    ItemStack inSlot = inv.getItem(i);
                    if (inSlot.isEmpty()) {
                        inv.setItem(i, stack.copy());
                        targetItem.discard();
                        break;
                    } else if (ItemStack.isSameItemSameComponents(inSlot, stack) && inSlot.getCount() < inSlot.getMaxStackSize()) {
                        int space = inSlot.getMaxStackSize() - inSlot.getCount();
                        int toAdd = Math.min(space, stack.getCount());
                        inSlot.grow(toAdd);
                        stack.shrink(toAdd);
                        if (stack.isEmpty()) {
                            targetItem.discard();
                            break;
                        }
                    }
                }
            } else if (girl.getNavigation().isDone()) {
                girl.getNavigation().moveTo(targetItem, 1.1D);
            }
        }
    }
}
