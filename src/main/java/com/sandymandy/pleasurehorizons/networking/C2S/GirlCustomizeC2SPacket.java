package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GirlCustomizeC2SPacket(int entityId, int breastSize, Vec3 breastOffset, boolean canGetImpregnated) implements CustomPacketPayload {
    public static final Type<GirlCustomizeC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "girlcustomizec2spacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GirlCustomizeC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, GirlCustomizeC2SPacket::entityId,
                    ByteBufCodecs.VAR_INT, GirlCustomizeC2SPacket::breastSize,
                    StreamCodec.of((buf, vec) -> {
                        buf.writeDouble(vec.x);
                        buf.writeDouble(vec.y);
                        buf.writeDouble(vec.z);
                    }, buf -> new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())), GirlCustomizeC2SPacket::breastOffset,
                    ByteBufCodecs.BOOL, GirlCustomizeC2SPacket::canGetImpregnated,
                    GirlCustomizeC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Entity entity = ctx.player().level().getEntity(this.entityId());
            if (entity instanceof GirlSceneEntity girl) {
                girl.setBreastSize(this.breastSize());
                girl.setBreastOffset(this.breastOffset());
                girl.canGetImpregnatedState(this.canGetImpregnated());
            }
        });
    }
}
