package com.sandymandy.pleasurehorizons.networking.S2C;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server→Client notification for the Goblin catch screen.
 *
 * <p>{@code active=true} opens the screen, {@code false} closes it. The payload avoids
 * {@code net.minecraft.client} and delegates to {@code ClientPacketHandlers} reflectively,
 * exactly like every other S2C payload in this mod.</p>
 */
public record GoblinCaughtScreenS2CPacket(int entityId, boolean active) implements CustomPacketPayload {
    public static final Type<GoblinCaughtScreenS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "goblincaughtscreens2cpacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GoblinCaughtScreenS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, GoblinCaughtScreenS2CPacket::entityId,
                    ByteBufCodecs.BOOL, GoblinCaughtScreenS2CPacket::active,
                    GoblinCaughtScreenS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                Class<?> handler = Class.forName("com.sandymandy.pleasurehorizons.client.networking.ClientPacketHandlers");
                handler.getMethod("handleGoblinCaughtScreen", int.class, boolean.class)
                        .invoke(null, this.entityId(), this.active());
            } catch (Exception e) {
                PleasureHorizons.LOGGER.debug("GoblinCaughtScreen packet on server");
            }
        });
    }
}
