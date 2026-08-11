package com.sandymandy.pleasurehorizons.networking.S2C;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayAttackAnimationS2CPacket() implements CustomPacketPayload {
    public static final Type<PlayAttackAnimationS2CPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "playattackanimations2cpacket"));
    public static final StreamCodec<ByteBuf, PlayAttackAnimationS2CPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> {},
        buf -> new PlayAttackAnimationS2CPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {});
    }
}
