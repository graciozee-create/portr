package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Actions sent by the unified {@code InteractionScreen}.
 *
 * <p>Unlike the inventory buttons, this screen is not backed by a container menu, so the server
 * validates ownership, distance and entity state instead of the menu binding. Allowed actions are
 * the quick toggles ({@code follow}, {@code sit}), {@code talk} (server-side greeting line) and
 * {@code inventory} (opens the girl's container menu).</p>
 */
public record InteractionActionC2SPacket(int entityId, String action) implements CustomPacketPayload {
    private static final java.util.Set<String> VALID_ACTIONS =
            java.util.Set.of("follow", "sit", "talk", "inventory");

    public static final Type<InteractionActionC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "interactionactionc2spacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, InteractionActionC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, InteractionActionC2SPacket::entityId,
                    ByteBufCodecs.STRING_UTF8, InteractionActionC2SPacket::action,
                    InteractionActionC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!VALID_ACTIONS.contains(this.action())) return;
            ServerPlayer player = ctx.player() instanceof ServerPlayer sp ? sp : null;
            if (player == null) return;
            Entity entity = player.level().getEntity(this.entityId());
            if (!(entity instanceof TameableGirlEntity girl)) return;
            if (!girl.isOwner(player) || !girl.isAlive() || girl.isDowned()
                    || girl.isSceneActive() || girl.isPassenger()
                    || player.distanceToSqr(girl) > 64.0D) {
                return;
            }
            switch (this.action()) {
                case "follow" -> girl.setFollowing(!girl.isFollowing());
                case "sit" -> girl.setSitting(!girl.isSitting());
                case "talk" -> girl.talkToPlayer(player);
                case "inventory" -> {
                    girl.setGUIOpenState(true, player);
                    player.openMenu(
                            new com.sandymandy.pleasurehorizons.screen.GirlInventoryScreenHandlerFactory(girl),
                            buf -> buf.writeVarInt(girl.getId()));
                }
            }
        });
    }
}
