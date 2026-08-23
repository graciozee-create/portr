package com.sandymandy.pleasurehorizons.networking.S2C;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.util.GirlStatusCache;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sends a girl's backpack fill to tracking clients for the HUD status panel.
 *
 * <p>The girl's own inventory is server-only, so this small change-driven payload (plus the
 * initial value from {@code sendPairingData}) is the only way the client can show how full her
 * backpack is.</p>
 */
public record GirlStatusS2CPacket(int entityId, int backpackUsedSlots) implements CustomPacketPayload {
    public static final Type<GirlStatusS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "girl_status"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GirlStatusS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, GirlStatusS2CPacket::entityId,
                    ByteBufCodecs.VAR_INT, GirlStatusS2CPacket::backpackUsedSlots,
                    GirlStatusS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> GirlStatusCache.put(this.entityId(), this.backpackUsedSlots()));
    }
}
