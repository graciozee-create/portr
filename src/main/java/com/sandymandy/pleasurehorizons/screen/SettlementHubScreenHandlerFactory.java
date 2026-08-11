package com.sandymandy.pleasurehorizons.screen;

import com.sandymandy.pleasurehorizons.settlement.Settlement;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

public class SettlementHubScreenHandlerFactory implements net.minecraft.world.inventory.AbstractContainerMenuProvider {
    private final Settlement data;

    public SettlementHubScreenHandlerFactory(Settlement data) {
        this.data = data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("screen.pleasurecraft.settlement_hub");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new SettlementHubScreenHandler(syncId, playerInventory, data);
    }
}
