package com.sandymandy.pleasurehorizons.mixins.geckolib;

import com.sandymandy.pleasurehorizons.util.rendering.GeoBoneExtension;

import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import software.bernie.geckolib.cache.object.GeoBone;

@OnlyIn(Dist.CLIENT)
@Mixin(GeoBone.class)
public abstract class GeoBoneMixin implements GeoBoneExtension {

    private boolean hidden;

    @Override
    public void setHiddenWithoutHidingChildren(boolean hidden) {
        // Just set this bone’s hidden state without touching children
        this.hidden = hidden;
    }
}
