package com.sandymandy.pleasurehorizons.item.items;

import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.networking.S2C.OpenNpcEditorS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.UUID;

/**
 * Girl Wand — right-click a girl to open the NPC editor. The server validates ownership
 * (or that the girl is unowned) before sending the open-screen packet to the client.
 */
public class GirlWandItem extends Item {

    public GirlWandItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
                                                  InteractionHand hand) {
        if (!(target instanceof GirlEntity girl)) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }
        UUID owner = girl instanceof TameableGirlEntity tamed ? tamed.getOwnerUUID() : null;
        if (owner != null && !owner.equals(player.getUUID())) {
            serverPlayer.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.wand_not_owner", girlName(girl)), true);
            return InteractionResult.FAIL;
        }
        PacketDistributor.sendToPlayer(serverPlayer, new OpenNpcEditorS2CPacket(girl.getId()));
        return InteractionResult.SUCCESS;
    }

    private static String girlName(GirlEntity girl) {
        if (girl.hasCustomName()) {
            return girl.getCustomName().getString();
        }
        String id = girl.getGirlID();
        return id.isEmpty() ? "Girl" : Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.pleasurehorizons.wand"));
        tooltip.add(Component.translatable("tooltip.pleasurehorizons.wand.hint"));
    }
}
