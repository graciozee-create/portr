package com.sandymandy.pleasurehorizons.screen;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class GirlInventoryScreenHandler extends AbstractContainerMenu {
    public GirlInventoryScreenHandler(int syncId, Inventory playerInventory, int girlId) {
        super(null, syncId);
    }

    public GirlInventoryScreenHandler(int syncId, Inventory playerInventory, Object data) {
        super(null, syncId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public Object getGirl() { return null; }
}
