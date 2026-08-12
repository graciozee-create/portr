package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent when the player clicks one of the girl-inventory buttons.
 *
 * <p>Previously this was an empty record carrying no data, so every button was a
 * no-op regardless of which one was pressed. It now carries the target entity id
 * and the action, matching the Fabric original.</p>
 */
public record InventoryButtonC2SPacket(int entityId, String actionId) implements CustomPacketPayload {

    public static final Type<InventoryButtonC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "girl_inventory_button"));

    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryButtonC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, InventoryButtonC2SPacket::entityId,
                    ByteBufCodecs.STRING_UTF8, InventoryButtonC2SPacket::actionId,
                    InventoryButtonC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Entity entity = ctx.player().level().getEntity(this.entityId());
            if (!(entity instanceof TameableGirlEntity girl)) {
                return;
            }

            // Only her owner may command her.
            if (!girl.isOwner(ctx.player())) {
                return;
            }

            switch (this.actionId()) {
                case "stripOrDressup" -> girl.requestStrip();
                case "breakUp" -> girl.breakUpParticles(ctx.player());
                case "setBase" -> girl.setBasePosHere();
                case "goToBase" -> girl.teleportToBase();
                case "sit" -> girl.setSitting(!girl.isSitting());
                case "follow" -> girl.setFollowing(!girl.isFollowing());
                // "talk" and "customize" need the scene/customise screens, which are not ported yet.
                case "talk", "customize" -> {}
                default -> PleasureHorizons.LOGGER.warn("Unknown girl interaction: {}", this.actionId());
            }
        });
    }
}
