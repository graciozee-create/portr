package com.sandymandy.pleasurehorizons.networking.codec;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ExtraPacketCodecs {
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockState> BLOCK_STATE_PACKET_CODEC = StreamCodec.ofStatic(
            (buf, state) -> buf.writeVarInt(Block.STATE_IDS.getRawId(state)),
            buf -> Block.STATE_IDS.get(buf.readVarInt())
    );
}
