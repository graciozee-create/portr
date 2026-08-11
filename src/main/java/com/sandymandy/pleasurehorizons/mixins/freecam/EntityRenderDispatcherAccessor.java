package com.sandymandy.pleasurehorizons.mixins.freecam;

import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.render.entity.EntityRenderDispatcher.class)
public interface EntityRenderDispatcherAccessor {
    @Accessor
    boolean isRenderShadows();
}
