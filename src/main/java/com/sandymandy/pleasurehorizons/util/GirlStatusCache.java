package com.sandymandy.pleasurehorizons.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side cache of server-owned girl state that is not part of synched entity data.
 *
 * <p>Kept in common code (no {@code net.minecraft.client} imports) so S2C packet handlers can
 * write to it without crashing a dedicated server, while the client HUD reads it. Currently only
 * holds the backpack fill; entity ids are reused after removal, so consumers should resolve the
 * entity first and ignore stale values.</p>
 */
public final class GirlStatusCache {
    private static final Map<Integer, Integer> BACKPACK_USED = new ConcurrentHashMap<>();

    private GirlStatusCache() {
    }

    public static void put(int entityId, int backpackUsedSlots) {
        BACKPACK_USED.put(entityId, backpackUsedSlots);
    }

    /** Returns the last known backpack fill, or -1 if unknown. */
    public static int backpackUsed(int entityId) {
        return BACKPACK_USED.getOrDefault(entityId, -1);
    }

    public static void remove(int entityId) {
        BACKPACK_USED.remove(entityId);
    }
}
