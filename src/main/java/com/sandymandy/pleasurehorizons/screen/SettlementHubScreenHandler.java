package com.sandymandy.pleasurehorizons.screen;

import com.sandymandy.pleasurehorizons.registries.PleasureHorizonsScreenHandlerRegistry;
import com.sandymandy.pleasurehorizons.settlement.Settlement;
import com.sandymandy.pleasurehorizons.settlement.SettlementSnapshot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Menu behind the settlement hub block.
 *
 * <p>The server side keeps the live {@link Settlement}; the client only ever receives a
 * {@link SettlementSnapshot} written into the opening buffer, because the mutable settlement
 * object cannot cross the network.</p>
 */
public class SettlementHubScreenHandler extends AbstractContainerMenu {
    @Nullable
    private final Settlement settlement;
    private final SettlementSnapshot snapshot;

    public SettlementHubScreenHandler(int syncId, Inventory playerInventory, @Nullable Settlement data) {
        super(PleasureHorizonsScreenHandlerRegistry.SETTLEMENT_HUB_HOLDER.get(), syncId);
        this.settlement = data;
        this.snapshot = SettlementSnapshot.of(data);
    }

    public SettlementHubScreenHandler(int syncId, Inventory playerInventory, SettlementSnapshot snapshot) {
        super(PleasureHorizonsScreenHandlerRegistry.SETTLEMENT_HUB_HOLDER.get(), syncId);
        this.settlement = null;
        this.snapshot = snapshot == null ? SettlementSnapshot.EMPTY : snapshot;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Nullable
    public Settlement getSettlement() {
        return settlement;
    }

    /** Always populated - use this from the client screen. */
    public SettlementSnapshot getSnapshot() {
        return snapshot;
    }
}
