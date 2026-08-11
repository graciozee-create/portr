package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

import static com.sandymandy.pleasurehorizons.util.Utils.isStringInQueue;

public class StripGoal extends Goal {
    private final GirlSceneEntity girl; ;
    private boolean stripTrigged = false;
    private boolean started = false;
    private Scene scene = Scene.EMPTY;

    public StripGoal(GirlSceneEntity girl) {
        this.girl = girl;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
    }

    @Override
    public boolean canStart() {
        // Only start if not already stripping/dressing and a strip request exists
        return girl.getOverrideAnim().isEmpty() && girl.shouldStrip();
    }

    @Override
    public void start() {
        girl.setFreeze(true);
        if(girl.hasStripAnim()) girl.playAnimation("strip", false, false); // play strip anim
        if(!girl.stripOptions.equals(Scene.EMPTY))
        {
            this.scene = girl.stripOptions;
            girl.stripOptions = Scene.EMPTY;
        }
        stripTrigged = false;
        started = true;
    }

    @Override
    public void tick() {
        if(!girl.hasStripAnim()) return;
        if(started) {
            if (!girl.isFrozenInPlace()) girl.setFreeze(true);
            if (isStringInQueue(girl.getAnimationKeyFrameEvent(), "becomeNude".toLowerCase()) && !stripTrigged) {
                girl.setStripped(!girl.isStripped()); // toggle stripped state
                stripTrigged = true;
            }
        }
    }

    @Override
    public boolean shouldContinue() {
        if(!girl.hasStripAnim()) return false;
        return !stripTrigged || !girl.getOverrideAnim().isEmpty();
    }

    @Override
    public void stop() {
        if(girl.hasStripAnim()) {
            girl.setFreeze(false);
        }
        else {
            girl.setStripped(!girl.isStripped());
        }
        started = false;
        if(!this.scene.equals(Scene.EMPTY)){
            girl.startScene(this.girl.getScenePlayer(), this.scene);
            this.scene = Scene.EMPTY;
        }
    }
}
