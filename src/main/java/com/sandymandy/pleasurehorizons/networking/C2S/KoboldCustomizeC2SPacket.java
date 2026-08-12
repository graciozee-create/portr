package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.girls.KoboldEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
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
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, KoboldCustomizeC2SPacket::entityId,
                    ByteBufCodecs.VAR_INT, KoboldCustomizeC2SPacket::bodySize,
                    ByteBufCodecs.VAR_INT, KoboldCustomizeC2SPacket::breastSize,
                    ByteBufCodecs.VAR_INT, KoboldCustomizeC2SPacket::primaryColor,
                    ByteBufCodecs.VAR_INT, KoboldCustomizeC2SPacket::secondaryColor,
                    ByteBufCodecs.VAR_INT, KoboldCustomizeC2SPacket::irisColor,
                    ByteBufCodecs.VAR_INT, KoboldCustomizeC2SPacket::topHornType,
                    ByteBufCodecs.VAR_INT, KoboldCustomizeC2SPacket::bottomHornType,
                    KoboldCustomizeC2SPacket::new
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
