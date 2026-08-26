package com.sandymandy.pleasurehorizons.item;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Creative tab holding every item the mod adds, including the spawn eggs. */
public class PleasureHorizonsItemGroups {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PleasureHorizons.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.pleasurehorizons.main"))
                    .icon(() -> new ItemStack(PleasureHorizonsSpawnEggs.LUCY_SPAWN_EGG.get()))
                    .displayItems((params, output) -> {
                        PleasureHorizonsSpawnEggs.ALL_EGGS.forEach(egg -> output.accept(egg.get()));
                        output.accept(PleasureHorizonsItems.SETTLEMENT_RECRUITMENT_TOKEN.get());
                        output.accept(PleasureHorizonsItems.MILK_JUG_EMPTY.get());
                        output.accept(PleasureHorizonsItems.MILK_JUG_HALF.get());
                        output.accept(PleasureHorizonsItems.MILK_JUG_FULL.get());
                        output.accept(PleasureHorizonsItems.SETTLEMENT_HUB_ITEM.get());
                        output.accept(PleasureHorizonsItems.HOUSE_TAG_ITEM.get());
                        output.accept(PleasureHorizonsItems.CARVED_GIRL_PUMPKIN_ITEM.get());
                        // Jenny Mod special items
                        output.accept(PleasureHorizonsItems.ALLIE_LAMP.get());
                        output.accept(PleasureHorizonsItems.GALATH_COIN.get());
                        output.accept(PleasureHorizonsItems.DRAGON_STAFF.get());
                        output.accept(PleasureHorizonsItems.TRIBE_EGG.get());
                        // Gadget items
                        output.accept(PleasureHorizonsItems.GUIDE_BOOK.get());
                        output.accept(PleasureHorizonsItems.MEMORY_CRYSTAL.get());
                        output.accept(PleasureHorizonsItems.SUMMONING_WHISTLE.get());
                        output.accept(PleasureHorizonsItems.GIRL_WAND.get());
                        output.accept(PleasureHorizonsItems.HORNY_POTION.get());
                        output.accept(PleasureHorizonsItems.HEALING_CHARM.get());
                        output.accept(PleasureHorizonsItems.BOND_BRACELET.get());
                        // Gift items
                        output.accept(PleasureHorizonsItems.GIFT_RED_ROSE.get());
                        output.accept(PleasureHorizonsItems.GIFT_CHOCOLATE_BOX.get());
                        output.accept(PleasureHorizonsItems.GIFT_TEDDY_BEAR.get());
                        output.accept(PleasureHorizonsItems.GIFT_LOVE_LETTER.get());
                        output.accept(PleasureHorizonsItems.GIFT_DIAMOND_RING.get());
                        output.accept(PleasureHorizonsItems.GIFT_COPPER_GEAR.get());
                        output.accept(PleasureHorizonsItems.GIFT_ENCHANTED_QUILL.get());
                        output.accept(PleasureHorizonsItems.GIFT_MOONLIGHT_LILY.get());
                        output.accept(PleasureHorizonsItems.GIFT_ANCIENT_COIN.get());
                        output.accept(PleasureHorizonsItems.GIFT_GOLDEN_HONEYCOMB.get());
                        output.accept(PleasureHorizonsItems.GIFT_SILVER_BELL.get());
                        output.accept(PleasureHorizonsItems.GIFT_MYSTIC_HERB.get());
                        output.accept(PleasureHorizonsItems.GIFT_DRAGON_SCALE.get());
                        output.accept(PleasureHorizonsItems.GIFT_CRYSTAL_SLIME.get());
                    })
                    .build());

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }
}
