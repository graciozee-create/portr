package com.sandymandy.pleasurehorizons.freecam;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A stored camera placement (position + rotation) plus the three basis vectors that
 * rotation implies.
 *
 * <p>Direct port of the upstream Fabric class. The basis vectors let {@link #move} walk the
 * camera along its own local axes, which is what the perspective offsets and the collision
 * stepping in {@link FreeCamera} need.</p>
 */
public class FreecamPosition {
    public double x;
    public double y;
    public double z;
    public float pitch;
    public float yaw;

    private final Quaternionf rotation = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
    private final Vector3f verticalPlane = new Vector3f(0.0F, 1.0F, 0.0F);
    private final Vector3f diagonalPlane = new Vector3f(1.0F, 0.0F, 0.0F);
    private final Vector3f horizontalPlane = new Vector3f(0.0F, 0.0F, 1.0F);

    public FreecamPosition(Entity entity) {
        this.x = entity.getX();
        // Upstream normalises to the swimming eye height so the camera starts at a
        // consistent height regardless of the player's pose. Using the eye position
        // directly is the equivalent here and avoids the pose bookkeeping.
        this.y = entity.getEyeY();
        this.z = entity.getZ();
        setRotation(entity.getYRot(), entity.getXRot());
    }

    public FreecamPosition(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        setRotation(yaw, pitch);
    }

    public void setRotation(float yaw, float pitch) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.rotation.rotationYXZ(-yaw * ((float) Math.PI / 180), pitch * ((float) Math.PI / 180), 0.0F);
        this.horizontalPlane.set(0.0F, 0.0F, 1.0F).rotate(this.rotation);
        this.verticalPlane.set(0.0F, 1.0F, 0.0F).rotate(this.rotation);
        this.diagonalPlane.set(1.0F, 0.0F, 0.0F).rotate(this.rotation);
    }

    public void mirrorRotation() {
        setRotation(this.yaw + 180.0F, -this.pitch);
    }

    public void moveForward(double distance) {
        move(distance, 0, 0);
    }

    public void move(double fwd, double up, double right) {
        this.x += (double) this.horizontalPlane.x() * fwd
                + (double) this.verticalPlane.x() * up
                + (double) this.diagonalPlane.x() * right;

        this.y += (double) this.horizontalPlane.y() * fwd
                + (double) this.verticalPlane.y() * up
                + (double) this.diagonalPlane.y() * right;

        this.z += (double) this.horizontalPlane.z() * fwd
                + (double) this.verticalPlane.z() * up
                + (double) this.diagonalPlane.z() * right;
    }

    public ChunkPos getChunkPos() {
        return new ChunkPos((int) (this.x / 16), (int) (this.z / 16));
    }
}
