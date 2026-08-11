package com.sandymandy.pleasurehorizons.block.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CarvedGirlPumpkinBlock extends CarvedPumpkinBlock {
    public static final MapCodec<CarvedGirlPumpkinBlock> CODEC = simpleCodec(CarvedGirlPumpkinBlock::new);

    public CarvedGirlPumpkinBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends CarvedPumpkinBlock> codec() {
        return CODEC;
    }
}
