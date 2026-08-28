package com.sandymandy.pleasurehorizons.freecam;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Turns the movement keys into a velocity for the free camera.
 *
 * <p>Upstream reads {@code freeCamera.input.playerInput}, i.e. the fake player's own
 * {@code KeyboardInput}. This port has no fake player, so the key mappings are polled
 * straight from {@link Options}. The resulting maths - including the diagonal correction so
 * strafing diagonally is not faster than going straight - matches upstream exactly.</p>
 */
@OnlyIn(Dist.CLIENT)
public class Motion {

    public static final double DIAGONAL_MULTIPLIER = Mth.sin((float) Math.toRadians(45));

    private Motion() {}

    public static void doMotion(FreeCamera freeCamera, double hSpeed, double vSpeed) {
        Minecraft mc = Minecraft.getInstance();
        Options options = mc.options;

        float yaw = freeCamera.getYRot();
        double velocityX = 0.0;
        double velocityY = 0.0;
        double velocityZ = 0.0;

        Vec3 forward = Vec3.directionFromRotation(0.0F, yaw);
        Vec3 side = Vec3.directionFromRotation(0.0F, yaw + 90.0F);

        // While the player retains control the camera must not also react to the keys.
        if (!Freecam.isPlayerControlEnabled()) {
            hSpeed = hSpeed * (options.keySprint.isDown() ? 1.5 : 1.0);

            boolean straight = false;
            if (options.keyUp.isDown()) {
                velocityX += forward.x * hSpeed;
                velocityZ += forward.z * hSpeed;
                straight = true;
            }
            if (options.keyDown.isDown()) {
                velocityX -= forward.x * hSpeed;
                velocityZ -= forward.z * hSpeed;
                straight = true;
            }

            boolean strafing = false;
            if (options.keyRight.isDown()) {
                velocityZ += side.z * hSpeed;
                velocityX += side.x * hSpeed;
                strafing = true;
            }
            if (options.keyLeft.isDown()) {
                velocityZ -= side.z * hSpeed;
                velocityX -= side.x * hSpeed;
                strafing = true;
            }

            if (straight && strafing) {
                velocityX *= DIAGONAL_MULTIPLIER;
                velocityZ *= DIAGONAL_MULTIPLIER;
            }

            if (options.keyJump.isDown()) {
                velocityY += vSpeed;
            }
            if (options.keyShift.isDown()) {
                velocityY -= vSpeed;
            }
        }

        freeCamera.setVelocity(velocityX, velocityY, velocityZ);
    }
}
