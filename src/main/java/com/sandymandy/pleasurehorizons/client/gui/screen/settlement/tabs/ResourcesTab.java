package com.sandymandy.pleasurehorizons.client.gui.screen.settlement.tabs;

import com.sandymandy.pleasurehorizons.client.gui.screen.settlement.SettlementHubScreen;
import com.sandymandy.pleasurehorizons.client.gui.screen.settlement.SettlementTab;
import com.sandymandy.pleasurehorizons.settlement.Settlement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Colors;
import net.minecraft.ChatFormatting;

public class ResourcesTab extends SettlementTab {

    public ResourcesTab(SettlementHubScreen screen, Settlement settlement) {
        super(screen, settlement);
    }

    @Override
    protected void createWidgets() {
        // Add resource management widgets here
        // Example: buttons for different resource categories
    }

    @Override
    public Component getTitle() {
        return Component.literal("Resources");
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        int yOffset = 10;

        // Header
        context.drawText(
                screen.getTextRenderer(),
                Component.literal("" + this.settlement.getId()).formatted(Formatting.UNDERLINE),
                10, yOffset,
                Colors.WHITE,
                true
        );

        yOffset += 20;

        // Example: Display resources
        context.drawText(
                screen.getTextRenderer(),
                Component.literal("Amount Of Buildings"),
                20, yOffset,
                Colors.LIGHT_GRAY,
                false
        );
        yOffset += 12;

        context.drawText(
                screen.getTextRenderer(),
                Component.literal("" + this.settlement.getBuildingIds().size()),
                20, yOffset,
                Colors.LIGHT_GRAY,
                false
        );
        yOffset += 12;

        context.drawText(
                screen.getTextRenderer(),
                Component.literal("Amount Of Settlers"),
                20, yOffset,
                Colors.LIGHT_GRAY,
                false
        );
        yOffset += 12;

        context.drawText(
                screen.getTextRenderer(),
                Component.literal("" + this.settlement.getMembers().size()),
                20, yOffset,
                Colors.LIGHT_GRAY,
                false
        );
        yOffset += 12;

        // Draw a simple progress bar example
        int barX = 20;
        int barY = yOffset + 10;
        int barWidth = 100;
        int barHeight = 8;

        context.drawText(
                screen.getTextRenderer(),
                Component.literal("Storage Usage:"),
                barX, barY - 12,
                Colors.LIGHT_GRAY,
                false
        );

        // Background
        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF000000);
        // Foreground (75% full)
        context.fill(barX + 1, barY + 1, barX + (int)(barWidth * 0.75), barY + barHeight - 1, 0xFF00AA00);
    }

    @Override
    protected void updateScrollBounds() {
        // If content is larger than display area, enable scrolling
        maxScrollY = Math.max(0, 100 - contentHeight); // 200 = estimated content height
        maxScrollX = 0; // No horizontal scroll needed
    }
}
