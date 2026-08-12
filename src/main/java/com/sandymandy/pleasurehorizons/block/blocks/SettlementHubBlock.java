package com.sandymandy.pleasurehorizons.block.blocks;

import com.mojang.serialization.MapCodec;
import com.sandymandy.pleasurehorizons.block.entity.entities.SettlementHubBlockEntity;
import com.sandymandy.pleasurehorizons.util.managers.SettlementBuildingManager;
import com.sandymandy.pleasurehorizons.util.managers.SettlementManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SettlementHubBlock extends BaseEntityBlock {
    public static final MapCodec<SettlementHubBlock> CODEC = simpleCodec(SettlementHubBlock::new);

    public SettlementHubBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SettlementHubBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel serverLevel && placer instanceof Player player) {
            if (level.getBlockEntity(pos) instanceof SettlementHubBlockEntity hub) {
                hub.initializeWithOwner(serverLevel, player.getUUID());
            }
        }
    }

    /**
     * 1.21.1 signature: {@code useWithoutItem} replaces Fabric/Yarn {@code onUse}.
     * Without this the hub block was completely inert - it never opened its screen.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (level.getBlockEntity(pos) instanceof SettlementHubBlockEntity hub
                && level instanceof ServerLevel serverLevel
                && player instanceof ServerPlayer serverPlayer) {
            hub.openGui(serverLevel, serverPlayer);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
            SettlementManager manager = SettlementManager.get(serverLevel);
            manager.getAllSettlements().stream()
                    .filter(s -> s.getCorePos().equals(pos))
                    .findFirst()
                    .ifPresent(settlement -> {
                        settlement.invalidateLoadedMembers(serverLevel.getServer());
                        SettlementBuildingManager buildings = SettlementBuildingManager.get(serverLevel);
                        settlement.getBuildingIds().forEach(buildings::removeBuilding);
                        manager.removeSettlement(settlement.getId());
                    });
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof SettlementHubBlockEntity hub) {
                SettlementHubBlockEntity.tick(lvl, pos, st, hub);
            }
        };
    }
}
