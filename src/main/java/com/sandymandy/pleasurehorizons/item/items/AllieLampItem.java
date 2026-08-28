package com.sandymandy.pleasurehorizons.item.items;

import com.sandymandy.pleasurehorizons.entity.girls.AllieEntity;
import com.sandymandy.pleasurehorizons.registries.GirlRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Allie's Lamp: summons Allie up to 3 times. Uses durability to track wishes remaining.
 * Ported from Mine335/JennysMod1.21.1 (AlliesLampItem).
 */
public class AllieLampItem extends Item {
    private static final int MAX_WISHES = 3;

    public AllieLampItem(Properties properties) {
        super(properties.durability(MAX_WISHES));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.getAbilities().instabuild && stack.getDamageValue() >= MAX_WISHES) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.pleasurehorizons.lamp_empty"), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (level instanceof ServerLevel serverLevel) {
            BlockPos pos = player.blockPosition().relative(player.getDirection(), 2);
            AllieEntity allie = GirlRegistry.ALLIE.get().spawn(serverLevel, pos, MobSpawnType.SPAWN_EGG);
            if (allie == null) {
                return InteractionResultHolder.fail(stack);
            }

            // Auto-tame and set following
            allie.setTamedBy(player);
            allie.setFollowing(true);
            allie.setPersistenceRequired();

            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    allie.getX(), allie.getY() + 0.5D, allie.getZ(),
                    24, 0.35D, 0.6D, 0.35D, 0.08D);
            serverLevel.playSound(null, allie.blockPosition(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.2F);

            if (!player.getAbilities().instabuild) {
                stack.setDamageValue(Math.min(MAX_WISHES, stack.getDamageValue() + 1));
            }

            player.displayClientMessage(Component.translatable(
                    "message.pleasurehorizons.lamp_summoned",
                    Math.max(0, MAX_WISHES - stack.getDamageValue())), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(
                "tooltip.pleasurehorizons.allies_lamp.wishes",
                Math.max(0, MAX_WISHES - stack.getDamageValue())
        ).withStyle(ChatFormatting.AQUA));
    }
}
