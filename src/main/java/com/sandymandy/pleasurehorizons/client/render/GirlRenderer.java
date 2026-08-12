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

import java.util.Map;

public class GirlRenderer<T extends GirlSceneEntity> extends GeoEntityRenderer<T> {
    public GirlRenderer(EntityRendererProvider.Context context) {
        super(context, new GirlModel<>());
        this.shadowRadius = 0.4F;

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
        boolean carriedByLocalPlayer = isCarried
                && animatable.getVehicle().is(net.minecraft.client.Minecraft.getInstance().player);

        if (carriedByLocalPlayer && net.minecraft.client.Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            // First-person carry: show her on the right side in arms instead of hiding.
            if (animatable.getVehicle() instanceof net.minecraft.world.entity.player.Player player) {
                float yaw = player.getYRot();
                double rad = Math.toRadians(yaw);
                double rightOffset = 0.6;
                double forwardOffset = 0.5;
                double downOffset = -0.4;

                double offsetX = rightOffset * Math.cos(rad) + forwardOffset * (-Math.sin(rad));
                double offsetZ = rightOffset * Math.sin(rad) + forwardOffset * Math.cos(rad);

                poseStack.translate(offsetX, downOffset, offsetZ);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw + 30.0F));
                poseStack.scale(0.5F, 0.5F, 0.5F);

                // Add subtle sway based on player movement for natural feel
                float sway = (float) Math.sin(player.tickCount * 0.1f) * 2.0f;
                poseStack.mulPose(Axis.ZP.rotationDegrees(sway));
            } else {
                poseStack.translate(0.6, -0.4, -0.6);
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }
        } else if (isCarried) {
            // Third-person carry: princess carry in front of player, slightly elevated
            if (animatable.getVehicle() instanceof net.minecraft.world.entity.player.Player player) {
                float yaw = player.getYRot();
                double rad = Math.toRadians(yaw);
                double forwardOffset = 0.4;
                double upOffset = 0.2;

                double offsetX = forwardOffset * (-Math.sin(rad));
                double offsetZ = forwardOffset * Math.cos(rad);

                poseStack.translate(offsetX, upOffset, offsetZ);
                // Face same direction as player but slightly tilted for bridal carry
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
                poseStack.mulPose(Axis.XP.rotationDegrees(-15.0F)); // slight backward lean
                poseStack.scale(0.85F, 0.85F, 0.85F);
            }
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

        applyBoneScales(animatable, model);
        applyBonePositions(animatable, model);
    }

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
        Map<String, Integer> colours = animatable.boneColorOverrides;
        int effective = colour;
        if (colours != null && !colours.isEmpty()) {
            Integer override = colours.get(bone.getName());
            if (override != null) {
                effective = override;
            }
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, effective);
    }

    /** Root bone of the embedded partner skeleton, present in every girl rig. */
    private static final String PARTNER_BONE = "steve";
}
