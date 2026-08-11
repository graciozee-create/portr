package com.sandymandy.pleasurehorizons.client.gui.screen.settlement.tabs;

import com.sandymandy.pleasurehorizons.client.gui.screen.settlement.SettlementHubScreen;
import com.sandymandy.pleasurehorizons.client.gui.screen.settlement.SettlementTab;
import com.sandymandy.pleasurehorizons.settlement.Settlement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Colors;
import net.minecraft.ChatFormatting;

public class SettlersTab extends SettlementTab {

    public SettlersTab(SettlementHubScreen screen, Settlement settlement) {
        super(screen, settlement);
    }

    @Override
    protected void createWidgets() {
        // Add settler management widgets here
    }

    @Override
    public Text getTitle() {
        return Text.literal("Settlers");
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        int yOffset = 10;

        // Header
        context.drawText(
                screen.getTextRenderer(),
                Text.literal("Settlement Population").formatted(Formatting.BOLD),
                10, yOffset,
                Colors.WHITE,
                true
        );

        yOffset += 20;

        // Population stats
        context.drawText(
                screen.getTextRenderer(),
                Text.literal("Total Population: 25"),
                20, yOffset,
                Colors.LIGHT_GRAY,
                false
        );
        yOffset += 12;

        context.drawText(
                screen.getTextRenderer(),
                Text.literal("Employed: 20"),
                20, yOffset,
                0x00FF0000,
                false
        );
        yOffset += 12;

        context.drawText(
                screen.getTextRenderer(),
                Text.literal("Idle: 5"),
                20, yOffset,
                0xFFAA0000,
                false
        );
        yOffset += 20;

        // Settler categories
        context.drawText(
                screen.getTextRenderer(),
                Text.literal("Occupations:").formatted(Formatting.UNDERLINE),
                20, yOffset,
                Colors.LIGHT_YELLOW,
                false
        );
        yOffset += 15;

        String[] occupations = {
                "Farmers: 8",
                "Miners: 4",
                "Builders: 3",
                "Guards: 2",
                "Merchants: 2",
                "Crafters: 1"
        };

        for (String occupation : occupations) {
            context.drawText(
                    screen.getTextRenderer(),
                    Text.literal("• " + occupation),
                    30, yOffset,
                    Colors.LIGHT_GRAY,
                    false
            );
            yOffset += 12;
        }

        yOffset += 15;

        // Happiness indicator
        context.drawText(
                screen.getTextRenderer(),
                Text.literal("Settlement Happiness:"),
                20, yOffset,
                Colors.GRAY,
                false
        );

        yOffset += 12;
        int barX = 30;
        int barWidth = 100;
        int barHeight = 8;

        // Background
        context.fill(barX, yOffset, barX + barWidth, yOffset + barHeight, 0xFF000000);
        // Foreground (85% happy)
        context.fill(barX + 1, yOffset + 1, barX + (int)(barWidth * 0.85), yOffset + barHeight - 1, 0xFFFFAA00);

        context.drawText(
                screen.getTextRenderer(),
                Text.literal("85%"),
                barX + barWidth + 5, yOffset,
                0xFFAA00,
                false
        );
    }

    @Override
    protected void updateScrollBounds() {
        maxScrollY = Math.max(0, 250 - contentHeight);
        maxScrollX = 0;
    }
}
