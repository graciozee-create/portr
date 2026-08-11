package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RegisterCustomGirlSoundC2SPacket(String soundId, String soundEvent) implements CustomPacketPayload {
    public static final Type<RegisterCustomGirlSoundC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "registercustomgirlsound"));
    public static final StreamCodec<ByteBuf, RegisterCustomGirlSoundC2SPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> { buf.writeUtf(soundId); buf.writeUtf(soundEvent); },
        buf -> new RegisterCustomGirlSoundC2SPacket(buf.readUtf(), buf.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Server-side handling for RegisterCustomGirlSoundC2SPacket
        });
    }
}
