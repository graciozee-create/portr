package com.sandymandy.pleasurehorizons.util.managers;

import com.sandymandy.pleasurehorizons.settlement.building.SettlementBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Per-level store of scanned buildings.
 *
 * <p>Indexed twice on purpose: by settlement (for counting and iteration) and by door position
 * (so a re-scan can find and replace an existing building without walking every settlement).</p>
 */
public class SettlementBuildingManager {
    private final Map<UUID, List<SettlementBuilding>> bySettlement = new HashMap<>();
    private final Map<BlockPos, SettlementBuilding> byDoor = new LinkedHashMap<>();
    private final Map<BlockPos, UUID> doorToSettlement = new HashMap<>();

    private static final Map<ServerLevel, SettlementBuildingManager> INSTANCES = new WeakHashMap<>();

    public static SettlementBuildingManager get(ServerLevel level) {
        return INSTANCES.computeIfAbsent(level, l -> new SettlementBuildingManager());
    }

    public void addBuilding(UUID settlementId, SettlementBuilding building) {
        bySettlement.computeIfAbsent(settlementId, k -> new ArrayList<>()).add(building);
        byDoor.put(building.doorPos(), building);
        doorToSettlement.put(building.doorPos(), settlementId);
    }

    public void removeBuilding(BlockPos doorPos) {
        SettlementBuilding building = byDoor.remove(doorPos);
        UUID settlementId = doorToSettlement.remove(doorPos);
        if (building != null && settlementId != null) {
            List<SettlementBuilding> list = bySettlement.get(settlementId);
            if (list != null) {
                list.remove(building);
            }
        }
    }

    @Nullable
    public SettlementBuilding getBuilding(BlockPos doorPos) {
        return byDoor.get(doorPos);
    }

    public List<SettlementBuilding> getBuildings(UUID settlementId) {
        return bySettlement.getOrDefault(settlementId, List.of());
    }

    public Map<BlockPos, SettlementBuilding> getAllBuildings() {
        return byDoor;
    }

    public void markDirty() {
        // Buildings are rebuilt by re-scanning; nothing persisted yet.
    }
}
