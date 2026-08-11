package com.sandymandy.pleasurehorizons.util.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Simple {@link Container} used for a girl's inventory.
 *
 * <p>Slot layout (kept identical to the Fabric original):</p>
 * <pre>
 * 0      - main hand
 * 1..4   - armour (feet, legs, chest, head)
 * 5..16  - backpack
 * 17     - off hand
 * </pre>
 */
public interface GirlInventory extends Container {
    int MAIN_HAND_SLOT = 0;

    int ARMOR_FEET_SLOT = 1;
    int ARMOR_LEGS_SLOT = 2;
    int ARMOR_CHEST_SLOT = 3;
    int ARMOR_HEAD_SLOT = 4;
    int ARMOR_START = ARMOR_FEET_SLOT;
    int ARMOR_END = ARMOR_HEAD_SLOT;

    int BACKPACK_START = 5;
    int BACKPACK_END = 16;

    int OFF_HAND_SLOT = BACKPACK_END + 1;
    int TOTAL_SLOTS = OFF_HAND_SLOT + 1;

    /** Must always return the same backing list instance. */
    NonNullList<ItemStack> getItems();

    static GirlInventory of(NonNullList<ItemStack> items) {
        return () -> items;
    }

    static GirlInventory ofSize() {
        return of(NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY));
    }

    @Override
    default int getContainerSize() {
        return getItems().size();
    }

    @Override
    default boolean isEmpty() {
        for (ItemStack stack : getItems()) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    default ItemStack getItem(int slot) {
        if (slot < 0 || slot >= getItems().size()) {
            return ItemStack.EMPTY;
        }
        return getItems().get(slot);
    }

    @Override
    default ItemStack removeItem(int slot, int amount) {
        // Written out by hand rather than via ContainerHelper so the behaviour is explicit.
        NonNullList<ItemStack> items = getItems();
        if (slot < 0 || slot >= items.size() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack existing = items.get(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack split = existing.split(amount);
        if (existing.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        }
        if (!split.isEmpty()) {
            setChanged();
        }
        return split;
    }

    @Override
    default ItemStack removeItemNoUpdate(int slot) {
        NonNullList<ItemStack> items = getItems();
        if (slot < 0 || slot >= items.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack existing = items.get(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        items.set(slot, ItemStack.EMPTY);
        return existing;
    }

    @Override
    default void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= getItems().size()) {
            return;
        }
        getItems().set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    default void setChanged() {}

    @Override
    default boolean stillValid(Player player) {
        return true;
    }

    @Override
    default void clearContent() {
        getItems().clear();
        setChanged();
    }
}
