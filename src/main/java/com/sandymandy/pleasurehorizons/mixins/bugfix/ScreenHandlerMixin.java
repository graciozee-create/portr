package com.sandymandy.pleasurehorizons.mixins.bugfix;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class ScreenHandlerMixin {

    /**FIXME: Even though this Mixin prevents out of bounds error
     * FIXME: Mainly out of bounds of 46 out of 46
     *FIXME: The issue shall be further investigated
     */


    @Inject(method = "setReceivedStack", at = @At("HEAD"), cancellable = true)
    private void preventSlotCrash(int slot, net.minecraft.world.item.ItemStack stack, CallbackInfo ci) {
        if (slot < 0 || slot >= ((AbstractContainerMenu) (Object) this).slots.size()) {
            PleasureHorizons.LOGGER.error(
                    "Prevented slot crash: Attempted to access slot {} but only {} slots available",
                    slot, ((AbstractContainerMenu) (Object) this).slots.size()
            );
            ci.cancel(); // Prevent the crash
        }
    }
}
