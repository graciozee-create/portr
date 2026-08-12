package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RemovePreviewEntityC2SPacket(int entityId, int previewEntityId) implements CustomPacketPayload {
    public static final Type<RemovePreviewEntityC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "removepreviewentityc2spacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemovePreviewEntityC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RemovePreviewEntityC2SPacket::entityId,
            ByteBufCodecs.VAR_INT, RemovePreviewEntityC2SPacket::previewEntityId,
            RemovePreviewEntityC2SPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Entity entity = ctx.player().level().getEntity(this.entityId());
            if (!(entity instanceof TameableGirlEntity girl)
                    || !girl.isOwner(ctx.player())
                    || !girl.hasPreviewSession(ctx.player())
                    || !girl.referencesPreviewEntityId(this.previewEntityId())) {
                return;
            }

            Entity claimedPreview = ctx.player().level().getEntity(this.previewEntityId());
            if (claimedPreview != null) {
                if (!(claimedPreview instanceof GirlEntity preview) || !girl.referencesPreview(preview)) {
                    return;
                }
                preview.discard();
            }
            girl.clearPreviewSession();
        });
    }
}
