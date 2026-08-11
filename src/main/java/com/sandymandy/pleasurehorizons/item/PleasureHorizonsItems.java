package com.sandymandy.pleasurehorizons.item;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.item.items.SettlementRecruitContract;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PleasureHorizonsItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, PleasureHorizons.MOD_ID);

    public static final DeferredHolder<Item, Item> SETTLEMENT_RECRUITMENT_TOKEN = ITEMS.register("settlement_recruit_contract",
            () -> new SettlementRecruitContract(new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<Item, Item> MILK_JUG_EMPTY = ITEMS.register("milk_jug_empty",
            () -> new Item(new Item.Properties().stacksTo(4)));

    public static final DeferredHolder<Item, Item> MILK_JUG_HALF = ITEMS.register("milk_jug_half",
            () -> new Item(new Item.Properties().stacksTo(1)
                    .food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationModifier(0.5f)
                            .effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 900, 1), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.JUMP, 900, 1), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 900, 1), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 900, 1), 1.0f)
                            .alwaysEdible()
                            .build())
                    .craftRemainder(MILK_JUG_EMPTY.get())));

    public static final DeferredHolder<Item, Item> MILK_JUG_FULL = ITEMS.register("milk_jug_full",
            () -> new Item(new Item.Properties().stacksTo(1)
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(0.8f)
                            .effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 900, 1), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.JUMP, 900, 1), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 900, 1), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 900, 1), 1.0f)
                            .alwaysEdible()
                            .build())
                    .craftRemainder(MILK_JUG_HALF.get())));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    public static void registerItems() {
        PleasureHorizons.LOGGER.info("Registering items for " + PleasureHorizons.MOD_NAME);
    }
}
