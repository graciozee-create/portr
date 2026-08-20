package com.sandymandy.pleasurehorizons.client.rendering.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Map;

/**
 * Re-draws individual bones with a texture of their own.
 *
 * <p>The girl rigs share one texture per girl, but a few bones have to be drawn with something
 * else - above all the embedded {@code steve} partner skeleton, which must wear the scene
 * player's actual skin instead of the girl's texture sheet. Without this layer the partner
 * body is drawn with whatever happens to sit at those UVs on the girl's texture, which is the
 * "partner looks wrong in scenes" nuance.</p>
 *
 * <p>Upstream implements this against GeckoLib 5's render-state API
 * ({@code state.getGeckolibData(...)}, {@code renderRecursively(state, ...)}). GeckoLib 4.9.2
 * has neither, so the overrides are read straight off the entity and
 * {@code renderRecursively} is called with the 4.x signature.</p>
 *
 * <p>The bone is force-shown for the duration of the extra pass and its previous hidden flag is
 * restored afterwards, because baked GeckoLib bones are shared, mutable state.</p>
 */
public class BoneOverrideRenderLayer<T extends GirlSceneEntity> extends GeoRenderLayer<T> {

    public BoneOverrideRenderLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {
        if (renderType == null || bufferSource == null) {
            return;
        }

        renderOverrideLayer(animatable, bakedModel, poseStack, bufferSource,
                animatable.boneTextureOverrides, partialTick, packedLight, packedOverlay);
        renderOverrideLayer(animatable, bakedModel, poseStack, bufferSource,
                animatable.boneTextureOverridesLayer2, partialTick, packedLight, packedOverlay);
        renderOverrideLayer(animatable, bakedModel, poseStack, bufferSource,
                animatable.boneTextureOverridesLayer3, partialTick, packedLight, packedOverlay);
    }

    private void renderOverrideLayer(T animatable, BakedGeoModel model, PoseStack poseStack,
                                     MultiBufferSource bufferSource, Map<String, ResourceLocation> overrides,
                                     float partialTick, int packedLight, int packedOverlay) {
        if (overrides == null || overrides.isEmpty()) {
            return;
        }

        overrides.forEach((boneName, texture) -> {
            if (texture == null) {
                return;
            }

            model.getBone(boneName).ifPresent(bone -> {
                RenderType overrideType = RenderType.entityTranslucent(texture);
                VertexConsumer overrideBuffer = bufferSource.getBuffer(overrideType);
                if (overrideBuffer == null) {
                    return;
                }

                // Bones are shared baked state, so remember and restore the flags.
                boolean wasHidden = bone.isHidden();
                boolean childrenWereHidden = bone.isHidingChildren();

                bone.setHidden(false);
                bone.setChildrenHidden(false);

                // Render layers run after the main model pass, at a pose-stack level that no
                // longer has the entity's body-yaw rotation applied. Re-enter the model space
                // (scale + yaw + the 0.01 unit offset GeoEntityRenderer applies before
                // descending into the bone tree) so an overridden bone such as the partner
                // skeleton lines up with the animated girl instead of staying in world space.
                poseStack.pushPose();
                applyModelSpaceTransform(poseStack, animatable, partialTick);

                // isReRender = true: this is an extra pass over a bone the main pass already
                // drew, so GeckoLib must not re-apply the model transforms. Colour is left
                // white (-1) so the override texture is shown as-is.
                this.getRenderer().renderRecursively(poseStack, animatable, bone, overrideType,
                        bufferSource, overrideBuffer, true, partialTick, packedLight, packedOverlay, -1);
                poseStack.popPose();

                bone.setHidden(wasHidden);
                bone.setChildrenHidden(childrenWereHidden);
            });
        });
    }

    /**
     * Re-applies the model-space transforms that {@code GeoEntityRenderer#actuallyRender} wraps
     * around the main bone-tree render: native scale, body-yaw rotation and the 0.01 unit offset.
     *
     * <p>The main pass applies these inside its own {@code poseStack} push/pop, so by the time a
     * render layer runs they are gone. The partner skeleton is a top-level bone whose position
     * is animated by the scene animation, so without these transforms it would be drawn at its
     * un-rotated world position and drift away from the girl whenever she turns.</p>
     */
    private void applyModelSpaceTransform(PoseStack poseStack, T animatable, float partialTick) {
        float scale = animatable.getScale();
        poseStack.scale(scale, scale, scale);
        float lerpBodyRot = Mth.rotLerp(partialTick, animatable.yBodyRotO, animatable.yBodyRot);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - lerpBodyRot));
        poseStack.translate(0.0F, 0.01F, 0.0F);
    }
}
