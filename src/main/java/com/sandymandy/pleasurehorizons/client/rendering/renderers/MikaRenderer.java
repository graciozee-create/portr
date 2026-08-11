package com.sandymandy.pleasurehorizons.client.rendering.renderers;

import com.sandymandy.pleasurehorizons.client.models.MikaModel;
import com.sandymandy.pleasurehorizons.entity.girls.MikaEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class MikaRenderer<R extends LivingEntityRenderState & GeoRenderState> extends AbstractGirlRenderer<MikaEntity, R>{
    public MikaRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new MikaModel());
    }


}
