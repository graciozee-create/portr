package com.sandymandy.pleasurehorizons.block.entity.entities;

import com.sandymandy.pleasurehorizons.block.entity.PleasureHorizonsBlockEntities;
import com.sandymandy.pleasurehorizons.settlement.building.BuildingType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AbstractBuildingTagBlockEntity extends BlockEntity {
    private BuildingType buildingType;

    public AbstractBuildingTagBlockEntity(BlockPos pos, BlockState state, BuildingType type) {
        super(PleasureHorizonsBlockEntities.BUILDING_TAG_BLOCK_ENTITY.get(), pos, state);
        this.buildingType = type;
    }

    public BuildingType getBuildingType() {
        return buildingType;
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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (buildingType != null) {
            tag.putString("BuildingType", buildingType.name());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("BuildingType")) {
            try {
                buildingType = BuildingType.valueOf(tag.getString("BuildingType"));
            } catch (Exception ignored) {}
        }
    }
}
