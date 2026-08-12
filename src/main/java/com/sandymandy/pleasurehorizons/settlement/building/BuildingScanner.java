package com.sandymandy.pleasurehorizons.settlement.building;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.settlement.Settlement;
import com.sandymandy.pleasurehorizons.util.Utils;
import com.sandymandy.pleasurehorizons.util.managers.SettlementBuildingManager;
import com.sandymandy.pleasurehorizons.util.variables.BlockEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Detects an enclosed room around a building tag and registers it with the settlement.
 *
 * <p>Ported from Fabric. Yarn to Mojang: {@code World} → {@link Level},
 * {@code Direction.Type.HORIZONTAL} → {@code Direction.Plane.HORIZONTAL},
 * {@code pos.offset(dir)} → {@code pos.relative(dir)}, {@code down()/up()} → {@code below()/above()},
 * {@code toImmutable()} → {@code immutable()}, {@code mutableCopy()} → {@code mutable()},
 * {@code state.isIn(tag)} → {@code state.is(tag)}, {@code state.isOf(block)} → {@code state.is(block)},
 * {@code state.contains(prop)} → {@code state.hasProperty(prop)}, {@code Properties} →
 * {@link BlockStateProperties}, and {@code Text.literal(...).formatted(...)} →
 * {@code Component.translatable(...).withStyle(...)} (the messages are localised here rather
 * than hardcoded English as upstream had them).</p>
 */
public class BuildingScanner {

    private final Settlement settlement;

    private static final int MAX_VERTICAL_SCAN = 15;
    private static final int MIN_CLEARANCE = 2;
    private static final int MIN_VALID_QUADRANTS = 9;
    private static final int MAX_GROUND_SEARCH = 20;
    private static final int MAX_VERIFY_VISITS = 400;

    public BuildingScanner(Settlement settlement) {
        this.settlement = settlement;
    }

    public void scanForBuilding(Level world, BlockPos origin, BlockPos doorPos, BlockPos tagPos,
                                BuildingType type, Player player) {
        if (world.isClientSide()) return;

        BlockPos groundAligned = findGroundLevel(world, origin);
        if (groundAligned == null) {
            message(player, "msg.pleasurehorizons.building.no_ground");
            return;
        }

        Set<BlockPos> validQuadrants = floodFill(world, groundAligned, Integer.MAX_VALUE);
        List<BlockEntry> structureBlocks = captureStructure(world, validQuadrants);

        boolean hasSize = validQuadrants.size() >= MIN_VALID_QUADRANTS;
        boolean hasRequirements = checkRequirements(type, structureBlocks, player);

        if (hasSize && hasRequirements) {
            registerBuilding(world, doorPos, tagPos, type, structureBlocks, validQuadrants.size(), player);
        } else if (!hasSize && player != null) {
            player.displayClientMessage(Component.translatable(
                            "msg.pleasurehorizons.building.too_small", validQuadrants.size(), MIN_VALID_QUADRANTS)
                    .withStyle(net.minecraft.ChatFormatting.RED), false);
        }
    }

    /** Re-checks a previously registered building; used to invalidate broken structures. */
    public boolean reScanVerify(Level world, BlockPos doorPos, BuildingType type, Direction tagFacing) {
        if (world.isClientSide()) return false;

        BlockPos origin = Utils.getBlockBehind(doorPos, tagFacing);
        BlockPos groundAligned = findGroundLevel(world, origin);
        if (groundAligned == null) return false;

        Set<BlockPos> validQuadrants = floodFill(world, groundAligned, MAX_VERIFY_VISITS);
        if (validQuadrants.size() < MIN_VALID_QUADRANTS) return false;

        return checkRequirements(type, captureStructure(world, validQuadrants), null);
    }

    /** Horizontal flood fill over walkable interior columns. */
    private Set<BlockPos> floodFill(Level world, BlockPos start, int maxVisits) {
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> validQuadrants = new HashSet<>();
        Queue<BlockPos> toVisit = new ArrayDeque<>();
        toVisit.add(start);

        while (!toVisit.isEmpty()) {
            BlockPos pos = toVisit.poll();
            if (!visited.add(pos)) continue;

            if (isValidQuadrant(world, pos)) {
                validQuadrants.add(pos.immutable());

                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockPos neighbor = pos.relative(dir);
                    if (isEmpty(world, neighbor) && !visited.contains(neighbor)) {
                        toVisit.add(neighbor);
                    }
                }
            }

            if (visited.size() > maxVisits) break;
        }

