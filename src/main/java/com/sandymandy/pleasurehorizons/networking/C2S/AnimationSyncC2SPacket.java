package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AnimationSyncC2SPacket(int entityId, String animationState, boolean loopState, boolean holdState) implements CustomPacketPayload {
    public static final Type<AnimationSyncC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "animationsync"));
    public static final StreamCodec<ByteBuf, AnimationSyncC2SPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> { buf.writeVarInt(entityId); buf.writeUtf(animationState); buf.writeBoolean(loopState); buf.writeBoolean(holdState); },
        buf -> new AnimationSyncC2SPacket(buf.readVarInt(), buf.readUtf(), buf.readBoolean(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Server-side handling for AnimationSyncC2SPacket
        });
    }
}
