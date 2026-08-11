package com.sandymandy.pleasurehorizons.util.variables;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

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

    public static final StreamCodec<ByteBuf, ScenePhase> PACKET_CODEC = ByteBufCodecs.BYTE.map(
            b -> ScenePhase.values()[b],
            v -> (byte) v.ordinal()
    );

    public static final Codec<ScenePhase> CODEC =
            Codec.INT.xmap(
                    i -> ScenePhase.values()[i],
                    ScenePhase::ordinal
            );
}
