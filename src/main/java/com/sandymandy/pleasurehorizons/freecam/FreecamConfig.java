package com.sandymandy.pleasurehorizons.freecam;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Freecam settings.
 *
 * <p>Upstream stores these with Cloth Config / AutoConfig, which is Fabric-only and would pull
 * in an extra hard dependency, so this port uses NeoForge's own {@link ModConfigSpec}. The
 * option names, defaults and meanings are carried over unchanged; the file lands in
 * {@code config/pleasurehorizons-freecam.toml} and can be edited while the game runs.</p>
 *
 * <p>Values are mirrored into plain fields on {@link #INSTANCE} so the hot paths (the camera
 * tick, the motion solver) do not go through the config lookup every frame.</p>
 */
public final class FreecamConfig {

    public static final FreecamConfig INSTANCE = new FreecamConfig();

    /** How the camera is placed relative to the player when freecam starts. */
    public enum Perspective {
        FIRST_PERSON,
        THIRD_PERSON,
        THIRD_PERSON_MIRROR,
        INSIDE
    }

    /** Whether attacks/interactions act from the camera or from the player. */
    public enum InteractionMode {
        CAMERA,
        PLAYER
    }

    // ---- live values (defaults match upstream) ----
    public double horizontalSpeed = 1.0;
    public double verticalSpeed = 1.0;
    public Perspective perspective = Perspective.INSIDE;
    public boolean hidePlayer = true;
    public boolean showHand = false;
    public boolean showSubmersion = false;
    public boolean disableOnDamage = true;
    public boolean allowInteract = false;
    public InteractionMode interactionMode = InteractionMode.CAMERA;
    public boolean notifyFreecam = true;
    public boolean notifyTripod = true;

    private FreecamConfig() {}

    // ---- spec ----
    /** Config file name; also used to identify the config in the load/reload events. */
    public static final String FILE_NAME = "pleasurehorizons-freecam.toml";

    public static final ModConfigSpec SPEC;

    // Declared with the generic ConfigValue/EnumValue types rather than the IntValue /
    // DoubleValue / BooleanValue conveniences so this only depends on the ModConfigSpec
    // overloads that are guaranteed present.
    private static final ModConfigSpec.ConfigValue<Double> HORIZONTAL_SPEED;
    private static final ModConfigSpec.ConfigValue<Double> VERTICAL_SPEED;
    private static final ModConfigSpec.EnumValue<Perspective> PERSPECTIVE;
    private static final ModConfigSpec.ConfigValue<Boolean> HIDE_PLAYER;
    private static final ModConfigSpec.ConfigValue<Boolean> SHOW_HAND;
    private static final ModConfigSpec.ConfigValue<Boolean> SHOW_SUBMERSION;
    private static final ModConfigSpec.ConfigValue<Boolean> DISABLE_ON_DAMAGE;
    private static final ModConfigSpec.ConfigValue<Boolean> ALLOW_INTERACT;
    private static final ModConfigSpec.EnumValue<InteractionMode> INTERACTION_MODE;
    private static final ModConfigSpec.ConfigValue<Boolean> NOTIFY_FREECAM;
    private static final ModConfigSpec.ConfigValue<Boolean> NOTIFY_TRIPOD;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Freecam movement").push("movement");
        HORIZONTAL_SPEED = builder
                .comment("Horizontal fly speed of the free camera.")
                .defineInRange("horizontalSpeed", 1.0, 0.0, 10.0, Double.class);
        VERTICAL_SPEED = builder
                .comment("Vertical fly speed of the free camera.")
                .defineInRange("verticalSpeed", 1.0, 0.0, 10.0, Double.class);
        builder.pop();

        builder.comment("Freecam visuals").push("visual");
        PERSPECTIVE = builder
                .comment("Where the camera starts relative to the player.")
                .defineEnum("perspective", Perspective.INSIDE);
        HIDE_PLAYER = builder
                .comment("Hide your own player model while in freecam.")
                .define("hidePlayer", true);
        SHOW_HAND = builder
                .comment("Keep rendering the held item while in freecam.")
                .define("showHand", false);
        SHOW_SUBMERSION = builder
                .comment("Apply water/lava overlays when the camera is submerged.")
                .define("showSubmersion", false);
        builder.pop();

