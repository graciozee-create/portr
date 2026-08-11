package com.sandymandy.pleasurehorizons.screen;

import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

public class GirlInventoryScreenHandlerFactory implements net.minecraft.world.inventory.AbstractContainerMenuProvider {
    private final GirlEntity girl;

    public GirlInventoryScreenHandlerFactory(GirlEntity girl) {
        this.girl = girl;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new GirlInventoryScreenHandler(syncId, playerInventory, girl.getId());
    }
}
