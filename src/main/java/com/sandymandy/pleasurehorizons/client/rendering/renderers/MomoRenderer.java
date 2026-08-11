package com.sandymandy.pleasurehorizons.client.rendering.renderers;

import com.sandymandy.pleasurehorizons.client.models.MomoModel;
import com.sandymandy.pleasurehorizons.entity.girls.MomoEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class MomoRenderer<R extends LivingEntityRenderState & GeoRenderState> extends AbstractGirlRenderer<MomoEntity, R> {
    public MomoRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new MomoModel());
    }
}
