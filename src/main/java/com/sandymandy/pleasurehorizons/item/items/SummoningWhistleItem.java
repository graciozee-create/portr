package com.sandymandy.pleasurehorizons.item.items;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Summoning Whistle — calls the player's closest owned girl (within 64 blocks) to their side.
 * Uses a 2-second cooldown and picks a safe open-air landing spot near the player.
 */
public class SummoningWhistleItem extends Item {

    private static final int COOLDOWN_TICKS = 40;
    private static final double SEARCH_RANGE = 64.0D;
    private static final double SEARCH_RANGE_SQ = SEARCH_RANGE * SEARCH_RANGE;

    public SummoningWhistleItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.success(stack);
        }
        if (player.getCooldowns().isOnCooldown(this)) {
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.whistle_recharge"), true);
            return InteractionResultHolder.success(stack);
        }

        TameableGirlEntity nearest = null;
        double bestDist = SEARCH_RANGE_SQ;
        for (TameableGirlEntity girl : serverLevel.getEntitiesOfClass(TameableGirlEntity.class,
                player.getBoundingBox().inflate(SEARCH_RANGE))) {
            if (!girl.isAlive()) continue;
            if (!(girl.getOwnerUUID() != null && girl.getOwnerUUID().equals(player.getUUID()))) continue;
            double dist = girl.distanceToSqr(player);
            if (dist < bestDist) {
                bestDist = dist;
                nearest = girl;
            }
        }
        if (nearest == null) {
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.whistle_nobody"), true);
            return InteractionResultHolder.success(stack);
        }

        BlockPos landing = findOpenAir(serverLevel, player);
        nearest.teleportTo(landing.getX() + 0.5D, landing.getY(), landing.getZ() + 0.5D);
        player.displayClientMessage(
                Component.translatable("msg.pleasurehorizons.whistle_called", nearest.getGirlDisplayName()), true);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        return InteractionResultHolder.success(stack);
    }

    private BlockPos findOpenAir(ServerLevel level, Player player) {
        BlockPos.MutableBlockPos pos = player.blockPosition().mutable();
        while (pos.getY() < level.getMaxBuildHeight() - 1
                && !(level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir())) {
            pos.move(0, 1, 0);
        }
        return pos.immutable();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.pleasurehorizons.whistle.use"));
        tooltip.add(Component.translatable("tooltip.pleasurehorizons.whistle.calls"));
    }
}
