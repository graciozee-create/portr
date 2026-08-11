package com.sandymandy.pleasurehorizons.util;

import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.util.variables.CustomGirlProfile;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

@FunctionalInterface
public interface ProfileFactory<T extends GirlSceneEntity> {
    T create(EntityType<T> type, World world, CustomGirlProfile profile);
}
