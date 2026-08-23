package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.entity.girls.KoboldEntity;
import com.sandymandy.pleasurehorizons.networking.S2C.OpenCustomizeScreenS2CPacket;
import com.sandymandy.pleasurehorizons.networking.S2C.SceneOptionsS2CPacket;
import com.sandymandy.pleasurehorizons.networking.S2C.OpenKoboldCustomizeScreenS2CPacket;
import com.sandymandy.pleasurehorizons.screen.GirlInventoryScreenHandler;
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
            if (this.actionId().length() > 32) return;

            Entity entity = ctx.player().level().getEntity(this.entityId());
            if (!(entity instanceof TameableGirlEntity girl)) return;

            // Bind the claimed id to the menu that produced the click; ownership alone must not
            // turn an arbitrary entity-id packet into a remote-control API.
            if (!(ctx.player().containerMenu instanceof GirlInventoryScreenHandler menu)
                    || menu.getGirl() != girl
                    || !girl.isOwner(ctx.player())
                    || !girl.isAlive()
                    || girl.isDowned()
                    || girl.isSceneActive()
                    || girl.isPassenger()
                    || ctx.player().distanceToSqr(girl) > 64.0D) {
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
                case "cycleFollowDistance" -> {
                    int next = girl.getFollowDistance() >= 16 ? 2 : girl.getFollowDistance() + 2;
                    girl.setFollowDistance(next);
                    ctx.player().displayClientMessage(net.minecraft.network.chat.Component.literal(
                            "Follow distance: " + next + " blocks"), true);
                }
                case "talk" -> {
                    // Upstream sends the scene list here; the GirlSceneScreen is what the Talk
                    // button is supposed to open. Girls without scenes fall back to small talk.
                    if (ctx.player() instanceof ServerPlayer serverPlayer && !girl.getScenes().isEmpty()) {
                        girl.setGUIOpenState(true, ctx.player());
                        PacketDistributor.sendToPlayer(serverPlayer, new SceneOptionsS2CPacket(
                                girl.getId(),
                                girl.getCurrentRelationshipLevel(),
                                new net.minecraft.world.item.ItemStack(girl.isAttractedTo()),
                                girl.getScenes()));
                    } else {
                        girl.talkToPlayer(ctx.player());
                    }
                }
                case "customize" -> {
                    if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;
                    GirlEntity clone = girl.createTempClone(ctx.player());
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
                case "chopTrees" -> {
                    girl.setChopTreesEnabled(!girl.isChopTreesEnabled());
                    ctx.player().displayClientMessage(
                            net.minecraft.network.chat.Component.translatable(
                                    girl.isChopTreesEnabled() ? "msg.pleasurehorizons.chopTreesEnabled" : "msg.pleasurehorizons.chopTreesDisabled",
                                    girl.getGirlDisplayName()), true);
                }
                case "feedOwner" -> {
                    girl.setFeedOwnerEnabled(!girl.isFeedOwnerEnabled());
                    ctx.player().displayClientMessage(
                            net.minecraft.network.chat.Component.translatable(
                                    girl.isFeedOwnerEnabled() ? "msg.pleasurehorizons.feedOwnerEnabled" : "msg.pleasurehorizons.feedOwnerDisabled",
                                    girl.getGirlDisplayName()), true);
                }
                case "dropLoot" -> {
                    girl.giveBackpackTo(ctx.player());
                    ctx.player().displayClientMessage(
                            net.minecraft.network.chat.Component.translatable(
                                    "msg.pleasurehorizons.lootTransferred", girl.getGirlDisplayName()), true);
                }
                case "cook" -> {
                    girl.setCookEnabled(!girl.isCookEnabled());
                    ctx.player().displayClientMessage(
                            net.minecraft.network.chat.Component.translatable(
                                    girl.isCookEnabled() ? "msg.pleasurehorizons.cookEnabled" : "msg.pleasurehorizons.cookDisabled",
                                    girl.getGirlDisplayName()), true);
                }
                case "hunt" -> {
                    girl.setHuntEnabled(!girl.isHuntEnabled());
                    ctx.player().displayClientMessage(
                            net.minecraft.network.chat.Component.translatable(
                                    girl.isHuntEnabled() ? "msg.pleasurehorizons.huntEnabled" : "msg.pleasurehorizons.huntDisabled",
                                    girl.getGirlDisplayName()), true);
                }
                case "cycleRole" -> {
                    com.sandymandy.pleasurehorizons.util.variables.GirlRole next =
                            girl.getRole().next();
                    girl.setRole(next);
                    ctx.player().displayClientMessage(
                            net.minecraft.network.chat.Component.translatable(
                                    "msg.pleasurehorizons.roleApplied",
                                    girl.getGirlDisplayName(),
                                    net.minecraft.network.chat.Component.translatable(
                                            "role.pleasurehorizons." + next.id())), true);
                }
                default -> { /* Unknown actions are untrusted input; ignore without log spam. */ }
            }
        });
    }
}
