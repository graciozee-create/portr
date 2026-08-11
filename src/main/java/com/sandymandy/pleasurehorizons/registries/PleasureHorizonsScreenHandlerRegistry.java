package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PleasureHorizonsScreenHandlerRegistry {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, PleasureHorizons.MOD_ID);

    public static void register(IEventBus bus) {
        MENU_TYPES.register(bus);
    }

    public static void registerScreenHandlers() {
        PleasureHorizons.LOGGER.info("Registering screen handlers for " + PleasureHorizons.MOD_NAME);
    }
}
