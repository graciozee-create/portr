package com.sandymandy.pleasurehorizons.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.Goal;

public class GirlFollowOwnerGoal extends Goal {
    public GirlFollowOwnerGoal(Object girl, double speed, float minDist, float maxDist) {}
    @Override public boolean canUse() { return false; }
}
