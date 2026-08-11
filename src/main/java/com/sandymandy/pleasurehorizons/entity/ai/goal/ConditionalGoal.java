package com.sandymandy.pleasurehorizons.entity.ai.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import java.util.function.Supplier;

public class ConditionalGoal extends Goal {
    private final Goal wrapped;
    private final Supplier<Boolean> condition;
    public ConditionalGoal(Goal wrapped, Supplier<Boolean> condition) {
        this.wrapped = wrapped;
        this.condition = condition;
    }
    @Override public boolean canUse() { return condition.get() && wrapped.canUse(); }
    @Override public boolean canContinueToUse() { return condition.get() && wrapped.canContinueToUse(); }
    @Override public void start() { wrapped.start(); }
    @Override public void stop() { wrapped.stop(); }
    @Override public void tick() { wrapped.tick(); }
}
