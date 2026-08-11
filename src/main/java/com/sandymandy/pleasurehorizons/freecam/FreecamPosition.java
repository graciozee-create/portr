package com.sandymandy.pleasurehorizons.freecam;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ChunkPos;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FreecamPosition {
    public double x, y, z;
    public float pitch, yaw;

    public FreecamPosition(Entity entity) {
        x = entity.getX();
        y = entity.getY();
        z = entity.getZ();
        yaw = entity.getYRot();
        pitch = entity.getXRot();
    }

    public void setRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void mirrorRotation() {
        setRotation(yaw + 180.0F, -pitch);
    }

    public void moveForward(double distance) {}

    public void move(double fwd, double up, double right) {}

    public ChunkPos getChunkPos() {
        return new ChunkPos((int)(x/16), (int)(z/16));
    }
}
