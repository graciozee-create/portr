package com.sandymandy.pleasurehorizons.util.rendering;

import net.minecraft.world.phys.Vec3;

/**
 * A single spring-damper used to make a bone lag behind the body's motion.
 *
 * <p>Direct port of the upstream class - the maths is plain vector arithmetic with no
 * version-specific API, so it carries over unchanged apart from {@code Vec3d} to {@link Vec3}.
 * The port previously shipped this as a stub whose {@code update} did nothing and whose
 * getters always returned {@link Vec3#ZERO}, which is why the girls' models were completely
 * rigid.</p>
 *
 * <p>{@link #update} must be driven on a fixed timestep; the caller interpolates between the
 * last two steps with {@link #getInterpolatedDisplacement(double)} so the result is smooth at
 * any frame rate.</p>
 */
public class JigglePhysics {
    private Vec3 velocity = Vec3.ZERO;
    private Vec3 displacement = Vec3.ZERO;
    private Vec3 prevDisplacement = Vec3.ZERO;

    private final double stiffness;
    private final double damping;

    public JigglePhysics(double stiffness, double damping) {
        this.stiffness = stiffness;
        this.damping = damping;
    }

    /**
     * Advances the simulation by one fixed step under the given driving force
     * (entity inertia).
     */
    public void update(Vec3 force) {
        this.prevDisplacement = this.displacement;

        Vec3 acceleration = force
                .subtract(this.displacement.scale(this.stiffness))
                .subtract(this.velocity.scale(this.damping));

        this.velocity = this.velocity.add(acceleration);
        this.displacement = this.displacement.add(this.velocity);
    }

    public Vec3 getDisplacement() {
        return this.displacement;
    }

    /**
     * Smoothly blends the last two simulation steps.
     *
     * @param alpha 0.0 for the previous step, 1.0 for the current one
     */
    public Vec3 getInterpolatedDisplacement(double alpha) {
        return this.prevDisplacement.lerp(this.displacement, alpha);
    }

    public void reset() {
        this.velocity = Vec3.ZERO;
        this.displacement = Vec3.ZERO;
        this.prevDisplacement = Vec3.ZERO;
    }

    public void dampen(double factor) {
        this.velocity = this.velocity.scale(factor);
        this.displacement = this.displacement.scale(factor);
    }
}
