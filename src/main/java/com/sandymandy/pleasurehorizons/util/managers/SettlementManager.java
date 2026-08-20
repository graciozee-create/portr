package com.sandymandy.pleasurehorizons.util.managers;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.settlement.Settlement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Per-dimension persistent settlement store. */
public class SettlementManager extends SavedData {
    private static final String DATA_NAME = PleasureHorizons.MOD_ID + "_settlements";
    private static final String SETTLEMENTS_TAG = "settlements";
    private static final SavedData.Factory<SettlementManager> FACTORY =
            new SavedData.Factory<>(SettlementManager::new, SettlementManager::load);

    private final Map<UUID, Settlement> settlements = new HashMap<>();

    public static SettlementManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    private static SettlementManager load(CompoundTag tag, HolderLookup.Provider registries) {
        SettlementManager manager = new SettlementManager();
        if (!tag.contains(SETTLEMENTS_TAG, Tag.TAG_LIST)) {
            return manager;
        }

        Settlement.CODEC.listOf().parse(NbtOps.INSTANCE, tag.get(SETTLEMENTS_TAG))
                .resultOrPartial(error -> PleasureHorizons.LOGGER.error("Could not load settlements: {}", error))
                .ifPresent(loaded -> loaded.forEach(manager::putLoaded));
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        Settlement.CODEC.listOf().encodeStart(NbtOps.INSTANCE, new ArrayList<>(settlements.values()))
                .resultOrPartial(error -> PleasureHorizons.LOGGER.error("Could not save settlements: {}", error))
                .ifPresent(encoded -> tag.put(SETTLEMENTS_TAG, encoded));
        return tag;
    }

    private void putLoaded(Settlement settlement) {
        settlement.attachManager(this);
        settlements.put(settlement.getId(), settlement);
    }

    public Settlement createSettlement(BlockPos pos, String name, UUID owner) {
        Settlement settlement = new Settlement(UUID.randomUUID(), owner, name, pos.immutable());
        putLoaded(settlement);
        setDirty();
        PleasureHorizons.LOGGER.info("Settlement created: {}", settlement.getId());
        return settlement;
    }

    @Nullable
    public Settlement getSettlement(UUID id) {
        return id == null ? null : settlements.get(id);
    }

    @Nullable
    public Settlement getSettlementWithGirl(UUID girlId) {
        for (Settlement settlement : settlements.values()) {
            if (settlement.hasMember(girlId)) return settlement;
        }
        return null;
    }

    public void removeSettlement(UUID id) {
        if (settlements.remove(id) != null) {
            setDirty();
        }
    }

    public List<Settlement> getAllSettlements() {
        return List.copyOf(settlements.values());
    }
}
