package com.sandymandy.pleasurehorizons.item;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.block.PleasureHorizonsBlocks;
import com.sandymandy.pleasurehorizons.item.items.SettlementRecruitContract;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
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

    // Blocks were registered without matching BlockItems, so they could never be
    // obtained or shown in any creative tab.
    public static final DeferredHolder<Item, Item> SETTLEMENT_HUB_ITEM = ITEMS.register("settlement_hub",
            () -> new BlockItem(PleasureHorizonsBlocks.SETTLEMENT_HUB.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> HOUSE_TAG_ITEM = ITEMS.register("house_tag",
            () -> new BlockItem(PleasureHorizonsBlocks.HOUSE_BUILDING_TAG.get(), new Item.Properties()));

    public static final DeferredHolder<Item, Item> CARVED_GIRL_PUMPKIN_ITEM = ITEMS.register("carved_girl_pumpkin",
            () -> new BlockItem(PleasureHorizonsBlocks.CARVED_GIRL_PUMPKIN.get(), new Item.Properties()));

    // Jenny Mod special items (ported from Mine335/JennysMod1.21.1)
    public static final DeferredHolder<Item, Item> ALLIE_LAMP = ITEMS.register("allies_lamp",
            () -> new com.sandymandy.pleasurehorizons.item.items.AllieLampItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GALATH_COIN = ITEMS.register("galath_coin",
            () -> new com.sandymandy.pleasurehorizons.item.items.GalathCoinItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> DRAGON_STAFF = ITEMS.register("dragon_staff",
            () -> new com.sandymandy.pleasurehorizons.item.items.DragonStaffItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> TRIBE_EGG = ITEMS.register("tribe_egg",
            () -> new com.sandymandy.pleasurehorizons.item.items.TribeEggItem(new Item.Properties()));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
