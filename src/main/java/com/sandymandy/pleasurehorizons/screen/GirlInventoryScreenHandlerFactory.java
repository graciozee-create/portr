package com.sandymandy.pleasurehorizons.screen;

import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

import org.jetbrains.annotations.Nullable;

public class GirlInventoryScreenHandlerFactory implements net.minecraft.world.inventory.AbstractContainerMenuProvider {
    private final GirlEntity girl;

    public GirlInventoryScreenHandlerFactory(GirlEntity girl) {
        this.girl = girl;
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new GirlInventoryScreenHandler(syncId, playerInventory, girl.getId());
    }
}
