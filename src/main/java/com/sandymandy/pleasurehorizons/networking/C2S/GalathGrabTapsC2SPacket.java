package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.girls.GalathEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client→Server escape-tap report from the Galath grab minigame.
 *
 * <p>Batched so the client does not send one tiny packet per A/D tap; the server treats
 * {@code taps} as number of extra escape points and releases the victim when the threshold
 * is reached.</p>
 */
public record GalathGrabTapsC2SPacket(int entityId, int taps) implements CustomPacketPayload {
    public static final Type<GalathGrabTapsC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "galathgrabtapsc2spacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GalathGrabTapsC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, GalathGrabTapsC2SPacket::entityId,
                    ByteBufCodecs.VAR_INT, GalathGrabTapsC2SPacket::taps,
                    GalathGrabTapsC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (this.taps() < 1 || this.taps() > 10) return;
            Entity entity = ctx.player().level().getEntity(this.entityId());
            if (entity instanceof GalathEntity galath) {
                galath.onEscapeTap(this.taps());
            }
        });
    }
}
