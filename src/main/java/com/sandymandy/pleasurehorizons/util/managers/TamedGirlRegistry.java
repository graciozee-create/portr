package com.sandymandy.pleasurehorizons.util.managers;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side registry of every tamed girl's last known location.
 *
 * <p>An entity in an unloaded chunk is not in memory, so {@code ServerLevel#getAllEntities()}
 * cannot see it. This registry remembers where each tamed girl was the last time she was loaded,
 * which lets the "call girls" feature force-load her chunk and teleport her to the player even
 * from across the world.</p>
 *
 * <p>It is a runtime cache, not persisted state: it is rebuilt whenever a girl loads (her owner
 * is read from NBT), and entries are dropped only when a girl actually dies or is discarded. A
 * girl whose chunk has never been loaded since the server started is simply not known yet.</p>
 */
public final class TamedGirlRegistry {

    public record Entry(
            UUID girlId,
            UUID ownerId,
            ResourceKey<Level> dimension,
            double x,
            double y,
            double z,
            String rigId,
            @Nullable String customName
    ) {
        public boolean matchesName(@Nullable String name) {
            if (name == null || name.isEmpty()) {
                return true;
            }
            if (customName != null && customName.equalsIgnoreCase(name)) {
                return true;
            }
            return rigId.equalsIgnoreCase(name);
        }
    }

    private static final Map<UUID, Entry> GIRLS = new HashMap<>();

    private TamedGirlRegistry() {
    }

    /** Records (or refreshes) a tamed girl's location. No-op on the client and for wild girls. */
    public static void update(TameableGirlEntity girl) {
        if (girl.level().isClientSide()) {
            return;
        }
        UUID owner = girl.getOwnerUUID();
        if (owner == null || !girl.isTamed()) {
            GIRLS.remove(girl.getUUID());
            return;
        }
        GIRLS.put(girl.getUUID(), new Entry(
                girl.getUUID(),
                owner,
                girl.level().dimension(),
                girl.getX(), girl.getY(), girl.getZ(),
                girl.getGirlID(),
                girl.hasCustomName() ? girl.getCustomName().getString() : null));
    }

    public static void remove(UUID girlId) {
        GIRLS.remove(girlId);
    }

    @Nullable
    public static Entry get(UUID girlId) {
        return GIRLS.get(girlId);
    }

    /** All known entries owned by the given player, optionally filtered by a name. */
    public static List<Entry> ownedBy(UUID ownerId, @Nullable String name) {
        List<Entry> result = new ArrayList<>();
        for (Entry entry : GIRLS.values()) {
            if (!entry.ownerId.equals(ownerId) || !entry.matchesName(name)) {
                continue;
            }
            result.add(entry);
        }
        return result;
    }

    public static void clear() {
        GIRLS.clear();
    }
}
