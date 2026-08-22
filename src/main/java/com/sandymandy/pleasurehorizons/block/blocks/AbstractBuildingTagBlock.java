package com.sandymandy.pleasurehorizons.block.blocks;

import com.mojang.serialization.MapCodec;
import com.sandymandy.pleasurehorizons.block.entity.entities.AbstractBuildingTagBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** A wall tag that registers and continuously validates one settlement building. */
public abstract class AbstractBuildingTagBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final VoxelShape NORTH_SHAPE = Shapes.box(0.0, 3.0 / 16.0, 14.0 / 16.0, 1.0, 13.0 / 16.0, 1.0);
    public static final VoxelShape SOUTH_SHAPE = Shapes.box(0.0, 3.0 / 16.0, 0.0, 1.0, 13.0 / 16.0, 2.0 / 16.0);
    public static final VoxelShape EAST_SHAPE = Shapes.box(0.0, 3.0 / 16.0, 0.0, 2.0 / 16.0, 13.0 / 16.0, 1.0);
    public static final VoxelShape WEST_SHAPE = Shapes.box(14.0 / 16.0, 3.0 / 16.0, 0.0, 1.0, 13.0 / 16.0, 1.0);

    protected AbstractBuildingTagBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel serverLevel
                && placer instanceof Player player
                && level.getBlockEntity(pos) instanceof AbstractBuildingTagBlockEntity tag) {
            tag.registerBuilding(serverLevel, player, state);
        }
    }

    /** Allows a failed or invalidated tag to be registered again after the room is repaired. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level instanceof ServerLevel serverLevel
                && level.getBlockEntity(pos) instanceof AbstractBuildingTagBlockEntity tag) {
            return tag.registerBuilding(serverLevel, player, state);
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && level instanceof ServerLevel serverLevel
                && level.getBlockEntity(pos) instanceof AbstractBuildingTagBlockEntity tag) {
            tag.removeRegisteredBuilding(serverLevel);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide() ? null : (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof AbstractBuildingTagBlockEntity tag) {
                AbstractBuildingTagBlockEntity.tick(tickerLevel, tickerPos, tickerState, tag);
            }
        };
    }

    @Override
    protected abstract MapCodec<? extends BaseEntityBlock> codec();
}
