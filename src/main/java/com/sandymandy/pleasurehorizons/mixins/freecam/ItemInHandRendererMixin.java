package com.sandymandy.pleasurehorizons.mixins.freecam;

import com.sandymandy.pleasurehorizons.freecam.Freecam;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.sandymandy.pleasurehorizons.freecam.Freecam.MC;

@Mixin(HeldItemRenderer.class)
public class ItemInHandRendererMixin {

    @Unique private float freecam$tickDelta;

    @ModifyVariable(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At("HEAD"), argsOnly = true)
    private ClientPlayerEntity onRenderItem(ClientPlayerEntity player) {
        if (Freecam.isEnabled()) {
            return Freecam.getFreeCamera();
        }
        return player;
    }

    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At("HEAD"))
    private void storeTickDelta(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light, CallbackInfo ci) {
        this.freecam$tickDelta = tickDelta;
    }

    @ModifyVariable(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At("HEAD"), argsOnly = true)
    private int onRenderItem2(int light) {
        if (Freecam.isEnabled()) {
            return MC.getEntityRenderDispatcher().getLight(Freecam.getFreeCamera(), freecam$tickDelta);
        }
        return light;
    }
}
