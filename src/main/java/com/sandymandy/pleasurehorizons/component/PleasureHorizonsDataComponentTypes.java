package com.sandymandy.pleasurehorizons.component;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.neoforged.bus.api.IEventBus;

public class PleasureHorizonsDataComponentTypes {
    public static void register(IEventBus bus) {
        PleasureHorizons.LOGGER.info("Registering data components for " + PleasureHorizons.MOD_NAME);
    }

    public static void registerDataComponentsTypes() {
        PleasureHorizons.LOGGER.info("Registering data component types for " + PleasureHorizons.MOD_NAME);
    }
}
