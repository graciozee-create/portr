package com.sandymandy.pleasurehorizons.client.render;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Resolves the geo model, texture and animation file for a girl.
 *
 * <p>The mod does not use GeckoLib's default asset layout: everything lives under
 * {@code assets/pleasurehorizons/geckolib/...} and textures under {@code textures/entities/}
 * (note the plural), so the paths are built manually instead of via DefaultedEntityGeoModel.</p>
 */
public class GirlModel<T extends GirlSceneEntity> extends GeoModel<T> {
    @Override
    public ResourceLocation getModelResource(T girl) {
        // Nude and dressed variants are separate models; she "undresses" by swapping model.
        String variant = girl.isStripped() ? "nude" : "dressed";
        return ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID,
                "geckolib/models/" + variant + "/" + girl.getGirlID() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T girl) {
        return ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID,
                "textures/entities/" + girl.getGirlID() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(T girl) {
        return ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID,
                "geckolib/animations/" + girl.getGirlID() + ".animation.json");
    }
}
