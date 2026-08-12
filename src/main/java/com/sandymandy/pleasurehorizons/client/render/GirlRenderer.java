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
