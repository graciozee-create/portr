package com.sandymandy.pleasurehorizons.client.rendering.renderers;

import com.sandymandy.pleasurehorizons.client.models.CustomGirlModel;
import com.sandymandy.pleasurehorizons.entity.girls.CustomGirlEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class CustomGirlRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends AbstractGirlRenderer<CustomGirlEntity, R> {

    public CustomGirlRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new CustomGirlModel());
    }
}
