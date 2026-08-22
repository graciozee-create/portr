package com.sandymandy.pleasurehorizons.util.managers;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Server-side registry of every tamed girl's last known location.
 *
 * <p>An entity in an unloaded chunk is not in memory, so {@code ServerLevel#getAllEntities()}
 * cannot see it. This registry remembers where each tamed girl was the last time she was loaded,
 * which lets the "call girls" feature force-load her chunk and teleport her to the player even
 * from across the world.</p>
 *
 * <p>This class is a thin static facade over {@link TamedGirlSavedData}, which persists the record
 * across server restarts. The data is attached once on server start; before that (or on the
 * client) every operation is a harmless no-op.</p>
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

    @Nullable
    private static TamedGirlSavedData data;

    private TamedGirlRegistry() {
    }

    /** Attaches the persistent registry for the current server. */
    public static void attach(TamedGirlSavedData savedData) {
        data = savedData;
    }

    /** Records (or refreshes) a tamed girl's location. No-op on the client and for wild girls. */
    public static void update(TameableGirlEntity girl) {
        if (girl.level().isClientSide() || data == null) {
            return;
        }
        UUID owner = girl.getOwnerUUID();
        if (owner == null || !girl.isTamed()) {
            data.remove(girl.getUUID());
            return;
        }
        data.put(girl.getUUID(), new Entry(
                girl.getUUID(),
                owner,
                girl.level().dimension(),
                girl.getX(), girl.getY(), girl.getZ(),
                girl.getGirlID(),
                girl.hasCustomName() ? girl.getCustomName().getString() : null));
    }

    public static void remove(UUID girlId) {
        if (data != null) {
            data.remove(girlId);
        }
    }

    @Nullable
    public static Entry get(UUID girlId) {
        return data == null ? null : data.get(girlId);
    }

    /** All known entries owned by the given player, optionally filtered by a name. */
    public static List<Entry> ownedBy(UUID ownerId, @Nullable String name) {
        return data == null ? List.of() : data.ownedBy(ownerId, name);
    }
}
