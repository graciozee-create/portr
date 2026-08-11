package com.sandymandy.pleasurehorizons.freecam;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.level.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.ServerLinks;
import net.minecraft.util.PlayerInput;
import net.minecraft.core.BlockPos;
import com.sandymandy.pleasurehorizons.config.ModConfig;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.UUID;

import static com.sandymandy.pleasurehorizons.freecam.Freecam.MC;

@ApiStatus.Internal
@ApiStatus.AvailableSince("0.4.0")
public class FreeCamera extends ClientPlayerEntity {

    private static final ClientPlayNetworkHandler NETWORK_HANDLER = new ClientPlayNetworkHandler(
            MC,
            MC.getNetworkHandler().getConnection(),
            new ClientConnectionState(
                    new GameProfile(UUID.randomUUID(), "FreeCamera"),
                    MC.getTelemetryManager().createWorldSession(false, null, null),
                    MC.player.getRegistryManager().toImmutable(),
                    FeatureSet.empty(),
                    null,
                    MC.getCurrentServerEntry(),
                    MC.currentScreen,
                    Collections.emptyMap(),
                    MC.inGameHud.getChatHud().toChatState(),
                    Collections.emptyMap(),
                    ServerLinks.EMPTY)) {
        @Override
        public void sendPacket(Packet<?> packet) {
        }
    };

    public FreeCamera(int id) {
        super(MC, MC.world, NETWORK_HANDLER, MC.player.getStatHandler(), MC.player.getRecipeBook(), PlayerInput.DEFAULT, false);

        setId(id);
        setPose(EntityPose.SWIMMING);
        setLoaded(true); // Otherwise input is frozen until timeout
        getAbilities().flying = true;
        input = new KeyboardInput(MC.options);
    }

    @Override
    public void copyPositionAndRotation(Entity entity) {
        applyPosition(new FreecamPosition(entity));
    }

    public void applyPosition(FreecamPosition position) {
        refreshPositionAndAngles(position.x, position.y, position.z, position.yaw, position.pitch);
        renderPitch = getPitch();
        renderYaw = getYaw();
        lastRenderPitch = renderPitch; // Prevents camera from rotating upon entering freecam.
        lastRenderYaw = renderYaw;
    }

    public void applyPerspective(ModConfig.Perspective perspective) {
        FreecamPosition position = new FreecamPosition(this);

        switch (perspective) {
            case INSIDE:
                break;
            case FIRST_PERSON:
                moveForwardUntilCollision(position, 0.4);
                break;
            case THIRD_PERSON_MIRROR:
                position.mirrorRotation();
            case THIRD_PERSON:
                moveForwardUntilCollision(position, -4.0);
                break;
        }
    }

    private boolean moveForwardUntilCollision(FreecamPosition position, double distance, boolean checkCollision) {
        if (!checkCollision) {
            position.moveForward(distance);
            applyPosition(position);
            return true;
        }
        return moveForwardUntilCollision(position, distance);
    }

    private boolean moveForwardUntilCollision(FreecamPosition position, double maxDistance) {
        boolean negative = maxDistance < 0;
        maxDistance = negative ? -1 * maxDistance : maxDistance;
        double increment = 0.1;

        // Move forward by increment until we reach maxDistance or hit a collision
        for (double distance = 0.0; distance < maxDistance; distance += increment) {
            FreecamPosition oldPosition = new FreecamPosition(this);

            position.moveForward(negative ? -1 * increment : increment);
            applyPosition(position);

            if (!wouldNotSuffocateInPose(getPose())) {
                // Revert to last non-colliding position and return whether we were unable to move at all
                applyPosition(oldPosition);
                return distance > 0;
            }
        }

        return true;
    }

    public void spawn() {
        if (clientWorld != null) {
            clientWorld.addEntity(this);
        }
    }

    public void despawn() {
        if (clientWorld != null && clientWorld.getEntityById(getId()) != null) {
            clientWorld.removeEntity(getId(), RemovalReason.DISCARDED);
        }
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
    }

    @Override
    public float getHandSwingProgress(float tickDelta) {
        return MC.player.getHandSwingProgress(tickDelta);
    }

    @Override
    public int getItemUseTimeLeft() {
        return MC.player.getItemUseTimeLeft();
    }

    @Override
    public boolean isUsingItem() {
        return MC.player.isUsingItem();
    }

    @Override
    public boolean isClimbing() {
        return false;
    }

    @Override
    public boolean isTouchingWater() {
        return false;
    }

    @Override
    public StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> holder) {
        return MC.player.getStatusEffect(holder);
    }

    @Override
    public PistonBehavior getPistonBehavior() {
        return PistonBehavior.IGNORE;
    }

    @Override
    public boolean collidesWith(Entity other) {
        return false;
    }

    @Override
    public void setPose(EntityPose pose) {
        super.setPose(EntityPose.SWIMMING);
    }

    @Override
    public boolean shouldSlowDown() {
        return false;
    }

    @Override
    protected boolean updateWaterSubmersionState() {
        this.isSubmergedInWater = this.isSubmergedIn(FluidTags.WATER);
        return this.isSubmergedInWater;
    }

    @Override
    protected void onSwimmingStart() {}

    @Override
    public void tickMovement() {
        if (ModConfig.INSTANCE.movement.flightMode.equals(ModConfig.FlightMode.DEFAULT)) {
            getAbilities().setFlySpeed(0);
            Motion.doMotion(this, ModConfig.INSTANCE.movement.horizontalSpeed, ModConfig.INSTANCE.movement.verticalSpeed);
        } else {
            getAbilities().setFlySpeed((float) ModConfig.INSTANCE.movement.verticalSpeed / 10);
        }
        super.tickMovement();
        getAbilities().flying = true;
        setOnGround(false);
    }
}
