package com.sandymandy.pleasurehorizons.client.networking;

import com.sandymandy.pleasurehorizons.client.gui.screen.GalathGrabScreen;
import com.sandymandy.pleasurehorizons.client.gui.screen.GirlCustomizeScreen;
import com.sandymandy.pleasurehorizons.client.gui.screen.GirlSceneScreen;
import com.sandymandy.pleasurehorizons.client.gui.screen.KoboldCustomizeScreen;
import com.sandymandy.pleasurehorizons.client.gui.screen.hud.SceneProgressOverlay;
import com.sandymandy.pleasurehorizons.entity.girls.GalathEntity;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Client-only entry points for S2C packets.
 *
 * <p>Packet classes live in common code and must never reference {@code net.minecraft.client}
 * directly, so they call into this class reflectively.</p>
 */
@OnlyIn(Dist.CLIENT)
public class ClientPacketHandlers {

    public static void handleOpenCustomizeScreen(int entityId, int previewEntityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.execute(() -> mc.setScreen(new GirlCustomizeScreen(
                Component.translatable("gui.pleasurehorizons.customize.titleGirl"), entityId, previewEntityId)));
    }

    public static void handleOpenKoboldCustomizeScreen(int entityId, int previewEntityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.execute(() -> mc.setScreen(new KoboldCustomizeScreen(
                Component.translatable("gui.pleasurehorizons.customize.titleKobold"), entityId, previewEntityId)));
    }

    public static void handleSceneOptions(int entityId, int relationshipLevel, ItemStack attractedTo,
                                          List<Scene> scenes) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.execute(() -> mc.setScreen(new GirlSceneScreen(entityId, relationshipLevel, attractedTo, scenes)));
    }

    public static void handleCumHudAnimation() {
        Minecraft.getInstance().execute(SceneProgressOverlay::triggerCumAnimation);
    }

    public static void handleGalathGrabScreen(int entityId, boolean grabActive) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        mc.execute(() -> {
            Entity entity = mc.level.getEntity(entityId);
            if (grabActive) {
                if (entity instanceof GalathEntity galath) {
                    mc.setScreen(new GalathGrabScreen(galath, mc.player));
                }
            } else if (mc.screen instanceof GalathGrabScreen screen) {
                screen.onClose();
            }
        });
    }
}
