package com.sandymandy.pleasurehorizons.settlement.building;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Blocks;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.tag.BlockTags;

import java.util.Map;

public enum BuildingType {
    HOUSE, STORAGE, BLACKSMITH, FARM, BROTHEL, NONE;

    // Returns a map of Tag/Block requirements and the minimum count needed
    public Map<Object, Integer> getRequirements() {
        return switch (this) {
            case HOUSE -> Map.of(BlockTags.BEDS, 2); // Needs at least 2 bed (any type)
            case BLACKSMITH -> Map.of(Blocks.ANVIL, 1, Blocks.BLAST_FURNACE, 1);
            case STORAGE -> Map.of(Blocks.CHEST, 4);
            default -> Map.of();
        };
    }

    public static final PacketCodec<ByteBuf, BuildingType> PACKET_CODEC = PacketCodecs.indexed(
            i -> BuildingType.values()[i],  // Decode: int ordinal -> enum
            BuildingType::ordinal           // Encode: enum -> int ordinal
    );

    public static final Codec<BuildingType> CODEC = Codec.STRING.xmap(
            name -> {
                try {
                    return BuildingType.valueOf(name.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return BuildingType.NONE; // fallback if unknown
                }
            },
            type -> type.name().toLowerCase()
    );

    public String getTranslationKey() {
        return "building.pleasurecraft." + name().toLowerCase();
    }
}
