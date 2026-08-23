package com.sandymandy.pleasurehorizons.freecam.tripod;

import com.sandymandy.pleasurehorizons.freecam.FreecamPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Remembers a saved camera placement per tripod slot, per dimension.
 *
 * <p>Upstream keys the outer map on {@code DimensionType}. This port keys it on the level's
 * {@link ResourceKey}, which is the stable identity of a dimension in 1.21.1 - two dimensions
 * can share a {@code DimensionType} instance, which would make their tripods collide.</p>
 */
@OnlyIn(Dist.CLIENT)
public class TripodRegistry {
    private final Map<ResourceKey<Level>, Map<TripodSlot, FreecamPosition>> tripods = new HashMap<>();

    public @Nullable FreecamPosition get(TripodSlot tripod) {
        ResourceKey<Level> dimension = dimension();
        return dimension == null ? null : get(dimension, tripod);
    }

    public @Nullable FreecamPosition get(ResourceKey<Level> dimension, TripodSlot tripod) {
        return Optional.ofNullable(this.tripods.get(dimension))
                .map(positions -> positions.get(tripod))
                .orElse(null);
    }

    public void put(TripodSlot tripod, @Nullable FreecamPosition position) {
        ResourceKey<Level> dimension = dimension();
        if (dimension != null) {
            put(dimension, tripod, position);
        }
    }

    public void put(ResourceKey<Level> dimension, TripodSlot tripod, @Nullable FreecamPosition position) {
        this.tripods.computeIfAbsent(dimension, key -> new EnumMap<>(TripodSlot.class))
                .put(tripod, position);
    }

    public void clear() {
        this.tripods.clear();
    }

    private static @Nullable ResourceKey<Level> dimension() {
        return Minecraft.getInstance().level == null
                ? null
                : Minecraft.getInstance().level.dimension();
    }
}
