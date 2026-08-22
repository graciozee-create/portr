package com.sandymandy.pleasurehorizons.util.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.registries.SceneKeyframeEventRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads {@code assets/<namespace>/keyframe_events/*.json} into
 * {@link SceneKeyframeEventRegistry}.
 *
 * <p>Yarn to Mojang: {@code resourceManager.findResources(path, pred)} keeps the same shape but
 * returns {@code Map<ResourceLocation, Resource>} and the predicate takes a
 * {@link ResourceLocation}; {@code SoundEvent.of(id)} became
 * {@link SoundEvent#createVariableRangeEvent(ResourceLocation)}; {@code Identifier.of} became
 * {@link ResourceLocation#parse}.</p>
 */
public class SceneKeyframeEventLoader {

    private SceneKeyframeEventLoader() {
    }

    public static void loadFromAssets(ResourceManager resourceManager) {
        SceneKeyframeEventRegistry.clear();

        Map<ResourceLocation, Resource> resources =
                resourceManager.listResources("keyframe_events", path -> path.getPath().endsWith(".json"));

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();

            try (InputStreamReader reader =
                         new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                if (!json.has("girl_id")) {
                    PleasureHorizons.LOGGER.warn("[SceneKeyframeEventLoader] {} has no girl_id, skipping", id);
                    continue;
                }

                String girlID = json.get("girl_id").getAsString();
                JsonArray events = json.getAsJsonArray("events");
                if (events == null) continue;

                for (JsonElement element : events) {
                    JsonObject event = element.getAsJsonObject();
                    if (!event.has("key")) continue;
                    String key = event.get("key").getAsString();

                    if (event.has("sounds")) {
                        for (String soundId : toStringList(event.getAsJsonArray("sounds"))) {
                            SceneKeyframeEventRegistry.registerSound(girlID, key, soundOf(soundId));
                        }
                    }

                    if (event.has("random_sounds")) {
                        List<SoundEvent> pool = new ArrayList<>();
                        for (String soundId : toStringList(event.getAsJsonArray("random_sounds"))) {
                            pool.add(soundOf(soundId));
                        }
                        if (!pool.isEmpty()) {
                            SceneKeyframeEventRegistry.registerSound(girlID, key, pool);
                        }
                    }

                    if (event.has("messages")) {
                        for (String message : toStringList(event.getAsJsonArray("messages"))) {
                            SceneKeyframeEventRegistry.registerMessage(girlID, key, message);
                        }
                    }

                    if (event.has("player_messages")) {
                        for (String message : toStringList(event.getAsJsonArray("player_messages"))) {
                            SceneKeyframeEventRegistry.registerPlayerMessage(key, message);
                        }
                    }
                }

                PleasureHorizons.LOGGER.info("[SceneKeyframeEventLoader] Loaded keyframe events for {}", girlID);
            } catch (Exception e) {
                PleasureHorizons.LOGGER.error("[SceneKeyframeEventLoader] Failed to load " + id, e);
            }
        }
    }

    private static SoundEvent soundOf(String id) {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.parse(id));
    }

    private static List<String> toStringList(JsonArray array) {
        List<String> list = new ArrayList<>();
        if (array != null) {
            for (JsonElement e : array) {
                list.add(e.getAsString());
            }
        }
        return list;
    }
}
