package com.sandymandy.pleasurehorizons.util.rendering;
import net.minecraft.world.phys.Vec3;

public class JigglePhysics {
    private Vec3 velocity = Vec3.ZERO;
    private Vec3 displacement = Vec3.ZERO;
    public void update(Vec3 force) {}
    public Vec3 getDisplacement() { return Vec3.ZERO; }
    public Vec3 getInterpolatedDisplacement(double alpha) { return Vec3.ZERO; }
}
