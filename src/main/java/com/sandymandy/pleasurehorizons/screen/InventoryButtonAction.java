package com.sandymandy.pleasurehorizons.screen;

import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.function.BiConsumer;

public record InventoryButtonAction(Text label, int requiredRelationshipLevel, BiConsumer<GirlEntity, PlayerEntity> action) {}
