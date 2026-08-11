package com.sandymandy.pleasurehorizons.util;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.settlement.Settlement;
import com.sandymandy.pleasurehorizons.util.managers.SettlementManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Utils {

    public static Boolean isStringInQueue(Queue<String> queue, String text) {
        for (String event : queue) {
            if (event.contains(text)) {
                return true;
            }
        }
        return false;
    }



    public static BlockPos getBlockPosFromVec3d(Vec3d pos) {return new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ());}

    public static Settlement findNearestSettlement(World world, BlockPos pos) {
        if (!(world instanceof ServerWorld serverWorld)) return null;

        SettlementManager manager = SettlementManager.get(serverWorld);

        return manager.getAllSettlements().stream()
                .filter(s -> s.getCorePos().isWithinDistance(pos, 200))
                .min(Comparator.comparingDouble(s -> s.getCorePos().getSquaredDistance(pos)))
                .orElse(null);
    }

    public static Settlement findSettlementByBuilding(ServerWorld world, BlockPos doorPos) {
        List<Settlement> settlements = SettlementManager.get(world).getAllSettlements();

        for(Settlement settlement : settlements) {
            if(settlement.getBuildingIds().contains(doorPos)) return settlement;
        }
        return null;
    }

    public static BlockPos findNearbyDoor(World world, BlockPos origin, Direction facing) {
        BlockPos check = getBlockBehind(origin, facing);
        BlockPos result = null;

        // Check directly below (tag above upper door half)
        BlockPos below = check.down();
        if (world.getBlockState(below).isIn(BlockTags.DOORS)) {
            result = below;
        }

        // Check horizontally around
        for (Direction dir : Direction.Type.HORIZONTAL) {
            BlockPos side = check.offset(dir);
            BlockState state = world.getBlockState(side);

            // If the side block itself is a door
            if (state.isIn(BlockTags.DOORS)) {
                result = side;
            }

            // Or if the door is one below that (tag placed one higher on the wall)
            BlockPos sideBelow = side.down();
            if (world.getBlockState(sideBelow).isIn(BlockTags.DOORS)) {
                result = sideBelow;
            }

            // Or one above (in case tag placed at lower door half)
            BlockPos sideAbove = side.up();
            if (world.getBlockState(sideAbove).isIn(BlockTags.DOORS)) {
                result = sideAbove;
            }
        }

        if(result != null){
            if(world.getBlockState(result).get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER)){
                return new BlockPos(result.getX(), result.getY() - 1, result.getZ());
            }
        }

        return result;
    }

    public static BlockPos getBlockBehind(BlockPos origin, Direction dir){
        if(dir.equals(Direction.SOUTH)){
            return new BlockPos(origin.getX(), origin.getY(), origin.getZ() - 1);
        }

        if(dir.equals(Direction.EAST)){
            return new BlockPos(origin.getX() - 1, origin.getY(), origin.getZ());
        }

        if(dir.equals(Direction.WEST)){
            return new BlockPos(origin.getX() + 1, origin.getY(), origin.getZ());
        }

        return new BlockPos(origin.getX(), origin.getY(), origin.getZ() + 1);

    }

    public static boolean assetExistsClient(Identifier path) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getResourceManager() == null)
            return false;

        ResourceManager manager = client.getResourceManager();
        Optional<Resource> resource = manager.getResource(path);
        return resource.isPresent();
    }

    public static BlockInfo findNearbyBed(World world, BlockPos center, int radius) {
        if (radius <= 0) return null;

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(center);
        visited.add(center);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();

            // --- Check this block ---
            BlockState state = world.getBlockState(pos);
            if (state.isIn(BlockTags.BEDS)
                    && state.get(Properties.BED_PART) == BedPart.FOOT
                    && !PleasureHorizons.usedBeds.containsValue(pos)) {

                Direction facing = state.contains(Properties.HORIZONTAL_FACING)
                        ? state.get(Properties.HORIZONTAL_FACING)
                        : Direction.NORTH;

                return new BlockInfo(pos.toImmutable(), state, facing);
            }

            // --- Expand neighbours ---
            for (Direction dir : Direction.Type.HORIZONTAL) {
                BlockPos next = pos.offset(dir);
                if (!visited.contains(next)
                        && center.getManhattanDistance(next) <= radius) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }

        return null;
    }


    public static boolean checkForBlockAt(World world, BlockPos blockPos, @Nullable Block block, @Nullable TagKey<Block> blockTag){
        BlockState state = world.getBlockState(blockPos);
        return isBlockOrTag(state, block, blockTag);
    }

    private static boolean isBlockOrTag(BlockState state, @Nullable Block block, @Nullable TagKey<Block> tag) {
        if (block != null && state.isOf(block)) {
            return true;
        }
        return tag != null && state.isIn(tag);
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
        Identifier id = Registries.ITEM.getId(tameItem);

        if (id != null) {
            String path = id.getPath(); // e.g., "blue_allium"

            return getFormattedByUnderscore(path);
        } else {
            return "Unknown Item";
        }
    }

    public static String getFormattedByUnderscore(String input){
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

    public static String getPlayerName(PlayerEntity player){
        String name = player.getName().getString();
        name.replace("literal{","").replace("}","");
        return name;
    }

    // simple record to hold info
    public record BlockInfo(BlockPos pos, BlockState state, Direction facing) {}


}
