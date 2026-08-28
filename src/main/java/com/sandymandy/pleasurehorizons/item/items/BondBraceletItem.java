package com.sandymandy.pleasurehorizons.item.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Bond Bracelet — passive item. In the original mod it slows affection decay for nearby girls.
 * This port keeps the item (and its tooltip); affection decay itself is not yet modelled, so the
 * bracelet currently has no runtime effect beyond being an inventory charm.
 */
public class BondBraceletItem extends Item {

    public BondBraceletItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.pleasurehorizons.bond_bracelet"));
        tooltip.add(Component.translatable("tooltip.pleasurehorizons.bond_bracelet.hint"));
    }
}
