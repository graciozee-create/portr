package com.sandymandy.pleasurehorizons.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class GirlBowAttackGoal extends Goal {
    public GirlBowAttackGoal(Object girl, double speed, int interval, float range) {}
    @Override public boolean canUse() { return false; }
}
