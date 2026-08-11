package com.sandymandy.pleasurehorizons.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.Goal;

public class StopMovementGoal extends Goal {
    public StopMovementGoal(Object girl) {}
    @Override public boolean canUse() { return false; }
}
