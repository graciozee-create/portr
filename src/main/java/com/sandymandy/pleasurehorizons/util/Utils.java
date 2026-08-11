package com.sandymandy.pleasurehorizons.util;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.settlement.Settlement;
import com.sandymandy.pleasurehorizons.util.managers.SettlementManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Shared helpers ported from the Fabric original.
 *
 * <p>Yarn to Mojang mappings applied here: {@code World} to {@link Level},
 * {@code ServerWorld} to {@link ServerLevel}, {@code Vec3d} to {@link Vec3},
 * {@code Identifier} to {@link ResourceLocation}, {@code Properties} to
 * {@link BlockStateProperties}, {@code Direction.Type.HORIZONTAL} to
 * {@link Direction.Plane#HORIZONTAL}, {@code pos.offset(dir)} to {@code pos.relative(dir)}, {@code state.isIn(tag)} to
 * {@code state.is(tag)}, {@code state.isOf(block)} to {@code state.is(block)},
 * {@code pos.down()/up()} to {@code below()/above()},
 * {@code isWithinDistance} to {@code closerThan},
 * {@code getSquaredDistance} to {@code distSqr}, and
 * {@code getManhattanDistance} to {@code distManhattan}.</p>
 */
public class Utils {

    public static Boolean isStringInQueue(Queue<String> queue, String text) {
        for (String event : queue) {
            if (event.contains(text)) {
                return true;
            }
        }
        return false;
    }

    public static BlockPos getBlockPosFromVec3d(Vec3 pos) {
        return new BlockPos((int) pos.x(), (int) pos.y(), (int) pos.z());
    }

    public static Settlement findNearestSettlement(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return null;

        SettlementManager manager = SettlementManager.get(serverLevel);

        return manager.getAllSettlements().stream()
                .filter(s -> s.getCorePos().closerThan(pos, 200))
                .min(Comparator.comparingDouble(s -> s.getCorePos().distSqr(pos)))
                .orElse(null);
    }

    public static Settlement findSettlementByBuilding(ServerLevel level, BlockPos doorPos) {
        List<Settlement> settlements = SettlementManager.get(level).getAllSettlements();

        for (Settlement settlement : settlements) {
            if (settlement.getBuildingIds().contains(doorPos)) return settlement;
        }
        return null;
    }

    public static BlockPos findNearbyDoor(Level level, BlockPos origin, Direction facing) {
        BlockPos check = getBlockBehind(origin, facing);
        BlockPos result = null;

        // Check directly below (tag above upper door half)
        BlockPos below = check.below();
        if (level.getBlockState(below).is(BlockTags.DOORS)) {
            result = below;
        }

        // Check horizontally around
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos side = check.relative(dir);
            BlockState state = level.getBlockState(side);

            // If the side block itself is a door
            if (state.is(BlockTags.DOORS)) {
                result = side;
            }

            // Or if the door is one below that (tag placed one higher on the wall)
            BlockPos sideBelow = side.below();
            if (level.getBlockState(sideBelow).is(BlockTags.DOORS)) {
                result = sideBelow;
            }

            // Or one above (in case tag placed at lower door half)
            BlockPos sideAbove = side.above();
            if (level.getBlockState(sideAbove).is(BlockTags.DOORS)) {
                result = sideAbove;
            }
        }

        if (result != null) {
            BlockState resultState = level.getBlockState(result);
            if (resultState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                    && resultState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
                return new BlockPos(result.getX(), result.getY() - 1, result.getZ());
            }
        }

        return result;
    }

    public static BlockPos getBlockBehind(BlockPos origin, Direction dir) {
        if (dir.equals(Direction.SOUTH)) {
            return new BlockPos(origin.getX(), origin.getY(), origin.getZ() - 1);
        }

        if (dir.equals(Direction.EAST)) {
            return new BlockPos(origin.getX() - 1, origin.getY(), origin.getZ());
        }

        if (dir.equals(Direction.WEST)) {
            return new BlockPos(origin.getX() + 1, origin.getY(), origin.getZ());
        }

        return new BlockPos(origin.getX(), origin.getY(), origin.getZ() + 1);
    }

    public static BlockInfo findNearbyBed(Level level, BlockPos center, int radius) {
        if (radius <= 0) return null;

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(center);
        visited.add(center);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();

            // --- Check this block ---
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.BEDS)
                    && state.hasProperty(BlockStateProperties.BED_PART)
                    && state.getValue(BlockStateProperties.BED_PART) == BedPart.FOOT
                    && !PleasureHorizons.usedBeds.containsValue(pos)) {

                Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                        ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                        : Direction.NORTH;

                return new BlockInfo(pos.immutable(), state, facing);
            }

            // --- Expand neighbours ---
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos next = pos.relative(dir);
                if (!visited.contains(next)
                        && center.distManhattan(next) <= radius) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }

        return null;
    }

    public static boolean checkForBlockAt(Level level, BlockPos blockPos,
                                          @Nullable Block block, @Nullable TagKey<Block> blockTag) {
        BlockState state = level.getBlockState(blockPos);
        return isBlockOrTag(state, block, blockTag);
    }

    private static boolean isBlockOrTag(BlockState state, @Nullable Block block, @Nullable TagKey<Block> tag) {
        if (block != null && state.is(block)) {
            return true;
        }
        return tag != null && state.is(tag);
    }

    public static float Round(float d, int decimalPlace) {
        return BigDecimal.valueOf(d).setScale(decimalPlace, RoundingMode.HALF_DOWN).floatValue();
    }

    public static int withFullAlpha(int color) {
        // If already has an alpha byte (ARGB)
        if ((color & 0xFF000000) != 0) {
            return color;
        }
        // Add FF as the alpha (shift by 24 bits)
        return 0xFF000000 | color;
    }

    public static String getReadableItemName(Item tameItem) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(tameItem);

        if (id != null) {
            String path = id.getPath(); // e.g., "blue_allium"

            return getFormattedByUnderscore(path);
        } else {
            return "Unknown Item";
        }
    }

    public static String getFormattedByUnderscore(String input) {
        // Capitalize each word split by underscores
        // first_second
        String[] words = input.split("_"); // "first", "second"
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return formatted.toString().trim(); // "First Second"
    }

    public static String getFirstLetterCapitalized(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    public static String getPlayerName(Player player) {
        return player.getName().getString()
                .replace("literal{", "")
                .replace("}", "");
    }

    // simple record to hold info
    public record BlockInfo(BlockPos pos, BlockState state, Direction facing) {}
}
