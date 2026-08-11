package com.sandymandy.pleasurehorizons.util.managers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.settlement.building.SettlementBuilding;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.*;

public class SettlementBuildingManager extends PersistentState {
    private final Map<BlockPos, SettlementBuilding> buildings = new HashMap<>();

    // __Codec__
    public static final Codec<SettlementBuildingManager> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(SettlementBuilding.CODEC)
                    .fieldOf("buildings")
                    .forGetter(manager -> new ArrayList<>(manager.buildings.values()))
    ).apply(instance, list -> {
        SettlementBuildingManager manager = new SettlementBuildingManager();
        for (SettlementBuilding b : list) manager.buildings.put(b.getDoorPos(), b);
        return manager;
    }));

    // __PersistentStateType__
    public static final PersistentStateType<SettlementBuildingManager> TYPE = new PersistentStateType<>(
            PleasureHorizons.MOD_ID + "_settlements_buildings",
            SettlementBuildingManager::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    // __Core Methods__
    public static SettlementBuildingManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public void registerBuildings(SettlementBuilding building) {
        buildings.put(building.getDoorPos(), building);
        markDirty();
    }

    public SettlementBuilding getBuilding(BlockPos id) {
        SettlementBuilding b = buildings.get(id);
        if (b == null) {
            PleasureHorizons.LOGGER.error("Failed to find building: " + id + " | Manager has: " + buildings.keySet());
        }
        return b;
    }

    public void removeBuilding(BlockPos id) {
        buildings.remove(id);
        markDirty();
    }

    public SettlementBuilding getBuildingAt(BlockPos pos) {
        // If no buildings exist in this world, exit immediately
        if (this.buildings.isEmpty()) return null;

        for (SettlementBuilding building : this.buildings.values()) {
            // Fast Bounding Box Check (Integer comparisons only)
            if (building.getBoundingBox().contains(pos.getX(), pos.getY(), pos.getZ())) {
                return building;
            }
        }
        return null;
    }

    public Map<BlockPos, SettlementBuilding> getAllBuildings() {
        return buildings;
    }

}
