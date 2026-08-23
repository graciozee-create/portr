package com.sandymandy.pleasurehorizons.util.inventory.slot;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

public class TexturedSlot extends Slot {
    private final ResourceLocation backgroundSprite;

    public TexturedSlot(Container inventory, int index, int x, int y, ResourceLocation backgroundSprite) {
        super(inventory, index, x, y);
        this.backgroundSprite = backgroundSprite;
    }

    @Nullable
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return this.backgroundSprite != null ? Pair.of(InventoryMenu.BLOCK_ATLAS, this.backgroundSprite) : super.getNoItemIcon();
    }
}
