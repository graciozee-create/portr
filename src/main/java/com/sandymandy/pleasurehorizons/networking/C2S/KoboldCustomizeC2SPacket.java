package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.girls.KoboldEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record KoboldCustomizeC2SPacket(
        int entityId,
        int bodySize,
        int breastSize,
        int primaryColor,
        int secondaryColor,
        int irisColor,
        int topHornType,
        int bottomHornType
) implements CustomPacketPayload {

    public static final Type<KoboldCustomizeC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "koboldcustomizec2spacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, KoboldCustomizeC2SPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeVarInt(pkt.entityId());
                        buf.writeVarInt(pkt.bodySize());
                        buf.writeVarInt(pkt.breastSize());
                        buf.writeVarInt(pkt.primaryColor());
                        buf.writeVarInt(pkt.secondaryColor());
                        buf.writeVarInt(pkt.irisColor());
                        buf.writeVarInt(pkt.topHornType());
                        buf.writeVarInt(pkt.bottomHornType());
                    },
                    buf -> new KoboldCustomizeC2SPacket(
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readVarInt(),
                            buf.readVarInt()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Entity entity = ctx.player().level().getEntity(this.entityId());
            if (entity instanceof KoboldEntity kobold) {
                kobold.setBodySize(this.bodySize());
                kobold.setKoboldBreastSize(this.breastSize());
                kobold.setPrimaryColor(this.primaryColor());
                kobold.setSecondaryColor(this.secondaryColor());
                kobold.setIrisColor(this.irisColor());
                kobold.setTopHornType(this.topHornType());
                kobold.setBottomHornType(this.bottomHornType());
            }
        });
    }
}
