package com.sandymandy.pleasurehorizons.block.entity.entities;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.block.blocks.AbstractBuildingTagBlock;
import com.sandymandy.pleasurehorizons.block.entity.PleasureHorizonsBlockEntities;
import com.sandymandy.pleasurehorizons.settlement.Settlement;
import com.sandymandy.pleasurehorizons.settlement.building.BuildingScanner;
import com.sandymandy.pleasurehorizons.settlement.building.BuildingType;
import com.sandymandy.pleasurehorizons.util.Utils;
import com.sandymandy.pleasurehorizons.util.managers.SettlementManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Persistent owner for a registered settlement building.
 *
 * <p>The tag remembers both the door and settlement so removing the tag or invalidating the
 * room can remove the exact persistent building record. A staggered server-side re-scan catches
 * door, requirement, wall, and roof changes without relying on the Fabric-only world mixin.</p>
 */
public class AbstractBuildingTagBlockEntity extends BlockEntity {
    private static final int VERIFY_INTERVAL_TICKS = 100;

    private BuildingType buildingType;
    @Nullable
    private BlockPos doorPos;
    @Nullable
    private UUID settlementId;
    @Nullable
    private transient Settlement settlementCache;

    public AbstractBuildingTagBlockEntity(BlockPos pos, BlockState state, @Nullable BuildingType type) {
        super(PleasureHorizonsBlockEntities.BUILDING_TAG_BLOCK_ENTITY.get(), pos, state);
        this.buildingType = type == null ? BuildingType.NONE : type;
    }

    public BuildingType getBuildingType() {
        return buildingType;
    }

    @Nullable
    public BlockPos getDoorPos() {
        return doorPos;
    }

    @Nullable
    public Settlement getSettlement() {
        if (settlementId == null || !(level instanceof ServerLevel serverLevel)) {
            return settlementCache;
        }
        Settlement stored = SettlementManager.get(serverLevel).getSettlement(settlementId);
        if (stored == null) {
            clearAssignment();
            return null;
        }
        settlementCache = stored;
        return stored;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("BuildingType", buildingType.name());
        if (doorPos != null) {
            tag.putInt("DoorX", doorPos.getX());
            tag.putInt("DoorY", doorPos.getY());
            tag.putInt("DoorZ", doorPos.getZ());
        }
        if (settlementId != null) {
            tag.putUUID("SettlementId", settlementId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("BuildingType")) {
            try {
                buildingType = BuildingType.valueOf(tag.getString("BuildingType"));
            } catch (IllegalArgumentException ignored) {
                buildingType = BuildingType.NONE;
            }
        }
        doorPos = tag.contains("DoorX") && tag.contains("DoorY") && tag.contains("DoorZ")
                ? new BlockPos(tag.getInt("DoorX"), tag.getInt("DoorY"), tag.getInt("DoorZ"))
                : null;
        settlementId = tag.hasUUID("SettlementId") ? tag.getUUID("SettlementId") : null;
        settlementCache = null;
    }

    public InteractionResult registerBuilding(ServerLevel serverLevel, Player player, BlockState state) {
        BlockPos foundDoor = Utils.findNearbyDoor(serverLevel, worldPosition,
                state.getValue(AbstractBuildingTagBlock.FACING));
        if (foundDoor == null) {
            player.displayClientMessage(Component.translatable("msg.pleasurehorizons.building.no_door")
                    .withStyle(ChatFormatting.RED), false);
            return InteractionResult.FAIL;
        }

        Settlement settlement = getSettlement();
        if (settlement == null) {
            settlement = Utils.findNearestSettlement(serverLevel, worldPosition);
        }
        if (settlement == null) {
            player.displayClientMessage(Component.translatable("msg.pleasurehorizons.building.no_settlement")
                    .withStyle(ChatFormatting.RED), false);
            return InteractionResult.FAIL;
        }
        if (!settlement.getOwner().equals(player.getUUID())) {
            player.displayClientMessage(Component.translatable("msg.pleasurehorizons.building.not_owner")
                    .withStyle(ChatFormatting.RED), false);
            return InteractionResult.FAIL;
        }

        BlockPos origin = Utils.getBlockBehind(foundDoor, state.getValue(AbstractBuildingTagBlock.FACING));
        if (!new BuildingScanner(settlement).scanForBuilding(serverLevel, origin, foundDoor,
                worldPosition, buildingType, player)) {
            return InteractionResult.FAIL;
        }

        Settlement previousSettlement = getSettlement();
        BlockPos previousDoor = doorPos;
        if (previousSettlement != null && previousDoor != null && !previousDoor.equals(foundDoor)) {
            previousSettlement.removeBuilding(previousDoor, serverLevel);
        }

        settlementId = settlement.getId();
        settlementCache = settlement;
        doorPos = foundDoor.immutable();
        setChanged();
        return InteractionResult.CONSUME;
    }

    public void removeRegisteredBuilding(ServerLevel serverLevel) {
        Settlement settlement = getSettlement();
        if (settlement != null && doorPos != null) {
            settlement.removeBuilding(doorPos, serverLevel);
            PleasureHorizons.LOGGER.info("Removed settlement building at {} with its tag", doorPos);
        }
        clearAssignment();
    }

    private void clearAssignment() {
        settlementId = null;
        settlementCache = null;
        doorPos = null;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AbstractBuildingTagBlockEntity tag) {
        if (!(level instanceof ServerLevel serverLevel) || tag.doorPos == null || tag.settlementId == null) {
            return;
        }
        if (Math.floorMod(serverLevel.getGameTime() + pos.asLong(), VERIFY_INTERVAL_TICKS) != 0) {
            return;
        }

        BlockPos door = tag.doorPos;
        if (!serverLevel.hasChunkAt(door)) {
            return;
        }

        Settlement settlement = tag.getSettlement();
        if (settlement == null) {
            return;
        }

        boolean valid = serverLevel.getBlockState(door).is(net.minecraft.tags.BlockTags.DOORS)
                && new BuildingScanner(settlement).reScanVerify(serverLevel, door, tag.buildingType,
                state.getValue(AbstractBuildingTagBlock.FACING));
        if (!valid) {
            settlement.removeBuilding(door, serverLevel);
            PleasureHorizons.LOGGER.info("Removed invalid settlement building at {}", door);
            tag.clearAssignment();
        }
    }
}
