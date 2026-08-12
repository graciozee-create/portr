package com.sandymandy.pleasurehorizons.freecam;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.client.PleasureHorizonsKeybinds;
import com.sandymandy.pleasurehorizons.freecam.tripod.TripodSlot;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * Wires {@link Freecam} into the NeoForge client event bus.
 *
 * <p>This replaces the eighteen Fabric mixins the upstream freecam relies on. Each handler
 * below notes which mixin it stands in for, so the mapping back to the original is traceable.</p>
 */
@EventBusSubscriber(modid = PleasureHorizons.MOD_ID, value = Dist.CLIENT)
public class FreecamHandler {

    /** Replaces {@code MinecraftMixin#handleInputEvents} and {@code Freecam.preTick/postTick}. */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        // Drain the click queues unconditionally, otherwise presses made while no world is
        // loaded all fire at once on join.
        boolean toggle = PleasureHorizonsKeybinds.FREECAM_TOGGLE_KEY.consumeClick();
        boolean playerControl = PleasureHorizonsKeybinds.FREECAM_PLAYER_CONTROL_KEY.consumeClick();
        boolean tripodReset = PleasureHorizonsKeybinds.FREECAM_TRIPOD_RESET_KEY.consumeClick();

        if (mc.level == null || mc.player == null) {
            return;
        }

        // Keys are ignored while a screen is open, but the camera must still be ticked or it
        // would stop dead (and stop being disable-able) whenever the inventory is opened.
        if (mc.screen != null) {
            Freecam.onClientTick();
            return;
        }

        // Upstream binds tripods as "toggle key + hotbar number". Reproduced here: holding the
        // freecam key and tapping 1-9 addresses that tripod slot, tapping it alone toggles the
        // plain freecam.
        boolean hotbarCombo = false;
        if (PleasureHorizonsKeybinds.FREECAM_TOGGLE_KEY.isDown()
                || PleasureHorizonsKeybinds.FREECAM_TRIPOD_RESET_KEY.isDown()) {
            for (int slot = 0; slot < mc.options.keyHotbarSlots.length; slot++) {
                while (mc.options.keyHotbarSlots[slot].consumeClick()) {
                    TripodSlot tripod = TripodSlot.ofKeyCode(
                            org.lwjgl.glfw.GLFW.GLFW_KEY_1 + slot);
                    if (PleasureHorizonsKeybinds.FREECAM_TRIPOD_RESET_KEY.isDown()) {
                        Freecam.resetCamera(tripod);
                    } else {
                        Freecam.toggleTripod(tripod);
                    }
                    hotbarCombo = true;
                }
            }
        }

        if (toggle && !hotbarCombo) {
            Freecam.toggle();
        }
        if (playerControl) {
            Freecam.switchControls();
        }
        if (tripodReset && !hotbarCombo && Freecam.isTripodEnabled()) {
            Freecam.resetCamera(Freecam.getActiveTripod());
        }

        // The frame handler below transfers mouse movement to the camera and rewinds the
        // player. Re-pin it here too: rotation is sent to the server on the tick, so this
        // guarantees the server never sees the player spinning while the camera moves.
        if (Freecam.isEnabled() && !Freecam.isPlayerControlEnabled() && rotationTracked) {
            mc.player.setYRot(lastPlayerYaw);
            mc.player.setXRot(lastPlayerPitch);
            mc.player.setYHeadRot(lastPlayerYaw);
            mc.player.yBodyRot = lastPlayerYaw;
        }

