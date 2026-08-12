package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.PleasureHorizons;

/**
 * Placeholder kept so common-side init keeps its call site.
 *
 * <p>The scene HUD itself is a client-only concern and is registered through
 * {@code RegisterGuiLayersEvent} in {@code PleasureHorizonsClientEvents}; Fabric's
 * {@code HudElementRegistry} has no common-side counterpart on NeoForge.</p>
 */
public class PleasureHorizonsHudRegistry {
    public static void register() {
        PleasureHorizons.LOGGER.info("Scene HUD is registered client-side via RegisterGuiLayersEvent");
    }
}
