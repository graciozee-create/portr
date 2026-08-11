package com.sandymandy.pleasurehorizons.mixins.freecam;

import com.sandymandy.pleasurehorizons.freecam.Freecam;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.sandymandy.pleasurehorizons.freecam.Freecam.MC;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void onRenderLabel(EntityRenderState renderState,
                               Text component,
                               MatrixStack poseStack,
                               VertexConsumerProvider multiBufferSource,
                               int packedLightCoords,
                               CallbackInfo ci) {
        if (Freecam.isEnabled() && ((EntityRenderDispatcherAccessor) MC.getEntityRenderDispatcher()).isRenderShadows()) {
            ci.cancel();
        }
    }
}
