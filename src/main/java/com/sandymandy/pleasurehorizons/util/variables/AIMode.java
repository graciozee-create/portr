package com.sandymandy.pleasurehorizons.util.variables;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum AIMode {
    SETTLEMENT,
    WILDERNESS,
    SCENE;

    public static final StreamCodec<ByteBuf, AIMode> PACKET_CODEC = ByteBufCodecs.BYTE.map(
            b -> AIMode.values()[b],
            v -> (byte) v.ordinal()
    );
}
