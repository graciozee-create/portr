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
     * Applies whole-entity transforms before rendering. Bone transforms are deliberately
     * deferred until after GeckoLib evaluates the animation; see {@link #renderRecursively}.
     */
    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model,
                          @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight,
                          int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, colour);

        if (!isReRender) {
            currentRenderModel = model;
        }

        if (!isReRender
                && animatable.getVehicle() instanceof net.minecraft.world.entity.player.Player carrier) {
            applyFirstPersonCarryFraming(poseStack, carrier);
        }

        // GeckoLib applies controller animations in actuallyRender, after preRender. Bone
        // changes made here would therefore be overwritten. They are applied lazily from the
        // first renderRecursively call instead, then restored in postRender.
        updatePartnerSkin(animatable);
    }

    /**
     * Keeps the full-size carried model out of the centre of the local carrier's first-person
     * view. This is deliberately a camera-only translation: observers still see the authoritative
     * close attachment, and the model is never shrunk into a miniature passenger.
     */
    private void applyFirstPersonCarryFraming(PoseStack poseStack,
                                               net.minecraft.world.entity.player.Player carrier) {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.player != carrier
                || minecraft.getCameraEntity() != carrier
                || minecraft.options.getCameraType() != net.minecraft.client.CameraType.FIRST_PERSON) {
            return;
        }

        // Use the camera basis rather than body yaw so the framing stays low and toward the
        // outside of the viewport even while the player looks around independently of their body.
        net.minecraft.client.Camera camera = minecraft.gameRenderer.getMainCamera();
        org.joml.Vector3f outside = camera.getLeftVector();
        org.joml.Vector3f forward = camera.getLookVector();
        org.joml.Vector3f up = camera.getUpVector();

        poseStack.translate(
                outside.x() * FIRST_PERSON_OUTWARD_SHIFT
                        + forward.x() * FIRST_PERSON_FORWARD_SHIFT
                        + up.x() * FIRST_PERSON_DOWN_SHIFT,
                outside.y() * FIRST_PERSON_OUTWARD_SHIFT
                        + forward.y() * FIRST_PERSON_FORWARD_SHIFT
                        + up.y() * FIRST_PERSON_DOWN_SHIFT,
                outside.z() * FIRST_PERSON_OUTWARD_SHIFT
                        + forward.z() * FIRST_PERSON_FORWARD_SHIFT
                        + up.z() * FIRST_PERSON_DOWN_SHIFT);
    }

    /**
     * Applies the lean after GeckoLib rotates the model to entity yaw. Doing this in preRender
     * rotates around world Z instead, so the girl leans away whenever the carrier changes facing.
     */
    @Override
    protected void applyRotations(T animatable, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);

        if (animatable.getVehicle() instanceof net.minecraft.world.entity.player.Player carrier) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(CARRY_INWARD_LEAN));

            float walk = carrier.walkAnimation.position(partialTick);
            float sway = net.minecraft.util.Mth.sin(walk * 0.6F) * CARRY_SWAY_DEGREES
                    * carrier.walkAnimation.speed(partialTick);
            poseStack.mulPose(Axis.XP.rotationDegrees(sway));
        }
    }

    /**
     * Applies every entity-specific bone override after GeckoLib has evaluated the animation.
     * The baked bones are shared between all entities using this renderer, so every original
     * value is captured first and restored from {@link #postRender} after all render layers.
     */
    private void applyBoneOverrides(T animatable, BakedGeoModel model) {
        boolean showPartner = shouldRenderPartner(animatable);

        // The partner must never be drawn in the main girl's-texture pass. Its dedicated skin
        // layer temporarily reveals this tree only when the current scene actually uses it.
        setBoneTreeHidden(model, PARTNER_BONE, true);

        if (showPartner) {
            boolean slim = animatable.isPlayerModelSlim();
            PARTNER_SLIM_ARMS.forEach(name -> setBoneHidden(model, name, !slim));
            PARTNER_WIDE_ARMS.forEach(name -> setBoneHidden(model, name, slim));
        }

        Map<String, Boolean> visibility = animatable.boneVisibility;
        if (visibility != null) {
            for (Map.Entry<String, Boolean> entry : visibility.entrySet()) {
                if (entry.getValue() == null) continue;
                model.getBone(entry.getKey()).ifPresent(bone -> {
                    rememberBone(bone);
                    bone.setHidden(!entry.getValue());
                });
            }
        }

        if (animatable.getVehicle() instanceof net.minecraft.world.entity.player.Player) {
            applyCarryPose(model);
        }

        applyHeadTracking(animatable, model);
        applyJigglePhysics(animatable, model);
        applyBoneScales(animatable, model);
        applyBonePositions(animatable, model);
    }

    private void rememberBone(GeoBone bone) {
        renderedBoneStates.computeIfAbsent(bone, BoneRenderState::capture);
    }

    private void setBoneHidden(BakedGeoModel model, String name, boolean hidden) {
        model.getBone(name).ifPresent(bone -> {
            rememberBone(bone);
            bone.setHidden(hidden);
        });
    }

    private void setBoneTreeHidden(BakedGeoModel model, String name, boolean hidden) {
        model.getBone(name).ifPresent(bone -> {
            rememberBone(bone);
            bone.setHidden(hidden);
            bone.setChildrenHidden(hidden);
        });
    }

    private boolean shouldRenderPartner(T animatable) {
        boolean activePartnerPhase = switch (animatable.getCurrentScenePhase()) {
            case NONE, BED_IDLE, LAYING_DOWN, DIALOG -> false;
            default -> true;
        };
        return animatable.isSceneActive()
                && activePartnerPhase
                && !animatable.getCurrentScene().hidePlayer();
    }

    /**
     * Folds both knees toward the carrier and brings both forearms around the carrier's upper
     * body. The pose is applied after the neutral animation and restored after this render pass.
     */
    private void applyCarryPose(BakedGeoModel model) {
        setCarryBoneRotation(model, "legL",
                CARRY_THIGH_PITCH, -CARRY_THIGH_WRAP, CARRY_THIGH_SPLAY);
        setCarryBoneRotation(model, "legR",
                CARRY_THIGH_PITCH, CARRY_THIGH_WRAP, -CARRY_THIGH_SPLAY);
        setCarryBoneRotation(model, "shinL", CARRY_SHIN_PITCH, 0.0F, 0.0F);
        setCarryBoneRotation(model, "shinR", CARRY_SHIN_PITCH, 0.0F, 0.0F);

        setCarryBoneRotation(model, "armL",
                CARRY_LEFT_UPPER_ARM_PITCH, CARRY_LEFT_UPPER_ARM_YAW,
                CARRY_LEFT_UPPER_ARM_ROLL);
        setCarryBoneRotation(model, "armR",
                CARRY_RIGHT_UPPER_ARM_PITCH, CARRY_RIGHT_UPPER_ARM_YAW,
                CARRY_RIGHT_UPPER_ARM_ROLL);
        setCarryBoneRotation(model, "lowerArmL", CARRY_LEFT_ELBOW_PITCH, 0.0F, 0.0F);
        setCarryBoneRotation(model, "lowerArmR", CARRY_RIGHT_ELBOW_PITCH, 0.0F, 0.0F);
    }

    /**
     * Accepts the same Blockbench degrees used by the animation JSON files. GeckoLib negates
     * animation X/Y while baking but keeps Z unchanged; writing those JSON values directly into
     * GeoBone was what made the old knees bend backwards. Initial rig rotation is retained just
     * as it is for a normally evaluated GeckoLib animation.
     */
    private void setCarryBoneRotation(BakedGeoModel model, String boneName,
                                      float xDeg, float yDeg, float zDeg) {
        model.getBone(boneName).ifPresent(bone -> {
            rememberBone(bone);
            software.bernie.geckolib.animation.state.BoneSnapshot initial = bone.getInitialSnapshot();
            float radians = (float) Math.PI / 180F;
            bone.setRotX(initial.getRotX() - xDeg * radians);
            bone.setRotY(initial.getRotY() - yDeg * radians);
            bone.setRotZ(initial.getRotZ() + zDeg * radians);
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
        if (!shouldRenderPartner(animatable)) {
            if (!animatable.boneTextureOverrides.isEmpty()
                    || !animatable.boneTextureOverridesLayer2.isEmpty()) {
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
        if (animatable.isSceneActive()) return;

        model.getBone(HEAD_BONE).ifPresent(bone -> {
            rememberBone(bone);

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
                // Controller animations have already established this frame's pose. Add the
                // spring displacement to that pose and restore it after rendering, preventing
                // the offset from accumulating on the shared baked bone.
                rememberBone(bone);
                bone.setRotX(bone.getRotX() + (float) offset.x);
                bone.setRotY(bone.getRotY() + (float) offset.y);
                bone.setRotZ(bone.getRotZ() + (float) offset.z);
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

    /** Per-bone scale overrides (kobold body/breast size, pregnancy belly). */
    private void applyBoneScales(T animatable, BakedGeoModel model) {
        Map<String, net.minecraft.world.phys.Vec3> sizes = animatable.boneSizeOverrides;
        if (sizes == null) return;

        for (Map.Entry<String, net.minecraft.world.phys.Vec3> entry : sizes.entrySet()) {
            net.minecraft.world.phys.Vec3 scale = entry.getValue();
            if (scale == null) continue;
            model.getBone(entry.getKey()).ifPresent(bone -> {
                rememberBone(bone);
                bone.setScaleX((float) scale.x);
                bone.setScaleY((float) scale.y);
                bone.setScaleZ((float) scale.z);
            });
        }
    }

    /** Per-bone position offsets, restored with the other bone state after rendering. */
    private void applyBonePositions(T animatable, BakedGeoModel model) {
        Map<String, net.minecraft.world.phys.Vec3> offsets = animatable.bonePositionOffset;
        if (offsets == null) return;

        for (Map.Entry<String, net.minecraft.world.phys.Vec3> entry : offsets.entrySet()) {
            net.minecraft.world.phys.Vec3 pos = entry.getValue();
            if (pos == null) continue;
            model.getBone(entry.getKey()).ifPresent(bone -> {
                rememberBone(bone);
                bone.setPosX((float) pos.x);
                bone.setPosY((float) pos.y);
                bone.setPosZ((float) pos.z);
            });
        }
    }

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
        // GeoEntityRenderer evaluates controller animations immediately before descending into
        // the bone tree. This is therefore the first safe point to layer our custom pose on top.
        if (!isReRender && !boneOverridesApplied && currentRenderModel != null) {
            boneOverridesApplied = true;
            applyBoneOverrides(animatable, currentRenderModel);
        }

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

    @Override
    public void postRender(PoseStack poseStack, T animatable, BakedGeoModel model,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                           boolean isReRender, float partialTick, int packedLight,
                           int packedOverlay, int colour) {
        if (!isReRender) {
            renderedBoneStates.forEach((bone, state) -> state.restore(bone));
            renderedBoneStates.clear();
            boneOverridesApplied = false;
            currentRenderModel = null;
        }
        super.postRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, colour);
    }

    private final Map<GeoBone, BoneRenderState> renderedBoneStates = new java.util.IdentityHashMap<>();
    @Nullable
    private BakedGeoModel currentRenderModel;
    private boolean boneOverridesApplied;

    private record BoneRenderState(boolean hidden, boolean childrenHidden,
                                   float rotX, float rotY, float rotZ,
                                   float posX, float posY, float posZ,
                                   float scaleX, float scaleY, float scaleZ) {
        static BoneRenderState capture(GeoBone bone) {
            return new BoneRenderState(
                    bone.isHidden(), bone.isHidingChildren(),
                    bone.getRotX(), bone.getRotY(), bone.getRotZ(),
                    bone.getPosX(), bone.getPosY(), bone.getPosZ(),
                    bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
        }

        void restore(GeoBone bone) {
            bone.setHidden(hidden);
            bone.setChildrenHidden(childrenHidden);
            bone.setRotX(rotX);
            bone.setRotY(rotY);
            bone.setRotZ(rotZ);
            bone.setPosX(posX);
            bone.setPosY(posY);
            bone.setPosZ(posZ);
            bone.setScaleX(scaleX);
            bone.setScaleY(scaleY);
            bone.setScaleZ(scaleZ);
        }
    }

    /** Root bone of the embedded partner skeleton, present in every girl rig. */
    private static final String PARTNER_BONE = "steve";
    private static final List<String> PARTNER_SLIM_ARMS = List.of(
            "rightArmAlex", "rightLowerArmAlex", "leftLowerArmAlex", "leftArmAlex");
    private static final List<String> PARTNER_WIDE_ARMS = List.of(
            "rightArmSteve", "rightLowerArmSteve", "leftLowerArmSteve", "leftArmSteve");

    // ---- carried pose (Blockbench animation degrees) ----
    private static final float CARRY_INWARD_LEAN = -8.0F;
    private static final float CARRY_SWAY_DEGREES = 1.5F;
    private static final float CARRY_THIGH_PITCH = -82.0F;
    private static final float CARRY_THIGH_WRAP = 16.0F;
    private static final float CARRY_THIGH_SPLAY = 5.0F;
    private static final float CARRY_SHIN_PITCH = 72.0F;
    private static final float CARRY_LEFT_UPPER_ARM_PITCH = -87.0F;
    private static final float CARRY_LEFT_UPPER_ARM_YAW = -10.0F;
    private static final float CARRY_LEFT_UPPER_ARM_ROLL = 8.0F;
    private static final float CARRY_RIGHT_UPPER_ARM_PITCH = -85.0F;
    private static final float CARRY_RIGHT_UPPER_ARM_YAW = 10.0F;
    private static final float CARRY_RIGHT_UPPER_ARM_ROLL = -12.0F;
    private static final float CARRY_LEFT_ELBOW_PITCH = -84.0F;
    private static final float CARRY_RIGHT_ELBOW_PITCH = -93.0F;

    // Local first-person framing only; third-person and remote observers retain world attachment.
    private static final double FIRST_PERSON_OUTWARD_SHIFT = 0.32D;
    private static final double FIRST_PERSON_FORWARD_SHIFT = 0.24D;
    private static final double FIRST_PERSON_DOWN_SHIFT = -0.24D;
}
