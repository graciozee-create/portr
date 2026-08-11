package com.sandymandy.pleasurehorizons.util.managers;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.settlement.Settlement;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SettlementManager {
    private final Map<UUID, Settlement> settlements = new HashMap<>();
    private static final Map<ServerLevel, SettlementManager> INSTANCES = new WeakHashMap<>();

    public static SettlementManager get(ServerLevel level) {
        return INSTANCES.computeIfAbsent(level, l -> new SettlementManager());
    }

    public Settlement createSettlement(BlockPos pos, String name, UUID owner) {
        Settlement settlement = new Settlement(UUID.randomUUID(), owner, name, pos);
        settlements.put(settlement.getId(), settlement);
        PleasureHorizons.LOGGER.info("Settlement Created: " + settlement.getId());
        return settlement;
    }

    public Settlement getSettlement(UUID id) {
        return settlements.get(id);
    }

    @Nullable
    public Settlement getSettlementWithGirl(UUID girlId) {
        for (Settlement s : settlements.values()) {
            if (s.getMembers().contains(girlId)) return s;
        }
        return null;
    }

    public void removeSettlement(UUID id) {
        settlements.remove(id);
    }

    public List<Settlement> getAllSettlements() {
        return List.copyOf(settlements.values());
    }

    public void markDirty() {}
}
