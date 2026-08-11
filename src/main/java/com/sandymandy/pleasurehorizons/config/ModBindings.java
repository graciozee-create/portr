package com.sandymandy.pleasurehorizons.config;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.sandymandy.pleasurehorizons.freecam.Freecam;
import me.shedaniel.autoconfig.AutoConfig;
import com.sandymandy.pleasurehorizons.config.keys.FreecamKeyMapping;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.sandymandy.pleasurehorizons.freecam.Freecam.MC;
import static com.sandymandy.pleasurehorizons.config.keys.FreecamKeyMappingBuilder.builder;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F4;

public enum ModBindings {

    KEY_TOGGLE(() -> builder("toggle")
            .action(Freecam::toggle)
            .holdAction(Freecam::activateTripodHandler)
            .defaultKey(GLFW_KEY_F4)
            .build()),
    KEY_PLAYER_CONTROL(() -> builder("playerControl")
            .action(Freecam::switchControls)
            .build()),
    KEY_TRIPOD_RESET(() -> builder("tripodReset")
            .holdAction(Freecam::resetTripodHandler)
            .build()),
    KEY_CONFIG_GUI(() -> builder("configGui")
            .action(() -> MC.setScreen(AutoConfig.getConfigScreen(ModConfig.class, MC.currentScreen).get()))
            .build());

    private final Supplier<FreecamKeyMapping> lazyMapping;

    ModBindings(Supplier<FreecamKeyMapping> mappingSupplier) {
        lazyMapping = Suppliers.memoize(mappingSupplier);
    }

    public FreecamKeyMapping get() {
        return lazyMapping.get();
    }

    public static void forEach(@NotNull Consumer<FreecamKeyMapping> action) {
        Objects.requireNonNull(action);
        iterator().forEachRemaining(action);
    }

    public static @NotNull Iterator<FreecamKeyMapping> iterator() {
        return stream().iterator();
    }

    public static @NotNull Spliterator<FreecamKeyMapping> spliterator() {
        return stream().spliterator();
    }

    public static @NotNull Stream<FreecamKeyMapping> stream() {
        return Arrays.stream(values()).map(ModBindings::get);
    }
}
