package com.sandymandy.pleasurehorizons.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.Goal;

public class BedGoal extends Goal {
    public BedGoal(Object girl) {}
    @Override public boolean canUse() { return false; }
}
