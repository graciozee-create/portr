package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.neoforged.bus.api.IEventBus;

public class PleasureHorizonsTrackedDataRegistry {
    public static void register(IEventBus bus) {
        PleasureHorizons.LOGGER.info("Registering tracked data");
    }

    public static void registerTrackedData() {
        PleasureHorizons.LOGGER.info("Registering tracked data");
    }
}
