package com.sandymandy.pleasurehorizons.networking.S2C;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Asks clients to drop cached girl models.
 *
 * <p>Upstream called {@code AbstractGirlModel.refreshAllModels()} here. That custom model cache
 * is not part of this port - GeckoLib 4 owns the cache and rebuilds it on a resource reload -
 * so the payload is kept for wire compatibility but performs no client work.</p>
 */
public record RefreshModelsS2CPacket() implements CustomPacketPayload {
    public static final Type<RefreshModelsS2CPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "refreshmodelss2cpacket"));
    public static final StreamCodec<ByteBuf, RefreshModelsS2CPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> {},
        buf -> new RefreshModelsS2CPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> PleasureHorizons.LOGGER.debug(
                "Model refresh requested; GeckoLib rebuilds its cache on resource reload"));
    }
}
