package com.sandymandy.pleasurehorizons.screen;

import com.sandymandy.pleasurehorizons.settlement.Settlement;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

import org.jetbrains.annotations.Nullable;

public class SettlementHubScreenHandlerFactory implements net.minecraft.world.inventory.AbstractContainerMenuProvider {
    private final Settlement data;

    public SettlementHubScreenHandlerFactory(Settlement data) {
        this.data = data;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("screen.pleasurecraft.settlement_hub");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new SettlementHubScreenHandler(syncId, playerInventory, data);
    }
}
