package com.sandymandy.pleasurehorizons.client.rendering.layers;

import com.sandymandy.pleasurehorizons.registries.PleasureHorizonsDataTicketRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.Map;

public class BoneOverrideRenderLayer<T extends GeoAnimatable, O, R extends GeoRenderState>
        extends GeoRenderLayer<T, O, R> {

    public BoneOverrideRenderLayer(GeoRenderer<T, O, R> renderer) {
        super(renderer);
    }

    @Override
    public void render(R state,
                       MatrixStack poseStack,
                       BakedGeoModel model,
                       RenderLayer mainRenderLayer,
                       VertexConsumerProvider buffers,
                       VertexConsumer buffer,
                       int packedLight,
                       int packedOverlay,
                       int renderColor) {

        // Skip invisible or weird passes
        if (mainRenderLayer == null) return;
        if (poseStack == null || poseStack.peek() == null) return;

        // Maps
        Map<String, ResourceLocation> layer1 = state.getGeckolibData(PleasureHorizonsDataTicketRegistry.GIRL_BONE_TEXTURE_OVERRIDES);
        Map<String, ResourceLocation> layer2 = state.getGeckolibData(PleasureHorizonsDataTicketRegistry.GIRL_BONE_TEXTURE_OVERRIDES_LAYER_TWO);
        Map<String, ResourceLocation> layer3 = state.getGeckolibData(PleasureHorizonsDataTicketRegistry.GIRL_BONE_TEXTURE_OVERRIDES_LAYER_THREE);

        // Render all 3 layers
        renderOverrideLayer(model, poseStack, buffers, state, layer1, packedLight, packedOverlay, renderColor);
        renderOverrideLayer(model, poseStack, buffers, state, layer2, packedLight, packedOverlay, renderColor);
        renderOverrideLayer(model, poseStack, buffers, state, layer3, packedLight, packedOverlay, renderColor);
    }

    private void renderOverrideLayer(
            BakedGeoModel model,
            MatrixStack poseStack,
            VertexConsumerProvider buffers,
            R state,
            Map<String, ResourceLocation> map,
            int packedLight,
            int packedOverlay,
            int renderColor
    ) {
        if (map == null || map.isEmpty()) return;

        map.forEach((boneName, tex) -> {

            if (tex == null) return;

            model.getBone(boneName).ifPresent(bone -> {

                RenderLayer rl = RenderLayer.getEntityTranslucent(tex);
                VertexConsumer bc = buffers.getBuffer(rl);

                if (bc == null) return;

                // --- FIX: force bone visible before rendering ---
                boolean oldHidden = bone.isHidden();

                bone.setHidden(false);
                bone.setChildrenHidden(false);

                this.renderer.renderRecursively(
                        state,
                        poseStack,
                        bone,
                        rl,
                        buffers,
                        bc,
                        false,
                        packedLight,
                        packedOverlay,
                        renderColor
                );

                // --- restore original state ---
                bone.setHidden(oldHidden);
            });

        });
    }
}
