package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fells natural trees near the girl while {@code isChopTreesEnabled()} is on.
 *
 * <p>The old goal destroyed the first log immediately and then tried to path to every higher log.
 * Once the bottom block was gone there was no path into the trunk, so she stood underneath it
 * swinging at the air until the goal timed out. This goal first snapshots the connected trunk,
 * walks to its base once, and chops the queued logs one by one with visible block-breaking
 * progress. An axe in the main-hand slot is required and loses durability normally.</p>
 */
public class GirlChopTreesGoal extends Goal {
    private static final int GIVE_UP_TICKS = 600;
    private static final int GIVE_UP_COOLDOWN = 200;
    private static final int SEARCH_COOLDOWN = 40;
    private static final int MAX_TREE_LOGS = 192;
    private static final int MAX_TREE_HEIGHT = 24;
    private static final int MAX_TREE_RADIUS = 5;
    private static final double WORK_DISTANCE_SQ = 3.0D * 3.0D;

    private final TameableGirlEntity girl;
    private final ArrayDeque<BlockPos> logsToChop = new ArrayDeque<>();

    private BlockPos trunkBase;
    private BlockPos currentLog;
    private int cooldown;
    private int elapsed;
    private int chopTicks;
    private int requiredChopTicks;
    private boolean gaveUp;

    public GirlChopTreesGoal(TameableGirlEntity girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!girl.isChopTreesEnabled() || !canWork() || !hasAxe()
                || !girl.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return false;
        }
        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        trunkBase = findNearestTreeBase();
        if (trunkBase == null) {
            cooldown = SEARCH_COOLDOWN;
            return false;
        }

