package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.world.entity.ai.goal.DoorInteractGoal;

/**
 * Opens and closes wooden doors like a villager, so a girl following or guarding the owner can
 * path through buildings instead of getting stuck behind a closed door.
 *
 * <p>{@link DoorInteractGoal} only detects the door (it needs the navigation to route through it
 * via {@code setCanOpenDoors(true)}); the subclass is responsible for actually toggling the door
 * open on {@link #start()} and closed on {@link #stop()}.</p>
 */
public class GirlOpenDoorGoal extends DoorInteractGoal {
    private final TameableGirlEntity girl;

    public GirlOpenDoorGoal(TameableGirlEntity girl) {
        super(girl);
        this.girl = girl;
    }

    @Override
    public boolean canUse() {
        if (girl.isSceneActive() || girl.isDowned() || girl.isPassenger() || girl.isSitting()) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !girl.isSceneActive() && !girl.isPassenger() && super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        this.setOpen(true);
    }

    @Override
    public void stop() {
        this.setOpen(false);
        super.stop();
    }
}
