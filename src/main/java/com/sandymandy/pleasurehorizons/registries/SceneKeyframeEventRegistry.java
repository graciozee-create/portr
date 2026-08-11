package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.sounds.SoundEvent;

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
        SOUND_EVENTS.put(key, new ArrayList<>(events));
    }

    public static List<SoundEvent> getSound(String girlID, String keyframe) {
        return SOUND_EVENTS.getOrDefault(new SceneKey(girlID, keyframe.toLowerCase()), List.of());
    }

    public static record SceneKey(String girlID, String frameKey) {}
}
