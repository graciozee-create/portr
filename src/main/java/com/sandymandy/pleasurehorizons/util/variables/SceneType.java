package com.sandymandy.pleasurehorizons.util.variables;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum SceneType {
    ON_PLAYER,
    ON_BED,
    STATIONARY_CONTACT,
    STATIONARY_INTRO,
    STATIONARY;

    public static final StreamCodec<ByteBuf, SceneType> PACKET_CODEC = ByteBufCodecs.BYTE.map(
            b -> SceneType.values()[b],
            v -> (byte) v.ordinal()
    );

    public static final Codec<SceneType> CODEC =
            Codec.INT.xmap(
                    i -> SceneType.values()[i],
                    SceneType::ordinal
            );
}
