package com.sandymandy.pleasurehorizons.item.items;

import com.sandymandy.pleasurehorizons.effects.PleasureHorizonsEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Horny Potion — gives the dedicated HORNY marker effect plus short Regen/Speed buffs.
 * In the original mod the marker lets untamed kobolds interact for free; the effect is
 * exposed here so that interaction can be gated by {@code hasEffect(ModEffects.hornyHolder())}.
 */
public class HornyPotionItem extends Item {

    public HornyPotionItem(Properties properties) {
        super(properties.stacksTo(16)
                .food(new FoodProperties.Builder()
                        .alwaysEdible()
                        .nutrition(1)
                        .saturationModifier(0.0F)
                        .effect(() -> new MobEffectInstance(PleasureHorizonsEffects.HORNY, 600, 0), 1.0F)
                        .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 200, 0), 1.0F)
                        .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 0), 1.0F)
                        .build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.horny_active"), true);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.pleasurehorizons.horny_potion"));
        tooltip.add(Component.translatable("tooltip.pleasurehorizons.horny_potion.hint"));
    }
}
