package com.sandymandy.pleasurehorizons.client.render;

import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GirlRenderer<T extends GirlSceneEntity> extends GeoEntityRenderer<T> {
    public GirlRenderer(EntityRendererProvider.Context context) {
        super(context, new GirlModel<>());
        this.shadowRadius = 0.4F;
    }
}
