package com.sandymandy.pleasurehorizons.screen;

import com.sandymandy.pleasurehorizons.settlement.Settlement;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class SettlementHubScreenHandler extends AbstractContainerMenu {
    private final Settlement settlement;

    public SettlementHubScreenHandler(int syncId, Inventory playerInventory, Settlement data) {
        super(null, syncId);
        this.settlement = data;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public Settlement getSettlement() {
        return settlement;
    }
}
