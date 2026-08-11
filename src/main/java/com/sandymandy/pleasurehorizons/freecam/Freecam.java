package com.sandymandy.pleasurehorizons.freecam;

/**
 * Placeholder for the not-yet-ported freecam feature.
 *
 * <p>This class must stay free of client-only types. It previously held a
 * {@code static final Minecraft MC = Minecraft.getInstance()} field, which would throw
 * {@link NoClassDefFoundError} on a dedicated server the moment the class was loaded,
 * since {@code net.minecraft.client.Minecraft} does not exist there.</p>
 */
public class Freecam {
    private Freecam() {}

    public static void preTick(Object client) {}

    public static void postTick(Object client) {}
}
