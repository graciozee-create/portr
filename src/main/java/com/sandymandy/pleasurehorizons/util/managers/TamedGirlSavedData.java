package com.sandymandy.pleasurehorizons.util.managers;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Persists {@link TamedGirlRegistry} across server restarts.
 *
 * <p>An entity in an unloaded chunk is absent from memory, so the "call girls" feature needs a
 * durable record of where every tamed girl was last seen. This {@link SavedData} stores that
 * record in the overworld's data storage and is the single source of truth;
 * {@code TamedGirlRegistry} is a thin static facade over the currently attached instance.</p>
 *
 * <p>Serialization is self-contained (UUIDs, dimension resource location, position, rig id and an
 * optional custom name), so a corrupt or missing entry is skipped without failing the load.</p>
 */
public class TamedGirlSavedData extends SavedData {

    private static final String NAME = "pleasurehorizons_tamed_girls";

    private final Map<UUID, TamedGirlRegistry.Entry> entries = new HashMap<>();

    public static SavedData.Factory<TamedGirlSavedData> factory() {
        return new SavedData.Factory<>(
                TamedGirlSavedData::new,
                TamedGirlSavedData::load,
                DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
    }

    public static String name() {
        return NAME;
    }

    public static TamedGirlSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        TamedGirlSavedData data = new TamedGirlSavedData();
        ListTag list = tag.getList("Girls", 10); // TAG_Compound
        for (int i = 0; i < list.size(); i++) {
            TamedGirlRegistry.Entry entry = readEntry(list.getCompound(i));
            if (entry != null) {
                data.entries.put(entry.girlId(), entry);
            }
        }
        return data;
    }

    @Nullable
    private static TamedGirlRegistry.Entry readEntry(CompoundTag tag) {
        try {
            UUID girlId = tag.getUUID("GirlId");
            UUID ownerId = tag.getUUID("OwnerId");
            ResourceKey<Level> dimension = ResourceKey.create(
                    Registries.DIMENSION, ResourceLocation.parse(tag.getString("Dimension")));
            double x = tag.getDouble("X");
            double y = tag.getDouble("Y");
            double z = tag.getDouble("Z");
            String rigId = tag.getString("RigId");
            String customName = tag.contains("CustomName") ? tag.getString("CustomName") : null;
            return new TamedGirlRegistry.Entry(girlId, ownerId, dimension, x, y, z, rigId, customName);
        } catch (RuntimeException ignored) {
            // A corrupt entry must not break loading the rest of the registry.
            return null;
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (TamedGirlRegistry.Entry entry : entries.values()) {
            CompoundTag c = new CompoundTag();
            c.putUUID("GirlId", entry.girlId());
            c.putUUID("OwnerId", entry.ownerId());
            c.putString("Dimension", entry.dimension().location().toString());
            c.putDouble("X", entry.x());
            c.putDouble("Y", entry.y());
            c.putDouble("Z", entry.z());
            c.putString("RigId", entry.rigId());
            if (entry.customName() != null) {
                c.putString("CustomName", entry.customName());
            }
            list.add(c);
        }
        tag.put("Girls", list);
        return tag;
    }

    void put(UUID girlId, TamedGirlRegistry.Entry entry) {
        entries.put(girlId, entry);
        setDirty();
    }

    void remove(UUID girlId) {
        if (entries.remove(girlId) != null) {
            setDirty();
        }
    }

    @Nullable
    TamedGirlRegistry.Entry get(UUID girlId) {
        return entries.get(girlId);
    }

    List<TamedGirlRegistry.Entry> ownedBy(UUID ownerId, @Nullable String name) {
        List<TamedGirlRegistry.Entry> result = new ArrayList<>();
        for (TamedGirlRegistry.Entry entry : entries.values()) {
            if (entry.ownerId().equals(ownerId) && entry.matchesName(name)) {
                result.add(entry);
            }
        }
        return result;
    }
}
