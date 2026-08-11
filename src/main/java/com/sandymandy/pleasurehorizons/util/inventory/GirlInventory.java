package com.sandymandy.pleasurehorizons.util.inventory;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

public interface GirlInventory extends Container {
    int TOTAL_SLOTS = 17;
    int MAIN_HAND_SLOT = 0;
    int OFF_HAND_SLOT = 1;
    int ARMOR_END = 4;
    int BACKPACK_START = 5;
    static GirlInventory ofSize() { return null; }
}
