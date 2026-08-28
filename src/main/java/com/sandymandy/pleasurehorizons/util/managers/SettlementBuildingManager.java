package com.sandymandy.pleasurehorizons.util.managers;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.settlement.building.SettlementBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/** Persistent index of scanned buildings in one dimension. */
public class SettlementBuildingManager extends SavedData {
    private static final String DATA_NAME = PleasureHorizons.MOD_ID + "_settlement_buildings";
    private static final String BUILDINGS_TAG = "buildings";
    private static final SavedData.Factory<SettlementBuildingManager> FACTORY =
            new SavedData.Factory<>(SettlementBuildingManager::new, SettlementBuildingManager::load);

    private final Map<BlockPos, SettlementBuilding> buildings = new LinkedHashMap<>();

    public static SettlementBuildingManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    private static SettlementBuildingManager load(CompoundTag tag, HolderLookup.Provider registries) {
        SettlementBuildingManager manager = new SettlementBuildingManager();
        if (!tag.contains(BUILDINGS_TAG, Tag.TAG_LIST)) {
            return manager;
        }

        SettlementBuilding.CODEC.listOf().parse(NbtOps.INSTANCE, tag.get(BUILDINGS_TAG))
                .resultOrPartial(error -> PleasureHorizons.LOGGER.error("Could not load settlement buildings: {}", error))
                .ifPresent(loaded -> loaded.forEach(
                        building -> manager.buildings.put(building.doorPos(), building)));
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        SettlementBuilding.CODEC.listOf().encodeStart(NbtOps.INSTANCE, new ArrayList<>(buildings.values()))
                .resultOrPartial(error -> PleasureHorizons.LOGGER.error("Could not save settlement buildings: {}", error))
                .ifPresent(encoded -> tag.put(BUILDINGS_TAG, encoded));
        return tag;
    }

    public void addBuilding(SettlementBuilding building) {
        buildings.put(building.doorPos(), building);
        setDirty();
    }

    public void removeBuilding(BlockPos doorPos) {
        if (buildings.remove(doorPos) != null) {
            setDirty();
        }
    }

    @Nullable
    public SettlementBuilding getBuilding(BlockPos doorPos) {
        return buildings.get(doorPos);
    }

    @Nullable
    public SettlementBuilding getBuildingAt(BlockPos pos) {
        for (SettlementBuilding building : buildings.values()) {
            if (building.getBoundingBox().contains(pos.getX(), pos.getY(), pos.getZ())) {
                return building;
            }
        }
        return null;
    }

    public Map<BlockPos, SettlementBuilding> getAllBuildings() {
        return Map.copyOf(buildings);
    }
}
