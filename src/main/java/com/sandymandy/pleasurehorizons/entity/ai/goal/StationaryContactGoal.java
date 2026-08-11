package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.util.variables.ScenePhase;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;

import java.util.UUID;

public class StationaryContactGoal extends Goal {
    private final GirlSceneEntity entity;
    private final EntityNavigation navigation;
    private boolean stop = false;
    public StationaryContactGoal(GirlSceneEntity entity) {
        this.entity = entity;
        this.navigation = entity.getNavigation();

    }

    @Override
    public boolean canStart() {
        return this.entity.shouldWaitForPlayer();
    }

    @Override
    public void start() {
        this.stop = false;
    }

    @Override
    public void tick() {
        handleMovement();
        startOnContact();
    }

    private void handleMovement() {
        if (this.entity.getScenePlayer() != null) {
            this.navigation.stop();

            float targetYaw = this.entity.getYaw();

            // Freeze state first
            this.entity.setWaitingAtBedState(true);

            // Mirror to model this tick (safe on both sides)
            this.entity.setYaw(targetYaw);
            this.entity.setHeadYaw(targetYaw);
            this.entity.setBodyYaw(targetYaw);

            // Keep LookControl from fighting the snap while waiting
            this.entity.getLookControl().lookAt(
                    this.entity.getX(), this.entity.getEyeY(), this.entity.getZ()
            );

            // Freeze state first
            this.entity.setWaitingForPlayerState(true);

            if (!this.entity.isSceneActive()) {
                this.entity.playPhase(ScenePhase.LAYING_DOWN);
            }
        }
    }

    private void startOnContact() {
        if (!entity.isWaitingForPlayer()) return;

        UUID playerId = entity.getScenePlayer().getUuid();

        // Someone else already has this player in a scene
        if (PleasureHorizons.activeScenes.containsKey(playerId)) return;

        if (this.entity.squaredDistanceTo(this.entity.getScenePlayer()) <= 1.5 &&
                entity.getCurrentScenePhase().equals(ScenePhase.BED_IDLE)) {

            PleasureHorizons.activeScenes.put(playerId, entity.getUuid()); // reserve player
            this.entity.startRidingScene(this.entity.getScenePlayer());
            this.stop = true;
        }
    }

    @Override
    public void stop() {
        this.navigation.stop();
        this.entity.setWaitingForPlayerState(false);
    }

    @Override
    public boolean shouldContinue() {
        return !this.stop;
    }
}
