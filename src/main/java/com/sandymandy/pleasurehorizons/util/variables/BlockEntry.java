package com.sandymandy.pleasurehorizons.util.variables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public record BlockEntry(BlockPos pos, BlockState state) {
    public static final Codec<BlockEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(BlockEntry::pos),
            BlockState.CODEC.fieldOf("state").forGetter(BlockEntry::state)
    ).apply(inst, BlockEntry::new));
}
