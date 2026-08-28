package com.sandymandy.pleasurehorizons.relationship;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Localized dialogue database.
 *
 * <p>Upstream hard-coded English strings; this port returns <em>translation keys</em> instead so
 * the same lines render in en_us and ru_ru. Every girl currently falls back to the tier greeting,
 * while {@link #greetingKey(String, AffectionLevel)} leaves room for girl-specific lines later
 * (add an entry to {@link #GIRL_SPECIFIC} and create the matching lang keys).</p>
 */
public class DialogueDB {

    private static final Map<String, String> GIRL_SPECIFIC = Map.of();

    private DialogueDB() {
    }

    /** Random greeting translation key for a girl at the given affection tier. */
    public static String greetingKey(String girlId, AffectionData.AffectionLevel level) {
        String girl = girlId == null ? "" : girlId.toLowerCase(java.util.Locale.ROOT);
        String base = GIRL_SPECIFIC.getOrDefault(girl, "generic");
        int line = ThreadLocalRandom.current().nextInt(1, 3 + 1);
        return "dialogue.pleasurehorizons.greeting." + base + "." + level.name().toLowerCase(java.util.Locale.ROOT) + "." + line;
    }

    /** Non-random first line, used by the NPC editor's static preview. */
    public static String previewKey(AffectionData.AffectionLevel level) {
        return "dialogue.pleasurehorizons.greeting.generic."
                + level.name().toLowerCase(java.util.Locale.ROOT) + ".1";
    }
}
