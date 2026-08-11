package com.sandymandy.pleasurehorizons.screen;

import com.sandymandy.pleasurehorizons.settlement.Settlement;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class SettlementHubScreenHandlerFactory implements MenuProvider {
    private final Settlement data;

    public SettlementHubScreenHandlerFactory(Settlement data) {
        this.data = data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("screen.pleasurecraft.settlement_hub");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new SettlementHubScreenHandler(syncId, playerInventory, data);
    }
}