        List<BlockPos> tree = collectTreeLogs(trunkBase);
        if (tree.isEmpty()) {
            trunkBase = null;
            cooldown = SEARCH_COOLDOWN;
            return false;
        }
        logsToChop.clear();
        logsToChop.addAll(tree);
        return true;
    }

    private boolean canWork() {
        return !girl.isSitting() && !girl.isSceneActive() && !girl.isDowned()
                && !girl.isPassenger();
    }

    private boolean hasAxe() {
        return girl.getItemBySlot(EquipmentSlot.MAINHAND).is(ItemTags.AXES);
    }

    /** Finds the closest bottom log that has a real leaf canopy above it. */
    private BlockPos findNearestTreeBase() {
        int range = Mth.ceil(7.0F * (float) girl.workRadiusScale());
        BlockPos center = girl.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (int y = -2; y <= 4; y++) {
            for (int x = -range; x <= range; x++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (!girl.level().getBlockState(pos).is(BlockTags.LOGS)
                            || girl.level().getBlockState(pos.below()).is(BlockTags.LOGS)
                            || !hasCanopy(pos)) {
                        continue;
                    }
                    double distance = pos.distSqr(center);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = pos.immutable();
                    }
                }
            }
        }
        return best;
    }

    /** Natural-tree safeguard: a leaf block must occur above and near the bottom log. */
    private boolean hasCanopy(BlockPos base) {
        for (int y = 1; y <= MAX_TREE_HEIGHT; y++) {
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    if (girl.level().getBlockState(base.offset(x, y, z)).is(BlockTags.LEAVES)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Flood-fills connected logs before chopping starts. Diagonal neighbours cover branches and
     * 2x2 trunks; strict height/radius/count limits prevent a touching log building from being
     * consumed without bound.
     */
    private List<BlockPos> collectTreeLogs(BlockPos base) {
        ArrayDeque<BlockPos> open = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> result = new ArrayList<>();
        open.add(base);

        while (!open.isEmpty() && result.size() < MAX_TREE_LOGS) {
            BlockPos pos = open.removeFirst();
            if (!visited.add(pos) || !insideTreeBounds(base, pos)
                    || !girl.level().getBlockState(pos).is(BlockTags.LOGS)) {
                continue;
            }
            result.add(pos.immutable());
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx != 0 || dy != 0 || dz != 0) {
                            open.addLast(pos.offset(dx, dy, dz));
                        }
                    }
                }
            }
        }

        result.sort(Comparator
                .comparingInt((BlockPos pos) -> pos.getY())
                .thenComparingDouble(pos -> pos.distSqr(base)));
        return result;
    }

    private boolean insideTreeBounds(BlockPos base, BlockPos pos) {
        return pos.getY() >= base.getY() - 1
                && pos.getY() <= base.getY() + MAX_TREE_HEIGHT
                && Math.abs(pos.getX() - base.getX()) <= MAX_TREE_RADIUS
                && Math.abs(pos.getZ() - base.getZ()) <= MAX_TREE_RADIUS;
    }

    @Override
    public boolean canContinueToUse() {
        return girl.isChopTreesEnabled() && canWork() && hasAxe()
                && trunkBase != null && (currentLog != null || !logsToChop.isEmpty());
    }

    @Override
    public void start() {
        elapsed = 0;
        chopTicks = 0;
        requiredChopTicks = 0;
        currentLog = null;
        gaveUp = false;
        girl.setDailyActivity("chop");
        moveToTree();
    }

    private void moveToTree() {
        if (trunkBase != null) {
            girl.getNavigation().moveTo(trunkBase.getX() + 0.5D, trunkBase.getY(),
                    trunkBase.getZ() + 0.5D, girl.workSpeedModifier());
        }
    }

    @Override
    public void tick() {
        if (trunkBase == null) {
            return;
        }
        if (++elapsed > GIVE_UP_TICKS) {
            gaveUp = true;
            logsToChop.clear();
            clearCurrentProgress();
            return;
        }

        double x = trunkBase.getX() + 0.5D;
        double z = trunkBase.getZ() + 0.5D;
        double horizontalDistance = (girl.getX() - x) * (girl.getX() - x)
                + (girl.getZ() - z) * (girl.getZ() - z);
        if (horizontalDistance >= WORK_DISTANCE_SQ
                || Math.abs(girl.getY() - trunkBase.getY()) > 3.0D) {
            girl.getLookControl().setLookAt(x, trunkBase.getY() + 1.0D, z, 30.0F, 30.0F);
            if (girl.getNavigation().isDone() || elapsed % 20 == 0) {
                moveToTree();
            }
            return;
        }

        girl.getNavigation().stop();
        if (!selectNextLog()) {
            return;
        }

        girl.getLookControl().setLookAt(currentLog.getX() + 0.5D, currentLog.getY() + 0.5D,
                currentLog.getZ() + 0.5D, 30.0F, 30.0F);
        if (chopTicks == 0 || chopTicks % 8 == 0) {
            girl.swing(InteractionHand.MAIN_HAND);
        }
        chopTicks++;
        int stage = Mth.clamp((chopTicks * 10) / Math.max(1, requiredChopTicks), 0, 9);
        girl.level().destroyBlockProgress(girl.getId(), currentLog, stage);

        if (chopTicks >= requiredChopTicks) {
            breakCurrentLog();
        }
    }

    /** Skips logs another actor already removed and starts progress for the next intact one. */
    private boolean selectNextLog() {
        if (currentLog != null) {
            return true;
        }
        while (!logsToChop.isEmpty()) {
            BlockPos candidate = logsToChop.removeFirst();
            BlockState state = girl.level().getBlockState(candidate);
            if (state.is(BlockTags.LOGS)) {
                currentLog = candidate;
                chopTicks = 0;
                requiredChopTicks = chopDuration(state, candidate);
                return true;
            }
        }
        return false;
    }

    /** Approximation of vanilla player mining time, adjusted by the configured work pace. */
    private int chopDuration(BlockState state, BlockPos pos) {
        ItemStack axe = girl.getItemBySlot(EquipmentSlot.MAINHAND);
        float hardness = Math.max(0.1F, state.getDestroySpeed(girl.level(), pos));
        float toolSpeed = Math.max(1.0F, axe.getDestroySpeed(state));
        int vanillaTicks = Mth.ceil(30.0F * hardness / toolSpeed);
        double paceScale = 1.3D / Math.max(0.1D, girl.workSpeedModifier());
        return Mth.clamp(Mth.ceil(vanillaTicks * paceScale), 5, 40);
    }

    private void breakCurrentLog() {
        BlockPos pos = currentLog;
        BlockState state = girl.level().getBlockState(pos);
        ItemStack axe = girl.getItemBySlot(EquipmentSlot.MAINHAND);
        ItemStack toolForDrops = axe.copy();
        girl.level().destroyBlockProgress(girl.getId(), pos, -1);

        if (state.is(BlockTags.LOGS) && girl.level().destroyBlock(pos, false, girl)) {
            Block.dropResources(state, girl.level(), pos, null, girl, toolForDrops);
            axe.hurtAndBreak(1, girl, EquipmentSlot.MAINHAND);
        }
        currentLog = null;
        chopTicks = 0;
        requiredChopTicks = 0;
    }

    private void clearCurrentProgress() {
        if (currentLog != null) {
            girl.level().destroyBlockProgress(girl.getId(), currentLog, -1);
        }
        currentLog = null;
    }

    @Override
    public void stop() {
        clearCurrentProgress();
        logsToChop.clear();
        trunkBase = null;
        girl.getNavigation().stop();
        if ("chop".equals(girl.getDailyActivity())) {
            girl.setDailyActivity("");
        }
        cooldown = gaveUp ? GIVE_UP_COOLDOWN : 10;
        gaveUp = false;
    }
}
