package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

/**
 * Chops down actual trees near the girl while {@code isChopTreesEnabled()} is on.
 *
 * <p>Only logs with leaves overhead qualify, so she harvests real trees instead of griefing a
 * player's log-built house. Each broken log drops its loot, which {@code GirlGatherItemsGoal}
 * then carries back to the backpack when gathering is also enabled.</p>
 */
public class GirlChopTreesGoal extends Goal {
    private final TameableGirlEntity girl;
    private BlockPos targetLog;
    private int cooldown = 0;

    public GirlChopTreesGoal(TameableGirlEntity girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!girl.isChopTreesEnabled()) return false;
        if (girl.isSitting() || girl.isFollowing() || girl.isSceneActive() || girl.isDowned() || girl.isPassenger()) {
            return false;
        }
        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        BlockPos base = girl.blockPosition();
        for (int y = -2; y <= 6; y++) {
            for (int x = -7; x <= 7; x++) {
                for (int z = -7; z <= 7; z++) {
                    BlockPos pos = base.offset(x, y, z);
                    BlockState state = girl.level().getBlockState(pos);
                    if (state.is(BlockTags.LOGS) && hasLeavesAbove(pos)) {
                        targetLog = pos;
                        return true;
                    }
                }
            }
        }
        cooldown = 40;
        return false;
    }

    /** A log is only a tree if there is a leaf canopy within five blocks above it. */
    private boolean hasLeavesAbove(BlockPos log) {
        for (int y = 1; y <= 5; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (girl.level().getBlockState(log.offset(x, y, z)).is(BlockTags.LEAVES)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return girl.isChopTreesEnabled() && targetLog != null
                && girl.level().getBlockState(targetLog).is(BlockTags.LOGS)
                && !girl.isSitting() && !girl.isFollowing()
                && !girl.isSceneActive() && !girl.isDowned() && !girl.isPassenger();
    }

    @Override
    public void start() {
        girl.setDailyActivity("chop");
        if (targetLog != null) {
            girl.getNavigation().moveTo(targetLog.getX() + 0.5D, targetLog.getY(), targetLog.getZ() + 0.5D, 1.0D);
        }
    }

    @Override
    public void tick() {
        if (targetLog == null) return;

        girl.getLookControl().setLookAt(targetLog.getX() + 0.5D, targetLog.getY(), targetLog.getZ() + 0.5D, 30.0F, 30.0F);
        if (girl.distanceToSqr(targetLog.getX() + 0.5D, targetLog.getY(), targetLog.getZ() + 0.5D) < 9.0D) {
            girl.level().destroyBlock(targetLog, true, girl);
            targetLog = null;
        } else if (girl.getNavigation().isDone()) {
            girl.getNavigation().moveTo(targetLog.getX() + 0.5D, targetLog.getY(), targetLog.getZ() + 0.5D, 1.0D);
        }
    }

    @Override
    public void stop() {
        targetLog = null;
        girl.getNavigation().stop();
        if ("chop".equals(girl.getDailyActivity())) {
            girl.setDailyActivity("");
        }
        cooldown = 10;
    }
}
