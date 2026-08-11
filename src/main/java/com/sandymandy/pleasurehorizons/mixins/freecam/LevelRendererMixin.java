package com.sandymandy.pleasurehorizons.mixins.freecam;

import com.sandymandy.pleasurehorizons.freecam.Freecam;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import com.sandymandy.pleasurehorizons.config.ModConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.sandymandy.pleasurehorizons.freecam.Freecam.MC;
import static org.spongepowered.asm.mixin.injection.callback.LocalCapture.CAPTURE_FAILHARD;

@Mixin(WorldRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    @Shadow protected abstract void renderEntity(Entity entity, double camX, double camY, double camZ, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource);

    @Inject(method = "renderEntities", at = @At("TAIL"), locals = CAPTURE_FAILHARD)
    private void onRender(
            MatrixStack poseStack,
                          VertexConsumerProvider.Immediate bufferSource,
                          Camera camera,
                          RenderTickCounter deltaTracker,
                          List<Entity> entities,
                          CallbackInfo ci) {
        if (Freecam.isEnabled() && !ModConfig.INSTANCE.visual.hidePlayer) {
            Vec3d position = camera.getPos();
            float partialTick = deltaTracker.getTickProgress(false);
            renderEntity(MC.player, position.x, position.y, position.z, partialTick, poseStack, bufferBuilders.getEntityVertexConsumers());
        }
    }
}
