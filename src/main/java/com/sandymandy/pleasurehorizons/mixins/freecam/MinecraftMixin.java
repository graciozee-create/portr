package com.sandymandy.pleasurehorizons.mixins.freecam;

import com.sandymandy.pleasurehorizons.freecam.Freecam;
import com.sandymandy.pleasurehorizons.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.sandymandy.pleasurehorizons.config.ModBindings.KEY_TOGGLE;
import static com.sandymandy.pleasurehorizons.config.ModBindings.KEY_TRIPOD_RESET;
import static com.sandymandy.pleasurehorizons.config.ModConfig.InteractionMode.PLAYER;

@Mixin(MinecraftClient.class)
public class MinecraftMixin {

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
        if (freecam$disableInteract()) {
            cir.cancel();
        }
    }

    @Inject(method = "doItemPick", at = @At("HEAD"), cancellable = true)
    private void onDoItemPick(CallbackInfo ci) {
        if (freecam$disableInteract()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void onHandleBlockBreaking(CallbackInfo ci) {
        if (freecam$disableInteract()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;wasPressed()Z", ordinal = 2), cancellable = true)
    private void onHandleInputEvents(CallbackInfo ci) {
        if (KEY_TOGGLE.get().isPressed() || KEY_TRIPOD_RESET.get().isPressed()) {
            ci.cancel();
        }
    }

    @Inject(method = "disconnect", at = @At(value = "HEAD"))
    private void onDisconnect(CallbackInfo ci) {
        Freecam.onDisconnect();
    }

    @Unique
    private static boolean freecam$disableInteract() {
        return Freecam.isEnabled() && !Freecam.isPlayerControlEnabled() && !freecam$allowInteract();
    }

    @Unique
    private static boolean freecam$allowInteract() {
        return ModConfig.INSTANCE.utility.allowInteract && ModConfig.INSTANCE.utility.interactionMode.equals(PLAYER);
    }
}
