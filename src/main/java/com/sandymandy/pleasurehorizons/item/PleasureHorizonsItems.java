package com.sandymandy.pleasurehorizons.item;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PleasureHorizonsItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, PleasureHorizons.MOD_ID);

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    public static void registerItems() {
        PleasureHorizons.LOGGER.info("Registering items for " + PleasureHorizons.MOD_NAME);
    }
}
