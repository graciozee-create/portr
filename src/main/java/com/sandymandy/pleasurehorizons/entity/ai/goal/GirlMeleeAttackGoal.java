package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class GirlMeleeAttackGoal extends MeleeAttackGoal {
    private final GirlEntity girl;
    private int ticks;

    public GirlMeleeAttackGoal(GirlEntity girl, double speed, boolean pauseWhenMobIdle) {
        super(girl, speed, pauseWhenMobIdle);
        this.girl = girl;
    }

    private boolean isUnavailable() {
        return this.girl.isSceneActive() || this.girl.isDowned()
                || this.girl.isPassenger() || this.girl.isSitting();
    }

    @Override
    public boolean canUse() {
        return !isUnavailable() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !isUnavailable() && super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        this.girl.setSprinting(true);
        this.ticks = 0;
    }

    @Override
    public void stop() {
        super.stop();
        this.girl.setSprinting(false);
        this.girl.setAggressive(false);
    }

    @Override
    public void tick() {
        super.tick();
        this.ticks++;

        // Yarn's getCooldown/getMaxCooldown are getTicksUntilNextAttack/getAttackInterval
        // in Mojang mappings. Preserve upstream's short wind-up before the attack pose.
        boolean inAttackWindup = this.ticks >= 5
                && this.getTicksUntilNextAttack() < this.getAttackInterval() / 2;
        this.girl.setAggressive(inAttackWindup);
    }
}
