package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class GirlHarvestCropsGoal extends Goal {
    private final TameableGirlEntity girl;
    private BlockPos targetCrop;
    private int cooldown = 0;

    public GirlHarvestCropsGoal(TameableGirlEntity girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (girl.isSitting() || girl.isFollowing() || girl.isSceneActive()) {
            return false;
        }
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        BlockPos basePos = girl.blockPosition();
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                for (int y = -2; y <= 2; y++) {
                    BlockPos pos = basePos.offset(x, y, z);
                    BlockState state = girl.level().getBlockState(pos);
                    if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                        targetCrop = pos;
                        return true;
                    }
                }
            }
        }
        cooldown = 40;
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return targetCrop != null && !girl.isSitting() && !girl.isFollowing() && !girl.isSceneActive()
                && girl.level().getBlockState(targetCrop).getBlock() instanceof CropBlock crop
                && crop.isMaxAge(girl.level().getBlockState(targetCrop));
    }

    @Override
    public void start() {
        if (targetCrop != null) {
            girl.getNavigation().moveTo(targetCrop.getX() + 0.5D, targetCrop.getY(), targetCrop.getZ() + 0.5D, 1.0D);
        }
    }

    @Override
    public void stop() {
        targetCrop = null;
        girl.getNavigation().stop();
        cooldown = 20;
    }

    @Override
    public void tick() {
        if (targetCrop != null) {
            girl.getLookControl().setLookAt(targetCrop.getX() + 0.5D, targetCrop.getY(), targetCrop.getZ() + 0.5D, 30.0F, 30.0F);
            double dist = girl.distanceToSqr(targetCrop.getX() + 0.5D, targetCrop.getY(), targetCrop.getZ() + 0.5D);
            if (dist < 4.0D) {
                BlockState state = girl.level().getBlockState(targetCrop);
                if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                    girl.level().destroyBlock(targetCrop, true, girl);
                    girl.level().setBlock(targetCrop, crop.getStateForAge(0), 3);
                    targetCrop = null;
                }
            } else if (girl.getNavigation().isDone()) {
                girl.getNavigation().moveTo(targetCrop.getX() + 0.5D, targetCrop.getY(), targetCrop.getZ() + 0.5D, 1.0D);
            }
        }
    }
}
