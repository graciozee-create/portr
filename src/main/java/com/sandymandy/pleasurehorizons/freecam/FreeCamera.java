package com.sandymandy.pleasurehorizons.freecam;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * The entity the camera is attached to while freecam is active.
 *
 * <p>Upstream (Fabric) subclasses {@code ClientPlayerEntity} and hands it a hand-built
 * {@code ClientPlayNetworkHandler} so the vanilla player movement code can drive it. That
 * constructor takes a {@code ClientConnectionState} whose shape changes almost every release
 * and it drags in a pile of client-internal state purely to be thrown away. It is also the
 * reason upstream needs a dozen mixins to stop the fake player from being ticked, rendered,
 * lit and sound-tracked like a real one.</p>
 *
 * <p>This port instead uses a minimal free-floating entity. Everything the freecam actually
 * needs from the fake player is position, rotation and velocity, and this provides exactly
 * that. Minecraft only requires the camera entity to be an {@link Entity} - see
 * {@code GameRenderer#renderLevel}, which passes {@code minecraft.getCameraEntity()} straight
 * into {@code Camera#setup}. It is spawned into the {@link ClientLevel} so chunk loading and
 * the camera's own block lookups behave, and it never touches the server.</p>
 *
 * <p>{@link EntityType#MARKER} is used as the type because it is the one vanilla entity type
 * with no renderer, no hitbox and no ticking behaviour, so nothing is ever drawn where the
 * camera is.</p>
 */
@OnlyIn(Dist.CLIENT)
public class FreeCamera extends Entity {

    /** Movement carried over between ticks so acceleration feels like upstream's. */
    private Vec3 velocity = Vec3.ZERO;

    public FreeCamera(ClientLevel level, int id) {
        super(EntityType.MARKER, level);
        this.setId(id);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /** Places the camera at an entity, matching its rotation. */
    public void copyPositionAndRotation(Entity entity) {
        applyPosition(new FreecamPosition(entity));
    }

    public void applyPosition(FreecamPosition position) {
        this.setPos(position.x, position.y, position.z);
        this.setYRot(position.yaw);
        this.setXRot(position.pitch);
        // Prevents the camera from visibly interpolating in from the previous position
        // on the first frame after it is enabled.
        this.xRotO = position.pitch;
        this.yRotO = position.yaw;
        this.xo = position.x;
        this.yo = position.y;
        this.zo = position.z;
    }

    /**
     * Offsets the starting position according to the configured perspective, stopping early
     * if the camera would end up inside a block.
     */
    public void applyPerspective(FreecamConfig.Perspective perspective) {
        FreecamPosition position = new FreecamPosition(this);

        switch (perspective) {
            case INSIDE -> {
                // Camera stays exactly where the player's eyes are.
            }
            case FIRST_PERSON -> moveForwardUntilCollision(position, 0.4);
            case THIRD_PERSON_MIRROR -> {
                position.mirrorRotation();
                moveForwardUntilCollision(position, -4.0);
            }
            case THIRD_PERSON -> moveForwardUntilCollision(position, -4.0);
        }
    }

    /** Steps forward in small increments, backing off as soon as the camera hits geometry. */
    private void moveForwardUntilCollision(FreecamPosition position, double maxDistance) {
        boolean negative = maxDistance < 0;
        double remaining = negative ? -maxDistance : maxDistance;
        double increment = 0.1;

        for (double distance = 0.0; distance < remaining; distance += increment) {
            FreecamPosition previous = new FreecamPosition(
                    this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());

            position.moveForward(negative ? -increment : increment);
            applyPosition(position);

            if (!this.level().noCollision(this, this.getBoundingBox())) {
                applyPosition(previous);
                return;
            }
        }
    }

    public void spawn() {
        if (this.level() instanceof ClientLevel clientLevel) {
            clientLevel.addEntity(this);
        }
    }

    public void despawn() {
        if (this.level() instanceof ClientLevel clientLevel
                && clientLevel.getEntity(this.getId()) != null) {
            clientLevel.removeEntity(this.getId(), RemovalReason.DISCARDED);
        }
    }

    /** Applies the movement computed by {@link Motion} for this tick. */
    public void setVelocity(double x, double y, double z) {
        this.velocity = new Vec3(x, y, z);
    }

    public Vec3 getVelocity() {
        return this.velocity;
    }

    @Override
    public void tick() {
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        Motion.doMotion(this,
                FreecamConfig.INSTANCE.horizontalSpeed,
                FreecamConfig.INSTANCE.verticalSpeed);

        this.setPos(this.getX() + this.velocity.x,
                this.getY() + this.velocity.y,
                this.getZ() + this.velocity.z);
        this.setOnGround(false);
    }

    /** The camera must never be culled away or the world stops rendering around it. */
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    /** Keeps the camera out of water/lava fog and swimming logic. */
    @Override
    public boolean isInWater() {
        return FreecamConfig.INSTANCE.showSubmersion && super.isInWater();
    }

    @Override
    public boolean isUnderWater() {
        return FreecamConfig.INSTANCE.showSubmersion && super.isUnderWater();
    }

    /**
     * Swimming is the flattest vanilla pose, so the camera's eye sits essentially at its
     * position. {@code getEyeHeight} itself is final in 1.21.1 and derives from the pose,
     * so the pose is the only lever available here.
     */
    @Override
    public Pose getPose() {
        return Pose.SWIMMING;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {}
}
