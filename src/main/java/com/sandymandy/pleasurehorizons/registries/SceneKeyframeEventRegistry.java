package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Sounds and chat lines bound to animation keyframes.
 *
 * <p>Previously only the fixed-sound half existed and {@code getSound} did a plain map lookup,
 * so nothing ever matched: GeckoLib delivers keyframe payloads as comma-separated token lists
 * (e.g. {@code "paizurislowmsg1,thrust"}), which never equals a single registered key. This
 * restores the upstream token-splitting behaviour, plus the random-sound, girl-message and
 * player-message maps that were missing entirely.</p>
 */
public class SceneKeyframeEventRegistry {
    private static final Map<SceneKey, List<SoundEvent>> SOUND_EVENTS = new HashMap<>();
    private static final Map<SceneKey, List<SoundEvent>> RANDOM_SOUNDS = new HashMap<>();
    private static final Map<SceneKey, List<String>> CHAT_MESSAGES = new HashMap<>();
    private static final Map<String, List<String>> PLAYER_MESSAGES = new HashMap<>();
    private static final Random RANDOM = new Random();

    public static void registerSoundEvents() {
        PleasureHorizons.LOGGER.info("Registering Scene Keyframe Events for " + PleasureHorizons.MOD_NAME);
    }

    /** Wipes every entry; called before each resource reload so entries do not pile up. */
    public static void clear() {
        SOUND_EVENTS.clear();
        RANDOM_SOUNDS.clear();
        CHAT_MESSAGES.clear();
        PLAYER_MESSAGES.clear();
    }

    // ------------------------------------------------------------------ sounds

    public static void registerSound(String girlID, String frameKey, SoundEvent event) {
        SOUND_EVENTS.computeIfAbsent(new SceneKey(girlID, frameKey), k -> new ArrayList<>()).add(event);
    }

    /** Registers a pool from which exactly one sound is picked each time the keyframe fires. */
    public static void registerSound(String girlID, String frameKey, List<SoundEvent> events) {
        RANDOM_SOUNDS.computeIfAbsent(new SceneKey(girlID, frameKey), k -> new ArrayList<>()).addAll(events);
    }

    public static List<SoundEvent> getSound(String girlID, String keyframe) {
        List<SoundEvent> result = new ArrayList<>();
        String[] tokens = tokenize(keyframe);

        for (String token : tokens) {
            SceneKey key = new SceneKey(girlID, token);

            List<SoundEvent> fixed = SOUND_EVENTS.get(key);
            if (fixed != null) {
                result.addAll(fixed);
            }

            List<SoundEvent> pool = RANDOM_SOUNDS.get(key);
            if (pool != null && !pool.isEmpty()) {
                result.add(pool.get(RANDOM.nextInt(pool.size())));
            }
        }

        return result;
    }

    // ---------------------------------------------------------------- messages

    public static void registerMessage(String girlID, String frameKey, String message) {
        CHAT_MESSAGES.computeIfAbsent(new SceneKey(girlID, frameKey), k -> new ArrayList<>()).add(message);
    }

    public static void registerPlayerMessage(String frameKey, String message) {
        PLAYER_MESSAGES.computeIfAbsent(frameKey.toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(message);
    }

    public static List<String> getMessage(String girlID, String keyframe) {
        List<String> result = new ArrayList<>();
        for (String token : tokenize(keyframe)) {
            List<String> messages = CHAT_MESSAGES.get(new SceneKey(girlID, token));
            if (messages != null) {
                result.addAll(messages);
            }
        }
        return result;
    }

    public static List<String> getPlayerMessage(String keyframe) {
        List<String> result = new ArrayList<>();
        for (String token : tokenize(keyframe)) {
            List<String> messages = PLAYER_MESSAGES.get(token);
            if (messages != null) {
                result.addAll(messages);
            }
        }
        return result.isEmpty() ? Collections.emptyList() : result;
    }

    /**
     * Keyframe payloads arrive as comma- or dash-separated token lists; upstream split on
     * "," for sounds and "-" for messages, so accept both and trim each token.
     */
    private static String[] tokenize(String keyframe) {
        String[] raw = keyframe.toLowerCase(Locale.ROOT).strip().split("[,-]");
        for (int i = 0; i < raw.length; i++) {
            raw[i] = raw[i].strip();
        }
        return raw;
    }

    public record SceneKey(String girlID, String key) {
        public SceneKey {
            key = key.toLowerCase(Locale.ROOT);
        }
    }
}
