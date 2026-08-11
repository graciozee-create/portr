package com.sandymandy.pleasurehorizons.util.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.registries.SceneKeyframeEventRegistry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SceneKeyframeEventLoader {

    public static void loadFromAssets(ResourceManager resourceManager) {

        resourceManager.findResources("keyframe_events", path -> path.getPath().endsWith(".json")).forEach((id, resource) -> {
            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                String girlID = json.get("girl_id").getAsString();
                JsonArray scenes = json.getAsJsonArray("events");

                for (JsonElement elem : scenes) {
                    JsonObject scene = elem.getAsJsonObject();
                    String key = scene.get("key").getAsString().toLowerCase();

                    // --- Fixed sounds ---
                    if (scene.has("sounds")) {
                        for (String soundId : jsonArrayToList(scene.getAsJsonArray("sounds"))) {
                            SoundEvent sound = SoundEvent.of(Identifier.of(soundId));
                            SceneKeyframeEventRegistry.registerSound(girlID, key, sound);
                        }
                    }

                    // --- Random sounds ---
                    if (scene.has("random_sounds")) {
                        List<String> randomIds = jsonArrayToList(scene.getAsJsonArray("random_sounds"));
                        List<SoundEvent> soundEvents = randomIds.stream()
                                .map(idStr -> SoundEvent.of(Identifier.of(idStr)))
                                .toList();
                        SceneKeyframeEventRegistry.registerSound(girlID, key, soundEvents);
                    }

                    // --- Messages ---
                    if (scene.has("messages")) {
                        for (String message : jsonArrayToList(scene.getAsJsonArray("messages"))) {
                            SceneKeyframeEventRegistry.registerMessage(girlID, key, message);
                        }
                    }

                    // --- Messages ---
                    if (scene.has("player_messages")) {
                        for (String message : jsonArrayToList(scene.getAsJsonArray("player_messages"))) {
                            SceneKeyframeEventRegistry.registerPlayerMessage(key, message);
                        }
                    }
                }

                PleasureHorizons.LOGGER.info("[SceneKeyframeEventLoader] Loaded scene keyframe events for {}", girlID);

            } catch (Exception e) {
                PleasureHorizons.LOGGER.error("[SceneKeyframeEventLoader] Failed to load scene keyframe events " + id, e);
            }
        });
    }

    private static List<String> jsonArrayToList(JsonArray array) {
        List<String> list = new ArrayList<>();
        if (array != null) {
            for (JsonElement e : array) list.add(e.getAsString());
        }
        return list;
    }
}
