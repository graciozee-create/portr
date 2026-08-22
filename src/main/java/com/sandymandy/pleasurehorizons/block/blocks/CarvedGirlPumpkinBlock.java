package com.sandymandy.pleasurehorizons.block.blocks;

import com.mojang.serialization.MapCodec;
import com.sandymandy.pleasurehorizons.block.PleasureHorizonsBlocks;
import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import com.sandymandy.pleasurehorizons.registries.GirlRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * Summons a custom girl from two house-tag blocks, or Coppie from a copper golem shape.
 *
 * <p>This is a separate pumpkin from the vanilla carved pumpkin, so it intentionally does not
 * delegate placement to {@link CarvedPumpkinBlock}: doing so would spawn vanilla golems instead
 * of the girls represented by these patterns.</p>
 */
public class CarvedGirlPumpkinBlock extends CarvedPumpkinBlock {
    public static final MapCodec<CarvedGirlPumpkinBlock> CODEC = simpleCodec(CarvedGirlPumpkinBlock::new);
    private static final Predicate<BlockState> GIRL_PUMPKIN_PREDICATE = state ->
            state != null && state.getBlock() instanceof CarvedGirlPumpkinBlock;

    @Nullable
    private BlockPattern customGirlBase;
    @Nullable
    private BlockPattern customGirlFull;
    @Nullable
    private BlockPattern coppieBase;
    @Nullable
    private BlockPattern coppieFull;

    public CarvedGirlPumpkinBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends CarvedPumpkinBlock> codec() {
        return CODEC;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(state.getBlock()) && level instanceof ServerLevel serverLevel) {
            trySpawnGirl(serverLevel, pos);
        }
    }

    /** Returns whether placing this pumpkin at {@code pos} would complete either girl pattern. */
    public boolean canDispense(LevelReader level, BlockPos pos) {
        return getOrCreateCustomGirlBase().find(level, pos) != null
                || getOrCreateCoppieBase().find(level, pos) != null;
    }

    private void trySpawnGirl(ServerLevel level, BlockPos pos) {
        BlockPattern.BlockPatternMatch match = getOrCreateCustomGirlFull().find(level, pos);
        if (match != null) {
            GirlEntity girl = GirlRegistry.CUSTOM_GIRL.get().create(level);
            if (girl != null) {
                spawnGirlInWorld(level, match, girl, match.getBlock(0, 2, 0).getPos());
            }
            return;
        }

        match = getOrCreateCoppieFull().find(level, pos);
        if (match != null) {
            GirlEntity girl = GirlRegistry.COPPIE.get().create(level);
            if (girl != null) {
                spawnGirlInWorld(level, match, girl, match.getBlock(1, 2, 0).getPos());
            }
        }
    }

    private static void spawnGirlInWorld(ServerLevel level, BlockPattern.BlockPatternMatch match,
                                         GirlEntity girl, BlockPos spawnPos) {
        clearPatternBlocks(level, match);
        girl.moveTo(spawnPos.getX() + 0.5, spawnPos.getY() + 0.05, spawnPos.getZ() + 0.5,
                0.0F, 0.0F);
        // Building the pattern is a deliberate player construction (same intent as the
        // vanilla iron golem, which never despawns). Without persistence the untamed
        // girl is discarded by vanilla checkDespawn() once the builder walks away.
        girl.setPersistenceRequired();
        level.addFreshEntity(girl);

        for (ServerPlayer player : level.getEntitiesOfClass(
                ServerPlayer.class, girl.getBoundingBox().inflate(5.0))) {
            CriteriaTriggers.SUMMONED_ENTITY.trigger(player, girl);
        }

        updatePatternBlocks(level, match);
    }

    private BlockPattern getOrCreateCustomGirlBase() {
        if (customGirlBase == null) {
            customGirlBase = BlockPatternBuilder.start()
                    .aisle(" ", "#", "#")
                    .where('#', BlockInWorld.hasState(
                            BlockStatePredicate.forBlock(PleasureHorizonsBlocks.HOUSE_BUILDING_TAG.get())))
                    .build();
        }
        return customGirlBase;
    }

    private BlockPattern getOrCreateCustomGirlFull() {
        if (customGirlFull == null) {
            customGirlFull = BlockPatternBuilder.start()
                    .aisle("^", "#", "#")
                    .where('^', BlockInWorld.hasState(GIRL_PUMPKIN_PREDICATE))
                    .where('#', BlockInWorld.hasState(
                            BlockStatePredicate.forBlock(PleasureHorizonsBlocks.HOUSE_BUILDING_TAG.get())))
                    .build();
        }
        return customGirlFull;
    }

    private BlockPattern getOrCreateCoppieBase() {
        if (coppieBase == null) {
            coppieBase = BlockPatternBuilder.start()
                    .aisle("~ ~", "###", "~#~")
                    .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.COPPER_BLOCK)))
                    .where('~', block -> block.getState().isAir())
                    .build();
        }
        return coppieBase;
    }

    private BlockPattern getOrCreateCoppieFull() {
        if (coppieFull == null) {
            coppieFull = BlockPatternBuilder.start()
                    .aisle("~^~", "###", "~#~")
                    .where('^', BlockInWorld.hasState(GIRL_PUMPKIN_PREDICATE))
                    .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.COPPER_BLOCK)))
                    .where('~', block -> block.getState().isAir())
                    .build();
        }
        return coppieFull;
    }
}
