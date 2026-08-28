package com.sandymandy.pleasurehorizons.item;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.block.PleasureHorizonsBlocks;
import com.sandymandy.pleasurehorizons.item.items.BondBraceletItem;
import com.sandymandy.pleasurehorizons.item.items.GiftItem;
import com.sandymandy.pleasurehorizons.item.items.GirlWandItem;
import com.sandymandy.pleasurehorizons.item.items.GuideBookItem;
import com.sandymandy.pleasurehorizons.item.items.HealingCharmItem;
import com.sandymandy.pleasurehorizons.item.items.HornyPotionItem;
import com.sandymandy.pleasurehorizons.item.items.MemoryCrystalItem;
import com.sandymandy.pleasurehorizons.item.items.SettlementRecruitContract;
import com.sandymandy.pleasurehorizons.item.items.SummoningWhistleItem;
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

    // ---- Gadget items (rechenz/Jenny-mod-1.21.1 port) -----------------------
    public static final DeferredHolder<Item, Item> GUIDE_BOOK = ITEMS.register("guide_book",
            () -> new GuideBookItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> MEMORY_CRYSTAL = ITEMS.register("memory_crystal",
            () -> new MemoryCrystalItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> SUMMONING_WHISTLE = ITEMS.register("summoning_whistle",
            () -> new SummoningWhistleItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> GIRL_WAND = ITEMS.register("girl_wand",
            () -> new GirlWandItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> HORNY_POTION = ITEMS.register("horny_potion",
            () -> new HornyPotionItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> HEALING_CHARM = ITEMS.register("healing_charm",
            () -> new HealingCharmItem(new Item.Properties()));

    public static final DeferredHolder<Item, Item> BOND_BRACELET = ITEMS.register("bond_bracelet",
            () -> new BondBraceletItem(new Item.Properties()));

    // ---- Gift items ----------
    public static final DeferredHolder<Item, Item> GIFT_RED_ROSE = registerGift("gift_red_rose",
            "tooltip.pleasurehorizons.gift.red_rose", 5);
    public static final DeferredHolder<Item, Item> GIFT_CHOCOLATE_BOX = registerGift("gift_chocolate_box",
            "tooltip.pleasurehorizons.gift.chocolate_box", 6);
    public static final DeferredHolder<Item, Item> GIFT_TEDDY_BEAR = registerGift("gift_teddy_bear",
            "tooltip.pleasurehorizons.gift.teddy_bear", 8);
    public static final DeferredHolder<Item, Item> GIFT_LOVE_LETTER = registerGift("gift_love_letter",
            "tooltip.pleasurehorizons.gift.love_letter", 10);
    public static final DeferredHolder<Item, Item> GIFT_DIAMOND_RING = registerGift("gift_diamond_ring",
            "tooltip.pleasurehorizons.gift.diamond_ring", 18);
    public static final DeferredHolder<Item, Item> GIFT_COPPER_GEAR = registerGift("gift_copper_gear",
            "tooltip.pleasurehorizons.gift.copper_gear", 5);
    public static final DeferredHolder<Item, Item> GIFT_ENCHANTED_QUILL = registerGift("gift_enchanted_quill",
            "tooltip.pleasurehorizons.gift.enchanted_quill", 6);
    public static final DeferredHolder<Item, Item> GIFT_MOONLIGHT_LILY = registerGift("gift_moonlight_lily",
            "tooltip.pleasurehorizons.gift.moonlight_lily", 5);
    public static final DeferredHolder<Item, Item> GIFT_ANCIENT_COIN = registerGift("gift_ancient_coin",
            "tooltip.pleasurehorizons.gift.ancient_coin", 8);
    public static final DeferredHolder<Item, Item> GIFT_GOLDEN_HONEYCOMB = registerGift("gift_golden_honeycomb",
            "tooltip.pleasurehorizons.gift.golden_honeycomb", 5);
    public static final DeferredHolder<Item, Item> GIFT_SILVER_BELL = registerGift("gift_silver_bell",
            "tooltip.pleasurehorizons.gift.silver_bell", 6);
    public static final DeferredHolder<Item, Item> GIFT_MYSTIC_HERB = registerGift("gift_mystic_herb",
            "tooltip.pleasurehorizons.gift.mystic_herb", 5);
    public static final DeferredHolder<Item, Item> GIFT_DRAGON_SCALE = registerGift("gift_dragon_scale",
            "tooltip.pleasurehorizons.gift.dragon_scale", 10);
    public static final DeferredHolder<Item, Item> GIFT_CRYSTAL_SLIME = registerGift("gift_crystal_slime",
            "tooltip.pleasurehorizons.gift.crystal_slime", 5);

    private static DeferredHolder<Item, Item> registerGift(String name, String tooltip, int value) {
        return ITEMS.register(name,
                () -> new GiftItem(new Item.Properties().stacksTo(16), tooltip, value));
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
