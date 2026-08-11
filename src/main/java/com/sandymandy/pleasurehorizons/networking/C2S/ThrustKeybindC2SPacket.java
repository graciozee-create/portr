package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ThrustKeybindC2SPacket(boolean held) implements CustomPacketPayload {
    public static final Type<ThrustKeybindC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "thrustkeybind"));
    public static final StreamCodec<ByteBuf, ThrustKeybindC2SPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> { buf.writeBoolean(held); },
        buf -> new ThrustKeybindC2SPacket(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Server-side handling for ThrustKeybindC2SPacket
        });
    }
}
