package com.sandymandy.pleasurehorizons.util.managers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.settlement.Settlement;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SettlementManager extends PersistentState {

    private final Map<UUID, Settlement> settlements = new HashMap<>();

    // __Codec__
    public static final Codec<SettlementManager> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Settlement.CODEC)
                    .fieldOf("settlements")
                    .forGetter(manager -> new ArrayList<>(manager.settlements.values()))
    ).apply(instance, list -> {
        SettlementManager manager = new SettlementManager();
        for (Settlement s : list) {
            s.setManager(manager);
            manager.settlements.put(s.getId(), s);
        }
        return manager;
    }));

    // __PersistentStateType__
    public static final PersistentStateType<SettlementManager> TYPE = new PersistentStateType<>(
            "pleasurecraft_settlements",
            SettlementManager::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    // __Core Methods__
    public static SettlementManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public Settlement createSettlement(BlockPos pos, String name, UUID owner) {
        Settlement settlement = new Settlement(UUID.randomUUID(), owner, name, pos);
        settlements.put(settlement.getId(), settlement);
        markDirty();
        PleasureHorizons.LOGGER.info("Settlement Created: " + settlement.getId() + " | Total: " + settlements.size());
        return settlement;
    }

    public Settlement getSettlement(UUID id) {
        Settlement s = settlements.get(id);
        if (s == null) {
            PleasureHorizons.LOGGER.error("Failed to find settlement: " + id + " | Manager has: " + settlements.keySet());
        }
        return s;
    }

    @Nullable
    public Settlement getSettlementWithGirl(UUID girlId) {
        for (Settlement settlement : settlements.values()) {
            if (settlement.hasMember(girlId)) {
                return settlement;
            }
        }
        return null;
    }

    public void removeSettlement(UUID id) {
        settlements.remove(id);
        markDirty();
    }

    public List<Settlement> getAllSettlements() {
        return List.copyOf(settlements.values());
    }
}
