package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.util.variables.ScenePhase;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.UUID;

/**
 * Girl stands still and waits for the player to walk into her for a stationary-contact scene.
 *
 * <p>Yarn to Mojang: {@code getLookControl().lookAt} → {@code getLookControl().setLookAt},
 * {@code setYaw/setHeadYaw/setBodyYaw} → {@code setYRot/setYHeadRot/setYBodyRot},
 * {@code getEyeY} → {@code getEyeY()} (unchanged) and {@code squaredDistanceTo} → {@code distanceToSqr}.</p>
 */
public class StationaryContactGoal extends Goal {
    private final GirlSceneEntity entity;
    private final PathNavigation navigation;
    private boolean finished = false;
    private int ticksRunning = 0;

    public StationaryContactGoal(GirlSceneEntity entity) {
        this.entity = entity;
        this.navigation = entity.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return this.entity.shouldWaitForPlayer();
    }

    @Override
    public void start() {
        this.finished = false;
        this.ticksRunning = 0;
    }

    @Override
    public void tick() {
        ticksRunning++;
        handleMovement();
        startOnContact();
    }

    private void handleMovement() {
        Player player = this.entity.getScenePlayer();
        if (player == null) return;

        this.navigation.stop();

        float targetYaw = this.entity.getYRot();
        this.entity.setWaitingAtBedState(true);
        this.entity.setYRot(targetYaw);
        this.entity.setYHeadRot(targetYaw);
        this.entity.setYBodyRot(targetYaw);

        this.entity.getLookControl().setLookAt(
                this.entity.getX(), this.entity.getEyeY(), this.entity.getZ());

        this.entity.setWaitingForPlayerState(true);

        if (!this.entity.isSceneActive()) {
            this.entity.setSceneState(true);
            this.entity.playPhase(ScenePhase.LAYING_DOWN);
        }
    }

    private void startOnContact() {
        if (!this.entity.isWaitingForPlayer()) return;

        Player player = this.entity.getScenePlayer();
        if (player == null) return;

        UUID playerId = player.getUUID();
        if (PleasureHorizons.activeScenes.containsKey(playerId)) return;

        if (this.entity.distanceToSqr(player) <= 1.5D
                && this.entity.getCurrentScenePhase() == ScenePhase.BED_IDLE) {
            PleasureHorizons.activeScenes.put(playerId, this.entity.getUUID());
            this.entity.startRidingScene(player);
            this.finished = true;
        }
    }

    @Override
    public void stop() {
        this.navigation.stop();
        this.entity.setWaitingAtBedState(false);
        this.entity.setWaitingForPlayerState(false);
    }

    @Override
    public boolean canContinueToUse() {
        // 30s cap so a player who wanders off does not leave her standing frozen for good.
        return !this.finished && this.ticksRunning < 600 && this.entity.getScenePlayer() != null;
    }
}
