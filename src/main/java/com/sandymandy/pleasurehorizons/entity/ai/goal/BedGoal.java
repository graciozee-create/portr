package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.util.Utils;
import com.sandymandy.pleasurehorizons.util.variables.ScenePhase;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

/**
 * Walks the girl to the reserved bed, snaps her into place and waits for the player.
 *
 * <p>Yarn to Mojang: {@code EntityNavigation} → {@link PathNavigation},
 * {@code findPathTo(pos, range)} → {@code createPath(pos, range)},
 * {@code startMovingAlong} → {@code moveTo(Path, double)},
 * {@code refreshPositionAndAngles} → {@code moveTo(x, y, z, yaw, pitch)},
 * {@code setHeadYaw/setBodyYaw} → {@code setYHeadRot/setYBodyRot},
 * {@code Properties.HORIZONTAL_FACING} → {@link BlockStateProperties#HORIZONTAL_FACING},
 * and {@code Direction.getHorizontalDegreesOrThrow} → {@code Direction#toYRot}.</p>
 */
public class BedGoal extends Goal {
    private final GirlSceneEntity entity;
    private final double speed;
    private final PathNavigation navigation;
    @Nullable
    private Direction bedFacing;
    @Nullable
    private Vec3 snapPos;
    @Nullable
    private Vec3 scenePos;
    @Nullable
    private Path pathToBed;

    public BedGoal(GirlSceneEntity entity, double speed) {
        this.entity = entity;
        this.speed = speed;
        this.navigation = entity.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return this.entity.shouldMoveToBed() && this.entity.targetBedPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.entity.targetBedPos != null
                && Utils.checkForBlockAt(this.entity.level(), this.entity.targetBedPos, null, BlockTags.BEDS);
    }

    @Override
    public void start() {
        if (this.entity.targetBedPos == null) return;

        PleasureHorizons.usedBeds.put(this.entity.getUUID(), this.entity.targetBedPos);

        BlockState state = this.entity.level().getBlockState(this.entity.targetBedPos);
        this.bedFacing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;

        double x = this.entity.targetBedPos.getX();
        double y = this.entity.targetBedPos.getY();
        double z = this.entity.targetBedPos.getZ();
        float offset = this.entity.getBedOffset();

        switch (this.bedFacing) {
            case EAST -> {
                this.snapPos = new Vec3(x - 0.5, y, z + 0.5);
                this.scenePos = this.snapPos.add(offset, 0, 0);
            }
            case SOUTH -> {
                this.snapPos = new Vec3(x + 0.5, y, z - 0.5);
                this.scenePos = this.snapPos.add(0, 0, offset);
            }
            case WEST -> {
                this.snapPos = new Vec3(x + 1.5, y, z + 0.5);
                this.scenePos = this.snapPos.add(-offset, 0, 0);
            }
            default -> {
                this.snapPos = new Vec3(x + 0.5, y, z + 1.5);
                this.scenePos = this.snapPos.add(0, 0, -offset);
            }
        }

        this.pathToBed = this.navigation.createPath(this.entity.targetBedPos, 1);
    }

    @Override
    public void tick() {
        handleMovement();
        startOnContact();
    }

    private void handleMovement() {
        if (this.entity.targetBedPos == null || this.snapPos == null) return;

        boolean nearBed = this.entity.distanceToSqr(Vec3.atCenterOf(this.entity.targetBedPos)) <= 3.0D;

        if (nearBed && this.entity.getScenePlayer() != null) {
            this.navigation.stop();

            float targetYaw = this.bedFacing != null ? this.bedFacing.toYRot() : this.entity.getYRot();

            this.entity.setWaitingAtBedState(true);

            if (!this.entity.level().isClientSide()) {
                this.entity.moveTo(this.snapPos.x, this.snapPos.y, this.snapPos.z, targetYaw, this.entity.getXRot());
            }

            this.entity.setYHeadRot(targetYaw);
            this.entity.setYBodyRot(targetYaw);

            if (!this.entity.isSceneActive()) {
                this.entity.setSceneState(true);
                this.entity.playPhase(ScenePhase.LAYING_DOWN);
            }
        } else if (!this.entity.isWaitingAtBed() && this.pathToBed != null) {
            this.navigation.moveTo(this.pathToBed, this.speed);
        }
    }

    private void startOnContact() {
        if (!this.entity.isWaitingAtBed()) return;

        Player player = this.entity.getScenePlayer();
        if (player == null) return;

        UUID playerId = player.getUUID();
        if (PleasureHorizons.activeScenes.containsKey(playerId)) return;

        if (this.entity.distanceToSqr(player) <= 1.5D
                && this.entity.getCurrentScenePhase() == ScenePhase.BED_IDLE) {
            PleasureHorizons.activeScenes.put(playerId, this.entity.getUUID());
            if (this.scenePos != null) {
                this.entity.setPos(this.scenePos.x, this.scenePos.y, this.scenePos.z);
            }
            this.entity.startRidingScene(player);
        }
    }

    @Override
    public void stop() {
        this.navigation.stop();
        this.entity.setWaitingAtBedState(false);
    }
}
