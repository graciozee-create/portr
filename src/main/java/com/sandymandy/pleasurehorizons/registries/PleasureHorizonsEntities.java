package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PleasureHorizonsEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, PleasureHorizons.MOD_ID);

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
        // Actual entities are in GirlRegistry
        GirlRegistry.register(bus);
    }

    public static void registerEntities() {
        PleasureHorizons.LOGGER.info("Registering entities for " + PleasureHorizons.MOD_NAME);
    }
}
