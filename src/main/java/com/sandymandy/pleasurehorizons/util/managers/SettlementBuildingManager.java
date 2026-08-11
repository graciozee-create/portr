package com.sandymandy.pleasurehorizons.util.managers;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.settlement.building.SettlementBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

public class SettlementBuildingManager {
    private final Map<UUID, List<SettlementBuilding>> buildings = new HashMap<>();
    private static final Map<ServerLevel, SettlementBuildingManager> INSTANCES = new WeakHashMap<>();

    public static SettlementBuildingManager get(ServerLevel level) {
        return INSTANCES.computeIfAbsent(level, l -> new SettlementBuildingManager());
    }

    public void addBuilding(UUID settlementId, SettlementBuilding building) {
        buildings.computeIfAbsent(settlementId, k -> new ArrayList<>()).add(building);
    }

    public List<SettlementBuilding> getBuildings(UUID settlementId) {
        return buildings.getOrDefault(settlementId, List.of());
    }

    public void markDirty() {}
}
