package com.sandymandy.pleasurehorizons.item.items;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Dragon Staff: commands all owned Kobolds within 48 blocks.
 * Shift+right-click to cycle mode, right-click to apply.
 * Modes: Follow, Stay, Guard, Wander.
 * Ported from Mine335/JennysMod1.21.1 (DragonStaffItem).
 */
public class DragonStaffItem extends Item {
    private static final double COMMAND_RANGE = 48.0D;
    private static final String MODE_KEY = "PleasureHorizonsDragonStaffMode";
    private static final String[] MODES = {"follow", "stay", "guard", "wander"};

    public DragonStaffItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level instanceof ServerLevel serverLevel) {
            if (player.isShiftKeyDown()) {
                // Cycle mode
                int next = Math.floorMod(player.getPersistentData().getInt(MODE_KEY) + 1, MODES.length);
                player.getPersistentData().putInt(MODE_KEY, next);
                player.displayClientMessage(Component.translatable(
                        "message.pleasurehorizons.staff_mode",
                        Component.translatable("mode.pleasurehorizons." + MODES[next])
                ), true);
                return InteractionResultHolder.success(stack);
            }

            // Apply mode to all owned kobolds in range
            int modeIndex = Math.floorMod(player.getPersistentData().getInt(MODE_KEY), MODES.length);
            String mode = MODES[modeIndex];

            List<TameableGirlEntity> kobolds = serverLevel.getEntitiesOfClass(
                    TameableGirlEntity.class,
                    player.getBoundingBox().inflate(COMMAND_RANGE),
                    girl -> girl.isTamed() && girl.isOwner(player)
                            && girl.getGirlID().equals("kobold"));

            for (TameableGirlEntity kobold : kobolds) {
                switch (mode) {
                    case "follow" -> { kobold.setFollowing(true); kobold.setSitting(false); }
                    case "stay" -> { kobold.setFollowing(false); kobold.setSitting(true); }
                    case "guard" -> { kobold.setFollowing(true); kobold.setSitting(false); kobold.setGuardOwnerEnabled(true); }
                    case "wander" -> { kobold.setFollowing(false); kobold.setSitting(false); }
                }
            }

            player.displayClientMessage(Component.translatable(
                    kobolds.isEmpty() ? "message.pleasurehorizons.staff_no_kobolds"
                            : "message.pleasurehorizons.staff_applied",
                    kobolds.size(),
                    Component.translatable("mode.pleasurehorizons." + mode)
            ), true);

            if (!kobolds.isEmpty()) {
                serverLevel.playSound(null, player.blockPosition(),
                        SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                        0.7F, 1.0F + modeIndex * 0.08F);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.pleasurehorizons.dragon_staff")
                .withStyle(ChatFormatting.GRAY));
    }
}
