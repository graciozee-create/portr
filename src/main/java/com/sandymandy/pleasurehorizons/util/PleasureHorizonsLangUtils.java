package com.sandymandy.pleasurehorizons.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sandymandy.pleasurehorizons.PleasureHorizons;

import net.minecraft.util.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PleasureHorizonsLangUtils {
    private static final Map<String, String> translations = new HashMap<>();
    private static final Gson GSON = new Gson();

    static {
        try {
            Identifier id = Identifier.of(PleasureHorizons.MOD_ID, "lang/en_us.json");
            try (InputStreamReader reader = new InputStreamReader(
                    Objects.requireNonNull(PleasureHorizonsLangUtils.class.getClassLoader().getResourceAsStream("assets/" + id.getNamespace() + "/" + id.getPath())),
                    StandardCharsets.UTF_8)) {
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> data = GSON.fromJson(reader, type);
                if (data != null) translations.putAll(data);
            }
        } catch (Exception e) {
            System.err.println("[PleasureCraft] Failed to load server translations: " + e.getMessage());
        }
    }

    public static String getStringFromKey(String key, Object... args) {
        if (FMLEnvironment.getSide() == Dist.CLIENT) {
            try {
                return net.minecraft.client.resource.language.I18n.translate(key, args);
            } catch (Throwable ignored) {}
        }
        String base = translations.getOrDefault(key, key);
        return args.length > 0 ? String.format(base, args) : base;
    }
}
