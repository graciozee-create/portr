package com.sandymandy.pleasurehorizons.item.items;

import com.sandymandy.pleasurehorizons.block.entity.entities.SettlementHubBlockEntity;
import com.sandymandy.pleasurehorizons.component.PleasureHorizonsDataComponentTypes;
import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import com.sandymandy.pleasurehorizons.settlement.Settlement;
import com.sandymandy.pleasurehorizons.util.managers.SettlementManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import java.util.UUID;

/** Binds to an owned settlement hub, then recruits an owned settlement-capable girl. */
public class SettlementRecruitContract extends Item {
    public SettlementRecruitContract(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity,
                                                  InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(entity instanceof SettlementGirlEntityAI girl)) {
            message(player, "item.pleasurehorizons.settlement_recruit_contract.use_on_entity.invalid_entity",
                    ChatFormatting.RED);
            return InteractionResult.FAIL;
        }
        if (!girl.isOwner(player)) {
            message(player, "item.pleasurehorizons.settlement_recruit_contract.use_on_entity.is_not_owner",
                    ChatFormatting.RED);
            return InteractionResult.FAIL;
        }

        UUID settlementId = stack.get(PleasureHorizonsDataComponentTypes.SETTLEMENT_UUID.get());
        if (settlementId == null) {
            message(player,
                    "item.pleasurehorizons.settlement_recruit_contract.use_on_entity.settlement_no_assigned_overlay",
                    ChatFormatting.RED);
            player.displayClientMessage(Component.translatable(
                    "item.pleasurehorizons.settlement_recruit_contract.use_on_entity.settlement_no_assigned_chat"), false);
            return InteractionResult.FAIL;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }
        Settlement settlement = SettlementManager.get(serverLevel).getSettlement(settlementId);
        if (settlement == null) {
            message(player,
                    "item.pleasurehorizons.settlement_recruit_contract.use_on_entity.settlement_no_longer_exists",
                    ChatFormatting.RED);
            return InteractionResult.FAIL;
        }
        if (!settlement.getOwner().equals(player.getUUID())) {
            message(player, "msg.pleasurehorizons.settlement.not_owner", ChatFormatting.RED);
            return InteractionResult.FAIL;
        }

        Settlement oldSettlement = girl.getSettlement();
        if (oldSettlement != null && oldSettlement.getId().equals(settlement.getId())) {
            settlementMessage(player, girl, "already_in_settlement", settlement, ChatFormatting.YELLOW, true);
            return InteractionResult.FAIL;
        }
        if (oldSettlement != null) {
            oldSettlement.removeMember(girl);
            settlementMessage(player, girl, "left_settlement", oldSettlement, ChatFormatting.GRAY, false);
        }

        settlement.addMember(girl);
        settlementMessage(player, girl, "joined_settlement", settlement, ChatFormatting.GREEN, true);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()
                || !(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof SettlementHubBlockEntity hub)) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Settlement settlement = hub.getSettlement();
        if (settlement == null) {
            message(player,
                    "item.pleasurehorizons.settlement_recruit_contract.use_on_entity.settlement_no_longer_exists",
                    ChatFormatting.RED);
            return InteractionResult.FAIL;
        }
        if (!settlement.getOwner().equals(player.getUUID())) {
            message(player, "msg.pleasurehorizons.settlement.not_owner", ChatFormatting.RED);
            return InteractionResult.FAIL;
        }

        context.getItemInHand().set(PleasureHorizonsDataComponentTypes.SETTLEMENT_UUID.get(), settlement.getId());
        player.displayClientMessage(Component.empty()
                .append(Component.translatable(
                        "item.pleasurehorizons.settlement_recruit_contract.use_on_block.set_settlement_id"))
                .append(" " + settlement.getName())
                .withStyle(ChatFormatting.GREEN), true);
        return InteractionResult.SUCCESS;
    }

    private static void settlementMessage(Player player, SettlementGirlEntityAI girl, String suffix,
                                          Settlement settlement, ChatFormatting formatting, boolean overlay) {
        player.displayClientMessage(Component.literal(girl.getGirlDisplayName() + " ")
                .append(Component.translatable(
                        "item.pleasurehorizons.settlement_recruit_contract.use_on_entity." + suffix))
                .append(" " + settlement.getName() + "!")
                .withStyle(formatting), overlay);
    }

    private static void message(Player player, String key, ChatFormatting formatting) {
        player.displayClientMessage(Component.translatable(key).withStyle(formatting), true);
    }
}
