package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StopSceneOnServerC2SPacket(int entityId) implements CustomPacketPayload {
    public static final Type<StopSceneOnServerC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "stopsceneonserver"));
    public static final StreamCodec<ByteBuf, StopSceneOnServerC2SPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> { buf.writeVarInt(entityId); },
        buf -> new StopSceneOnServerC2SPacket(buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Server-side handling for StopSceneOnServerC2SPacket
        });
    }
}
