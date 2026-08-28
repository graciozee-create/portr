package com.sandymandy.pleasurehorizons.util.inventory.slot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ExclusiveSlot extends TexturedSlot {
    private final Item canNotInsertItem;

    public ExclusiveSlot(Container inventory, int index, int x, int y, ResourceLocation backgroundSprite, Item canNotInsertItem) {
        super(inventory, index, x, y, backgroundSprite);
        this.canNotInsertItem = canNotInsertItem;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.is(this.canNotInsertItem);
    }
}
