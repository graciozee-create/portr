package com.sandymandy.pleasurehorizons.util.inventory.slot;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

public class PublicArmorSlot extends Slot {
    private final LivingEntity entity;
    private final EquipmentSlot equipmentSlot;
    @Nullable
    private final ResourceLocation backgroundSprite;

    public PublicArmorSlot(Container inventory, LivingEntity entity, EquipmentSlot equipmentSlot, int index, int x, int y, @Nullable ResourceLocation backgroundSprite) {
        super(inventory, index, x, y);
        this.entity = entity;
        this.equipmentSlot = equipmentSlot;
        this.backgroundSprite = backgroundSprite;
    }

    @Override
    public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
        this.entity.onEquipItem(this.equipmentSlot, oldStack, newStack);
        super.setByPlayer(newStack, oldStack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.equipmentSlot == this.entity.getEquipmentSlotForItem(stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack itemStack = this.getItem();
        return (itemStack.isEmpty()
                || player.isCreative()
                || !EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) && super.mayPickup(player);
    }

    @Nullable
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return this.backgroundSprite != null ? Pair.of(InventoryMenu.BLOCK_ATLAS, this.backgroundSprite) : super.getNoItemIcon();
    }
}
