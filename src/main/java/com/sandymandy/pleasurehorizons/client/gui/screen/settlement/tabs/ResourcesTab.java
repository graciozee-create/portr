package com.sandymandy.pleasurehorizons.client.gui.screen.settlement.tabs;

import com.sandymandy.pleasurehorizons.client.gui.screen.settlement.SettlementHubScreen;
import com.sandymandy.pleasurehorizons.client.gui.screen.settlement.SettlementTab;
import com.sandymandy.pleasurehorizons.settlement.SettlementResourceData;
import com.sandymandy.pleasurehorizons.settlement.SettlementSnapshot;
import com.sandymandy.pleasurehorizons.util.Colors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Resource overview.
 *
 * <p>The Fabric version printed the raw settlement UUID and a hardcoded 75% bar. This port shows
 * the settlement name, core position and the real {@link SettlementResourceData} values that the
 * server sent along with the menu.</p>
 */
@OnlyIn(Dist.CLIENT)
public class ResourcesTab extends SettlementTab {

    public ResourcesTab(SettlementHubScreen screen, SettlementSnapshot settlement) {
        super(screen, settlement);
    }

    @Override
    protected void createWidgets() {
        // Read-only overview - no widgets yet.
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.pleasurehorizons.settlement.resources");
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        SettlementResourceData data = settlement.resources();
        int y = 10;

        guiGraphics.drawString(screen.getTextRenderer(),
                Component.literal(settlement.name()).withStyle(ChatFormatting.UNDERLINE),
                10, y, Colors.WHITE, true);
        y += 14;

        guiGraphics.drawString(screen.getTextRenderer(),
                Component.translatable("gui.pleasurehorizons.settlement.core_pos",
                        settlement.corePos().getX(), settlement.corePos().getY(), settlement.corePos().getZ()),
                10, y, Colors.LIGHT_GRAY, false);
        y += 18;

        guiGraphics.drawString(screen.getTextRenderer(),
                Component.translatable("gui.pleasurehorizons.settlement.buildings", settlement.buildingCount()),
                20, y, Colors.LIGHT_GRAY, false);
        y += 12;

        guiGraphics.drawString(screen.getTextRenderer(),
                Component.translatable("gui.pleasurehorizons.settlement.settler_count", settlement.memberCount()),
                20, y, Colors.LIGHT_GRAY, false);
        y += 12;

        guiGraphics.drawString(screen.getTextRenderer(),
                Component.translatable("gui.pleasurehorizons.settlement.food", data.food()),
                20, y, Colors.LIGHT_GREEN, false);
        y += 12;

        guiGraphics.drawString(screen.getTextRenderer(),
                Component.translatable("gui.pleasurehorizons.settlement.materials", data.materials()),
                20, y, Colors.LIGHT_ORANGE, false);
        y += 12;

        guiGraphics.drawString(screen.getTextRenderer(),
                Component.translatable("gui.pleasurehorizons.settlement.tokens", data.settlementTokens()),
                20, y, Colors.GOLD, false);
        y += 20;

        // Morale bar, driven by the real morale value (0..1).
        float morale = Math.max(0f, Math.min(1f, data.morale()));
        int barX = 20;
        int barWidth = 100;
        int barHeight = 8;

        guiGraphics.drawString(screen.getTextRenderer(),
                Component.translatable("gui.pleasurehorizons.settlement.morale"),
                barX, y - 12, Colors.LIGHT_GRAY, false);

        guiGraphics.fill(barX, y, barX + barWidth, y + barHeight, Colors.BLACK);
        int filled = (int) (barWidth * morale);
        if (filled > 2) {
            guiGraphics.fill(barX + 1, y + 1, barX + filled - 1, y + barHeight - 1, Colors.FOREST_GREEN);
        }
        guiGraphics.drawString(screen.getTextRenderer(),
                Component.literal(Math.round(morale * 100) + "%"),
                barX + barWidth + 5, y, Colors.WHITE, false);
    }

    @Override
    protected void updateScrollBounds() {
        maxScrollY = Math.max(0, 150 - contentHeight);
        maxScrollX = 0;
    }
}
