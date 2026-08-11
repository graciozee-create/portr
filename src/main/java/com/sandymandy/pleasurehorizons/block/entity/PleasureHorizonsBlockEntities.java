package com.sandymandy.pleasurehorizons.block.entity;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.block.PleasureHorizonsBlocks;
import com.sandymandy.pleasurehorizons.block.entity.entities.AbstractBuildingTagBlockEntity;
import com.sandymandy.pleasurehorizons.block.entity.entities.SettlementHubBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PleasureHorizonsBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PleasureHorizons.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SettlementHubBlockEntity>> SETTLEMENT_HUB_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("settlement_hub", () ->
                    BlockEntityType.Builder.of(SettlementHubBlockEntity::new,
                            PleasureHorizonsBlocks.SETTLEMENT_HUB.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AbstractBuildingTagBlockEntity>> BUILDING_TAG_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("building_tag", () ->
                    BlockEntityType.Builder.of(
                            (pos, state) -> new AbstractBuildingTagBlockEntity(pos, state, null),
                            PleasureHorizonsBlocks.HOUSE_BUILDING_TAG.get()
                    ).build(null));

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }

    public static void registerBlockEntities() {
        PleasureHorizons.LOGGER.info("Registering block entities for " + PleasureHorizons.MOD_NAME);
    }
}
