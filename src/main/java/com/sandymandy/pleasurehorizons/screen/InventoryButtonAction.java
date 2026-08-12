package com.sandymandy.pleasurehorizons.screen;

import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;

public record InventoryButtonAction(String labelKey, Component label, int requiredRelationshipLevel, BiConsumer<GirlEntity, Player> action) {
    public InventoryButtonAction(String labelKey, int requiredRelationshipLevel, BiConsumer<GirlEntity, Player> action) {
        this(labelKey, Component.translatable(labelKey), requiredRelationshipLevel, action);
    }
}
