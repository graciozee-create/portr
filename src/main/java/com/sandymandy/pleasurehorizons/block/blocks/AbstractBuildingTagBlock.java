package com.sandymandy.pleasurehorizons.block.blocks;

import com.mojang.serialization.MapCodec;
import com.sandymandy.pleasurehorizons.block.entity.entities.AbstractBuildingTagBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBuildingTagBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final VoxelShape NORTH_SHAPE = Shapes.box(0.0, 3.0/16.0, 14.0/16.0, 1.0, 13.0/16.0, 1.0);
    public static final VoxelShape SOUTH_SHAPE = Shapes.box(0.0, 3.0/16.0, 0.0, 1.0, 13.0/16.0, 2.0/16.0);
    public static final VoxelShape EAST_SHAPE  = Shapes.box(0.0, 3.0/16.0, 0.0, 2.0/16.0, 13.0/16.0, 1.0);
    public static final VoxelShape WEST_SHAPE  = Shapes.box(14.0/16.0, 3.0/16.0, 0.0, 1.0, 13.0/16.0, 1.0);

    public AbstractBuildingTagBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
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

    /**
     * Placing a tag next to a door triggers a building scan.
     *
     * <p>Nothing called {@code BuildingScanner} before, so the hub GUI always reported zero
     * buildings no matter what the player built.</p>
     */
    @Override
    public void setPlacedBy(net.minecraft.world.level.Level level, BlockPos pos, BlockState state,
                            @Nullable net.minecraft.world.entity.LivingEntity placer,
                            net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        if (!(placer instanceof net.minecraft.world.entity.player.Player player)) return;

        Direction facing = state.getValue(FACING);
        BlockPos doorPos = com.sandymandy.pleasurehorizons.util.Utils.findNearbyDoor(level, pos, facing);
        if (doorPos == null) {
            player.displayClientMessage(net.minecraft.network.chat.Component
                    .translatable("msg.pleasurehorizons.building.no_door")
                    .withStyle(net.minecraft.ChatFormatting.RED), false);
            return;
        }

        com.sandymandy.pleasurehorizons.settlement.Settlement settlement =
                com.sandymandy.pleasurehorizons.util.Utils.findNearestSettlement(level, pos);
        if (settlement == null) {
            player.displayClientMessage(net.minecraft.network.chat.Component
                    .translatable("msg.pleasurehorizons.building.no_settlement")
                    .withStyle(net.minecraft.ChatFormatting.RED), false);
            return;
        }

        com.sandymandy.pleasurehorizons.settlement.building.BuildingType type =
                level.getBlockEntity(pos) instanceof AbstractBuildingTagBlockEntity tag
                        ? tag.getBuildingType()
                        : com.sandymandy.pleasurehorizons.settlement.building.BuildingType.NONE;

        // Scan starts from the block on the inside of the door.
        BlockPos origin = com.sandymandy.pleasurehorizons.util.Utils.getBlockBehind(doorPos, facing);

        new com.sandymandy.pleasurehorizons.settlement.building.BuildingScanner(settlement)
                .scanForBuilding(serverLevel, origin, doorPos, pos, type, player);
    }

    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(props -> {
            // This will be overridden by subclasses
            throw new UnsupportedOperationException();
        });
    }
}
