package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class MoveToPlayerGoal extends Goal {
    private final GirlSceneEntity girl;
    private boolean started = false;
    private final double speed;

    public MoveToPlayerGoal(GirlSceneEntity girl, double speed) {
        this.girl = girl;
        this.speed = speed;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
    }

    @Override
    public boolean canStart() {
        return girl.shouldMoveToPlayer();
    }

    @Override
    public void tick() {
        handleMovement();
        startOnContact();
    }

    @Override
    public boolean shouldContinue() {
        return !started ;
    }

    @Override
    public void stop() {
        this.started = false;
    }

    private void handleMovement() {
        if (!started) {
            this.girl.getNavigation().startMovingTo(this.girl.getScenePlayer(), this.speed);
        }
    }

    private void startOnContact(){
        if(this.girl.squaredDistanceTo(this.girl.getScenePlayer()) <= 2.5){
            this.girl.setVelocity(Vec3d.ZERO);
            this.girl.getNavigation().stop();
            this.girl.startRidingScene(this.girl.getScenePlayer());
            this.started = true;
        }
    }
}
