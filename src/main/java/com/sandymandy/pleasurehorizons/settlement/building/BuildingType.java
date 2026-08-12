package com.sandymandy.pleasurehorizons.settlement.building;

import com.mojang.serialization.Codec;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

import java.util.Map;

public enum BuildingType {
    HOUSE, STORAGE, BLACKSMITH, FARM, BROTHEL, NONE;

    public Map<Object, Integer> getRequirements() {
        return switch (this) {
            case HOUSE -> Map.of(BlockTags.BEDS, 2);
            case BLACKSMITH -> Map.of(Blocks.ANVIL, 1, Blocks.BLAST_FURNACE, 1);
            case STORAGE -> Map.of(Blocks.CHEST, 4);
            default -> Map.of();
        };
    }

    public static final Codec<BuildingType> CODEC = Codec.STRING.xmap(
            name -> {
                try {
                    return BuildingType.valueOf(name.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return BuildingType.NONE;
                }
            },
            type -> type.name().toLowerCase()
    );

    public String getTranslationKey() {
        return "building.pleasurehorizons." + name().toLowerCase();
    }
}
