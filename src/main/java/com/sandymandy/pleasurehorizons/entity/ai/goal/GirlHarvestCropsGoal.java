package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class GirlHarvestCropsGoal extends Goal {
    private static final int GIVE_UP_TICKS = 600;
    private static final int GIVE_UP_COOLDOWN = 200;

    private final TameableGirlEntity girl;
    private BlockPos targetCrop;
    private int cooldown = 0;
    private int elapsed = 0;
    private boolean gaveUp = false;

    public GirlHarvestCropsGoal(TameableGirlEntity girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!girl.isHarvestEnabled()) return false;
        if (girl.isSitting() || girl.isSceneActive() || girl.isDowned() || girl.isPassenger()) {
            return false;
        }
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        int range = Mth.ceil(6.0F * (float) girl.workRadiusScale());
        BlockPos basePos = girl.blockPosition();
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
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
        return girl.isHarvestEnabled() && targetCrop != null && !girl.isSitting()
                && !girl.isSceneActive() && !girl.isDowned() && !girl.isPassenger()
                && girl.level().getBlockState(targetCrop).getBlock() instanceof CropBlock crop
                && crop.isMaxAge(girl.level().getBlockState(targetCrop));
    }

    @Override
    public void start() {
        this.elapsed = 0;
        girl.setDailyActivity("harvest");
        if (targetCrop != null) {
            girl.getNavigation().moveTo(targetCrop.getX() + 0.5D, targetCrop.getY(), targetCrop.getZ() + 0.5D,
                    girl.workSpeedModifier());
        }
    }

    @Override
    public void stop() {
        targetCrop = null;
        girl.getNavigation().stop();
        if ("harvest".equals(girl.getDailyActivity())) {
            girl.setDailyActivity("");
        }
        // stop() must not clobber the give-up cooldown set in tick().
        cooldown = gaveUp ? GIVE_UP_COOLDOWN : 20;
        gaveUp = false;
    }

    @Override
    public void tick() {
        if (targetCrop == null) return;

        if (++elapsed > GIVE_UP_TICKS) {
            // Crop unreachable (behind a wall, across water) - drop it for a while instead of
            // freezing in front of the obstacle forever.
            gaveUp = true;
            targetCrop = null;
            return;
        }

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
            girl.getNavigation().moveTo(targetCrop.getX() + 0.5D, targetCrop.getY(), targetCrop.getZ() + 0.5D,
                    girl.workSpeedModifier());
        }
    }
}
