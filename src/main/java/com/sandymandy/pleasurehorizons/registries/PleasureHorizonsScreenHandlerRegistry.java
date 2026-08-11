package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PleasureHorizonsScreenHandlerRegistry {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, PleasureHorizons.MOD_ID);

    public static final MenuType<?> GIRL_INVENTORY_SCREEN_HANDLER = new MenuType<>((id, inv) -> null, net.minecraft.world.flag.FeatureFlags.VANILLA_SET);
    public static final MenuType<?> SETTLEMENT_HUB_SCREEN_HANDLER = new MenuType<>((id, inv) -> null, net.minecraft.world.flag.FeatureFlags.VANILLA_SET);

    public static final DeferredHolder<MenuType<?>, MenuType<?>> GIRL_INVENTORY_HOLDER =
            MENU_TYPES.register("girl_inventory_screen", () -> GIRL_INVENTORY_SCREEN_HANDLER);
    public static final DeferredHolder<MenuType<?>, MenuType<?>> SETTLEMENT_HUB_HOLDER =
            MENU_TYPES.register("settlement_hub", () -> SETTLEMENT_HUB_SCREEN_HANDLER);

    public static void register(IEventBus bus) {
        MENU_TYPES.register(bus);
    }

    public static void registerScreenHandlers() {
        PleasureHorizons.LOGGER.info("Registering screen handlers for " + PleasureHorizons.MOD_NAME);
    }
}
