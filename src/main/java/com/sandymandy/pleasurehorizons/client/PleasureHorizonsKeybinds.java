package com.sandymandy.pleasurehorizons.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Scene keybinds.
 *
 * <p>Fabric registered these through {@code KeyBindingHelper}; on NeoForge they are handed to
 * {@code RegisterKeyMappingsEvent} from {@code PleasureHorizonsClientEvents}. The mappings are
 * plain static fields so the tick handler can poll them.</p>
 */
@OnlyIn(Dist.CLIENT)
public class PleasureHorizonsKeybinds {
    public static final String CATEGORY = "key.categories.pleasurehorizons";

    public static final KeyMapping THRUST_KEY = new KeyMapping(
            "key.pleasurehorizons.thrust",
            InputConstants.Type.KEYSYM,
            org.lwjgl.glfw.GLFW.GLFW_KEY_Z,
            CATEGORY);

    public static final KeyMapping CUM_KEY = new KeyMapping(
            "key.pleasurehorizons.cum",
            InputConstants.Type.KEYSYM,
            org.lwjgl.glfw.GLFW.GLFW_KEY_V,
            CATEGORY);

    /** Kept for the old call sites that referenced the lowercase names. */
    public static KeyMapping thrustKey = THRUST_KEY;
    public static KeyMapping cumKey = CUM_KEY;

    public static void register() {
        // Actual registration happens in PleasureHorizonsClientEvents#registerKeyMappings.
    }
}
