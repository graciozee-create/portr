package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BonePosSyncC2SPacket(int entityId, Vec3 position) implements CustomPacketPayload {
    public static final Type<BonePosSyncC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "bonepossyncc2spacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BonePosSyncC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, BonePosSyncC2SPacket::entityId,
                    StreamCodec.of((buf, vec) -> {
                        buf.writeDouble(vec.x);
                        buf.writeDouble(vec.y);
                        buf.writeDouble(vec.z);
                    }, buf -> new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())), BonePosSyncC2SPacket::position,
                    BonePosSyncC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Entity entity = ctx.player().level().getEntity(this.entityId());
            if (entity instanceof TameableGirlEntity girl && girl.isOwner(ctx.player())) {
                girl.setPassengerBonePosition(this.position());
            }
        });
    }
}
