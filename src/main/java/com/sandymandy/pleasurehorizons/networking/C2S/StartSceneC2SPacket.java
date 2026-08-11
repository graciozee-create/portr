package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StartSceneC2SPacket() implements CustomPacketPayload {
    public static final Type<StartSceneC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "startscenec2spacket"));
    public static final StreamCodec<ByteBuf, StartSceneC2SPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> {},
        buf -> new StartSceneC2SPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {});
    }
}
