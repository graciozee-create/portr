package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.girls.GoblinEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client→Server choice from the Goblin catch screen.
 *
 * <p>Allowed actions: {@code return}, {@code scene}, {@code make_queen}, {@code dismiss}.
 * The server is authoritative and ignores malformed/unknown values.</p>
 */
public record GoblinActionC2SPacket(int entityId, String action) implements CustomPacketPayload {
    private static final java.util.Set<String> VALID_ACTIONS =
            java.util.Set.of("return", "scene", "make_queen", "dismiss");

    public static final Type<GoblinActionC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "goblinactionc2spacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GoblinActionC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, GoblinActionC2SPacket::entityId,
                    ByteBufCodecs.STRING_UTF8, GoblinActionC2SPacket::action,
                    GoblinActionC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!VALID_ACTIONS.contains(this.action())) return;
            ServerPlayer player = ctx.player();
            if (player == null) return;
            Entity entity = player.level().getEntity(this.entityId());
            if (entity instanceof GoblinEntity goblin) {
                goblin.handleCatchAction(player, this.action());
            }
        });
    }
}
