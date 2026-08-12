package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AnimationSyncC2SPacket(int entityId, String animationState, boolean loopState, boolean holdState) implements CustomPacketPayload {
    public static final Type<AnimationSyncC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "animationsyncc2spacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AnimationSyncC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, AnimationSyncC2SPacket::entityId,
                    ByteBufCodecs.STRING_UTF8, AnimationSyncC2SPacket::animationState,
                    ByteBufCodecs.BOOL, AnimationSyncC2SPacket::loopState,
                    ByteBufCodecs.BOOL, AnimationSyncC2SPacket::holdState,
                    AnimationSyncC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Entity entity = ctx.player().level().getEntity(this.entityId());
            if (entity instanceof GirlSceneEntity girl) {
                girl.setOverrideAnim(this.animationState());
                girl.setOverrideLoop(this.loopState());
                girl.setOverrideHold(this.holdState());
            }
        });
    }
}
