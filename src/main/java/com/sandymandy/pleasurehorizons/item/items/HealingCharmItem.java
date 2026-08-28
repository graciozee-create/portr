package com.sandymandy.pleasurehorizons.item.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Healing Charm — passive item. While it is in a player's inventory it slowly heals that
 * player's nearby girls. The per-tick logic lives in {@code PleasureHorizonsPassiveItems}.
 */
public class HealingCharmItem extends Item {

    public HealingCharmItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.pleasurehorizons.healing_charm"));
        tooltip.add(Component.translatable("tooltip.pleasurehorizons.healing_charm.hint"));
    }
}
