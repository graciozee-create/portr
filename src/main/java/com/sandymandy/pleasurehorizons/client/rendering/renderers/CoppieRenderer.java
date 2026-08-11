package com.sandymandy.pleasurehorizons.client.rendering.renderers;

import com.sandymandy.pleasurehorizons.client.models.CoppieModel;
import com.sandymandy.pleasurehorizons.entity.girls.CoppieEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class CoppieRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends AbstractGirlRenderer<CoppieEntity, R> {

    public CoppieRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new CoppieModel());
    }
}
