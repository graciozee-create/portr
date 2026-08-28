package com.sandymandy.pleasurehorizons.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Client-side UI options that are not big enough to deserve their own NeoForge config.
 *
 * <p>{@code holdThrust} and {@code disableShading} are persisted to
 * {@code config/pleasurehorizons/options.json} so the in-game settings screen has stable,
 * visible values across restarts (unlike the previous in-memory-only defaults).</p>
 */
public class ModConfig {
    public static final ModConfig INSTANCE = new ModConfig();

    public Girls girls = new Girls();
    public Keybinds keybinds = new Keybinds();

    public static class Girls {
        public boolean disableShading = false;
    }

    public static class Keybinds {
        public boolean holdThrust = true;
    }

    public void setDisableShading(boolean value) {
        this.girls.disableShading = value;
        save();
    }

    public void setHoldThrust(boolean value) {
        this.keybinds.holdThrust = value;
        save();
    }

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve(PleasureHorizons.MOD_ID).resolve("options.json");
    }

    /** Reads the persisted options, falling back to the compiled defaults on any error. */
    public static void load() {
        Path file = file();
        try {
            if (!Files.exists(file)) {
                return;
            }
            String json = Files.readString(file);
            if (json.isBlank()) {
                return;
            }
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("girls") && root.getAsJsonObject("girls").has("disableShading")) {
                INSTANCE.girls.disableShading = root.getAsJsonObject("girls").get("disableShading").getAsBoolean();
            }
            if (root.has("keybinds") && root.getAsJsonObject("keybinds").has("holdThrust")) {
                INSTANCE.keybinds.holdThrust = root.getAsJsonObject("keybinds").get("holdThrust").getAsBoolean();
            }
        } catch (Exception e) {
            PleasureHorizons.LOGGER.warn("Could not read {}; using defaults", file, e);
        }
    }

    /** Writes the current options, silently keeping defaults if the config dir is unavailable. */
    public void save() {
        Path file = file();
        try {
            Files.createDirectories(file.getParent());
            JsonObject root = new JsonObject();
            JsonObject girls = new JsonObject();
            girls.addProperty("disableShading", this.girls.disableShading);
            root.add("girls", girls);
            JsonObject keybinds = new JsonObject();
            keybinds.addProperty("holdThrust", this.keybinds.holdThrust);
            root.add("keybinds", keybinds);
            Files.writeString(file, PleasureHorizons.GSON.toJson(root));
        } catch (Exception e) {
            PleasureHorizons.LOGGER.warn("Could not write {}", file, e);
        }
    }
}
