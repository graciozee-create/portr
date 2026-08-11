package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class GirlMeleeAttackGoal extends MeleeAttackGoal {
    private final SettlementGirlEntityAI girl;
    public GirlMeleeAttackGoal(SettlementGirlEntityAI girl, double speed, boolean pauseWhenMobIdle) {
        super(girl, speed, pauseWhenMobIdle);
        this.girl = girl;
    }
}
