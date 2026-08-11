package com.sandymandy.pleasurehorizons.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.Goal;

public class MoveToPlayerGoal extends Goal {
    public MoveToPlayerGoal(Object girl, double speed) {}
    @Override public boolean canUse() { return false; }
}
