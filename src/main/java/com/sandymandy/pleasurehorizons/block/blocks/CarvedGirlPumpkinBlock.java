package com.sandymandy.pleasurehorizons.block.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.CarvedPumpkinBlock;

public class CarvedGirlPumpkinBlock extends CarvedPumpkinBlock {
    public static final MapCodec<CarvedGirlPumpkinBlock> CODEC = simpleCodec(CarvedGirlPumpkinBlock::new);

    public CarvedGirlPumpkinBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends CarvedPumpkinBlock> codec() {
        return CODEC;
    }
}
