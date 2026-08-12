package com.sandymandy.pleasurehorizons.freecam;

import com.sandymandy.pleasurehorizons.freecam.tripod.TripodRegistry;
import com.sandymandy.pleasurehorizons.freecam.tripod.TripodSlot;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

/**
 * Freecam state machine: detaches the camera from the player and flies it around.
 *
 * <p>Ported from the Fabric original. The behaviour - free camera, nine tripod slots, an
 * optional "player control" mode that hands the keys back to the player while the camera stays
 * put - is preserved. The <em>mechanism</em> differs, because the upstream version is built on
 * eighteen mixins into client internals and NeoForge offers first-class events for nearly all
 * of it:</p>
 *
 * <ul>
 *   <li>Camera detach: {@code Minecraft#setCameraEntity}. {@code GameRenderer#renderLevel}
 *       feeds that entity straight into {@code Camera#setup}, so no {@code CameraMixin} is
 *       needed.</li>
 *   <li>Suppressing player movement: {@code MovementInputUpdateEvent} zeroes the player's
 *       input instead of swapping {@code player.input} for a neutered instance.</li>
 *   <li>Hiding the player model, the held item and the block outline: the corresponding
 *       NeoForge render events, instead of {@code EntityRendererMixin} /
 *       {@code ItemInHandRendererMixin} / {@code LevelRendererMixin}.</li>
 * </ul>
 *
 * <p>All of that lives in {@code FreecamHandler}; this class only owns the state.</p>
 */
@OnlyIn(Dist.CLIENT)
public class Freecam {

    private static boolean freecamEnabled = false;
    private static boolean tripodEnabled = false;
    private static boolean playerControlEnabled = false;
    private static boolean disableNextTick = false;

    private static final TripodRegistry TRIPODS = new TripodRegistry();
    private static TripodSlot activeTripod = TripodSlot.NONE;
    private static @Nullable FreeCamera freeCamera;
    private static @Nullable CameraType rememberedF5 = null;

    /** Ids are negative so they cannot collide with server-assigned entity ids. */
    private static final int FREECAM_ENTITY_ID = -420;

    private Freecam() {}

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    // ------------------------------------------------------------------ lifecycle

    public static void onClientTick() {
        if (disableNextTick && isEnabled()) {
            toggle();
        }
        disableNextTick = false;

        // The camera entity is not in the level's ticking list, so drive it here.
        if (isEnabled() && freeCamera != null) {
            freeCamera.tick();
        }
    }

    /** Leaves freecam and forgets all tripods when the world goes away. */
    public static void onDisconnect() {
        if (isEnabled()) {
            toggle();
        }
        TRIPODS.clear();
        freecamEnabled = false;
        tripodEnabled = false;
        activeTripod = TripodSlot.NONE;
        freeCamera = null;
    }

    // ------------------------------------------------------------------ toggles

    public static void toggle() {
        if (mc().level == null || mc().player == null) {
            return;
        }

        if (tripodEnabled) {
            toggleTripod(activeTripod);
            return;
        }

        if (freecamEnabled) {
            onDisableFreecam();
        } else {
            onEnableFreecam();
        }
        freecamEnabled = !freecamEnabled;
        if (!freecamEnabled) {
            onDisabled();
        }
    }

    public static void toggleTripod(TripodSlot tripod) {
        if (tripod == TripodSlot.NONE || mc().level == null || mc().player == null) {
            return;
        }

        if (tripodEnabled) {
            if (activeTripod == tripod) {
                onDisableTripod();
                tripodEnabled = false;
            } else {
                onDisableTripod();
                onEnableTripod(tripod);
            }
        } else {
            if (freecamEnabled) {
                toggle();
            }
            onEnableTripod(tripod);
            tripodEnabled = true;
        }
        if (!tripodEnabled) {
            onDisabled();
        }
    }

    /** Hands the movement keys back to the player while the camera stays where it is. */
    public static void switchControls() {
        if (!isEnabled()) {
            return;
        }
        playerControlEnabled = !playerControlEnabled;
    }

    // ------------------------------------------------------------------ tripods

    private static void onEnableTripod(TripodSlot tripod) {
        onEnable();

        FreecamPosition position = TRIPODS.get(tripod);
        boolean chunkLoaded = false;
        if (position != null && mc().level != null) {
            ChunkPos chunkPos = position.getChunkPos();
            chunkLoaded = mc().level.getChunkSource().hasChunk(chunkPos.x, chunkPos.z);
        }

        if (!chunkLoaded) {
            resetCamera(tripod);
            position = null;
        }

        freeCamera = new FreeCamera(mc().level, FREECAM_ENTITY_ID - tripod.ordinal());
        if (position == null) {
            moveToPlayer();
        } else {
            moveToPosition(position);
        }

        freeCamera.spawn();
        mc().setCameraEntity(freeCamera);
        activeTripod = tripod;

        if (FreecamConfig.INSTANCE.notifyTripod) {
            notifyPlayer(Component.translatable("msg.pleasurehorizons.freecam.open_tripod", tripod.toString()));
        }
    }

