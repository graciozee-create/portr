package com.sandymandy.pleasurehorizons.util;

import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.util.variables.CustomGirlProfile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public interface ProfileFactory<T extends GirlSceneEntity> {
    T create(EntityType<T> type, Level level, CustomGirlProfile profile);
}
