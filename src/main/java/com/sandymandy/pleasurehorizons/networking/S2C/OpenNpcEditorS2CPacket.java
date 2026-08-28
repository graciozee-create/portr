package com.sandymandy.pleasurehorizons.networking.S2C;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server→Client notification that the Girl Wand editor should open for a girl.
 * Delegates to {@code ClientPacketHandlers} reflectively so common code never links against
 * {@code net.minecraft.client}.
 */
public record OpenNpcEditorS2CPacket(int entityId) implements CustomPacketPayload {
    public static final Type<OpenNpcEditorS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "opennpceditors2cpacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenNpcEditorS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, OpenNpcEditorS2CPacket::entityId,
                    OpenNpcEditorS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                Class<?> handler = Class.forName("com.sandymandy.pleasurehorizons.client.networking.ClientPacketHandlers");
                handler.getMethod("handleOpenNpcEditorScreen", int.class)
                        .invoke(null, this.entityId());
            } catch (Exception e) {
                PleasureHorizons.LOGGER.debug("OpenNpcEditor packet on non-client side");
            }
        });
    }
}
