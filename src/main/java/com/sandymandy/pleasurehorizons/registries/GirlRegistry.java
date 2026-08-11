package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GirlRegistry {
    public static final DeferredRegister<EntityType<?>> GIRLS =
            DeferredRegister.create(Registries.ENTITY_TYPE, PleasureHorizons.MOD_ID);

    public static void register(IEventBus bus) {
        GIRLS.register(bus);
    }

    public static void registerGirls() {
        PleasureHorizons.LOGGER.info("Registering girls for " + PleasureHorizons.MOD_NAME);
    }
}
