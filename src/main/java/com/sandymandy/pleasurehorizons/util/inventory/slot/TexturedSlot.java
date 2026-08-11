package com.sandymandy.pleasurehorizons.util.inventory.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class TexturedSlot extends Slot {
    private final Identifier backgroundSpite;

    public TexturedSlot(Inventory inventory, int index, int x, int y, Identifier backgroundSpite) {
        super(inventory, index, x, y);
        this.backgroundSpite = backgroundSpite;
    }

    @Override
    public @Nullable Identifier getBackgroundSprite() {
        return backgroundSpite;
    }

}
