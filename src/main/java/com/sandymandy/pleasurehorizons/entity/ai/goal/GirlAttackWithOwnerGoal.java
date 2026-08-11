package com.sandymandy.pleasurehorizons.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.Mob;

public class GirlAttackWithOwnerGoal extends TargetGoal {
    public GirlAttackWithOwnerGoal(Mob mob, Class clazz) { super(mob, false); }
    @Override public boolean canUse() { return false; }
}
