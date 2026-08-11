package com.sandymandy.pleasurehorizons.settlement;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SettlementDisplay {
    private final ItemStack icon;
    private final Component title;
    private final Component description;
    private final ResourceLocation background;
    private float x;
    private float y;

    public SettlementDisplay(ItemStack icon, Component title, Component description, ResourceLocation background) {
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.background = background;
    }

    public ItemStack getIcon() { return icon; }
    public Component getTitle() { return title; }
    public Component getDescription() { return description; }
    public ResourceLocation getBackground() { return background; }
    public float getX() { return x; }
    public float getY() { return y; }
    public void setPos(float x, float y) { this.x = x; this.y = y; }

    public static SettlementDisplay ofBasic(Component title, Component description) {
        return new SettlementDisplay(
                Items.BOOK.getDefaultInstance(),
                title,
                description,
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png")
        );
    }

    public static SettlementDisplay create(ItemStack icon, Component title, Component description, ResourceLocation background){
        return new SettlementDisplay(icon, title, description, background);
    }
}
