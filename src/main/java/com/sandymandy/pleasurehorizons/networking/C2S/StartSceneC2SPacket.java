package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.screen.GirlInventoryScreenHandler;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StartSceneC2SPacket(int entityId, String scene) implements CustomPacketPayload {
    public static final Type<StartSceneC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "startscenec2spacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StartSceneC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, StartSceneC2SPacket::entityId,
                    ByteBufCodecs.STRING_UTF8, StartSceneC2SPacket::scene,
                    StartSceneC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (this.scene().length() > 128) return;

            Entity entity = ctx.player().level().getEntity(this.entityId());
            if (!(entity instanceof TameableGirlEntity girl)
                    || !(ctx.player().containerMenu instanceof GirlInventoryScreenHandler menu)
                    || menu.getGirl() != girl
                    || !girl.isOwner(ctx.player())
                    || !girl.isGUIOpen()
                    || girl.getLookAtTarget() == null
                    || !girl.getLookAtTarget().getUUID().equals(ctx.player().getUUID())
                    || !girl.isAlive()
                    || girl.isDowned()
                    || girl.isSceneActive()
                    || girl.getScenePlayer() != null
                    || ctx.player().distanceToSqr(girl) > 64.0D) {
                return;
            }

            Scene selectedScene = girl.findScene(this.scene());
            if (selectedScene == Scene.EMPTY
                    || girl.getCurrentRelationshipLevel() < selectedScene.requiredRelationshipLevel()
                    || girl.isPregnant()) {
                return;
            }

            // The scene selection screen replaces the inventory screen client-side, but the
            // inventory menu is still open server-side. Close it once its interaction is accepted.
            girl.setGUIOpenState(false, null);
            ctx.player().closeContainer();
            girl.startScene(ctx.player(), selectedScene);
        });
    }
}
