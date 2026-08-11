package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

public class GirlMeleeAttackGoal extends MeleeAttackGoal {
    private final SettlementGirlEntityAI girl;
    private int ticks;

    public GirlMeleeAttackGoal(SettlementGirlEntityAI girl, double speed, boolean pauseWhenMobIdle) {
        super(girl, speed, pauseWhenMobIdle);
        this.girl = girl;
    }

    @Override
    public void start() {
        this.girl.setSprinting(true);
        super.start();
        this.ticks = 0;
    }

    @Override
    public void stop() {
        this.girl.setSprinting(false);
        super.stop();
        this.girl.setAttacking(false);
    }

    @Override
    public void tick() {
        super.tick();
        this.ticks++;
        if (this.ticks >= 5 && this.getCooldown() < this.getMaxCooldown() / 2) {
            this.girl.setAttacking(true);
        } else {
            this.girl.setAttacking(false);
        }
    }
}
