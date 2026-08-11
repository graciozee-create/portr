package com.sandymandy.pleasurehorizons.util.variables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.List;

public record CustomGirlProfile(
        String id,
        String name,
        float hitboxHeight,
        int guiSize,
        float guiYOffset,
        float weaponBoneRotation,
        Item tameItem,
        double maxHealth,
        double movementSpeed,
        double attackDamage,
        List<Scene> scenes
) {
    public static final Codec<CustomGirlProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(CustomGirlProfile::id),
            Codec.STRING.fieldOf("name").forGetter(CustomGirlProfile::name),
            Codec.FLOAT.fieldOf("hitboxHeight").forGetter(CustomGirlProfile::hitboxHeight),
            Codec.INT.fieldOf("guiSize").forGetter(CustomGirlProfile::guiSize),
            Codec.FLOAT.fieldOf("guiYOffset").forGetter(CustomGirlProfile::guiYOffset),
            Codec.FLOAT.fieldOf("weaponBoneRotation").forGetter(CustomGirlProfile::weaponBoneRotation),
            Registries.ITEM.getCodec().fieldOf("tameItem").forGetter(CustomGirlProfile::tameItem),
            Codec.DOUBLE.fieldOf("maxHealth").forGetter(CustomGirlProfile::maxHealth),
            Codec.DOUBLE.fieldOf("movementSpeed").forGetter(CustomGirlProfile::movementSpeed),
            Codec.DOUBLE.fieldOf("attackDamage").forGetter(CustomGirlProfile::attackDamage),
            Scene.CODEC.listOf().fieldOf("scenes").forGetter(CustomGirlProfile::scenes)
    ).apply(instance, CustomGirlProfile::new));

    public static final CustomGirlProfile DEFAULT = new CustomGirlProfile(
            "default",
            "Default Custom Girl",
            1.65f,
            30,                   // gui size
            0.0625F,                // gui offset
            150f,                 // weapon bone rotation
            Items.APPLE,          // tame item
            20.0,                 // health
            0.20,                 // speed
            2.0,                  // damage
            List.of()             // scenes
    );
}
