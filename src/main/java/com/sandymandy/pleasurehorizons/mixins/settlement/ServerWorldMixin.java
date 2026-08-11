package com.sandymandy.pleasurehorizons.mixins.settlement;

import com.sandymandy.pleasurehorizons.settlement.Settlement;
import com.sandymandy.pleasurehorizons.settlement.building.SettlementBuilding;
import com.sandymandy.pleasurehorizons.util.PleasureHorizonsMessages;
import com.sandymandy.pleasurehorizons.util.Utils;
import com.sandymandy.pleasurehorizons.util.managers.SettlementBuildingManager;
import net.minecraft.world.level.block.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Inject(method = "onBlockStateChanged", at = @At("HEAD"))
    private void onBlockChanged(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        ServerWorld serverWorld = (ServerWorld) (Object) this;

        // Check if the block type actually changed (ignore state-only changes like fence connections)
        if (oldState.isOf(newState.getBlock())) return;

        SettlementBuildingManager manager = SettlementBuildingManager.get(serverWorld);
        SettlementBuilding building = manager.getBuildingAt(pos);

        if (building == null) return;

        // trigger the validation scan
        validateBuildingRequirements(serverWorld, building, pos);
    }

    @Unique
    private void validateBuildingRequirements(ServerWorld world, SettlementBuilding building, BlockPos brokenPos) {
        BlockPos doorPos = building.doorPos();
        Settlement settlement = Utils.findSettlementByBuilding(world, doorPos);
        if (settlement == null) return;

        // Trigger the full re-scan logic
        Direction facingDirection = world.getBlockState(building.tagPos()).get(Properties.HORIZONTAL_FACING);
        boolean stillValid = settlement.getScanner().reScanVerify(world, doorPos, building.getBuildingType(), facingDirection);

        if (!stillValid) {
            settlement.removeBuilding(building.getDoorPos(), world);
            PleasureHorizonsMessages.GlobleMessage(world, Component.literal("[BuildingScanner] Invalid building at: X" + doorPos.getX() + ", Y" + doorPos.getY() + ", Z" + doorPos.getZ() + " due to changes inside").formatted(Formatting.RED));
        }
    }
}