        Freecam.onClientTick();
    }

    // Player rotation as of the previous frame, used to steal the mouse delta below.
    private static float lastPlayerYaw;
    private static float lastPlayerPitch;
    private static boolean rotationTracked = false;

    /**
     * Replaces {@code CameraMixin} and the {@code EntityMixin#changeLookDirection} redirect.
     *
     * <p>Upstream cancels the player's look change and forwards the mouse delta to the fake
     * camera player. NeoForge has no event around {@code Entity#turn}, so this does the
     * equivalent from the other end: the mouse has already turned the real player by the time
     * this fires, so the delta is transferred to the camera and the player is rewound to where
     * he was. The player therefore never turns while the camera does, with no mixin needed.</p>
     *
     * <p>{@code ComputeCameraAngles} is the right hook because NeoForge feeds its values into
     * {@code Camera#setAnglesInternal} immediately afterwards, and it runs every frame rather
     * than every tick - matching the mouse's own update rate.</p>
     */
    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        FreeCamera camera = Freecam.getFreeCamera();
        Minecraft mc = Minecraft.getInstance();

        if (!Freecam.isEnabled() || camera == null || mc.player == null) {
            rotationTracked = false;
            return;
        }

        if (!rotationTracked) {
            // Anchor on the rotation the player had when freecam started; every mouse delta
            // from here on belongs to the camera, and the player is pinned to this value.
            lastPlayerYaw = mc.player.getYRot();
            lastPlayerPitch = mc.player.getXRot();
            rotationTracked = true;
        }

        if (!Freecam.isPlayerControlEnabled()) {
            float yawDelta = mc.player.getYRot() - lastPlayerYaw;
            float pitchDelta = mc.player.getXRot() - lastPlayerPitch;

            if (yawDelta != 0.0F || pitchDelta != 0.0F) {
                Freecam.turnCamera(yawDelta, pitchDelta);

                // Rewind the player so only the camera moved.
                mc.player.setYRot(lastPlayerYaw);
                mc.player.setXRot(lastPlayerPitch);
                mc.player.yRotO = lastPlayerYaw;
                mc.player.xRotO = lastPlayerPitch;
                mc.player.setYHeadRot(lastPlayerYaw);
                mc.player.yHeadRotO = lastPlayerYaw;
                mc.player.yBodyRot = lastPlayerYaw;
                mc.player.yBodyRotO = lastPlayerYaw;
            }
        } else {
            // Player control: the mouse belongs to the player again, so just follow along.
            lastPlayerYaw = mc.player.getYRot();
            lastPlayerPitch = mc.player.getXRot();
        }

        // Rotation is driven per frame, so suppress the entity's own interpolation.
        camera.yRotO = camera.getYRot();
        camera.xRotO = camera.getXRot();

        event.setYaw(camera.getYRot());
        event.setPitch(camera.getXRot());
    }

    /**
     * Replaces the {@code Input} swap in {@code Freecam#preTick}: while the camera has control,
     * the player must not walk. Sneak is left alone so the player can still descend if control
     * is handed back.
     */
    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!Freecam.isEnabled() || Freecam.isPlayerControlEnabled()) {
            return;
        }
        var input = event.getInput();
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.forwardImpulse = 0.0F;
        input.leftImpulse = 0.0F;
    }

    /** Replaces {@code EntityRendererMixin}: hide your own body while flying around. */
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (Freecam.isEnabled()
                && FreecamConfig.INSTANCE.hidePlayer
                && event.getEntity() == Minecraft.getInstance().player) {
            event.setCanceled(true);
        }
    }

    /** Replaces {@code ItemInHandRendererMixin}: the held item would otherwise float in view. */
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (Freecam.isEnabled() && !FreecamConfig.INSTANCE.showHand) {
            event.setCanceled(true);
        }
    }

    /** Replaces {@code LivingEntityMixin}: optionally bail out of freecam when hurt. */
    @SubscribeEvent
    public static void onPlayerDamaged(LivingDamageEvent.Post event) {
        if (Freecam.isEnabled()
                && FreecamConfig.INSTANCE.disableOnDamage
                && event.getEntity() == Minecraft.getInstance().player) {
            Freecam.disableNextTick();
        }
    }

    /** Replaces {@code ClientPacketListenerMixin}: never survive a world change. */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            Freecam.onDisconnect();
        }
    }

    /**
     * Replaces {@code MinecraftMixin#doAttack/doItemPick/handleBlockBreaking}: block world
     * interaction while the camera is detached, unless the config opts back in.
     */
    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (Freecam.isEnabled()
                && !Freecam.isPlayerControlEnabled()
                && !FreecamConfig.INSTANCE.allowInteract
                && FreecamConfig.INSTANCE.interactionMode == FreecamConfig.InteractionMode.CAMERA) {
            event.setCanceled(true);
        }
    }
}
