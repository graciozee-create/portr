package com.sandymandy.pleasurehorizons.util.inventory.slot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class PublicArmorSlot extends Slot {
    public PublicArmorSlot(Container inventory, LivingEntity entity, EquipmentSlot slot, int index, int x, int y, @Nullable ResourceLocation bg) {
        super(inventory, index, x, y);
    }
}
