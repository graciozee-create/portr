package com.sandymandy.pleasurehorizons.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.Goal;

public class GirlStayNearBaseGoal extends Goal {
    public GirlStayNearBaseGoal(Object girl, double speed, float minDist, float maxDist, int interval) {}
    @Override public boolean canUse() { return false; }
}
