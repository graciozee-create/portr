package com.sandymandy.pleasurehorizons.client.gui.screen.settlement;

import com.sandymandy.pleasurehorizons.screen.SettlementHubScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SettlementHubScreen extends AbstractContainerScreen<SettlementHubScreenHandler> {
    public SettlementHubScreen(SettlementHubScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    }
}
