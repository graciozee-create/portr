package com.sandymandy.pleasurehorizons.util.managers;
import net.minecraft.server.level.ServerLevel;
import java.util.UUID;

public class TamedGirlManager {
    public static TamedGirlManager get(ServerLevel level) { return new TamedGirlManager(); }
    public void cleanupDeadGirls(ServerLevel level) {}
    public void registerGirl(Object girl) {}
    public void removeGirl(UUID id) {}
    public boolean containsGirl(UUID id) { return false; }
}
