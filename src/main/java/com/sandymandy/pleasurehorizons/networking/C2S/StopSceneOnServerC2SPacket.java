package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StopSceneOnServerC2SPacket(int entityId) implements CustomPacketPayload {
    public static final Type<StopSceneOnServerC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "stopsceneonserverc2spacket"));

    public static final StreamCodec<ByteBuf, StopSceneOnServerC2SPacket> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(StopSceneOnServerC2SPacket::new, StopSceneOnServerC2SPacket::entityId);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Entity entity = ctx.player().level().getEntity(this.entityId());
            if (entity instanceof GirlSceneEntity girl) {
                girl.stopScene();
            }
        });
    }
}
