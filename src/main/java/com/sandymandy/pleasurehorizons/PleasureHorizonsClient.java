package com.sandymandy.pleasurehorizons;

import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PleasureHorizonsClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(PleasureHorizons.MOD_NAME + "-Client");

    public static void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.info("Pleasure Horizons client setup");
        // Client-side initialization will be ported here
        com.sandymandy.pleasurehorizons.client.PleasureHorizonsKeybinds.register();
        com.sandymandy.pleasurehorizons.registries.PleasureHorizonsHudRegistry.register();
        com.sandymandy.pleasurehorizons.config.ModConfig.init();
    }

    public static boolean areIrisShadersDisabled() {
        return true;
    }
}
