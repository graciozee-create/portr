package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.util.variables.ScenePhase;
import com.sandymandy.pleasurehorizons.util.Utils;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.UUID;


public class BedGoal extends Goal {
    private final GirlSceneEntity entity;
    private final double speed;
    private final EntityNavigation navigation;
    private Direction bedFacing;
    private Vec3d snapPos;
    private Vec3d scenePos;
    private Path pathToBed;

    public BedGoal(GirlSceneEntity entity, double speed) {
        this.entity = entity;
        this.speed = speed;
        this.navigation = entity.getNavigation();
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
        if (!(entity.getNavigation() instanceof MobNavigation) && !(entity.getNavigation() instanceof BirdNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for BedGoal");
        }
    }

    @Override
    public boolean canStart() {
        return this.entity.shouldMoveToBed() && this.entity.targetBedPos != null;
    }

    @Override
    public boolean shouldContinue() {
        return this.entity.targetBedPos != null && Utils.checkForBlockAt(this.entity.getWorld(), this.entity.targetBedPos, null, BlockTags.BEDS);
    }

    @Override
    public void start() {
        PleasureHorizons.usedBeds.put(this.entity.getUuid(),this.entity.targetBedPos);
        var state = this.entity.getWorld().getBlockState(this.entity.targetBedPos);
        if (state.contains(Properties.HORIZONTAL_FACING)) {
            this.bedFacing = state.get(Properties.HORIZONTAL_FACING);
        }
        else {
            this.bedFacing = Direction.NORTH; // default fallback
        }

        if (bedFacing == Direction.NORTH){
            this.snapPos = new Vec3d(this.entity.targetBedPos.getX() + 0.5, this.entity.targetBedPos.getY(), this.entity.targetBedPos.getZ() + 1.5);
            this.scenePos = new Vec3d(this.snapPos.getX(), this.snapPos.getY(), this.snapPos.getZ() - entity.getBedOffset());
        }
        else if (bedFacing == Direction.EAST){
            this.snapPos = new Vec3d(this.entity.targetBedPos.getX() - 0.5, this.entity.targetBedPos.getY(), this.entity.targetBedPos.getZ() + 0.5);
            this.scenePos = new Vec3d(this.snapPos.getX() + entity.getBedOffset(), this.snapPos.getY(), this.snapPos.getZ());
        }
        else if (bedFacing == Direction.SOUTH){
            this.snapPos = new Vec3d(this.entity.targetBedPos.getX() + 0.5, this.entity.targetBedPos.getY(), this.entity.targetBedPos.getZ() - 0.5);
            this.scenePos = new Vec3d(this.snapPos.getX(), this.snapPos.getY(), this.snapPos.getZ() + entity.getBedOffset());
        }
        else if (bedFacing == Direction.WEST){
            this.snapPos = new Vec3d(this.entity.targetBedPos.getX() + 1.5, this.entity.targetBedPos.getY(), this.entity.targetBedPos.getZ() + 0.5);
            this.scenePos = new Vec3d(this.snapPos.getX() - entity.getBedOffset(), this.snapPos.getY(), this.snapPos.getZ());
        }

        pathToBed = this.navigation.findPathTo(this.entity.targetBedPos, 1);
    }

    @Override
    public void tick() {
        handleMovement();
        startOnContact();
    }

    private void handleMovement() {
        if (this.entity.targetBedPos != null && this.entity.squaredDistanceTo(this.entity.targetBedPos.toCenterPos()) <= 3.0D) {
            if (this.entity.getScenePlayer() != null) {
                this.navigation.stop();

                // Compute target yaw once
                float targetYaw = this.entity.getYaw();
                if (this.bedFacing != null) {
                    targetYaw = Direction.getHorizontalDegreesOrThrow(this.bedFacing);
                }

                // Freeze state first
                this.entity.setWaitingAtBedState(true);

                // Server-authoritative snap: position + yaw together
                if (!this.entity.getWorld().isClient()) {
                    this.entity.refreshPositionAndAngles(
                            this.snapPos,
                            targetYaw, this.entity.getPitch()
                    );
                }

                this.entity.setHeadYaw(targetYaw);
                this.entity.setBodyYaw(targetYaw);

                if (!this.entity.isSceneActive()) {
                    this.entity.playPhase(ScenePhase.LAYING_DOWN);
                }
            }
        } else if (!this.entity.isWaitingAtBed()) {
            this.navigation.startMovingAlong(this.pathToBed, this.speed);
        }
    }

    private void startOnContact() {
        if (!entity.isWaitingAtBed()) return;

        UUID playerId = entity.getScenePlayer().getUuid();

        // Someone else already has this player in a scene
        if (PleasureHorizons.activeScenes.containsKey(playerId)) return;

        if (this.entity.squaredDistanceTo(this.entity.getScenePlayer()) <= 1.5 &&
                entity.getCurrentScenePhase().equals(ScenePhase.BED_IDLE)) {

            PleasureHorizons.activeScenes.put(playerId, entity.getUuid()); // reserve player
            this.entity.setPosition(scenePos);
            this.entity.startRidingScene(entity.getScenePlayer());
        }
    }

    @Override
    public void stop() {
        this.navigation.stop();
        this.entity.setWaitingAtBedState(false);
    }

}
