package com.sandymandy.pleasurehorizons.entity;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class PleasureHorizonsEntities {

    private static final List<Runnable> ATTRIBUTE_REGISTRATIONS = new ArrayList<>();
    private static final List<Item> AUTO_SPAWN_EGGS = new ArrayList<>();
    private static final List<EntityType<? extends MobEntity>> GIRLS = new ArrayList<>();

    /**
     * Registers a girl entity using the entity's own getGirlID() method for ID generation.
     */
    public static <T extends GirlSceneEntity> EntityType<T> registerGirl(
            String id,
            BiFunction<EntityType<T>, net.minecraft.world.World, T> factory,
            float width,
            float height,
            Supplier<DefaultAttributeContainer.Builder> attributes
    ) {
        return registerGirl(id,factory, width, height, true, attributes);
    }

    public static <T extends GirlSceneEntity> EntityType<T> registerGirl(
            String id,
            BiFunction<EntityType<T>, net.minecraft.world.World, T> factory,
            float width,
            float height,
            boolean createSpawnEgg,
            Supplier<DefaultAttributeContainer.Builder> attributes
    ) {

        try {
            // Create a temporary instance to get the girl ID

            Identifier identifier = Identifier.of(PleasureHorizons.MOD_ID, id);
            RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, identifier);

            EntityType<T> type = Registry.register(
                    Registries.ENTITY_TYPE,
                    identifier,
                    EntityType.Builder.create(factory::apply, SpawnGroup.CREATURE)
                            .dimensions(width, height)
                            .build(key)
            );

            ATTRIBUTE_REGISTRATIONS.add(() ->
                    net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(type, attributes.get())
            );

            if(createSpawnEgg) {
                // Auto spawn egg
                Identifier eggId = Identifier.of(PleasureHorizons.MOD_ID, id + "_spawn_egg");
                Item egg = Registry.register(Registries.ITEM, eggId,
                        new SpawnEggItem(type, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, eggId))));
                AUTO_SPAWN_EGGS.add(egg);
            }

            GIRLS.add(type);
            return type;
        } catch (Exception e) {
            throw new RuntimeException("Failed to register girl entity: " + id, e);
        }
    }

    public static void registerAttributes() {
        ATTRIBUTE_REGISTRATIONS.forEach(Runnable::run);
    }

    // Helpers for item group icon or dynamic access
    public static Item getFirstSpawnEgg() {
        return AUTO_SPAWN_EGGS.isEmpty() ? null : AUTO_SPAWN_EGGS.get(0);
    }

    public static List<EntityType<? extends MobEntity>> getAllGirls() {
        return List.copyOf(GIRLS);
    }

    public static List<Item> getAllSpawnEggs() {
        return List.copyOf(AUTO_SPAWN_EGGS);
    }

}
