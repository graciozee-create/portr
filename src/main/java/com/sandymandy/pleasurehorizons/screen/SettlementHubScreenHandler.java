package com.sandymandy.pleasurehorizons.screen;

import com.sandymandy.pleasurehorizons.block.entity.entities.SettlementHubBlockEntity;
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
        double x = snapshot.corePos().getX() + 0.5D;
        double y = snapshot.corePos().getY() + 0.5D;
        double z = snapshot.corePos().getZ() + 0.5D;
        if (player.distanceToSqr(x, y, z) > 64.0D
                || !(player.level().getBlockEntity(snapshot.corePos()) instanceof SettlementHubBlockEntity hub)) {
            return false;
        }
        if (player.level().isClientSide()) {
            return true;
        }

        Settlement live = hub.getSettlement();
        return settlement != null
                && live != null
                && live.getId().equals(settlement.getId())
                && live.getOwner().equals(player.getUUID());
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
