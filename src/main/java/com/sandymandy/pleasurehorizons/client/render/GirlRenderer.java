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
    }

    /** Root bone of the embedded partner skeleton, present in every girl rig. */
    private static final String PARTNER_BONE = "steve";
}
