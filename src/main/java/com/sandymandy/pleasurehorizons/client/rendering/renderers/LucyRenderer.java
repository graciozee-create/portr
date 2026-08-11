package com.sandymandy.pleasurehorizons.client.rendering.renderers;

import com.sandymandy.pleasurehorizons.client.models.LucyModel;
import com.sandymandy.pleasurehorizons.entity.girls.LucyEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class LucyRenderer<R extends LivingEntityRenderState & GeoRenderState> extends AbstractGirlRenderer<LucyEntity, R> {
    public LucyRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new LucyModel());
    }
}
