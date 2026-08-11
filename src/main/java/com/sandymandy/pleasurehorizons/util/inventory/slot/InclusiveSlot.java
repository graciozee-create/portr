package com.sandymandy.pleasurehorizons.util.inventory.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public class InclusiveSlot extends TexturedSlot{
    private final Item insertItem;

    public InclusiveSlot(Inventory inventory, int index, int x, int y, ResourceLocation backgroundSpite, Item insertItem) {
        super(inventory, index, x, y, backgroundSpite);
        this.insertItem = insertItem;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return stack.isOf(this.insertItem);
    }
}
