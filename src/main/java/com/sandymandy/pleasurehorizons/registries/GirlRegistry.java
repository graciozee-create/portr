package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.girls.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = PleasureHorizons.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class GirlRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, PleasureHorizons.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<LucyEntity>> LUCY = ENTITY_TYPES.register("lucy",
            () -> EntityType.Builder.<LucyEntity>of(LucyEntity::new, MobCategory.CREATURE)
                    .sized(0.5f, 1.95f)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "lucy"))));

    public static final DeferredHolder<EntityType<?>, EntityType<MikaEntity>> MIKA = ENTITY_TYPES.register("mika",
            () -> EntityType.Builder.<MikaEntity>of(MikaEntity::new, MobCategory.CREATURE)
                    .sized(0.5f, 1.95f)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "mika"))));

    public static final DeferredHolder<EntityType<?>, EntityType<MomoEntity>> MOMO = ENTITY_TYPES.register("momo",
            () -> EntityType.Builder.<MomoEntity>of(MomoEntity::new, MobCategory.CREATURE)
                    .sized(0.5f, 1.65f)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "momo"))));

    public static final DeferredHolder<EntityType<?>, EntityType<SlimeEntity>> SLIME = ENTITY_TYPES.register("slime",
            () -> EntityType.Builder.<SlimeEntity>of(SlimeEntity::new, MobCategory.CREATURE)
                    .sized(0.5f, 1.95f)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "slime"))));

    public static final DeferredHolder<EntityType<?>, EntityType<KoboldEntity>> KOBOLD = ENTITY_TYPES.register("kobold",
            () -> EntityType.Builder.<KoboldEntity>of(KoboldEntity::new, MobCategory.CREATURE)
                    .sized(0.5f, 1.75f)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "kobold"))));

    public static final DeferredHolder<EntityType<?>, EntityType<CoppieEntity>> COPPIE = ENTITY_TYPES.register("coppie",
            () -> EntityType.Builder.<CoppieEntity>of(CoppieEntity::new, MobCategory.CREATURE)
                    .sized(0.5f, 1.35f)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "coppie"))));

    public static final DeferredHolder<EntityType<?>, EntityType<CustomGirlEntity>> CUSTOM_GIRL = ENTITY_TYPES.register("custom_girl",
            () -> EntityType.Builder.<CustomGirlEntity>of(CustomGirlEntity::new, MobCategory.CREATURE)
                    .sized(0.5f, 1.95f)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "custom_girl"))));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }

    public static void registerGirls() {
        PleasureHorizons.LOGGER.info("Registering girls for " + PleasureHorizons.MOD_NAME);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(LUCY.get(), LucyEntity.createAttributes().build());
        event.put(MIKA.get(), MikaEntity.createAttributes().build());
        event.put(MOMO.get(), MomoEntity.createAttributes().build());
        event.put(SLIME.get(), SlimeEntity.createAttributes().build());
        event.put(KOBOLD.get(), KoboldEntity.createAttributes().build());
        event.put(COPPIE.get(), CoppieEntity.createAttributes().build());
        event.put(CUSTOM_GIRL.get(), CustomGirlEntity.createAttributes().build());
    }
}
