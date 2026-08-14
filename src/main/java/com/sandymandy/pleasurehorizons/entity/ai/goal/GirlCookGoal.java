package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.util.inventory.GirlInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.EnumSet;

/**
 * Cooks raw food in a furnace near the girl while {@code isCookEnabled()} is on.
 *
 * <p>The pipeline: take smeltable raw food from her inventory into the furnace input slot, add
 * fuel, then carry the cooked result back to the backpack (or let {@code GirlFeedOwnerGoal} hand
 * it to a hungry owner). Works alongside harvest: both goals are independent, so a girl can grow
 * crops and cook at the same time when their toggles are on.</p>
 */
public class GirlCookGoal extends Goal {
    private static final int SEARCH_RADIUS = 12;
    private static final int SEARCH_HEIGHT = 4;
    private static final double USE_DISTANCE_SQ = 16.0D;

    private final TameableGirlEntity girl;
    private BlockPos furnacePos;
    private int cooldown = 0;
    private int repath = 0;

    public GirlCookGoal(TameableGirlEntity girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!girl.isCookEnabled()) return false;
        if (forbidden()) return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        AbstractFurnaceBlockEntity furnace = findFurnace();
        if (furnace == null) {
            cooldown = 60;
            return false;
        }
        furnacePos = furnace.getBlockPos();
        if (!hasWork(furnace)) {
            cooldown = 30; // furnace is already busy cooking - recheck shortly
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!girl.isCookEnabled() || forbidden()) return false;
        AbstractFurnaceBlockEntity furnace = furnaceAt(furnacePos);
        return furnace != null && hasWork(furnace);
    }

    private boolean forbidden() {
        return girl.isSitting() || girl.isFollowing() || girl.isSceneActive() || girl.isDowned() || girl.isPassenger();
    }

    private AbstractFurnaceBlockEntity findFurnace() {
        BlockPos center = girl.blockPosition();
        BlockPos from = center.offset(-SEARCH_RADIUS, -SEARCH_HEIGHT, -SEARCH_RADIUS);
        BlockPos to = center.offset(SEARCH_RADIUS, SEARCH_HEIGHT, SEARCH_RADIUS);
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            BlockEntity entity = girl.level().getBlockEntity(pos);
            if (entity instanceof AbstractFurnaceBlockEntity furnace) {
                return furnace;
            }
        }
        return null;
    }

    private AbstractFurnaceBlockEntity furnaceAt(BlockPos pos) {
        if (pos == null) return null;
        BlockEntity entity = girl.level().getBlockEntity(pos);
        return entity instanceof AbstractFurnaceBlockEntity furnace ? furnace : null;
    }

    private boolean hasWork(AbstractFurnaceBlockEntity furnace) {
        if (!furnace.getItem(2).isEmpty()) return true; // cooked output ready
        if (furnace.getItem(0).isEmpty()) return hasSmeltableFood(); // load raw food
        if (furnace.getItem(1).isEmpty()) return hasFuel(); // load fuel
        return false; // already has food + fuel, nothing to do right now
    }

    private boolean hasSmeltableFood() {
        return findSmeltableFoodSlot() >= 0;
    }

    private int findSmeltableFoodSlot() {
        GirlInventory inv = girl.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (isArmorSlot(i)) continue;
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty() || stack.get(DataComponents.FOOD) == null) continue;
            if (girl.level().getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), girl.level())
                    .isPresent()) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasFuel() {
        return findFuelSlot() >= 0;
    }

    private int findFuelSlot() {
        GirlInventory inv = girl.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (isArmorSlot(i)) continue;
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty() || stack.isDamageableItem()) continue;
            if (stack.get(DataComponents.FOOD) != null) continue; // never burn food as fuel
            if (AbstractFurnaceBlockEntity.isFuel(stack)) return i;
        }
        return -1;
    }

    private static boolean isArmorSlot(int slot) {
        return slot >= GirlInventory.ARMOR_START && slot <= GirlInventory.ARMOR_END;
    }

    @Override
    public void start() {
        this.repath = 0;
    }

    @Override
    public void tick() {
        AbstractFurnaceBlockEntity furnace = furnaceAt(furnacePos);
        if (furnace == null) return;

        double x = furnacePos.getX() + 0.5D;
        double y = furnacePos.getY();
        double z = furnacePos.getZ() + 0.5D;
        girl.getLookControl().setLookAt(x, y, z, 30.0F, 30.0F);

        if (girl.distanceToSqr(x, y, z) > USE_DISTANCE_SQ) {
            if (--repath <= 0) {
                repath = this.adjustedTickDelay(10);
                girl.getNavigation().moveTo(x, y, z, 1.0D);
            }
            return;
        }

        girl.getNavigation().stop();

        if (!furnace.getItem(2).isEmpty()) {
            collectOutput(furnace);
        } else if (furnace.getItem(0).isEmpty()) {
            if (!loadRawFood(furnace)) cooldown = 60;
        } else if (furnace.getItem(1).isEmpty()) {
            if (!loadFuel(furnace)) cooldown = 60;
        }
    }

    private boolean loadRawFood(AbstractFurnaceBlockEntity furnace) {
        int slot = findSmeltableFoodSlot();
        if (slot < 0) return false;
        GirlInventory inv = girl.getInventory();
        ItemStack moved = inv.removeItem(slot, inv.getItem(slot).getCount());
        if (moved.isEmpty()) return false;
        furnace.setItem(0, moved);
        return true;
    }

    private boolean loadFuel(AbstractFurnaceBlockEntity furnace) {
        int slot = findFuelSlot();
        if (slot < 0) return false;
        GirlInventory inv = girl.getInventory();
        // Commit a small batch so she does not walk back and forth per single item.
        int amount = Math.min(8, inv.getItem(slot).getCount());
        ItemStack fuel = inv.removeItem(slot, amount);
        if (fuel.isEmpty()) return false;
        furnace.setItem(1, fuel);
        return true;
    }

    private void collectOutput(AbstractFurnaceBlockEntity furnace) {
        ItemStack cooked = furnace.getItem(2).copy();
        furnace.setItem(2, ItemStack.EMPTY);
        if (cooked.isEmpty()) return;

        GirlInventory inv = girl.getInventory();
        for (int i = GirlInventory.BACKPACK_START; i <= GirlInventory.BACKPACK_END; i++) {
            ItemStack slot = inv.getItem(i);
            if (slot.isEmpty()) {
                inv.setItem(i, cooked);
                return;
            }
            if (ItemStack.isSameItemSameComponents(slot, cooked) && slot.getCount() < slot.getMaxStackSize()) {
                int space = slot.getMaxStackSize() - slot.getCount();
                int add = Math.min(space, cooked.getCount());
                slot.grow(add);
                cooked.shrink(add);
                if (cooked.isEmpty()) return;
            }
        }
        // Backpack full: drop at the furnace so the cooked food is not lost.
        girl.spawnAtLocation(cooked);
    }

    @Override
    public void stop() {
        furnacePos = null;
        girl.getNavigation().stop();
        cooldown = 20;
    }
}
