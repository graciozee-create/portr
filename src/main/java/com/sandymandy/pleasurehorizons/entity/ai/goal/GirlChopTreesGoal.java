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
    private static final int GIVE_UP_TICKS = 600;
    private static final int GIVE_UP_COOLDOWN = 200;

    private final TameableGirlEntity girl;
    private BlockPos targetLog;
    private int cooldown = 0;
    private int elapsed = 0;
    private boolean gaveUp = false;

    public GirlChopTreesGoal(TameableGirlEntity girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!girl.isChopTreesEnabled()) return false;
        if (girl.isSitting() || girl.isSceneActive() || girl.isDowned() || girl.isPassenger()) {
            return false;
        }
        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        int range = net.minecraft.util.Mth.ceil(7.0F * (float) girl.workRadiusScale());
        BlockPos base = girl.blockPosition();
        for (int y = -2; y <= 6; y++) {
            for (int x = -range; x <= range; x++) {
                for (int z = -range; z <= range; z++) {
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
                && !girl.isSitting()
                && !girl.isSceneActive() && !girl.isDowned() && !girl.isPassenger();
    }

    @Override
    public void start() {
        this.elapsed = 0;
        girl.setDailyActivity("chop");
        if (targetLog != null) {
            girl.getNavigation().moveTo(targetLog.getX() + 0.5D, targetLog.getY(), targetLog.getZ() + 0.5D,
                    girl.workSpeedModifier());
        }
    }

    @Override
    public void tick() {
        if (targetLog == null) return;

        if (++elapsed > GIVE_UP_TICKS) {
            // Tree unreachable - drop it for a while instead of freezing forever.
            gaveUp = true;
            targetLog = null;
            return;
        }

        girl.getLookControl().setLookAt(targetLog.getX() + 0.5D, targetLog.getY(), targetLog.getZ() + 0.5D, 30.0F, 30.0F);
        if (girl.distanceToSqr(targetLog.getX() + 0.5D, targetLog.getY(), targetLog.getZ() + 0.5D) < 9.0D) {
            // Break the block server-side (rather than merely swinging at it), then continue
            // through the connected trunk.  destroyBlock also produces the normal drops.
            if (girl.level().destroyBlock(targetLog, true, girl)) {
                girl.swing(net.minecraft.world.entity.HumanoidArm.RIGHT);
            }
            targetLog = findConnectedLog(targetLog);
        } else if (girl.getNavigation().isDone()) {
            girl.getNavigation().moveTo(targetLog.getX() + 0.5D, targetLog.getY(), targetLog.getZ() + 0.5D,
                    girl.workSpeedModifier());
        }
    }

    private BlockPos findConnectedLog(BlockPos broken) {
        // Pick another nearby log, preferring the trunk above the block just broken. This makes
        // the goal finish a tree instead of stopping after one visible swing.
        for (int dy = 0; dy <= 8; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos candidate = broken.offset(dx, dy, dz);
                    if (girl.level().getBlockState(candidate).is(BlockTags.LOGS)
                            && hasLeavesAbove(candidate)) return candidate;
                }
            }
        }
        return null;
    }

    @Override
    public void stop() {
        targetLog = null;
        girl.getNavigation().stop();
        if ("chop".equals(girl.getDailyActivity())) {
            girl.setDailyActivity("");
        }
        // stop() must not clobber the give-up cooldown set in tick().
        cooldown = gaveUp ? GIVE_UP_COOLDOWN : 10;
        gaveUp = false;
    }
}
