package com.sandymandy.pleasurehorizons.util.rendering;

import net.minecraft.util.math.Vec3d;

public class JigglePhysics {
    private Vec3d velocity = Vec3d.ZERO;
    private Vec3d displacement = Vec3d.ZERO;
    private Vec3d prevDisplacement = Vec3d.ZERO; // For interpolation

    private final double stiffness;
    private final double damping;

    public JigglePhysics(double stiffness, double damping) {
        this.stiffness = stiffness;
        this.damping = damping;
    }

    /**
     * Updates the jiggle simulation using a force vector (e.g., entity movement or inertia).
     * This should be called at a fixed timestep (e.g., 1/60 sec).
     */
    public void update(Vec3d force) {
        // Save previous state for interpolation
        prevDisplacement = displacement;

        // Basic spring-damper model integration
        Vec3d acceleration = force
                .subtract(displacement.multiply(stiffness))
                .subtract(velocity.multiply(damping));

        velocity = velocity.add(acceleration);
        displacement = displacement.add(velocity);
    }

    /**
     * Returns the current (latest) displacement of the jiggle bone.
     */
    public Vec3d getDisplacement() {
        return displacement;
    }

    /**
     * Returns a smooth interpolated displacement between the previous and current step.
     * @param alpha value between 0.0 (previous frame) and 1.0 (current frame)
     */
    public Vec3d getInterpolatedDisplacement(double alpha) {
        return prevDisplacement.lerp(displacement, alpha);
    }

    /**
     * Optional reset if needed (e.g., when entity respawns or resets)
     */
    public void reset() {
        velocity = Vec3d.ZERO;
        displacement = Vec3d.ZERO;
        prevDisplacement = Vec3d.ZERO;
    }

    public void dampen(double factor) {
        velocity = velocity.multiply(factor);
        displacement = displacement.multiply(factor);
    }
}
