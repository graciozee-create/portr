package com.sandymandy.pleasurehorizons.screen;

import com.sandymandy.pleasurehorizons.settlement.Settlement;
import com.sandymandy.pleasurehorizons.settlement.SettlementSnapshot;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
        return Component.translatable("screen.pleasurehorizons.settlement_hub");
    }

    /**
     * NeoForge equivalent of Fabric's {@code getScreenOpeningData} - the client menu constructor
     * reads exactly this back out of the buffer.
     */
    public void writeScreenOpeningData(RegistryFriendlyByteBuf buf) {
        SettlementSnapshot.STREAM_CODEC.encode(buf, SettlementSnapshot.of(data));
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new SettlementHubScreenHandler(syncId, playerInventory, data);
    }
}
