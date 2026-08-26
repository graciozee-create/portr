package com.sandymandy.pleasurehorizons.client.gui.screen.settlement.tabs;

import com.sandymandy.pleasurehorizons.client.gui.screen.settlement.SettlementHubScreen;
import com.sandymandy.pleasurehorizons.client.gui.screen.settlement.SettlementTab;
import com.sandymandy.pleasurehorizons.settlement.SettlementSnapshot;
import com.sandymandy.pleasurehorizons.util.Colors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Population overview.
 *
 * <p>Upstream hardcoded "25 settlers, 8 farmers, ..." placeholder text; that would be actively
 * misleading here, so this tab renders the real member and building counts the server sent plus a
 * housing-capacity bar derived from them (one house tag currently equals one settler slot).</p>
 */
@OnlyIn(Dist.CLIENT)
public class SettlersTab extends SettlementTab {

    public SettlersTab(SettlementHubScreen screen, SettlementSnapshot settlement) {
        super(screen, settlement);
    }

    @Override
    protected void createWidgets() {
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.pleasurehorizons.settlement.settlers");
    }

    @Override
    protected void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        int y = 10;

        guiGraphics.drawString(screen.getTextRenderer(),
                Component.translatable("gui.pleasurehorizons.settlement.population").withStyle(ChatFormatting.BOLD),
                10, y, Colors.WHITE, true);
        y += 20;

        int population = settlement.memberCount();
        int capacity = settlement.buildingCount();

        guiGraphics.drawString(screen.getTextRenderer(),
                Component.translatable("gui.pleasurehorizons.settlement.total_population", population),
                20, y, Colors.LIGHT_GRAY, false);
        y += 12;

        guiGraphics.drawString(screen.getTextRenderer(),
                Component.translatable("gui.pleasurehorizons.settlement.housing", population, capacity),
                20, y, capacity >= population ? Colors.LIGHT_GREEN : Colors.LIGHT_RED, false);
        y += 20;

        if (population == 0) {
            guiGraphics.drawString(screen.getTextRenderer(),
                    Component.translatable("gui.pleasurehorizons.settlement.no_settlers"),
                    20, y, Colors.LIGHT_GRAY, false);
            y += 12;
            guiGraphics.drawString(screen.getTextRenderer(),
                    Component.translatable("gui.pleasurehorizons.settlement.recruit_hint"),
                    20, y, Colors.GRAY, false);
            return;
        }

        guiGraphics.drawString(screen.getTextRenderer(),
                Component.translatable("gui.pleasurehorizons.settlement.occupancy").withStyle(ChatFormatting.UNDERLINE),
                20, y, Colors.LIGHT_YELLOW, false);
        y += 15;

        int barX = 30;
        int barWidth = 100;
        int barHeight = 8;
        float ratio = capacity <= 0 ? 1f : Math.min(1f, population / (float) capacity);

        guiGraphics.fill(barX, y, barX + barWidth, y + barHeight, Colors.BLACK);
        int filled = (int) (barWidth * ratio);
        if (filled > 2) {
            guiGraphics.fill(barX + 1, y + 1, barX + filled - 1, y + barHeight - 1, Colors.ORANGE);
        }
        guiGraphics.drawString(screen.getTextRenderer(),
                Component.literal(Math.round(ratio * 100) + "%"),
                barX + barWidth + 5, y, Colors.WHITE, false);
    }

    @Override
    protected void updateScrollBounds() {
        maxScrollY = Math.max(0, 140 - contentHeight);
        maxScrollX = 0;
    }
}
