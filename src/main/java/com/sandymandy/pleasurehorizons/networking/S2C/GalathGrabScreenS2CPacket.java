package com.sandymandy.pleasurehorizons.networking.S2C;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server→Client notification for the Galath combat grab.
 *
 * <p>{@code grabActive=true} opens the escape mash screen, {@code false} closes it.
 * Like every S2C payload this class avoids {@code net.minecraft.client} and delegates to
 * {@code ClientPacketHandlers} reflectively.</p>
 */
public record GalathGrabScreenS2CPacket(int entityId, boolean grabActive) implements CustomPacketPayload {
    public static final Type<GalathGrabScreenS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "galathgrabscreens2cpacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GalathGrabScreenS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, GalathGrabScreenS2CPacket::entityId,
                    ByteBufCodecs.BOOL, GalathGrabScreenS2CPacket::grabActive,
                    GalathGrabScreenS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                Class<?> handler = Class.forName("com.sandymandy.pleasurehorizons.client.networking.ClientPacketHandlers");
                handler.getMethod("handleGalathGrabScreen", int.class, boolean.class)
                        .invoke(null, this.entityId(), this.grabActive());
            } catch (Exception e) {
                PleasureHorizons.LOGGER.debug("GalathGrabScreen packet on server");
            }
        });
    }
}
