package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.screen.InventoryButtonAction;
import net.minecraft.network.chat.Component;

import java.util.List;

public class InventoryButtonRegistry {
    public static final List<InventoryButtonAction> BUTTONS_LEFT = List.of(
            new InventoryButtonAction(Component.literal("Break Up"), 0,(girl, player) -> {}),
            new InventoryButtonAction(Component.literal("Set Base Here"), 1,(girl, player) -> {}),
            new InventoryButtonAction(Component.literal("Go To Base"), 1,(girl, player) -> {}),
            new InventoryButtonAction(Component.literal("Customize"), 1,(girl, player) -> {})
    );

    public static final List<InventoryButtonAction> BUTTONS_RIGHT = List.of(
            new InventoryButtonAction(Component.literal("Sit"), 2,(girl, player) -> {}),
            new InventoryButtonAction(Component.literal("Follow Me"), 3,(girl, player) -> {}),
            new InventoryButtonAction(Component.literal("Strip"), 4,(girl, player) -> {}),
            new InventoryButtonAction(Component.literal("Talk"),4 , (girl, player) -> {})
    );
}
