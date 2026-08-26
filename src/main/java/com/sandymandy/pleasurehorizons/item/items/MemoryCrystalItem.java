package com.sandymandy.pleasurehorizons.item.items;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

/**
 * Memory Crystal (home-return system).
 *
 * <p>Right-click binds the crystal to the nearest owned girl (or untamed girl if the player owns
 * none nearby), remembering her home position. Shift + right-click teleports the player to that
 * home, matching the 1.12.2 "follow her home" mechanic.</p>
 */
public class MemoryCrystalItem extends Item {

    private static final String TAG_OWNER = "BoundOwner";
    private static final double SEARCH_RANGE = 16.0D;

    public MemoryCrystalItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.success(stack);
        }
        if (player.isShiftKeyDown()) {
            this.recall(serverLevel, player, stack);
            return InteractionResultHolder.success(stack);
        }
        this.bind(serverLevel, player, stack);
        return InteractionResultHolder.success(stack);
    }

    private void bind(ServerLevel level, Player player, ItemStack stack) {
        TameableGirlEntity nearest = null;
        double bestDist = SEARCH_RANGE * SEARCH_RANGE;
        for (TameableGirlEntity girl : level.getEntitiesOfClass(TameableGirlEntity.class,
                player.getBoundingBox().inflate(SEARCH_RANGE))) {
            if (!girl.isAlive()) continue;
            UUID owner = girl.getOwnerUUID();
            if (owner != null && !owner.equals(player.getUUID())) continue;
            double d = girl.distanceToSqr(player);
            if (d < bestDist) {
                bestDist = d;
                nearest = girl;
            }
        }
        if (nearest == null) {
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.crystal_none"), true);
            return;
        }
        final TameableGirlEntity bound = nearest;
        net.minecraft.nbt.CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString(TAG_OWNER, bound.getStringUUID());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        player.displayClientMessage(Component.translatable(
                "msg.pleasurehorizons.crystal_bound", bound.getGirlDisplayName()), true);
    }

    private void recall(ServerLevel level, Player player, ItemStack stack) {
        String ownerUUID = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag().getString(TAG_OWNER);
        if (ownerUUID.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.crystal_empty"), true);
            return;
        }
        UUID id;
        try {
            id = UUID.fromString(ownerUUID);
        } catch (IllegalArgumentException e) {
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.crystal_empty"), true);
            return;
        }
        TameableGirlEntity girl = level.getEntity(id) instanceof TameableGirlEntity g ? g : null;
        if (girl == null) {
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.crystal_gone"), true);
            return;
        }
        BlockPos home = girl.getBasePos();
        if (home == null || home.equals(BlockPos.ZERO)) {
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.crystal_no_home", girl.getGirlDisplayName()), true);
            return;
        }
        BlockPos landing = findOpenAir(level, home);
        player.teleportTo(landing.getX() + 0.5D, landing.getY(), landing.getZ() + 0.5D);
        player.displayClientMessage(
                Component.translatable("msg.pleasurehorizons.crystal_recalled"), true);
    }

    private BlockPos findOpenAir(ServerLevel level, BlockPos home) {
        BlockPos.MutableBlockPos pos = home.mutable();
        while (pos.getY() < level.getMaxBuildHeight() - 1
                && !(level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir())) {
            pos.move(0, 1, 0);
        }
        return pos.immutable();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.pleasurehorizons.crystal.use"));
        tooltip.add(Component.translatable("tooltip.pleasurehorizons.crystal.recall"));
    }
}
