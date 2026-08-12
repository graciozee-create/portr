package com.sandymandy.pleasurehorizons.client.networking;

import com.sandymandy.pleasurehorizons.client.gui.screen.GirlCustomizeScreen;
import com.sandymandy.pleasurehorizons.client.gui.screen.KoboldCustomizeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Client-only handlers for S2C packets.
 * Keeps net.minecraft.client.Minecraft imports out of the packet classes themselves,
 * so dedicated server validation doesn't crash.
 */
@OnlyIn(Dist.CLIENT)
public class ClientPacketHandlers {

    public static void handleOpenCustomizeScreen(int entityId, int previewEntityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.execute(() -> mc.setScreen(new GirlCustomizeScreen(Component.literal("Customize Girl"), entityId, previewEntityId)));
    }

    public static void handleOpenKoboldCustomizeScreen(int entityId, int previewEntityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.execute(() -> mc.setScreen(new KoboldCustomizeScreen(Component.literal("Kobold Customization"), entityId, previewEntityId)));
    }
}
