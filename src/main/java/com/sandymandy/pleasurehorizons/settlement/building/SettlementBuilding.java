package com.sandymandy.pleasurehorizons.settlement.building;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sandymandy.pleasurehorizons.util.variables.BlockEntry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;


public record SettlementBuilding(BlockPos doorPos, BlockPos tagPos, BuildingType buildingType, List<BlockEntry> structureBlocks) {



    public static final Codec<SettlementBuilding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("doorPos").forGetter(SettlementBuilding::getDoorPos),
            BlockPos.CODEC.fieldOf("tagPos").forGetter(SettlementBuilding::getTagPos),
            BuildingType.CODEC.fieldOf("buildingType").forGetter(SettlementBuilding::getBuildingType),
            BlockEntry.CODEC.listOf().fieldOf("structureBlocks").forGetter(SettlementBuilding::getStructureBlocks)
    ).apply(instance, SettlementBuilding::new));

    public static final PacketCodec<RegistryByteBuf, SettlementBuilding> PACKET_CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SettlementBuilding::getDoorPos,
            BlockPos.PACKET_CODEC, SettlementBuilding::getTagPos,
            BuildingType.PACKET_CODEC, SettlementBuilding::getBuildingType,
            PacketCodecs.collection(ArrayList::new, BlockEntry.PACKET_CODEC), SettlementBuilding::structureBlocks,
            SettlementBuilding::new
            );

    public SettlementBuilding(BlockPos doorPos, BlockPos tagPos, BuildingType buildingType, List<BlockEntry> structureBlocks) {
        this.doorPos = doorPos;
        this.tagPos = tagPos;
        this.buildingType = buildingType;
        this.structureBlocks = structureBlocks;
    }

    public Box getBoundingBox() {
        if (structureBlocks.isEmpty()) return new Box(doorPos);

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (BlockEntry entry : structureBlocks) {
            BlockPos pos = entry.pos();
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        // Expand by 1 to include the actual block volume
        return new Box(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
    }

    public BlockPos getDoorPos() { return doorPos; }
    public BlockPos getTagPos() { return tagPos; }
    public BuildingType getBuildingType() { return buildingType; }
    public List<BlockEntry> getStructureBlocks() { return structureBlocks; }
    public boolean contains(BlockPos pos) {
        for(BlockEntry entry : structureBlocks) {
            if(!entry.pos().equals(pos)) continue;
            return true;
        }
        return false;
    }
}
