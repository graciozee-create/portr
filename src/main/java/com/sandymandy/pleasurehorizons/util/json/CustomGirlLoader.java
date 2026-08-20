package com.sandymandy.pleasurehorizons.util.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.util.variables.CustomGirlProfile;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Loads custom girl profiles from {@code config/pleasurehorizons/girls/*.json}.
 *
 * <p>Yarn/Fabric to NeoForge: {@code FabricLoader.getInstance().getConfigDir()} became
 * {@code FMLPaths.CONFIGDIR.get()}.</p>
 */
public class CustomGirlLoader {

    public static final Map<String, CustomGirlProfile> LOADED_PROFILES = new HashMap<>();
    public static final Map<Item, CustomGirlProfile> REGISTERED_PROFILES = new HashMap<>();

    public static void register() {
        LOADED_PROFILES.clear();
        REGISTERED_PROFILES.clear();

        Path dir = FMLPaths.CONFIGDIR.get().resolve(PleasureHorizons.MOD_ID).resolve("girls");

        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            PleasureHorizons.LOGGER.error("[CustomGirlLoader] Could not create {}", dir, e);
            return;
        }

        try (Stream<Path> files = Files.list(dir)) {
            files.filter(f -> f.toString().endsWith(".json")).forEach(CustomGirlLoader::loadFile);
        } catch (Exception e) {
            PleasureHorizons.LOGGER.error("[CustomGirlLoader] Failed loading girl profiles", e);
        }

        validateAndRegisterProfiles();
    }

    private static void loadFile(Path file) {
        try {
            JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            CustomGirlProfile profile = CustomGirlParser.parse(json);
            LOADED_PROFILES.put(profile.id(), profile);
            PleasureHorizons.LOGGER.info("[CustomGirlLoader] Loaded custom girl: {}", profile.id());
        } catch (Exception e) {
            PleasureHorizons.LOGGER.error("[CustomGirlLoader] Error parsing girl JSON: " + file, e);
        }
    }

    /** Profiles are keyed by their tame item, so a duplicate item would silently shadow one. */
    private static void validateAndRegisterProfiles() {
        for (CustomGirlProfile profile : LOADED_PROFILES.values()) {
            Item tameItem = profile.tameItem();
            CustomGirlProfile existing = REGISTERED_PROFILES.get(tameItem);

            if (existing == null) {
                REGISTERED_PROFILES.put(tameItem, profile);
                continue;
            }

            PleasureHorizons.LOGGER.error(
                    "[CustomGirlLoader] Duplicate tame item {} used by '{}' and '{}' - skipping '{}'.",
                    tameItem, existing.id(), profile.id(), profile.id());
        }
    }

    @Nullable
    public static CustomGirlProfile checkItem(Item item) {
        return REGISTERED_PROFILES.get(item);
    }

    public static CustomGirlProfile getGirlOrDefault(String id) {
        return LOADED_PROFILES.getOrDefault(id, CustomGirlProfile.DEFAULT);
    }
}
