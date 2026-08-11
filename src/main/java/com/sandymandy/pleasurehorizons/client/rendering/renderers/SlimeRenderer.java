package com.sandymandy.pleasurehorizons.client.rendering.renderers;

import com.sandymandy.pleasurehorizons.client.models.SlimeModel;
import com.sandymandy.pleasurehorizons.entity.girls.SlimeEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class SlimeRenderer<R extends LivingEntityRenderState & GeoRenderState> extends AbstractGirlRenderer<SlimeEntity, R> {
    public SlimeRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new SlimeModel());
    }

    @Override
    protected boolean shouldUseTranslucentRendering(R renderState) {
        return true;
    }
}
