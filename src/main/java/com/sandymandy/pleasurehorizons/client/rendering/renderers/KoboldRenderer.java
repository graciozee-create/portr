package com.sandymandy.pleasurehorizons.client.rendering.renderers;

import com.sandymandy.pleasurehorizons.client.models.KoboldModel;
import com.sandymandy.pleasurehorizons.entity.girls.KoboldEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class KoboldRenderer<R extends LivingEntityRenderState & GeoRenderState> extends AbstractGirlRenderer<KoboldEntity, R> {
    public KoboldRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new KoboldModel());
    }
}