        builder.comment("Freecam behaviour").push("utility");
        DISABLE_ON_DAMAGE = builder
                .comment("Automatically leave freecam when you take damage.")
                .define("disableOnDamage", true);
        ALLOW_INTERACT = builder
                .comment("Allow attacking and using items while in freecam.")
                .define("allowInteract", false);
        INTERACTION_MODE = builder
                .comment("Whether interactions originate from the camera or the player.")
                .defineEnum("interactionMode", InteractionMode.CAMERA);
        builder.pop();

        builder.comment("Freecam notifications").push("notification");
        NOTIFY_FREECAM = builder
                .comment("Show a message when freecam is toggled.")
                .define("notifyFreecam", true);
        NOTIFY_TRIPOD = builder
                .comment("Show a message when a tripod is toggled.")
                .define("notifyTripod", true);
        builder.pop();

        SPEC = builder.build();
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC, FILE_NAME);
    }

    /** Copies the spec values into the plain fields; called on load and on reload. */
    public static void sync() {
        INSTANCE.horizontalSpeed = HORIZONTAL_SPEED.get();
        INSTANCE.verticalSpeed = VERTICAL_SPEED.get();
        INSTANCE.perspective = PERSPECTIVE.get();
        INSTANCE.hidePlayer = HIDE_PLAYER.get();
        INSTANCE.showHand = SHOW_HAND.get();
        INSTANCE.showSubmersion = SHOW_SUBMERSION.get();
        INSTANCE.disableOnDamage = DISABLE_ON_DAMAGE.get();
        INSTANCE.allowInteract = ALLOW_INTERACT.get();
        INSTANCE.interactionMode = INTERACTION_MODE.get();
        INSTANCE.notifyFreecam = NOTIFY_FREECAM.get();
        INSTANCE.notifyTripod = NOTIFY_TRIPOD.get();
    }

    // ---- setters used by the in-game settings screen ----
    // Each writes through the spec (so the toml is updated immediately) and then re-mirrors
    // the live values, so the camera tick picks the change up on the next frame.
    private static void setAndSync(Runnable setter) {
        setter.run();
        SPEC.save();
        sync();
    }

    public static void setHorizontalSpeed(double value) {
        double clamped = Math.max(0.0, Math.min(10.0, value));
        setAndSync(() -> HORIZONTAL_SPEED.set(clamped));
    }

    public static void setVerticalSpeed(double value) {
        double clamped = Math.max(0.0, Math.min(10.0, value));
        setAndSync(() -> VERTICAL_SPEED.set(clamped));
    }

    public static void setPerspective(Perspective value) {
        setAndSync(() -> PERSPECTIVE.set(value));
    }

    public static void setHidePlayer(boolean value) {
        setAndSync(() -> HIDE_PLAYER.set(value));
    }

    public static void setShowHand(boolean value) {
        setAndSync(() -> SHOW_HAND.set(value));
    }

    public static void setShowSubmersion(boolean value) {
        setAndSync(() -> SHOW_SUBMERSION.set(value));
    }

    public static void setDisableOnDamage(boolean value) {
        setAndSync(() -> DISABLE_ON_DAMAGE.set(value));
    }

    public static void setAllowInteract(boolean value) {
        setAndSync(() -> ALLOW_INTERACT.set(value));
    }

    public static void setInteractionMode(InteractionMode value) {
        setAndSync(() -> INTERACTION_MODE.set(value));
    }

    public static void setNotifyFreecam(boolean value) {
        setAndSync(() -> NOTIFY_FREECAM.set(value));
    }

    public static void setNotifyTripod(boolean value) {
        setAndSync(() -> NOTIFY_TRIPOD.set(value));
    }
}
