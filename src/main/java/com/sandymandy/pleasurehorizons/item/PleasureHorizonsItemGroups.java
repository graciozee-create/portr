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
                    })
                    .build());

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }
}
