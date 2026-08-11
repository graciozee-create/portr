package com.sandymandy.pleasurehorizons.screen;

import com.sandymandy.pleasurehorizons.settlement.Settlement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.sandymandy.pleasurehorizons.registries.PleasureHorizonsScreenHandlerRegistry.SETTLEMENT_HUB_SCREEN_HANDLER;

public class SettlementHubScreenHandler extends AbstractContainerMenu {
    private final Settlement settlement;

    public SettlementHubScreenHandler(int syncId, Inventory playerInventory, Settlement data) {
        super(SETTLEMENT_HUB_SCREEN_HANDLER, syncId);
        this.settlement = data;
    }

    @Override
    public ItemStack quickMove(Player player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }

    public Settlement getSettlement() {
        return settlement;
    }
}
