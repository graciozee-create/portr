package com.sandymandy.pleasurehorizons.item.items;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * In-game guide book explaining the mod's mechanics.
 *
 * <p>Right-click opens a multi-page client screen. The common item class never references
 * {@code net.minecraft.client}, so it reaches {@code ClientPacketHandlers} reflectively.</p>
 */
public class GuideBookItem extends Item {

    /** Localized page keys; {@code \\n} separates lines inside each page. */
    public static final String[] PAGE_KEYS = {
            "guide.pleasurehorizons.welcome",
            "guide.pleasurehorizons.girls",
            "guide.pleasurehorizons.gifts",
            "guide.pleasurehorizons.scenes",
            "guide.pleasurehorizons.quests",
            "guide.pleasurehorizons.items"
    };

    public GuideBookItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            openBook(stack);
        }
        return InteractionResultHolder.success(stack);
    }

    private void openBook(ItemStack stack) {
        try {
            Class<?> handler = Class.forName("com.sandymandy.pleasurehorizons.client.networking.ClientPacketHandlers");
            handler.getMethod("openGuideBook", ItemStack.class, String[].class)
                    .invoke(null, stack, PAGE_KEYS);
        } catch (Exception e) {
            PleasureHorizons.LOGGER.debug("GuideBook opened outside a client", e);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.pleasurehorizons.guide_book.read"));
        tooltip.add(Component.translatable("tooltip.pleasurehorizons.guide_book.pages", PAGE_KEYS.length));
    }
}
