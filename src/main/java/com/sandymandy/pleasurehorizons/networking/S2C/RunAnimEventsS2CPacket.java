package com.sandymandy.pleasurehorizons.networking.S2C;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RunAnimEventsS2CPacket() implements CustomPacketPayload {
    public static final Type<RunAnimEventsS2CPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "runanimeventss2cpacket"));
    public static final StreamCodec<ByteBuf, RunAnimEventsS2CPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> {},
        buf -> new RunAnimEventsS2CPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {});
    }
}
