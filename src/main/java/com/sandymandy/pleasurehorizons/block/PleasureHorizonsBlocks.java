package com.sandymandy.pleasurehorizons.block;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PleasureHorizonsBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, PleasureHorizons.MOD_ID);

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

    public static void registerBlocks() {
        PleasureHorizons.LOGGER.info("Registering blocks for " + PleasureHorizons.MOD_NAME);
    }
}
