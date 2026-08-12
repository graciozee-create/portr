package com.sandymandy.pleasurehorizons.client;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.config.ModConfig;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.networking.C2S.CumKeybindC2SPacket;
import com.sandymandy.pleasurehorizons.networking.C2S.ThrustKeybindC2SPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Polls the scene keybinds each client tick.
 *
 * <p>Fabric used {@code ClientTickEvents.END_CLIENT_TICK}; the NeoForge counterpart is
 * {@link ClientTickEvent.Post} on the game bus. {@code KeyMapping#wasPressed()} maps to
 * {@code consumeClick()} and {@code isPressed()} to {@code isDown()}.</p>
 *
 * <p>The thrust packet is only sent when the state actually flips, matching upstream, so held
 * keys do not flood the server with one packet per tick.</p>
 */
@EventBusSubscriber(modid = PleasureHorizons.MOD_ID, value = Dist.CLIENT)
public class PleasureHorizonsClientTickHandler {
    private static boolean thrustToggleState = false;
    private static boolean lastSentThrustState = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean inScene = mc.player.getVehicle() instanceof GirlSceneEntity;

        // Drain the click queue even when not in a scene, otherwise presses buffer up
        // and all fire at once the moment a scene starts.
        boolean thrustClicked = PleasureHorizonsKeybinds.THRUST_KEY.consumeClick();
        boolean cumClicked = PleasureHorizonsKeybinds.CUM_KEY.consumeClick();

        if (!inScene) {
            if (lastSentThrustState) {
                lastSentThrustState = false;
                thrustToggleState = false;
            }
            return;
        }

        boolean newThrustState;
        if (ModConfig.INSTANCE.keybinds.holdThrust) {
            newThrustState = PleasureHorizonsKeybinds.THRUST_KEY.isDown();
        } else {
            if (thrustClicked) {
                thrustToggleState = !thrustToggleState;
            }
            newThrustState = thrustToggleState;
        }

        if (newThrustState != lastSentThrustState) {
            lastSentThrustState = newThrustState;
            PacketDistributor.sendToServer(new ThrustKeybindC2SPacket(newThrustState));
        }

        if (cumClicked) {
            PacketDistributor.sendToServer(new CumKeybindC2SPacket(true));
        }
    }
}
