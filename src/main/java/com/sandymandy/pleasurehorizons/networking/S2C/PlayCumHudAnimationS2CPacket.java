package com.sandymandy.pleasurehorizons.networking.S2C;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayCumHudAnimationS2CPacket() implements CustomPacketPayload {
    public static final Type<PlayCumHudAnimationS2CPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "playcumhudanimations2cpacket"));
    public static final StreamCodec<ByteBuf, PlayCumHudAnimationS2CPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> {},
        buf -> new PlayCumHudAnimationS2CPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Reflective hop so this common class never loads client-only code on a server.
            try {
                Class.forName("com.sandymandy.pleasurehorizons.client.networking.ClientPacketHandlers")
                        .getMethod("handleCumHudAnimation")
                        .invoke(null);
            } catch (Exception e) {
                PleasureHorizons.LOGGER.debug("Cum HUD packet received outside a client");
            }
        });
    }
}
