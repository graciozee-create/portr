package com.sandymandy.pleasurehorizons.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Generic affection gift. Right-clicking a tamed girl with one in hand raises her
 * relationship level by {@link #getAffectionValue()} (consumed, creative keeps it).
 */
public class GiftItem extends Item {
    private final String descriptionKey;
    private final int affectionValue;

    public GiftItem(Properties properties, String descriptionKey, int affectionValue) {
        super(properties);
        this.descriptionKey = descriptionKey;
        this.affectionValue = affectionValue;
    }

    public int getAffectionValue() {
        return this.affectionValue;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("\u2764 +" + this.affectionValue).withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable(this.descriptionKey).withStyle(ChatFormatting.GRAY));
    }
}
