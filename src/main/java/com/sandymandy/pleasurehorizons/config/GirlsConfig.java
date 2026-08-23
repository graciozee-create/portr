package com.sandymandy.pleasurehorizons.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-side tuning for the girls (stats and self-healing).
 *
 * <p>Upstream has no equivalent file (it ships no per-girl behaviour config), so this is a
 * small extension for "advanced gameplay". It follows the same {@link ModConfigSpec} pattern as
 * {@code FreecamConfig}: the spec lives in the static initializer, the file lands in
 * {@code config/pleasurehorizons-girls.toml}, and the values are read through plain static
 * getters (the call sites are cold, so a per-call lookup is fine - unlike freecam's hot loop).</p>
 */
public final class GirlsConfig {

    /** Config file name; used to identify this config in load/reload handling. */
    public static final String FILE_NAME = "pleasurehorizons-girls.toml";

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.ConfigValue<Double> HEALTH_MULTIPLIER;
    private static final ModConfigSpec.ConfigValue<Double> SPEED_MULTIPLIER;
    private static final ModConfigSpec.ConfigValue<Boolean> SELF_HEAL_ENABLED;
    private static final ModConfigSpec.ConfigValue<Double> SELF_HEAL_BELOW;
    private static final ModConfigSpec.ConfigValue<Integer> SELF_HEAL_INTERVAL;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Base stats, applied when a girl is created (spawn or chunk load).")
                .push("girls");
        HEALTH_MULTIPLIER = builder
                .comment("Multiplier applied to every girl's max health (1.0 = unchanged).")
                .defineInRange("healthMultiplier", 1.0, 0.25, 10.0, Double.class);
        SPEED_MULTIPLIER = builder
                .comment("Multiplier applied to every girl's movement speed (1.0 = unchanged).")
                .defineInRange("speedMultiplier", 1.0, 0.25, 5.0, Double.class);
        builder.pop();

        builder.comment("Self-healing: a tamed girl eats food from her own inventory to heal.")
                .push("selfHeal");
        SELF_HEAL_ENABLED = builder
                .comment("Whether tamed girls heal themselves by eating their own food.")
                .define("enabled", true);
        SELF_HEAL_BELOW = builder
                .comment("She only eats when her health is below this fraction of her max health.")
                .defineInRange("belowHealthPercent", 0.8, 0.1, 1.0, Double.class);
        SELF_HEAL_INTERVAL = builder
                .comment("Minimum ticks between two self-heal bites (20 ticks = 1 second).")
                .defineInRange("intervalTicks", 40, 10, 400, Integer.class);
        builder.pop();

        SPEC = builder.build();
    }

    private GirlsConfig() {
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, SPEC, FILE_NAME);
    }

    public static double healthMultiplier() {
        return HEALTH_MULTIPLIER.get();
    }

    public static double speedMultiplier() {
        return SPEED_MULTIPLIER.get();
    }

    public static boolean selfHealEnabled() {
        return SELF_HEAL_ENABLED.get();
    }

    public static double selfHealBelowPercent() {
        return SELF_HEAL_BELOW.get();
    }

    public static int selfHealIntervalTicks() {
        return SELF_HEAL_INTERVAL.get();
    }
}
