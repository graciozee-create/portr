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

    /** One-button shift switch: advances every owned girl to the next survival role. */
    public static final KeyMapping SHIFT_ROLES_KEY = new KeyMapping(
            "key.pleasurehorizons.shift_roles",
            InputConstants.Type.KEYSYM,
            org.lwjgl.glfw.GLFW.GLFW_KEY_B,
            CATEGORY);

    /** Toggles the girl status HUD (off by default). */
    public static final KeyMapping TOGGLE_STATUS_HUD_KEY = new KeyMapping(
            "key.pleasurehorizons.toggle_status_hud",
            InputConstants.Type.KEYSYM,
            org.lwjgl.glfw.GLFW.GLFW_KEY_N,
            CATEGORY);

    /** Teleports every owned girl to the player - the "come defend me" button. */
    public static final KeyMapping CALL_GIRLS_KEY = new KeyMapping(
            "key.pleasurehorizons.call_girls",
            InputConstants.Type.KEYSYM,
            org.lwjgl.glfw.GLFW.GLFW_KEY_G,
            CATEGORY);

    /**
     * Freecam controls, mirroring upstream's {@code ModBindings}.
     *
     * <p>Upstream defaults the toggle to F4 and leaves the other two unbound; the same is done
     * here. Holding the toggle and tapping a hotbar number addresses tripod 1-9, which is the
     * "combo key" behaviour of {@code FreecamComboKeyMapping}.</p>
     */
    public static final KeyMapping FREECAM_TOGGLE_KEY = new KeyMapping(
            "key.pleasurehorizons.freecam.toggle",
            InputConstants.Type.KEYSYM,
            org.lwjgl.glfw.GLFW.GLFW_KEY_F4,
            CATEGORY);

    public static final KeyMapping FREECAM_PLAYER_CONTROL_KEY = new KeyMapping(
            "key.pleasurehorizons.freecam.player_control",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY);

    public static final KeyMapping FREECAM_TRIPOD_RESET_KEY = new KeyMapping(
            "key.pleasurehorizons.freecam.tripod_reset",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY);

}
