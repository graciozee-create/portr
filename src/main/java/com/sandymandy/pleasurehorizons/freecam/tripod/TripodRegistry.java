package com.sandymandy.pleasurehorizons.freecam.tripod;

import com.sandymandy.pleasurehorizons.freecam.FreecamPosition;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.dimension.DimensionType;

import static com.sandymandy.pleasurehorizons.freecam.Freecam.MC;

public class TripodRegistry {
    private final Map<DimensionType, Map<TripodSlot, FreecamPosition>> tripods = new HashMap<>();

    public @Nullable FreecamPosition get(TripodSlot tripod) {
        return get(dimension(), tripod);
    }

    public @Nullable FreecamPosition get(DimensionType dimension, TripodSlot tripod) {
        return Optional.ofNullable(tripods.get(dimension))
                .map(positions -> positions.get(tripod))
                .orElse(null);
    }

    public void put(TripodSlot tripod, @Nullable FreecamPosition position) {
        put(dimension(), tripod, position);
    }

    public void put(DimensionType dimension, TripodSlot tripod, @Nullable FreecamPosition position) {
        tripods.computeIfAbsent(dimension, TripodRegistry::newEntry)
                .put(tripod, position);

    }

    public void clear() {
        tripods.clear();
    }

    private static DimensionType dimension() {
        return MC.world.getDimension();
    }

    private static Map<TripodSlot, FreecamPosition> newEntry(DimensionType dimension) {
        return new EnumMap<>(TripodSlot.class);
    }
}
