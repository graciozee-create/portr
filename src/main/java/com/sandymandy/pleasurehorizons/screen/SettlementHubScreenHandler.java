package com.sandymandy.pleasurehorizons.screen;

import com.sandymandy.pleasurehorizons.settlement.Settlement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

import static com.sandymandy.pleasurehorizons.registries.PleasureHorizonsScreenHandlerRegistry.SETTLEMENT_HUB_SCREEN_HANDLER;

public class SettlementHubScreenHandler extends ScreenHandler {
    private final Settlement settlement;

    public SettlementHubScreenHandler(int syncId, PlayerInventory playerInventory, Settlement data) {
        super(SETTLEMENT_HUB_SCREEN_HANDLER, syncId);
        this.settlement = data;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public Settlement getSettlement() {
        return settlement;
    }
}
