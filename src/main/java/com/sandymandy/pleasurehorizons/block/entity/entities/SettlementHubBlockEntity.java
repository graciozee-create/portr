package com.sandymandy.pleasurehorizons.block.entity.entities;

import com.sandymandy.pleasurehorizons.block.entity.PleasureHorizonsBlockEntities;
import com.sandymandy.pleasurehorizons.screen.SettlementHubScreenHandlerFactory;
import com.sandymandy.pleasurehorizons.settlement.Settlement;
import com.sandymandy.pleasurehorizons.util.managers.SettlementManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Backing block entity for the settlement hub.
 *
 * <p>Ported from Fabric: {@code writeData/readData(WriteView/ReadView)} became
 * {@code saveAdditional/loadAdditional(CompoundTag, HolderLookup.Provider)}, and
 * {@code player.openHandledScreen} became {@link ServerPlayer#openMenu}. The extra buffer writer is
 * required because the client menu needs a {@code SettlementSnapshot} - see
 * {@link SettlementHubScreenHandlerFactory}.</p>
 */
public class SettlementHubBlockEntity extends BlockEntity {
    @Nullable
    private Settlement settlement;
    @Nullable
    private UUID settlementId;

    public SettlementHubBlockEntity(BlockPos pos, BlockState state) {
        super(PleasureHorizonsBlockEntities.SETTLEMENT_HUB_BLOCK_ENTITY.get(), pos, state);
    }

    /* === Networking & Sync === */

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    /* === GUI === */

    public void openGui(ServerLevel level, ServerPlayer player) {
        Settlement settlement = getSettlement();
        if (settlement == null) {
            // Hub placed before this feature existed, or its settlement was wiped - recreate it.
            initializeWithOwner(level, player.getUUID());
            settlement = getSettlement();
        }
        if (settlement == null) {
            return;
        }

        Player owner = level.getPlayerByUUID(settlement.getOwner());
        if (owner == null || player.getUUID().equals(settlement.getOwner())) {
            SettlementHubScreenHandlerFactory factory = new SettlementHubScreenHandlerFactory(settlement);
            player.openMenu(factory, factory::writeScreenOpeningData);
        } else {
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.settlement.not_owner"), true);
        }
    }

    /* === Tick === */

    public static void tick(Level level, BlockPos pos, BlockState state, SettlementHubBlockEntity be) {
        if (level.isClientSide) return;
        // Resolve lazily; the manager is only populated once the level is running.
        be.getSettlement();
    }

    /* === Setup === */

    public void initializeWithOwner(ServerLevel level, UUID ownerId) {
        SettlementManager manager = SettlementManager.get(level);

        if (this.settlement == null && this.settlementId == null) {
            Player owner = level.getPlayerByUUID(ownerId);
            String name = owner != null
                    ? owner.getGameProfile().getName() + "'s Settlement"
                    : "Settlement@" + getBlockPos().toShortString();

            this.settlement = manager.createSettlement(getBlockPos(), name, ownerId);
            this.settlementId = this.settlement.getId();
            setChanged();
        }
    }

    /* === Persistence === */

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (settlement != null) {
            tag.putUUID("SettlementId", settlement.getId());
        } else if (settlementId != null) {
            tag.putUUID("SettlementId", settlementId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("SettlementId")) {
            settlementId = tag.getUUID("SettlementId");
            settlement = null;
        }
    }

    /* === Lazy getter === */

    @Nullable
    public Settlement getSettlement() {
        if (this.settlement != null) return this.settlement;

        if (this.level instanceof ServerLevel serverLevel && this.settlementId != null) {
            this.settlement = SettlementManager.get(serverLevel).getSettlement(this.settlementId);
        }
        return this.settlement;
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level instanceof ServerLevel serverLevel && this.settlementId != null) {
            this.settlement = SettlementManager.get(serverLevel).getSettlement(this.settlementId);
        }
    }
}
