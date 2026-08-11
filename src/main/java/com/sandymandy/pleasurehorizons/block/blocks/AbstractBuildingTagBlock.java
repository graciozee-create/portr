package com.sandymandy.pleasurehorizons.block.blocks;

import com.sandymandy.pleasurehorizons.block.entity.PleasureHorizonsBlockEntities;
import com.sandymandy.pleasurehorizons.block.entity.entities.AbstractBuildingTagBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBuildingTagBlock extends BlockWithEntity implements BlockEntityProvider {
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    public static final VoxelShape NORTH_SHAPE = VoxelShapes.cuboid(0.0, 3.0/16.0, 14.0/16.0, 1.0, 13.0/16.0, 1.0);
    public static final VoxelShape SOUTH_SHAPE = VoxelShapes.cuboid(0.0, 3.0/16.0, 0.0,       1.0, 13.0/16.0, 2.0/16.0);
    public static final VoxelShape EAST_SHAPE  = VoxelShapes.cuboid(0.0, 3.0/16.0, 0.0,       2.0/16.0, 13.0/16.0, 1.0);
    public static final VoxelShape WEST_SHAPE  = VoxelShapes.cuboid(14.0/16.0, 3.0/16.0, 0.0, 1.0, 13.0/16.0, 1.0);

    public AbstractBuildingTagBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public boolean canMobSpawnInside(BlockState state) {
        return false;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    // --- Directional placement ---
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(Properties.HORIZONTAL_FACING)) {
              case SOUTH -> SOUTH_SHAPE;
            case EAST  -> EAST_SHAPE;
            case WEST  -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world, BlockState state, BlockEntityType<T> type) {

        return validateTicker(type, PleasureHorizonsBlockEntities.BUILDING_TAG_BLOCK_ENTITY,
                AbstractBuildingTagBlockEntity::tick);
    }


    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof AbstractBuildingTagBlockEntity tag) {
            return tag.onInteract(player, world, pos);
        }

        return ActionResult.FAIL;
    }
}