        return validQuadrants;
    }

    /** Captures floor, walls and ceiling around every interior column. */
    private List<BlockEntry> captureStructure(Level world, Set<BlockPos> validQuadrants) {
        Map<BlockPos, BlockState> captured = new HashMap<>();

        for (BlockPos floorPos : validQuadrants) {
            BlockPos ground = floorPos.below();
            captured.put(ground.immutable(), world.getBlockState(ground));

            for (int yOffset = 0; yOffset <= MAX_VERTICAL_SCAN; yOffset++) {
                BlockPos current = floorPos.above(yOffset);
                BlockState state = world.getBlockState(current);

                if (!isEmpty(world, current)) {
                    captured.put(current.immutable(), state);
                    break;
                }

                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockPos neighbor = current.relative(dir);
                    if (!isEmpty(world, neighbor)) {
                        captured.put(neighbor.immutable(), world.getBlockState(neighbor));
                    }
                }
            }
        }

        List<BlockEntry> blocks = new ArrayList<>();
        captured.forEach((pos, state) -> blocks.add(new BlockEntry(pos, state)));
        return blocks;
    }

    @SuppressWarnings("unchecked")
    public boolean checkRequirements(BuildingType type, List<BlockEntry> blocks, @Nullable Player player) {
        for (Map.Entry<Object, Integer> entry : type.getRequirements().entrySet()) {
            Object required = entry.getKey();
            int requiredAmount = entry.getValue();
            int foundCount = 0;

            for (BlockEntry blockEntry : blocks) {
                BlockState state = blockEntry.state();
                if (!isMainPart(state)) continue;

                if (required instanceof TagKey<?> tag) {
                    if (state.is((TagKey<Block>) tag)) foundCount++;
                } else if (required instanceof Block block) {
                    if (state.is(block)) foundCount++;
                }
            }

            if (foundCount < requiredAmount) {
                if (player != null) {
                    String name = (required instanceof TagKey<?> tag)
                            ? tag.location().getPath()
                            : ((Block) required).getName().getString();
                    player.displayClientMessage(Component.translatable(
                                    "msg.pleasurehorizons.building.missing", name, foundCount, requiredAmount)
                            .withStyle(net.minecraft.ChatFormatting.RED), false);
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Only the "main" half of a multi-block counts, so a single bed is not counted twice
     * (head + foot) and a door is not counted as two.
     */
    private boolean isMainPart(BlockState state) {
        if (state.hasProperty(BlockStateProperties.BED_PART)) {
            return state.getValue(BlockStateProperties.BED_PART) == BedPart.FOOT;
        }
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
        }
        return true;
    }

    private boolean isEmpty(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.isAir() || state.is(BlockTags.WOOL_CARPETS);
    }

    @Nullable
    private BlockPos findGroundLevel(Level world, BlockPos origin) {
        BlockPos.MutableBlockPos mutable = origin.mutable();
        for (int i = 0; i < MAX_GROUND_SEARCH; i++) {
            if (!world.getBlockState(mutable.below()).isAir()) {
                return mutable.immutable();
            }
            mutable.move(Direction.DOWN);
        }
        return null;
    }

    /** A column counts as interior if it has walking clearance and a roof above it. */
    private boolean isValidQuadrant(Level world, BlockPos pos) {
        int airHeight = 0;
        boolean hasRoof = false;

        for (int i = 1; i <= MAX_VERTICAL_SCAN; i++) {
            if (world.getBlockState(pos.above(i)).isAir()) {
                airHeight++;
            } else {
                hasRoof = true;
                break;
            }
        }

        return airHeight >= MIN_CLEARANCE - 1 && hasRoof;
    }

    private void registerBuilding(Level world, BlockPos doorPos, BlockPos tagPos, BuildingType type,
                                  List<BlockEntry> structureBlocks, int quadrants, @Nullable Player player) {
        if (!(world instanceof ServerLevel serverLevel)) return;

        SettlementBuilding building = new SettlementBuilding(doorPos, tagPos, type, structureBlocks);

        SettlementBuildingManager manager = SettlementBuildingManager.get(serverLevel);
        if (manager.getBuilding(doorPos) != null) {
            settlement.removeBuilding(doorPos, serverLevel);
        }
        settlement.addBuilding(doorPos, building, serverLevel);

        PleasureHorizons.LOGGER.info("[BuildingScanner] Registered {} with {} valid quadrants.",
                type.name(), quadrants);

        if (player != null) {
            player.displayClientMessage(Component.translatable(
                            "msg.pleasurehorizons.building.registered",
                            Component.translatable(type.getTranslationKey()), quadrants)
                    .withStyle(net.minecraft.ChatFormatting.GREEN), false);
        }
    }

    private void message(@Nullable Player player, String key) {
        if (player == null) return;
        player.displayClientMessage(
                Component.translatable(key).withStyle(net.minecraft.ChatFormatting.RED), false);
    }
}
