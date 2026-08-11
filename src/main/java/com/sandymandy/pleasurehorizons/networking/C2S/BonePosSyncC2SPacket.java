package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BonePosSyncC2SPacket(int entityId, double x, double y, double z) implements CustomPacketPayload {
    public static final Type<BonePosSyncC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "bone_pos_sync"));
    public static final StreamCodec<ByteBuf, BonePosSyncC2SPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> {
            buf.writeVarInt(packet.entityId);
            buf.writeDouble(packet.x);
            buf.writeDouble(packet.y);
            buf.writeDouble(packet.z);
        },
        buf -> new BonePosSyncC2SPacket(buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readDouble())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Server-side handling for BonePosSyncC2SPacket
        });
    }
}
