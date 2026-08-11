package com.sandymandy.pleasurehorizons.util.variables;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public enum SceneType {
    ON_PLAYER,
    ON_BED,
    STATIONARY_CONTACT,
    STATIONARY_INTRO,
    STATIONARY;

    /**
     * A PacketCodec for encoding/decoding SceneType values over the network.
     * Uses ordinal indexing for efficiency (like vanilla enums).
     */
    public static final PacketCodec<ByteBuf, SceneType> PACKET_CODEC = PacketCodecs.indexed(
            i -> SceneType.values()[i],  // Decode: int ordinal -> enum
            SceneType::ordinal           // Encode: enum -> int ordinal
    );

    public static final Codec<SceneType> CODEC =
            Codec.INT.xmap(
                    i -> SceneType.values()[i],   // decode
                    SceneType::ordinal            // encode
            );
}
