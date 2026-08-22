package com.sandymandy.pleasurehorizons.networking.S2C;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Tells clients to play a girl's attack animation.
 *
 * <p>Previously an empty record with a no-op handler, so melee swings were invisible.
 * It now carries the entity id and flips the vanilla swinging flag the GeckoLib attack
 * controller keys off.</p>
 */
public record PlayAttackAnimationS2CPacket(int entityId) implements CustomPacketPayload {
    public static final Type<PlayAttackAnimationS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "playattackanimations2cpacket"));

    public static final StreamCodec<ByteBuf, PlayAttackAnimationS2CPacket> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(PlayAttackAnimationS2CPacket::new, PlayAttackAnimationS2CPacket::entityId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Entity entity = ctx.player().level().getEntity(this.entityId());
            if (entity instanceof com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity girl) {
                girl.triggerSwing();
            }
        });
    }
}
