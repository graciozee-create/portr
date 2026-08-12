package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetGUIOpenStateC2SPacket(int entityId, boolean data) implements CustomPacketPayload {
    public static final Type<SetGUIOpenStateC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "setguiopenstatec2spacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetGUIOpenStateC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SetGUIOpenStateC2SPacket::entityId,
                    ByteBufCodecs.BOOL, SetGUIOpenStateC2SPacket::data,
                    SetGUIOpenStateC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Opening is exclusively a server-side interaction. Clients may only close the exact
            // interaction that the server associated with them.
            if (this.data()) return;

            Entity entity = ctx.player().level().getEntity(this.entityId());
            if (entity instanceof GirlEntity girl
                    && girl.isGUIOpen()
                    && girl.getLookAtTarget() != null
                    && girl.getLookAtTarget().getUUID().equals(ctx.player().getUUID())) {
                girl.setGUIOpenState(false, null);
                ctx.player().closeContainer();
            }
        });
    }
}
