package com.sandymandy.pleasurehorizons.util;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.util.json.SceneKeyframeEventLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

/**
 * Hooks keyframe-event loading into resource reloads.
 *
 * <p>Fabric used {@code ResourceManagerHelper.get(CLIENT_RESOURCES).registerReloadListener}.
 * On NeoForge, client assets go through {@link RegisterClientReloadListenersEvent} on the mod
 * bus. The keyframe JSONs live under {@code assets/}, so they are a client resource - the
 * listener is registered from the client-only entrypoint.</p>
 */
public class SceneKeyframeEventReloader {

    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "scene_keyframes");

    /** Kept so the common-side call site in PleasureHorizonsClient still compiles. */
    public static void registerReloader() {
        // Registration happens through the event below; nothing to do here.
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerClient(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> {
            PleasureHorizons.LOGGER.info("[SceneKeyframeEventReloader] Reloading scene keyframes...");
            SceneKeyframeEventLoader.loadFromAssets(manager);
        });
    }

    public static ResourceLocation id() {
        return ID;
    }
}
