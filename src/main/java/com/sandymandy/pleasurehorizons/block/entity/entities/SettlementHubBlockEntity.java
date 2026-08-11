package com.sandymandy.pleasurehorizons.block.entity.entities;

import com.sandymandy.pleasurehorizons.block.entity.PleasureHorizonsBlockEntities;
import com.sandymandy.pleasurehorizons.settlement.Settlement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SettlementHubBlockEntity extends BlockEntity {
    private Settlement settlement;
    private UUID settlementId;

    public SettlementHubBlockEntity(BlockPos pos, BlockState state) {
        super(PleasureHorizonsBlockEntities.SETTLEMENT_HUB_BLOCK_ENTITY.get(), pos, state);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SettlementHubBlockEntity be) {
        if (level.isClientSide) return;
        if (be.settlement != null) {
            // settlement.tick(level);
        }
    }

    public Settlement getSettlement() {
        return settlement;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (settlementId != null) {
            tag.putUUID("SettlementId", settlementId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("SettlementId")) {
            settlementId = tag.getUUID("SettlementId");
        }
    }
}
