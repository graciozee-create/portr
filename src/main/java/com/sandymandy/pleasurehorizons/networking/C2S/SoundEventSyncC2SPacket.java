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

import java.util.regex.Pattern;

public record SoundEventSyncC2SPacket(int entityId, String soundEvent) implements CustomPacketPayload {
    private static final Pattern VALID_KEYFRAME = Pattern.compile("[a-z0-9_, \\-]{1,128}");

    public static final Type<SoundEventSyncC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "soundeventsyncc2spacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SoundEventSyncC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SoundEventSyncC2SPacket::entityId,
                    ByteBufCodecs.STRING_UTF8, SoundEventSyncC2SPacket::soundEvent,
                    SoundEventSyncC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!VALID_KEYFRAME.matcher(this.soundEvent()).matches()) return;

            Entity entity = ctx.player().level().getEntity(this.entityId());
            if (entity instanceof GirlSceneEntity girl && girl.acceptsAnimationEventFrom(ctx.player())) {
                girl.handleAnimationEventServer(this.soundEvent());
            }
        });
    }
}
