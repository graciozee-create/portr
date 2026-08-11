package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.sound.SoundEvent;

import java.util.*;
public class SceneKeyframeEventRegistry {
    private static final Map<SceneKey, List<SoundEvent>> SOUND_EVENTS = new HashMap<>();
    private static final Map<SceneKey, List<SoundEvent>> RANDOM_SOUNDS = new HashMap<>();
    private static final Map<SceneKey, List<String>> CHAT_MESSAGES = new HashMap<>();
    private static final Map<String, List<String>> PLAYER_MESSAGES = new HashMap<>();
    private static final Random RANDOM = new Random();

    public static void registerSoundEvents() {
        PleasureHorizons.LOGGER.info("Registering Scene Keyframe Events for " + PleasureHorizons.MOD_NAME);
    }

    public static void registerSound(String girlID, String frameKey, SoundEvent event) {
        frameKey = frameKey.toLowerCase();
        SceneKey key = new SceneKey(girlID, frameKey);
        SOUND_EVENTS.computeIfAbsent(key, k -> new ArrayList<>()).add(event);
    }

    public static void registerSound(String girlID, String frameKey, List<SoundEvent> events) {
        frameKey = frameKey.toLowerCase();
        SceneKey key = new SceneKey(girlID, frameKey);
        RANDOM_SOUNDS.computeIfAbsent(key, k -> new ArrayList<>()).addAll(events);
    }

    public static List<SoundEvent> getSound(String girlID, String keyframe) {
        keyframe = keyframe.toLowerCase(Locale.ROOT);

        // Remove all white spaces and split on "," to different strings
        String[] tokens = keyframe.strip().split(",");

        List<SoundEvent> result = new ArrayList<>();

        // Exact match only
        for (Map.Entry<SceneKey, List<SoundEvent>> entry : SOUND_EVENTS.entrySet()) {
            SceneKey sk = entry.getKey();

            if (!sk.girlID().equals(girlID)) continue;

            for (String token : tokens) {
                if (token.equals(sk.key())) {
                    result.addAll(entry.getValue());
                }
            }
        }

        // Random sound system (exact match too)
        for (Map.Entry<SceneKey, List<SoundEvent>> entry : RANDOM_SOUNDS.entrySet()) {
            SceneKey sk = entry.getKey();

            if (!sk.girlID().equals(girlID)) continue;

            for (String token : tokens) {
                if (token.equals(sk.key())) {
                    List<SoundEvent> pool = entry.getValue();
                    if (!pool.isEmpty()) {
                        result.add(pool.get(RANDOM.nextInt(pool.size())));
                    }
                }
            }
        }

        return result;
    }


    public static void registerMessage(String girlID, String frameKey, String message) {
        frameKey = frameKey.toLowerCase();
        SceneKey key = new SceneKey(girlID, frameKey);
        CHAT_MESSAGES.computeIfAbsent(key, k -> new ArrayList<>()).add(message);
    }

    // --- Register Player message ---
    public static void registerPlayerMessage(String frameKey, String message) {
        frameKey = frameKey.toLowerCase();
        PLAYER_MESSAGES.computeIfAbsent(frameKey, k -> new ArrayList<>()).add(message);
    }

    public static List<String> getPlayerMessage(String key) {
        key = key.toLowerCase();
        return PLAYER_MESSAGES.getOrDefault(key, Collections.emptyList());
    }

    public static List<String> getMessage(String girlID, String keyframe) {
        keyframe = keyframe.toLowerCase(Locale.ROOT);
        String[] tokens = keyframe.split("-");

        List<String> result = new ArrayList<>();

        for (Map.Entry<SceneKey, List<String>> entry : CHAT_MESSAGES.entrySet()) {
            SceneKey sk = entry.getKey();

            if (!sk.girlID().equals(girlID)) continue;

            for (String token : tokens) {
                if (token.equals(sk.key())) {
                    result.addAll(entry.getValue());
                }
            }
        }

        return result;
    }

    public record SceneKey(String girlID, String key) {
        public SceneKey {
            key = key.toLowerCase(Locale.ROOT); // normalize record field
        }
    }
}
