package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ScenePhaseSyncC2SPacket(int entityId, int phase) implements CustomPacketPayload {
    public static final Type<ScenePhaseSyncC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "scenephasesync"));
    public static final StreamCodec<ByteBuf, ScenePhaseSyncC2SPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> { buf.writeVarInt(entityId); buf.writeVarInt(phase); },
        buf -> new ScenePhaseSyncC2SPacket(buf.readVarInt(), buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Server-side handling for ScenePhaseSyncC2SPacket
        });
    }
}
