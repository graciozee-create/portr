package com.sandymandy.pleasurehorizons.item.items;

import com.sandymandy.pleasurehorizons.block.entity.entities.SettlementHubBlockEntity;
import com.sandymandy.pleasurehorizons.component.PleasureHorizonsDataComponentTypes;
import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import com.sandymandy.pleasurehorizons.settlement.Settlement;
import com.sandymandy.pleasurehorizons.util.PleasureHorizonsLangUtils;
import com.sandymandy.pleasurehorizons.util.managers.SettlementManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.UUID;

public class SettlementRecruitContract extends Item {

    public SettlementRecruitContract(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if(user.getWorld().isClient()) return ActionResult.SUCCESS;

        if(!(entity instanceof SettlementGirlEntityAI girl)) {
            user.sendMessage(Text.translatable("item.pleasurehorizons.settlement_recruit_contract.use_on_entity.invalid_entity").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }

        if(!girl.isOwner(user)){
            user.sendMessage(Text.translatable("item.pleasurehorizons.settlement_recruit_contract.use_on_entity.is_not_owner").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }

        UUID settlementId = stack.get(PleasureHorizonsDataComponentTypes.SETTLEMENT_UUID);
        if(settlementId == null) {
            user.sendMessage(Text.translatable("item.pleasurehorizons.settlement_recruit_contract.use_on_entity.settlement_no_assigned_overlay").formatted(Formatting.RED), true);
            user.sendMessage(Text.translatable("item.pleasurehorizons.settlement_recruit_contract.use_on_entity.settlement_no_assigned_chat"), false);
            return ActionResult.FAIL;
        }

        ServerWorld world = (ServerWorld) user.getWorld();
        SettlementManager manager = SettlementManager.get(world);
        Settlement settlement = manager.getSettlement(settlementId);

        if(settlement == null){
            user.sendMessage(Text.translatable("item.pleasurehorizons.settlement_recruit_contract.use_on_entity.settlement_no_longer_exists").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }

        if (girl.hasSettlement()) {
            if(settlement.getId().equals(girl.getSettlement().getId())) {
                user.sendMessage(Text.literal(girl.getGirlDisplayName() + " " + PleasureHorizonsLangUtils.getStringFromKey("item.pleasurehorizons.settlement_recruit_contract.use_on_entity.already_in_settlement") + " " + settlement.getName() + "!").formatted(Formatting.YELLOW), true);
                return ActionResult.FAIL;
            }

            Settlement oldSettlement = girl.getSettlement();
            oldSettlement.removeMember(girl);
            user.sendMessage(Text.literal(girl.getGirlDisplayName() + " " + PleasureHorizonsLangUtils.getStringFromKey("item.pleasurehorizons.settlement_recruit_contract.use_on_entity.left_settlement") + " " + oldSettlement.getName()).formatted(Formatting.GRAY), false);
        }

        settlement.addMember(girl);
        manager.markDirty();

        user.sendMessage(Text.literal(girl.getGirlDisplayName() + " " + PleasureHorizonsLangUtils.getStringFromKey("item.pleasurehorizons.settlement_recruit_contract.use_on_entity.joined_settlement") + " " + settlement.getName() + "!").formatted(Formatting.GREEN), true);

        stack.decrementUnlessCreative(1, user);
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity user = context.getPlayer();
        ItemStack stack = context.getStack();
        if(user != null && user.isSneaking()) {
            BlockEntity be = world.getBlockEntity(context.getBlockPos());
            if (be instanceof SettlementHubBlockEntity hub) {
                if (world.isClient()) return ActionResult.SUCCESS;
                user.sendMessage(Text.literal(PleasureHorizonsLangUtils.getStringFromKey("item.pleasurehorizons.settlement_recruit_contract.use_on_block.set_settlement_id") + " " + hub.getSettlement().getName()).formatted(Formatting.GREEN), true);
                stack.set(PleasureHorizonsDataComponentTypes.SETTLEMENT_UUID, hub.getSettlement().getId());
            }
        }

        return ActionResult.PASS;
    }
}
