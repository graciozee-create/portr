package com.sandymandy.pleasurehorizons.networking.S2C;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Replays a keyframe event on clients that are not the scene player.
 *
 * <p>Was an empty record with a no-op handler. It now carries the entity and the event key so
 * bystanders see the same dialogue and hear the same cues as the player in the scene.</p>
 */
public record RunAnimEventsS2CPacket(int entityId, String event) implements CustomPacketPayload {
    public static final Type<RunAnimEventsS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "runanimeventss2cpacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RunAnimEventsS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, RunAnimEventsS2CPacket::entityId,
                    ByteBufCodecs.STRING_UTF8, RunAnimEventsS2CPacket::event,
                    RunAnimEventsS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Entity entity = ctx.player().level().getEntity(this.entityId());
            if (entity instanceof GirlSceneEntity girl) {
                girl.handleAnimationEventClient(this.event());
            }
        });
    }
}
