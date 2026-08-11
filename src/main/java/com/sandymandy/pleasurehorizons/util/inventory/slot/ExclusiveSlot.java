package com.sandymandy.pleasurehorizons.util.inventory.slot;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ExclusiveSlot extends Slot {
    public ExclusiveSlot(Container inventory, int index, int x, int y, ResourceLocation bg, Item item) {
        super(inventory, index, x, y);
    }
    public ExclusiveSlot(Container inventory, int index, int x, int y, ResourceLocation bg) {
        super(inventory, index, x, y);
    }
}
