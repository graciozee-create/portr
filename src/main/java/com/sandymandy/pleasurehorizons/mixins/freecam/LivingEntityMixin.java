package com.sandymandy.pleasurehorizons.mixins.freecam;

import com.sandymandy.pleasurehorizons.freecam.Freecam;
import com.sandymandy.pleasurehorizons.config.ModConfig;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.sandymandy.pleasurehorizons.freecam.Freecam.MC;
import static com.sandymandy.pleasurehorizons.config.ModConfig.FlightMode.CREATIVE;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow public abstract float getHealth();

    @Inject(method = "getMovementSpeed(F)F", at = @At("HEAD"), cancellable = true)
    private void onGetMovementSpeed(CallbackInfoReturnable<Float> cir) {
        if (Freecam.isEnabled() && ModConfig.INSTANCE.movement.flightMode.equals(CREATIVE) && this.equals(Freecam.getFreeCamera())) {
            cir.setReturnValue((float) (ModConfig.INSTANCE.movement.horizontalSpeed / 10) * (Freecam.getFreeCamera().isSprinting() ? 2 : 1));
        }
    }

    @Inject(method = "setHealth", at = @At("HEAD"))
    private void onSetHealth(float health, CallbackInfo ci) {
        if (Freecam.isEnabled() && ModConfig.INSTANCE.utility.disableOnDamage && this.equals(MC.player)) {
            if (!MC.player.isCreative() && getHealth() > health) {
                Freecam.disableNextTick();
            }
        }
    }
}
