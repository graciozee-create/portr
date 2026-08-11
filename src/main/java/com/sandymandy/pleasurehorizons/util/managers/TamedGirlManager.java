package com.sandymandy.pleasurehorizons.util.managers;

import net.minecraft.server.level.ServerLevel;

public class TamedGirlManager {
    public static TamedGirlManager get(ServerLevel level) {
        return new TamedGirlManager();
    }

    public void cleanupDeadGirls(ServerLevel level) {
        // Will be implemented
    }
}
