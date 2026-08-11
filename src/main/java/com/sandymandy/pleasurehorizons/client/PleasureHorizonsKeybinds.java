package com.sandymandy.pleasurehorizons.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class PleasureHorizonsKeybinds {
    public static final String CATEGORY = "key.categories.pleasurehorizons";
    public static KeyMapping thrustKey;
    public static KeyMapping cumKey;

    public static void register() {
        PleasureHorizons.LOGGER.info("Registering keybinds");
        thrustKey = new KeyMapping("key.pleasurehorizons.thrust", KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, CATEGORY);
        cumKey = new KeyMapping("key.pleasurehorizons.cum", KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_SHIFT, CATEGORY);
    }
}
