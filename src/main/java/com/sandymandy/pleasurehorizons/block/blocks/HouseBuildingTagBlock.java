package com.sandymandy.pleasurehorizons.block.blocks;

import com.mojang.serialization.MapCodec;
import com.sandymandy.pleasurehorizons.block.entity.entities.AbstractBuildingTagBlockEntity;
import com.sandymandy.pleasurehorizons.settlement.building.BuildingType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HouseBuildingTagBlock extends AbstractBuildingTagBlock {
    public static final MapCodec<HouseBuildingTagBlock> CODEC = simpleCodec(HouseBuildingTagBlock::new);

    public HouseBuildingTagBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AbstractBuildingTagBlockEntity(pos, state, BuildingType.HOUSE);
    }
}
