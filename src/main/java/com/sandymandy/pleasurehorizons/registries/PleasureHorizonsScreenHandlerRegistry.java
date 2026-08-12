package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.screen.GirlInventoryScreenHandler;
import com.sandymandy.pleasurehorizons.screen.SettlementHubScreenHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PleasureHorizonsScreenHandlerRegistry {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, PleasureHorizons.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<GirlInventoryScreenHandler>> GIRL_INVENTORY_HOLDER =
            MENU_TYPES.register("girl_inventory_screen", () -> IMenuTypeExtension.create((windowId, inv, buf) ->
                    new GirlInventoryScreenHandler(windowId, inv, buf.readVarInt())));

    public static final DeferredHolder<MenuType<?>, MenuType<SettlementHubScreenHandler>> SETTLEMENT_HUB_HOLDER =
            MENU_TYPES.register("settlement_hub", () -> IMenuTypeExtension.create((windowId, inv, buf) ->
                    new SettlementHubScreenHandler(windowId, inv,
                            com.sandymandy.pleasurehorizons.settlement.SettlementSnapshot.STREAM_CODEC.decode(buf))));

    public static void register(IEventBus bus) {
        MENU_TYPES.register(bus);
    }

    public static void registerScreenHandlers() {
        PleasureHorizons.LOGGER.info("Registering screen handlers for " + PleasureHorizons.MOD_NAME);
    }
}
