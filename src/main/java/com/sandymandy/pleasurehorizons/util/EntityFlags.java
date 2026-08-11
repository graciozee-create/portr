package com.sandymandy.pleasurehorizons.util;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataAccessorHandlerRegistry;
import net.minecraft.world.entity.player.Player;

public class EntityFlags {
    public static final TrackedData<Boolean> FULL_INVIS =
            DataTracker.registerData(Player.class, TrackedDataHandlerRegistry.BOOLEAN);

}
