package com.sandymandy.pleasurehorizons.util.variables;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public enum ScenePhase {
    NONE,
    DIALOG,
    LAYING_DOWN,
    BED_IDLE,
    INTRO,
    MOVING,
    HAVING_SEX,
    CUM,
    STATIONARY_INTRO,
    STATIONARY;

    /**
     * A PacketCodec for encoding/decoding ScenePhase values over the network.
     * Uses ordinal indexing for efficiency (like vanilla enums).
     */
    public static final PacketCodec<ByteBuf, ScenePhase> PACKET_CODEC = PacketCodecs.indexed(
            i -> ScenePhase.values()[i],  // Decode: int ordinal -> enum
            ScenePhase::ordinal           // Encode: enum -> int ordinal
    );

    public static final Codec<ScenePhase> CODEC =
            Codec.INT.xmap(
                    i -> ScenePhase.values()[i],   // decode
                    ScenePhase::ordinal            // encode
            );
}
