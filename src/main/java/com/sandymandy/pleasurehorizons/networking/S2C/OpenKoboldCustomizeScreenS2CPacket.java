package com.sandymandy.pleasurehorizons.networking.S2C;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenKoboldCustomizeScreenS2CPacket(int entityId, int previewEntityId) implements CustomPacketPayload {
    public static final Type<OpenKoboldCustomizeScreenS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "openkoboldcustomizescreens2cpacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenKoboldCustomizeScreenS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, OpenKoboldCustomizeScreenS2CPacket::entityId,
                    ByteBufCodecs.VAR_INT, OpenKoboldCustomizeScreenS2CPacket::previewEntityId,
                    OpenKoboldCustomizeScreenS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {});
    }
}
