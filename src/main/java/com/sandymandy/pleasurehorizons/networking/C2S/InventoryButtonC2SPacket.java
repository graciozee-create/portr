package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.entity.girls.KoboldEntity;
import com.sandymandy.pleasurehorizons.networking.S2C.OpenCustomizeScreenS2CPacket;
import com.sandymandy.pleasurehorizons.networking.S2C.OpenKoboldCustomizeScreenS2CPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
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
                case "stripOrDressup" -> {
                    // Original Fabric implementation only requests strip - the StripGoal
                    // handles the actual toggle + freeze/unfreeze. Immediate toggle here
                    // caused double-toggle and permanent freeze.
                    girl.requestStrip();
                    // Safety: ensure freeze flag is cleared if goal never starts (e.g. no anim)
                    // - StripGoal.stop() also clears it, but this covers manual call path.
                    if (!girl.hasStripAnim()) {
                        girl.setFreeze(false);
                    }
                }
                case "breakUp" -> girl.breakUpParticles(ctx.player());
                case "setBase" -> girl.setBasePosHere();
                case "goToBase" -> girl.teleportToBase();
                case "sit" -> girl.setSitting(!girl.isSitting());
                case "follow" -> girl.setFollowing(!girl.isFollowing());
                case "talk" -> girl.talkToPlayer(ctx.player());
                case "customize" -> {
                    if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
                    GirlEntity clone = girl.createTempClone();
                    if (clone != null) {
                        if (girl instanceof KoboldEntity) {
                            PacketDistributor.sendToPlayer(serverPlayer,
                                    new OpenKoboldCustomizeScreenS2CPacket(girl.getId(), clone.getId()));
                        } else {
                            PacketDistributor.sendToPlayer(serverPlayer,
                                    new OpenCustomizeScreenS2CPacket(girl.getId(), clone.getId()));
                        }
                    }
                }
                case "guardBase" -> {
                    girl.setGuardBaseEnabled(!girl.isGuardBaseEnabled());
                    ctx.player().displayClientMessage(
                            net.minecraft.network.chat.Component.translatable(
                                    girl.isGuardBaseEnabled() ? "msg.pleasurehorizons.guardBaseEnabled" : "msg.pleasurehorizons.guardBaseDisabled",
                                    girl.getGirlDisplayName()), true);
                }
                case "guardOwner" -> {
                    girl.setGuardOwnerEnabled(!girl.isGuardOwnerEnabled());
                    ctx.player().displayClientMessage(
                            net.minecraft.network.chat.Component.translatable(
                                    girl.isGuardOwnerEnabled() ? "msg.pleasurehorizons.guardOwnerEnabled" : "msg.pleasurehorizons.guardOwnerDisabled",
                                    girl.getGirlDisplayName()), true);
                }
                case "gather" -> {
                    girl.setGatherEnabled(!girl.isGatherEnabled());
                    ctx.player().displayClientMessage(
                            net.minecraft.network.chat.Component.translatable(
                                    girl.isGatherEnabled() ? "msg.pleasurehorizons.gatherEnabled" : "msg.pleasurehorizons.gatherDisabled",
                                    girl.getGirlDisplayName()), true);
                }
                case "harvest" -> {
                    girl.setHarvestEnabled(!girl.isHarvestEnabled());
                    ctx.player().displayClientMessage(
                            net.minecraft.network.chat.Component.translatable(
                                    girl.isHarvestEnabled() ? "msg.pleasurehorizons.harvestEnabled" : "msg.pleasurehorizons.harvestDisabled",
                                    girl.getGirlDisplayName()), true);
                }
                case "stayNearBase" -> {
                    girl.setStayNearBaseEnabled(!girl.isStayNearBaseEnabled());
                    ctx.player().displayClientMessage(
                            net.minecraft.network.chat.Component.translatable(
                                    girl.isStayNearBaseEnabled() ? "msg.pleasurehorizons.stayNearBaseEnabled" : "msg.pleasurehorizons.stayNearBaseDisabled",
                                    girl.getGirlDisplayName()), true);
                }
                default -> PleasureHorizons.LOGGER.warn("Unknown girl interaction: {}", this.actionId());
            }
        });
    }
}
