package com.sandymandy.pleasurehorizons.util.inventory.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public class ExclusiveSlot extends TexturedSlot {
    private final Item canNotInsertItem;

    public ExclusiveSlot(Inventory inventory, int index, int x, int y, Identifier backgroundSpite, Item canNotInsertItem) {
        super(inventory, index, x, y, backgroundSpite);
        this.canNotInsertItem = canNotInsertItem;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return !stack.isOf(this.canNotInsertItem);
    }
}
