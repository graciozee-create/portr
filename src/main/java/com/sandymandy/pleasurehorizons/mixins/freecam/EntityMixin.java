package com.sandymandy.pleasurehorizons.mixins.freecam;

import com.sandymandy.pleasurehorizons.freecam.Freecam;
import com.sandymandy.pleasurehorizons.config.ModConfig;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.sandymandy.pleasurehorizons.freecam.Freecam.MC;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void onChangeLookDirection(double x, double y, CallbackInfo ci) {
        if (Freecam.isEnabled() && this.equals(MC.player) && !Freecam.isPlayerControlEnabled()) {
            Freecam.getFreeCamera().changeLookDirection(x, y);
            ci.cancel();
        }
    }

    @Inject(method = "pushAwayFrom(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void onPushAwayFrom(Entity entity, CallbackInfo ci) {
        if (Freecam.isEnabled() && (entity.equals(Freecam.getFreeCamera()) || this.equals(Freecam.getFreeCamera()))) {
            ci.cancel();
        }
    }

    @Inject(method = "setVelocity(DDD)V", at = @At("HEAD"), cancellable = true)
    private void onSetVelocity(CallbackInfo ci) {
        if (freecam$shouldFreeze()) {
            ci.cancel();
        }
    }

    @Inject(method = "updateVelocity", at = @At("HEAD"), cancellable = true)
    private void onUpdateVelocity(CallbackInfo ci) {
        if (freecam$shouldFreeze()) {
            ci.cancel();
        }
    }

    @Inject(method = "setPosition(DDD)V", at = @At("HEAD"), cancellable = true)
    private void onSetPosition(CallbackInfo ci) {
        if (freecam$shouldFreeze()) {
            ci.cancel();
        }
    }

    @Inject(method = "setPos", at = @At("HEAD"), cancellable = true)
    private void onSetPos(CallbackInfo ci) {
        if (freecam$shouldFreeze()) {
            ci.cancel();
        }
    }

    @Unique
    private boolean freecam$shouldFreeze() {
        return Freecam.isEnabled() && this.equals(MC.player) && freecam$allowFreeze();
    }

    @Unique
    private boolean freecam$allowFreeze() {
        return ModConfig.INSTANCE.utility.freezePlayer && !Freecam.isPlayerControlEnabled();
    }
}
