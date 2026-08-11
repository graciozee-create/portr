package com.sandymandy.pleasurehorizons.util.inventory.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class TexturedSlot extends Slot {
    private final ResourceLocation backgroundSpite;

    public TexturedSlot(Inventory inventory, int index, int x, int y, ResourceLocation backgroundSpite) {
        super(inventory, index, x, y);
        this.backgroundSpite = backgroundSpite;
    }

    @Override
    public @Nullable ResourceLocation getBackgroundSprite() {
        return backgroundSpite;
    }

}
