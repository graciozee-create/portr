package com.sandymandy.pleasurehorizons.util.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sandymandy.pleasurehorizons.util.variables.CustomGirlProfile;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns a girl-profile JSON into a {@link CustomGirlProfile}.
 *
 * <p>Yarn to Mojang: {@code Registries.ITEM.get(Identifier.of(id))} became
 * {@code BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))}.</p>
 */
public class CustomGirlParser {

    private CustomGirlParser() {
    }

    public static CustomGirlProfile parse(JsonObject json) throws Exception {
        String id = json.get("id").getAsString();
        String name = json.get("name").getAsString();
        float hitboxHeight = json.has("hitbox_height") ? json.get("hitbox_height").getAsFloat() : 1.65f;
        int guiSize = json.has("gui_size") ? json.get("gui_size").getAsInt() : 30;
        float guiYOffset = json.has("gui_y_offset") ? json.get("gui_y_offset").getAsFloat() : 0.0625F;
        float weaponBoneRotation = json.has("weapon_bone_rotation")
                ? json.get("weapon_bone_rotation").getAsFloat() : 150f;

        if (!json.has("tame_item")) {
            throw new IllegalArgumentException("Girl '" + id + "' has no tame_item property");
        }
        Item tameItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(json.get("tame_item").getAsString()));

        JsonObject attr = json.getAsJsonObject("attributes");
        double health = attr != null && attr.has("health") ? attr.get("health").getAsDouble() : 20;
        double speed = attr != null && attr.has("speed") ? attr.get("speed").getAsDouble() : 0.2;
        double damage = attr != null && attr.has("damage") ? attr.get("damage").getAsDouble() : 2;

        List<Scene> scenes = new ArrayList<>();
        if (json.has("scenes")) {
            for (JsonElement element : json.getAsJsonArray("scenes")) {
                scenes.add(parseScene(element.getAsJsonObject()));
            }
        }

        return new CustomGirlProfile(id, name, hitboxHeight, guiSize, guiYOffset,
                weaponBoneRotation, tameItem, health, speed, damage, scenes);
    }

    private static Scene parseScene(JsonObject s) {
        String name = s.get("name").getAsString();
        int level = s.get("required_level").getAsInt();
        boolean needsStrip = s.has("needs_to_strip") && s.get("needs_to_strip").getAsBoolean();
        boolean useKeyframe = s.has("use_keyframe") && s.get("use_keyframe").getAsBoolean();
        boolean impregnation = s.has("counts_towards_impregnation")
                && s.get("counts_towards_impregnation").getAsBoolean();
        boolean hidePlayer = s.has("hide_player") && s.get("hide_player").getAsBoolean();

        String type = s.has("scene_type") ? s.get("scene_type").getAsString() : "on_player";

        return switch (type) {
            case "on_bed" -> Scene.onBed(name, level,
                    toStringList(s.getAsJsonArray("intro_anim")),
                    toStringList(s.getAsJsonArray("slow_anim")),
                    toStringList(s.getAsJsonArray("fast_anim")),
                    getString(s, "cum_anim", ""),
                    getFloat(s, "cum_threshold", 3f),
                    needsStrip, useKeyframe, impregnation,
                    getFloat(s, "bed_offset", 0f),
                    getString(s, "lay_on_bed_anim", ""),
                    getString(s, "bed_idle_anim", ""));

            case "stationary_contact" -> Scene.stationaryContact(name, level,
                    toStringList(s.getAsJsonArray("intro_anim")),
                    toStringList(s.getAsJsonArray("slow_anim")),
                    toStringList(s.getAsJsonArray("fast_anim")),
                    getString(s, "cum_anim", ""),
                    getFloat(s, "cum_threshold", 3f),
                    needsStrip, useKeyframe, impregnation,
                    getString(s, "lay_down_anim", ""),
                    getString(s, "idle_anim", ""));

            case "stationary_intro" -> Scene.stationaryIntro(name, level,
                    toStringList(s.getAsJsonArray("intro_anim")),
                    getString(s, "anim", ""),
                    getInt(s, "amount_of_loops", 1),
                    needsStrip, hidePlayer);

            case "stationary" -> Scene.stationary(name, level,
                    getString(s, "anim", ""),
                    getInt(s, "amount_of_loops", 1),
                    needsStrip, hidePlayer);

            // Also the fallback for older JSON without a scene_type.
            default -> Scene.onPlayer(name, level,
                    toStringList(s.getAsJsonArray("intro_anim")),
                    toStringList(s.getAsJsonArray("slow_anim")),
                    toStringList(s.getAsJsonArray("fast_anim")),
                    getString(s, "cum_anim", ""),
                    getFloat(s, "cum_threshold", 3f),
                    needsStrip, useKeyframe, impregnation);
        };
    }

    private static String getString(JsonObject o, String key, String fallback) {
        return o.has(key) ? o.get(key).getAsString() : fallback;
    }

    private static float getFloat(JsonObject o, String key, float fallback) {
        return o.has(key) ? o.get(key).getAsFloat() : fallback;
    }

    private static int getInt(JsonObject o, String key, int fallback) {
        return o.has(key) ? o.get(key).getAsInt() : fallback;
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
