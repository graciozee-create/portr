package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RegisterCustomGirlMessageC2SPacket(String messageId, String soundEvent) implements CustomPacketPayload {
    public static final Type<RegisterCustomGirlMessageC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "registercustomgirlmessage"));
    public static final StreamCodec<ByteBuf, RegisterCustomGirlMessageC2SPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> { buf.writeUtf(messageId); buf.writeUtf(soundEvent); },
        buf -> new RegisterCustomGirlMessageC2SPacket(buf.readUtf(), buf.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Server-side handling for RegisterCustomGirlMessageC2SPacket
        });
    }
}
