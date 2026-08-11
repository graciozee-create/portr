package com.sandymandy.pleasurehorizons.block;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.block.blocks.CarvedGirlPumpkinBlock;
import com.sandymandy.pleasurehorizons.block.blocks.HouseBuildingTagBlock;
import com.sandymandy.pleasurehorizons.block.blocks.SettlementHubBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PleasureHorizonsBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, PleasureHorizons.MOD_ID);

    public static final DeferredHolder<Block, Block> SETTLEMENT_HUB = BLOCKS.register("settlement_hub",
            () -> new SettlementHubBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f, 1200.0F)
                    .sound(SoundType.LODESTONE)
                    .requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, Block> HOUSE_BUILDING_TAG = BLOCKS.register("house_tag",
            () -> new HouseBuildingTagBlock(BlockBehaviour.Properties.of()
                    .strength(1.0F, 100.0F)
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredHolder<Block, Block> CARVED_GIRL_PUMPKIN = BLOCKS.register("carved_girl_pumpkin",
            () -> new CarvedGirlPumpkinBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

    public static void registerBlocks() {
        PleasureHorizons.LOGGER.info("Registering blocks for " + PleasureHorizons.MOD_NAME);
    }
}
