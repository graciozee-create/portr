package com.sandymandy.pleasurehorizons.block.blocks;

import com.mojang.serialization.MapCodec;
import com.sandymandy.pleasurehorizons.block.PleasureHorizonsBlocks;
import com.sandymandy.pleasurehorizons.entity.girls.CoppieEntity;
import com.sandymandy.pleasurehorizons.entity.girls.CustomGirlEntity;
import com.sandymandy.pleasurehorizons.registries.GirlRegistry;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class CarvedGirlPumpkinBlock extends HorizontalFacingBlock {
    public static final MapCodec<CarvedGirlPumpkinBlock> CODEC = createCodec(CarvedGirlPumpkinBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    @Nullable
    private BlockPattern customGirlDispenserPattern;
    @Nullable
    private BlockPattern customGirlPattern;
    @Nullable
    private BlockPattern copperGirlDispenserPattern;
    @Nullable
    private BlockPattern copperGirlPattern;

    private static final Predicate<BlockState> IS_GOLEM_HEAD_PREDICATE = state -> state != null
            && state.isOf(PleasureHorizonsBlocks.CARVED_GIRL_PUMPKIN);

    @Override
    public MapCodec<? extends CarvedGirlPumpkinBlock> getCodec() {
        return CODEC;
    }

    public CarvedGirlPumpkinBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock())) {
            this.trySpawnEntity(world, pos);
        }
    }

    public boolean canDispense(WorldView world, BlockPos pos) {
        return this.getCustomGirlDispenserPattern().searchAround(world, pos) != null || this.getCopperGirlDispenserPattern().searchAround(world, pos) != null;
    }

    private void trySpawnEntity(World world, BlockPos pos) {
        BlockPattern.Result result = this.getCustomGirlPattern().searchAround(world, pos);
        if (result != null) {
            CustomGirlEntity girl = GirlRegistry.CUSTOM_GIRL.create(world, SpawnReason.TRIGGERED);
            if (girl != null) {
                spawnEntity(world, result, girl, result.translate(0, 1, 0).getBlockPos());
            }
        } else {
            BlockPattern.Result result2 = this.getCopperGirlPattern().searchAround(world, pos);
            if (result2 != null) {
                CoppieEntity coppie = GirlRegistry.COPPIE.create(world, SpawnReason.TRIGGERED);
                if (coppie != null) {
                    spawnEntity(world, result2, coppie, result2.translate(0, 1, 0).getBlockPos());
                }
            }
        }
    }

    private static void spawnEntity(World world, BlockPattern.Result patternResult, Entity entity, BlockPos pos) {
        breakPatternBlocks(world, patternResult);
        entity.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 0.05, pos.getZ() + 0.5, 0.0F, 0.0F);
        world.spawnEntity(entity);

        for (ServerPlayerEntity serverPlayerEntity : world.getNonSpectatingEntities(ServerPlayerEntity.class, entity.getBoundingBox().expand(5.0))) {
            Criteria.SUMMONED_ENTITY.trigger(serverPlayerEntity, entity);
        }

        updatePatternBlocks(world, patternResult);
    }

    public static void breakPatternBlocks(World world, BlockPattern.Result patternResult) {
        for (int i = 0; i < patternResult.getWidth(); i++) {
            for (int j = 0; j < patternResult.getHeight(); j++) {
                CachedBlockPosition cachedBlockPosition = patternResult.translate(i, j, 0);
                world.setBlockState(cachedBlockPosition.getBlockPos(), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, cachedBlockPosition.getBlockPos(), Block.getRawIdFromState(cachedBlockPosition.getBlockState()));
            }
        }
    }

    public static void updatePatternBlocks(World world, BlockPattern.Result patternResult) {
        for (int i = 0; i < patternResult.getWidth(); i++) {
            for (int j = 0; j < patternResult.getHeight(); j++) {
                CachedBlockPosition cachedBlockPosition = patternResult.translate(i, j, 0);
                world.updateNeighbors(cachedBlockPosition.getBlockPos(), Blocks.AIR);
            }
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    private BlockPattern getCustomGirlDispenserPattern() {
        if (this.customGirlDispenserPattern == null) {
            this.customGirlDispenserPattern = BlockPatternBuilder.start()
                    .aisle(" ", "#")
                    .where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.WHITE_WOOL)))
                    .build();
        }

        return this.customGirlDispenserPattern;
    }

    private BlockPattern getCustomGirlPattern() {
        if (this.customGirlPattern == null) {
            this.customGirlPattern = BlockPatternBuilder.start()
                    .aisle("^", "#")
                    .where('^', CachedBlockPosition.matchesBlockState(IS_GOLEM_HEAD_PREDICATE))
                    .where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.WHITE_WOOL)))
                    .build();
        }

        return this.customGirlPattern;
    }

    private BlockPattern getCopperGirlDispenserPattern() {
        if (this.copperGirlDispenserPattern == null) {
            this.copperGirlDispenserPattern = BlockPatternBuilder.start()
                    .aisle(" ", "#")
                    .where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.COPPER_BLOCK)))
                    .build();
        }

        return this.copperGirlDispenserPattern;
    }

    private BlockPattern getCopperGirlPattern() {
        if (this.copperGirlPattern == null) {
            this.copperGirlPattern = BlockPatternBuilder.start()
                    .aisle("^", "#")
                    .where('^', CachedBlockPosition.matchesBlockState(IS_GOLEM_HEAD_PREDICATE))
                    .where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.COPPER_BLOCK)))
                    .build();
        }

        return this.copperGirlPattern;
    }
}
