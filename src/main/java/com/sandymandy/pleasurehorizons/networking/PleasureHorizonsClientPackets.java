package com.sandymandy.pleasurehorizons.networking;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.client.gui.screen.GirlCustomizeScreen;
import com.sandymandy.pleasurehorizons.client.gui.screen.KoboldCustomizeScreen;
import com.sandymandy.pleasurehorizons.client.models.AbstractGirlModel;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.client.gui.screen.hud.SceneProgressOverlay;
import com.sandymandy.pleasurehorizons.networking.S2C.*;
import com.sandymandy.pleasurehorizons.client.gui.screen.GirlSceneScreen;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;

@OnlyIn(Dist.CLIENT)
public class PleasureHorizonsClientPackets {
    public static void registerS2CPackets(){
        PleasureHorizons.LOGGER.info("Registering S2C Packets for PleasureCraft");

        // --- S2C (server → client) ---
        ClientPlayNetworking.registerGlobalReceiver(ClothingArmorVisibilityS2CPacket.ID,
                (packet, context) -> context.client().execute(() -> {
                    var world = context.client().world;
                    if (world == null) return;

                    Entity entity = world.getEntityById(packet.entityId());
                    if (entity instanceof GirlSceneEntity girl) {
                        int i = 0;
                        for (EquipmentSlot slot : EquipmentSlot.values()) {
                            girl.armorVisibility.put(slot, packet.armor().get(i));
                            i++;
                        }
                        girl.applyClothingAndArmor();
                    }
                }));

        ClientPlayNetworking.registerGlobalReceiver(SceneOptionsS2CPacket.ID, (packet, context) -> {
            context.client().execute(() -> {
                MinecraftClient.getInstance().setScreen(new GirlSceneScreen(packet.entityId(), packet.currentRelationshipLevel(), packet.attractedTo(),packet.options()));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(OpenCustomizeScreenS2CPacket.ID, (packet, context) -> {
            context.client().execute(() -> {
                MinecraftClient.getInstance().setScreen(new GirlCustomizeScreen(packet.entityId(), packet.previewEntityId()));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(
                PlayCumHudAnimationS2CPacket.ID,
                (packet, context) -> {
                    // trigger the HUD animation locally
                    context.client().execute(SceneProgressOverlay::triggerCumAnimation);
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                RefreshModelsS2CPacket.ID,
                (packet, context) -> {
                    AbstractGirlModel.refreshAllModels();
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                PlayAttackAnimationS2CPacket.ID,
                (packet, context) -> context.client().execute(() -> {
                    var world = context.client().world;
                    if (world == null) return;

                    Entity entity = world.getEntityById(packet.entityId());
                    if (entity instanceof GirlSceneEntity girl) {
                        girl.triggerSwing();
                    }
                })
        );

        ClientPlayNetworking.registerGlobalReceiver(
                RunAnimEventsS2CPacket.ID,
                (packet, context) -> context.client().execute(() -> {
                    var world = context.client().world;
                    if (world == null) return;

                    Entity entity = world.getEntityById(packet.entityId());
                    if (entity instanceof GirlSceneEntity girl) {
                        girl.handleAnimationEventClient(packet.event());
                    }
                })
        );

        ClientPlayNetworking.registerGlobalReceiver(
                OpenKoboldCustomizeScreenS2CPacket.ID,
                (packet, context) -> context.client().execute(() -> {
                    MinecraftClient.getInstance().setScreen(new KoboldCustomizeScreen(packet.entityId(), packet.previewEntityId()));
                })
        );

    }
}
