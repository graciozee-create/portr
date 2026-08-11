package com.sandymandy.pleasurehorizons.client.rendering.renderers;

public abstract class AbstractGirlRenderer {
    public static boolean IS_SHADING_DISABLED = false;

    public static void updateShadingState() {
        IS_SHADING_DISABLED = !IS_SHADING_DISABLED;
    }
}
