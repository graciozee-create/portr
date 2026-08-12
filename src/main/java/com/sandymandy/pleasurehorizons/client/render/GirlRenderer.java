package com.sandymandy.pleasurehorizons.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import com.sandymandy.pleasurehorizons.util.rendering.JigglePhysics;
import com.sandymandy.pleasurehorizons.util.rendering.OffsetVertexConsumer;
import com.sandymandy.pleasurehorizons.util.variables.JiggleBoneConfig;

import java.util.List;
import java.util.Map;

public class GirlRenderer<T extends GirlSceneEntity> extends GeoEntityRenderer<T> {
    public GirlRenderer(EntityRendererProvider.Context context) {
        super(context, new GirlModel<>());
        this.shadowRadius = 0.4F;

        // Draws the partner skeleton with the scene player's skin instead of the girl's sheet.
        this.addRenderLayer(new com.sandymandy.pleasurehorizons.client.rendering.layers
                .BoneOverrideRenderLayer<>(this));

        this.addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, T animatable) {
                if ("weapon".equals(bone.getName()) && !animatable.isSceneActive()) {
                    return animatable.getMainHandItem();
                }
                return null;
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, T animatable) {
                return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, T animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                if ("weapon".equals(bone.getName())) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(animatable.getWeaponBoneXRotation()));
                    poseStack.scale(0.7F, 0.7F, 0.7F);
                }
                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
    }

    /**
     * Applies per-bone visibility before the model is drawn.
     *
     * <p>Every girl rig embeds a second, full "steve" partner skeleton (132 bones / 670 cubes)
     * pivoted at z = -16, i.e. exactly one block behind her. It is only meant to be visible
     * during a scene. Nothing ever hid it in this port, so it rendered permanently as a
     * detached, un-animated body floating one block away from the girl.</p>
     *
     * <p>{@link software.bernie.geckolib.cache.object.GeoBone#setHidden(boolean)} also hides
     * children, which is what we want here: hiding {@code steve} removes the whole sub-tree.
     * Bones are shared, baked model state, so every flag must be written on every frame -
     * otherwise a hidden bone stays hidden for all other girls using the same model.</p>
     */
    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model,
                          @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight,
                          int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, colour);

        boolean isCarried = animatable.getVehicle() instanceof net.minecraft.world.entity.player.Player;

        if (isCarried) {
            // Client-side ticking does not reliably reach passengers, so her world position
            // and rotation are recomputed here rather than in tick(). See snapToCarrier.
            snapToCarrier(animatable);

            // Sat on the carrier's shoulder.
            //
            // The previous version placed her out in front of the player and, in first
            // person, off to the side at half scale - which read as "floating a couple of
            // blocks away" rather than being carried. She is now parked directly on the
            // right shoulder, close in, and the knees are tucked up by the bone pose below.
            //
            // Everything here is in the entity's own space, and the entity is already
            // positioned at the carrier by the vehicle attachment point, so these are small
            // local offsets rather than world coordinates. Because tick() pins her yaw to
            // the carrier's, no yaw compensation is needed - she turns with him.
            poseStack.translate(SHOULDER_RIGHT, SHOULDER_UP, SHOULDER_FORWARD);
            poseStack.scale(CARRY_SCALE, CARRY_SCALE, CARRY_SCALE);

            // Slight inward lean so she rests against the head instead of sitting bolt
            // upright, plus a gentle sway driven by the carrier's walk cycle.
            poseStack.mulPose(Axis.ZP.rotationDegrees(-8.0F));
            if (animatable.getVehicle() instanceof net.minecraft.world.entity.player.Player player) {
                float walk = player.walkAnimation.position(partialTick);
                float sway = net.minecraft.util.Mth.sin(walk * 0.6F) * 3.0F
                        * player.walkAnimation.speed(partialTick);
                poseStack.mulPose(Axis.XP.rotationDegrees(sway));
            }

            // Mika has a real carry animation, so let it own the pose rather than
            // overwriting the very bones it is animating.
            if (!animatable.hasCarryAnimation()) {
                applyCarryPose(model);
            } else {
                clearCarryPose(model);
            }
        } else {
            // Baked bones are shared, so the pose must be cleared for everyone else.
            clearCarryPose(model);
        }

        // The partner rig is scene-only; keep the whole sub-tree hidden otherwise.
        boolean sceneActive = animatable.isSceneActive();
        model.getBone(PARTNER_BONE).ifPresent(bone -> bone.setHidden(!sceneActive));

        Map<String, Boolean> boneVisibility = animatable.boneVisibility;
        if (boneVisibility != null && !boneVisibility.isEmpty()) {
            for (Map.Entry<String, Boolean> entry : boneVisibility.entrySet()) {
                Boolean visible = entry.getValue();
                if (visible == null) {
                    continue;
                }
                model.getBone(entry.getKey()).ifPresent(bone -> bone.setHidden(!visible));
            }
        }

        updatePartnerSkin(animatable);
        applyHeadTracking(animatable, model);
        applyJigglePhysics(animatable, model);
        applyBoneScales(animatable, model);
        applyBonePositions(animatable, model);
    }

    /**
     * Forces a carried girl to the carrier's position every frame.
     *
     * <p>This is the reason she appeared to levitate in place while the carrier walked and
     * turned around her. Positioning a passenger is done by {@code Entity#rideTick}, which
     * calls {@code vehicle.positionRider(this)} - but {@code ClientLevel#tickEntities} skips
     * every entity for which {@code isPassenger()} is true, and only reaches passengers
     * through {@code tickPassenger}, which in turn requires the passenger to be present in
     * the client's {@code tickingEntities} set. A girl who was already loaded before she was
     * mounted, or whose chunk stops ticking, never gets there, so {@code positionRider} is
     * never called on the client and she simply stays at the last position the server sent -
     * which is also why she stayed put while the player rotated, and turned up behind him.</p>
     *
     * <p>Rather than depend on that, the position is recomputed here from the vehicle itself.
     * Rendering happens every frame regardless of ticking, so this cannot be missed. The
     * previous-frame position is written too, otherwise the renderer interpolates from her
     * stale world position and she visibly streaks across the screen.</p>
     */
    private void snapToCarrier(T animatable) {
        net.minecraft.world.entity.Entity vehicle = animatable.getVehicle();
        if (vehicle == null) {
            return;
        }

        // Same maths as Entity#positionRider: where the vehicle wants the rider, minus the
        // rider's own attachment offset.
        net.minecraft.world.phys.Vec3 seat = vehicle.getPassengerRidingPosition(animatable);
        net.minecraft.world.phys.Vec3 attachment = animatable.getVehicleAttachmentPoint(vehicle);
        double x = seat.x - attachment.x;
        double y = seat.y - attachment.y;
        double z = seat.z - attachment.z;

        animatable.setPos(x, y, z);
        animatable.xo = x;
        animatable.yo = y;
        animatable.zo = z;
        animatable.xOld = x;
        animatable.yOld = y;
        animatable.zOld = z;

        // Rotation is pinned in TameableGirlEntity#tick, which is skipped for the same
        // reason, so it is mirrored here. Body yaw follows the carrier's body - not his
        // look yaw, or she would swing around whenever he moved the mouse.
        if (vehicle instanceof net.minecraft.world.entity.LivingEntity carrier) {
            float bodyYaw = carrier.yBodyRot;
            animatable.setYRot(bodyYaw);
            animatable.yRotO = bodyYaw;
            animatable.setYBodyRot(bodyYaw);
            animatable.yBodyRotO = bodyYaw;
            animatable.setYHeadRot(bodyYaw);
            animatable.yHeadRotO = bodyYaw;
            animatable.setXRot(0.0F);
            animatable.xRotO = 0.0F;
        }
    }

    /**
     * Tucks the knees up so she sits on the shoulder instead of standing rigid in mid-air.
     *
     * <p>None of the rigs ship a real carry animation - only Mika has {@code carry_slow1},
     * and that one belongs to a scene - so the pose is posed by hand here. Hips bend forward,
     * shins fold back under the thighs and the arms come down, which reads as sitting with
     * the knees drawn up.</p>
     *
     * <p>These bones are also driven by the walk/idle animation that just ran, so the values
     * are assigned rather than added; and since baked GeckoLib bones are shared between every
     * girl using the rig, {@link #CARRY_POSE_BONES} is reset on any girl that is not being
     * carried, otherwise one carried girl would leave every other girl in a sitting pose.</p>
     */
    private void applyCarryPose(BakedGeoModel model) {
        setBoneRotation(model, "legL", CARRY_THIGH_PITCH, 0.0F, CARRY_THIGH_SPREAD);
        setBoneRotation(model, "legR", CARRY_THIGH_PITCH, 0.0F, -CARRY_THIGH_SPREAD);
        setBoneRotation(model, "shinL", CARRY_SHIN_PITCH, 0.0F, 0.0F);
        setBoneRotation(model, "shinR", CARRY_SHIN_PITCH, 0.0F, 0.0F);
        setBoneRotation(model, "armL", CARRY_ARM_PITCH, 0.0F, CARRY_ARM_SPREAD);
        setBoneRotation(model, "armR", CARRY_ARM_PITCH, 0.0F, -CARRY_ARM_SPREAD);
    }

    /** Puts the carry-pose bones back to their animated rotation for girls on the ground. */
    private void clearCarryPose(BakedGeoModel model) {
        for (String boneName : CARRY_POSE_BONES) {
            model.getBone(boneName).ifPresent(bone -> {
                bone.setRotX(0.0F);
                bone.setRotY(0.0F);
                bone.setRotZ(0.0F);
            });
        }
    }

    private static void setBoneRotation(BakedGeoModel model, String boneName,
                                        float xDeg, float yDeg, float zDeg) {
        model.getBone(boneName).ifPresent(bone -> {
            bone.setRotX(xDeg * ((float) Math.PI / 180F));
            bone.setRotY(yDeg * ((float) Math.PI / 180F));
            bone.setRotZ(zDeg * ((float) Math.PI / 180F));
        });
    }

    /**
     * Dresses the embedded partner skeleton in the scene player's actual skin.
     *
     * <p>The {@code steve} sub-tree is a full second body baked into every girl rig. It has no
     * texture of its own, so without an override it is drawn with the girl's texture sheet and
     * comes out as a mess of misplaced UVs. Upstream sets this from
     * {@code GirlSceneEntity#applySkinToBone}; that method was never ported, so the maps stayed
     * empty and the partner was always mis-textured during scenes.</p>
     *
     * <p>Skins resolve asynchronously, so this is refreshed every frame while a scene runs
     * rather than once at scene start - the first frames would otherwise use the fallback.</p>
     */
    private void updatePartnerSkin(T animatable) {
        if (!animatable.isSceneActive()) {
            if (!animatable.boneTextureOverrides.isEmpty()) {
                animatable.boneTextureOverrides.clear();
                animatable.boneTextureOverridesLayer2.clear();
            }
            return;
        }

        // Vanilla Steve is the fallback so the partner is never left untextured.
        net.minecraft.resources.ResourceLocation skin =
                net.minecraft.resources.ResourceLocation.withDefaultNamespace(
                        "textures/entity/player/wide/steve.png");

        if (animatable.getScenePlayer() instanceof net.minecraft.client.player.AbstractClientPlayer scenePlayer) {
            net.minecraft.client.resources.PlayerSkin playerSkin = scenePlayer.getSkin();
            if (playerSkin != null && playerSkin.texture() != null) {
                skin = playerSkin.texture();
                // The slim/wide flag is synched entity data owned by the server; writing it
                // from the renderer would be overwritten on the next sync anyway, so the
                // model choice is left to whatever the server already published.
            }
        }

        animatable.boneTextureOverrides.put(PARTNER_BONE, skin);
        animatable.boneTextureOverridesLayer2.put(PARTNER_BONE,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                        com.sandymandy.pleasurehorizons.PleasureHorizons.MOD_ID,
                        "textures/player/penis.png"));
    }

    /**
     * Makes the girl actually look where she is facing.
     *
     * <p>Upstream does this in {@code AbstractGirlModel#setCustomAnimations}, which this port
     * never had, so the head stayed locked forward no matter where she looked - one of the
     * "model nuances" that made the girls look lifeless. Suppressed during scenes, where the
     * animation owns the head.</p>
     */
    private void applyHeadTracking(T animatable, BakedGeoModel model) {
        model.getBone(HEAD_BONE).ifPresent(bone -> {
            if (animatable.isSceneActive()) {
                bone.setRotX(0.0F);
                bone.setRotY(0.0F);
                return;
            }
            // Head yaw relative to the body, so she does not twist her neck when turning.
            float relativeYaw = net.minecraft.util.Mth.wrapDegrees(
                    animatable.getYHeadRot() - animatable.yBodyRot);
            relativeYaw = net.minecraft.util.Mth.clamp(relativeYaw, -60.0F, 60.0F);
            float pitch = net.minecraft.util.Mth.clamp(animatable.getXRot(), -45.0F, 45.0F);

            bone.setRotX(-pitch * ((float) Math.PI / 180F));
            bone.setRotY(-relativeYaw * ((float) Math.PI / 180F));
        });
    }

    /**
     * Secondary motion for the soft bones.
     *
     * <p>Upstream runs a spring-damper per bone from {@code AbstractGirlModel}; the port shipped
     * {@code JigglePhysics} as a no-op stub, so nothing ever moved. The simulation is stepped at
     * a fixed 25 Hz and interpolated, exactly as upstream does, so it is frame-rate independent.
     * State is keyed per entity, and the rest rotation is captured from the animation's current
     * pose each frame so the offset is added on top of whatever the animation is doing.</p>
     *
     * <p>Skipped while a scene is playing, while she is carried and while sprinting - the same
     * conditions upstream uses.</p>
     */
    private void applyJigglePhysics(T animatable, BakedGeoModel model) {
        boolean suppressed = animatable.isSceneActive()
                || animatable.isPassenger()
                || animatable.isSprinting();

        java.util.UUID id = animatable.getUUID();
        if (suppressed) {
            JIGGLE_STATE.remove(id);
            return;
        }

        JiggleState state = JIGGLE_STATE.computeIfAbsent(id, key -> new JiggleState());
        state.lastSeenFrame = FRAME_COUNTER;

        // Drop state for girls that have not been rendered recently, otherwise the map would
        // grow for the whole session as girls despawn or the player changes dimension.
        if (JIGGLE_STATE.size() > MAX_TRACKED_GIRLS) {
            JIGGLE_STATE.values().removeIf(tracked -> FRAME_COUNTER - tracked.lastSeenFrame > 200);
        }
        FRAME_COUNTER++;

        // Driving force: change in velocity plus a contribution from turning on the spot.
        net.minecraft.world.phys.Vec3 velocity = animatable.getDeltaMovement();
        net.minecraft.world.phys.Vec3 deltaVelocity = velocity.subtract(state.previousVelocity);
        state.previousVelocity = velocity;

        float currentYaw = animatable.getYRot();
        float yawDelta = net.minecraft.util.Mth.wrapDegrees(currentYaw - state.previousYaw);
        state.previousYaw = currentYaw;

        net.minecraft.world.phys.Vec3 force = deltaVelocity.scale(1.2)
                .add(Math.sin(Math.toRadians(currentYaw)) * yawDelta * 0.05,
                        0.0,
                        Math.cos(Math.toRadians(currentYaw)) * yawDelta * 0.05);

        long now = System.nanoTime();
        double deltaSec = state.lastUpdateNanos == 0L ? 0.0 : (now - state.lastUpdateNanos) / 1_000_000_000.0;
        state.lastUpdateNanos = now;

        state.accumulator = Math.min(state.accumulator + deltaSec, FIXED_TIMESTEP * 5);

        List<JiggleBoneConfig> configs = jiggleBones(animatable);

        while (state.accumulator >= FIXED_TIMESTEP) {
            for (JiggleBoneConfig config : configs) {
                state.physics
                        .computeIfAbsent(config.boneName(),
                                key -> new JigglePhysics(config.stiffness(), config.damping()))
                        .update(force);
            }
            state.accumulator -= FIXED_TIMESTEP;
            if (Double.isNaN(state.accumulator) || state.accumulator > 1.0) {
                state.accumulator = 0.0;
            }
        }

        double alpha = state.accumulator / FIXED_TIMESTEP;

        for (JiggleBoneConfig config : configs) {
            JigglePhysics physics = state.physics.get(config.boneName());
            if (physics == null) continue;

            net.minecraft.world.phys.Vec3 offset = physics.getInterpolatedDisplacement(alpha);
            model.getBone(config.boneName()).ifPresent(bone -> {
                // The rest rotation is whatever the animation posed this bone to *before* any
                // jiggle was applied. It has to be captured once per bone and reused, because
                // this runs every frame on shared baked bones: reading the live rotation and
                // adding to it would fold the previous frame's offset back in and the bone
                // would spiral away. Upstream keeps the same "default rotation" map.
                float[] rest = state.restRotations.computeIfAbsent(config.boneName(),
                        key -> new float[] {bone.getRotX(), bone.getRotY(), bone.getRotZ()});

                bone.setRotX(rest[0] + (float) offset.x);
                bone.setRotY(rest[1] + (float) offset.y);
                bone.setRotZ(rest[2] + (float) offset.z);
            });
        }
    }

    /** Which bones jiggle; the chest bones differ between the dressed and nude rigs. */
    private List<JiggleBoneConfig> jiggleBones(T animatable) {
        List<JiggleBoneConfig> bones = new java.util.ArrayList<>();
        bones.add(new JiggleBoneConfig("cheekL", 0.2, 0.2));
        bones.add(new JiggleBoneConfig("cheekR", 0.2, 0.2));
        bones.add(new JiggleBoneConfig("belly", 0.3, 0.4));

        if (!animatable.isStripped()) {
            bones.add(new JiggleBoneConfig("boobs", 0.2, 0.4));
        } else {
            bones.add(new JiggleBoneConfig("boobL", 0.2, 0.3));
            bones.add(new JiggleBoneConfig("boobR", 0.2, 0.3));
        }
        return bones;
    }

    /** Per-entity jiggle state. Cleared when the girl despawns or a scene starts. */
    private static final class JiggleState {
        final Map<String, JigglePhysics> physics = new java.util.HashMap<>();
        /** Rest rotation per bone, captured on first sight and reused every frame. */
        final Map<String, float[]> restRotations = new java.util.HashMap<>();
        net.minecraft.world.phys.Vec3 previousVelocity = net.minecraft.world.phys.Vec3.ZERO;
        float previousYaw = 0.0F;
        long lastUpdateNanos = 0L;
        double accumulator = 0.0;
        long lastSeenFrame = 0L;
    }

    private static final double FIXED_TIMESTEP = 1.0 / 25.0;
    private static final Map<java.util.UUID, JiggleState> JIGGLE_STATE = new java.util.HashMap<>();
    /** Above this many tracked girls, prune entries whose entity is no longer loaded. */
    private static final int MAX_TRACKED_GIRLS = 64;
    private static long FRAME_COUNTER = 0L;
    private static final String HEAD_BONE = "head";

    /**
     * Per-bone scale overrides (kobold body/breast size, pregnancy belly).
     *
     * <p>Bones are shared baked-model state, so the previous frame's values must be undone.
     * Every bone we ever touched is reset to 1.0 first, then the current overrides applied -
     * otherwise a resized kobold would permanently resize every other girl on the same rig.</p>
     */
    private void applyBoneScales(T animatable, BakedGeoModel model) {
        Map<String, net.minecraft.world.phys.Vec3> sizes = animatable.boneSizeOverrides;
        if (sizes == null) return;

        for (String boneName : touchedScaleBones) {
            if (!sizes.containsKey(boneName)) {
                model.getBone(boneName).ifPresent(bone -> {
                    bone.setScaleX(1.0F);
                    bone.setScaleY(1.0F);
                    bone.setScaleZ(1.0F);
                });
            }
        }

        for (Map.Entry<String, net.minecraft.world.phys.Vec3> entry : sizes.entrySet()) {
            net.minecraft.world.phys.Vec3 scale = entry.getValue();
            if (scale == null) continue;
            touchedScaleBones.add(entry.getKey());
            model.getBone(entry.getKey()).ifPresent(bone -> {
                bone.setScaleX((float) scale.x);
                bone.setScaleY((float) scale.y);
                bone.setScaleZ((float) scale.z);
            });
        }
    }

    /** Per-bone position offsets, reset the same way as the scales. */
    private void applyBonePositions(T animatable, BakedGeoModel model) {
        Map<String, net.minecraft.world.phys.Vec3> offsets = animatable.bonePositionOffset;
        if (offsets == null) return;

        for (String boneName : touchedPosBones) {
            if (!offsets.containsKey(boneName)) {
                model.getBone(boneName).ifPresent(bone -> {
                    bone.setPosX(0.0F);
                    bone.setPosY(0.0F);
                    bone.setPosZ(0.0F);
                });
            }
        }

        for (Map.Entry<String, net.minecraft.world.phys.Vec3> entry : offsets.entrySet()) {
            net.minecraft.world.phys.Vec3 pos = entry.getValue();
            if (pos == null) continue;
            touchedPosBones.add(entry.getKey());
            model.getBone(entry.getKey()).ifPresent(bone -> {
                bone.setPosX((float) pos.x);
                bone.setPosY((float) pos.y);
                bone.setPosZ((float) pos.z);
            });
        }
    }

    private final java.util.Set<String> touchedScaleBones = new java.util.HashSet<>();
    private final java.util.Set<String> touchedPosBones = new java.util.HashSet<>();

    /**
     * Per-bone colour tint (kobold scales, dyed leather armour).
     *
     * <p>GeckoLib 4 has no per-bone colour, so the tint is applied at draw time by overriding
     * the render colour for the bone currently being rendered.</p>
     */
    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, net.minecraft.client.renderer.RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                                  float partialTick, int packedLight, int packedOverlay, int colour) {
        // A bone with a texture override is drawn by BoneOverrideRenderLayer with its own
        // texture. Skipping it in the base pass avoids drawing it twice - once with the
        // girl's sheet underneath, which would z-fight with the override.
        // isReRender is the override layer's own call, which must go through.
        if (!isReRender
                && animatable.boneTextureOverrides != null
                && animatable.boneTextureOverrides.containsKey(bone.getName())) {
            return;
        }

        VertexConsumer targetBuffer = buffer;

        // Armour material selection: shift this bone's UVs into the right atlas column.
        Map<String, org.joml.Vector2f> uvOffsets = animatable.boneUVOffsets;
        if (uvOffsets != null && !uvOffsets.isEmpty()) {
            org.joml.Vector2f offset = uvOffsets.get(bone.getName());
            if (offset != null && (offset.x != 0.0F || offset.y != 0.0F)) {
                OffsetVertexConsumer offsetBuffer = new OffsetVertexConsumer();
                offsetBuffer.setup(targetBuffer, offset.x, offset.y);
                targetBuffer = offsetBuffer;
            }
        }

        Map<String, Integer> colours = animatable.boneColorOverrides;
        int effective = colour;
        if (colours != null && !colours.isEmpty()) {
            Integer override = colours.get(bone.getName());
            if (override != null) {
                effective = override;
            }
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, targetBuffer, isReRender,
                partialTick, packedLight, packedOverlay, effective);
    }

    /** Root bone of the embedded partner skeleton, present in every girl rig. */
    private static final String PARTNER_BONE = "steve";

    // ---- shoulder carry ----
    // Local offsets in entity space. The entity itself is already placed at the carrier by
    // TameableGirlEntity#getVehicleAttachmentPoint, so these only fine-tune the seat.
    /** Positive X is the carrier's right, so she sits on the right shoulder. */
    private static final float SHOULDER_RIGHT = 0.28F;
    /**
     * Fine-tuning only. The height now comes from the vehicle attachment point in
     * TameableGirlEntity, which already seats her origin at shoulder level; lifting her
     * again here is what made her float above the carrier's head.
     */
    private static final float SHOULDER_UP = 0.0F;
    /**
     * Nudges her forward onto the front of the shoulder. The sign is taken from the reported
     * behaviour: the previous -0.08 visibly placed her behind the carrier's back, so positive
     * Z is forward here.
     */
    private static final float SHOULDER_FORWARD = 0.12F;
    /** Scaled down a little so a full-size girl does not dwarf the player. */
    private static final float CARRY_SCALE = 0.62F;

    private static final float CARRY_THIGH_PITCH = -95.0F;
    private static final float CARRY_THIGH_SPREAD = 12.0F;
    private static final float CARRY_SHIN_PITCH = 105.0F;
    private static final float CARRY_ARM_PITCH = -12.0F;
    private static final float CARRY_ARM_SPREAD = 8.0F;

    /** Every bone the carry pose touches, so it can be undone on non-carried girls. */
    private static final String[] CARRY_POSE_BONES =
            {"legL", "legR", "shinL", "shinR", "armL", "armR"};
}
