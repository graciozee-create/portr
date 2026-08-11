package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RegisterCustomGirlRandomSoundC2SPacket(String soundId, String soundEvent) implements CustomPacketPayload {
    public static final Type<RegisterCustomGirlRandomSoundC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "registercustomgirlrandomsound"));
    public static final StreamCodec<ByteBuf, RegisterCustomGirlRandomSoundC2SPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> { buf.writeUtf(soundId); buf.writeUtf(soundEvent); },
        buf -> new RegisterCustomGirlRandomSoundC2SPacket(buf.readUtf(), buf.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Server-side handling for RegisterCustomGirlRandomSoundC2SPacket
        });
    }
}