    private static void onDisableTripod() {
        if (freeCamera != null) {
            TRIPODS.put(activeTripod, new FreecamPosition(freeCamera));
        }
        onDisable();

        if (FreecamConfig.INSTANCE.notifyTripod) {
            notifyPlayer(Component.translatable("msg.pleasurehorizons.freecam.close_tripod", activeTripod.toString()));
        }
        activeTripod = TripodSlot.NONE;
    }

    /** Clears a stored tripod position, or recentres it on the player if it is the live one. */
    public static void resetCamera(TripodSlot tripod) {
        if (tripodEnabled && activeTripod != TripodSlot.NONE && activeTripod == tripod && freeCamera != null) {
            moveToPlayer();
        } else {
            TRIPODS.put(tripod, null);
        }

        if (FreecamConfig.INSTANCE.notifyTripod) {
            notifyPlayer(Component.translatable("msg.pleasurehorizons.freecam.tripod_reset", tripod.toString()));
        }
    }

    // ------------------------------------------------------------------ freecam

    private static void onEnableFreecam() {
        onEnable();
        freeCamera = new FreeCamera(mc().level, FREECAM_ENTITY_ID);
        moveToPlayer();
        freeCamera.spawn();
        mc().setCameraEntity(freeCamera);

        if (FreecamConfig.INSTANCE.notifyFreecam) {
            notifyPlayer(Component.translatable("msg.pleasurehorizons.freecam.enable"));
        }
    }

    private static void onDisableFreecam() {
        onDisable();

        if (FreecamConfig.INSTANCE.notifyFreecam) {
            notifyPlayer(Component.translatable("msg.pleasurehorizons.freecam.disable"));
        }
    }

    private static void onEnable() {
        // Chunks behind the player must keep rendering once the camera can fly behind them.
        mc().smartCull = false;

        rememberedF5 = mc().options.getCameraType();
        if (mc().options.getCameraType() != CameraType.FIRST_PERSON) {
            mc().options.setCameraType(CameraType.FIRST_PERSON);
        }
    }

    private static void onDisable() {
        mc().smartCull = true;
        mc().setCameraEntity(mc().player);
        playerControlEnabled = false;
        if (freeCamera != null) {
            freeCamera.despawn();
            freeCamera = null;
        }
    }

    private static void onDisabled() {
        if (rememberedF5 != null) {
            mc().options.setCameraType(rememberedF5);
            rememberedF5 = null;
        }
    }

    // ------------------------------------------------------------------ movement

    public static void moveToEntity(@Nullable Entity entity) {
        if (freeCamera == null) {
            return;
        }
        if (entity == null) {
            moveToPlayer();
            return;
        }
        freeCamera.copyPositionAndRotation(entity);
    }

    public static void moveToPosition(@Nullable FreecamPosition position) {
        if (freeCamera == null) {
            return;
        }
        if (position == null) {
            moveToPlayer();
            return;
        }
        freeCamera.applyPosition(position);
    }

    public static void moveToPlayer() {
        if (freeCamera == null || mc().player == null) {
            return;
        }
        freeCamera.copyPositionAndRotation(mc().player);
        freeCamera.applyPerspective(FreecamConfig.INSTANCE.perspective);
    }

    /** Applies a mouse delta to the camera instead of the player. */
    public static void turnCamera(double yawDelta, double pitchDelta) {
        if (freeCamera == null) {
            return;
        }
        float yaw = (float) (freeCamera.getYRot() + yawDelta);
        float pitch = (float) (freeCamera.getXRot() + pitchDelta);
        freeCamera.setYRot(yaw);
        freeCamera.setXRot(Math.max(-90.0F, Math.min(90.0F, pitch)));
    }

    private static void notifyPlayer(Component message) {
        if (mc().player != null) {
            mc().player.displayClientMessage(message, true);
        }
    }

    // ------------------------------------------------------------------ state

    public static @Nullable FreeCamera getFreeCamera() {
        return freeCamera;
    }

    public static void disableNextTick() {
        disableNextTick = true;
    }

    public static boolean isEnabled() {
        return freecamEnabled || tripodEnabled;
    }

    public static boolean isFreecamEnabled() {
        return freecamEnabled;
    }

    public static boolean isTripodEnabled() {
        return tripodEnabled;
    }

    public static boolean isPlayerControlEnabled() {
        return playerControlEnabled;
    }

    public static TripodSlot getActiveTripod() {
        return activeTripod;
    }
}
