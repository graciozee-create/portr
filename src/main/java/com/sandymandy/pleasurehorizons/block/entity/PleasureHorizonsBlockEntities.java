package com.sandymandy.pleasurehorizons.block.entity;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PleasureHorizonsBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PleasureHorizons.MOD_ID);

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }

    public static void registerBlockEntities() {
        PleasureHorizons.LOGGER.info("Registering block entities for " + PleasureHorizons.MOD_NAME);
    }
}
