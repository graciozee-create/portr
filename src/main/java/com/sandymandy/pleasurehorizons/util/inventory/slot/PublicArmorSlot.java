package com.sandymandy.pleasurehorizons.util.inventory.slot;

import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.inventory.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class PublicArmorSlot extends Slot {
    private final LivingEntity entity;
    private final EquipmentSlot equipmentSlot;
    @Nullable
    private final ResourceLocation backgroundSprite;

    public PublicArmorSlot(Inventory inventory, LivingEntity entity, EquipmentSlot equipmentSlot, int index, int x, int y, @Nullable ResourceLocation backgroundSprite) {
        super(inventory, index, x, y);
        this.entity = entity;
        this.equipmentSlot = equipmentSlot;
        this.backgroundSprite = backgroundSprite;
    }

    @Override
    public void setStack(ItemStack stack, ItemStack previousStack) {
        this.entity.onEquipStack(this.equipmentSlot, previousStack, stack);
        super.setStack(stack, previousStack);
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return this.equipmentSlot == this.entity.getPreferredEquipmentSlot(stack);
    }

    @Override
    public boolean canTakeItems(Player playerEntity) {
        ItemStack itemStack = this.getStack();
        return (itemStack.isEmpty()
                || playerEntity.isCreative()
                || !EnchantmentHelper.hasAnyEnchantmentsWith(itemStack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE)) && super.canTakeItems(playerEntity);
    }

    @Override
    public @Nullable ResourceLocation getBackgroundSprite() {
        return this.backgroundSprite;
    }
}
