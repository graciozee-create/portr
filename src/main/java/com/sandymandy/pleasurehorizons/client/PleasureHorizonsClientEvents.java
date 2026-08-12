package com.sandymandy.pleasurehorizons.client;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.client.gui.screen.GirlInventoryScreen;
import com.sandymandy.pleasurehorizons.client.gui.screen.hud.SceneProgressOverlay;
import com.sandymandy.pleasurehorizons.client.gui.screen.settlement.SettlementHubScreen;
import com.sandymandy.pleasurehorizons.client.render.GirlRenderer;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.registries.GirlRegistry;
import com.sandymandy.pleasurehorizons.registries.PleasureHorizonsScreenHandlerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Without these registrations every girl entity exists on the server but is completely
 * invisible on the client - one of the reasons the previous port showed nothing.
 */
@EventBusSubscriber(modid = PleasureHorizons.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PleasureHorizonsClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GirlRegistry.LUCY.get(), GirlRenderer::new);
        event.registerEntityRenderer(GirlRegistry.MIKA.get(), GirlRenderer::new);
        event.registerEntityRenderer(GirlRegistry.MOMO.get(), GirlRenderer::new);
        event.registerEntityRenderer(GirlRegistry.SLIME.get(), GirlRenderer::new);
        event.registerEntityRenderer(GirlRegistry.KOBOLD.get(), GirlRenderer::new);
        event.registerEntityRenderer(GirlRegistry.COPPIE.get(), GirlRenderer::new);
        event.registerEntityRenderer(GirlRegistry.CUSTOM_GIRL.get(), GirlRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(
                PleasureHorizonsScreenHandlerRegistry.GIRL_INVENTORY_HOLDER.get(),
                GirlInventoryScreen::new
        );
        event.register(
                PleasureHorizonsScreenHandlerRegistry.SETTLEMENT_HUB_HOLDER.get(),
                SettlementHubScreen::new
        );
    }

    /** Keyframe sound/message tables are reloaded together with the resource packs. */
    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        com.sandymandy.pleasurehorizons.util.SceneKeyframeEventReloader.registerClient(event);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(PleasureHorizonsKeybinds.THRUST_KEY);
        event.register(PleasureHorizonsKeybinds.CUM_KEY);
    }

    /**
     * NeoForge equivalent of Fabric's {@code HudElementRegistry.addFirst} - the scene progress
     * bar only draws while the local player is riding a girl in a scene.
     */
    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "scene_progress_overlay"),
                (guiGraphics, deltaTracker) -> {
                    LocalPlayer player = Minecraft.getInstance().player;
                    if (player != null && player.getVehicle() instanceof GirlSceneEntity girl) {
                        if (girl.getAnimationKeyFrameEvent().contains("sexui")) {
                            SceneProgressOverlay.setActive(true);
                        }
                        SceneProgressOverlay.render(guiGraphics, girl.getSceneProgress(), girl.getCumThreshold());
                    } else {
                        SceneProgressOverlay.setActive(false);
                    }
                });
    }
}
