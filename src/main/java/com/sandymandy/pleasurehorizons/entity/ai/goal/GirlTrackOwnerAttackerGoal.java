package com.sandymandy.pleasurehorizons.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.Mob;

public class GirlTrackOwnerAttackerGoal extends TargetGoal {
    public GirlTrackOwnerAttackerGoal(Mob mob) { super(mob, false); }
    @Override public boolean canUse() { return false; }
}
