package com.sandymandy.pleasurehorizons.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Multi-page guide book screen opened by {@code GuideBookItem}.
 *
 * <p>Pages are localized translation keys; each page may contain multiple lines separated by
 * {@code \n}. Navigation buttons are drawn directly (no widgets) to keep the screen small.</p>
 */
@OnlyIn(Dist.CLIENT)
public class GuideBookScreen extends Screen {
    private final String[] pageKeys;
    private int currentPage = 0;
    private int pageX = 0;
    private int pageY = 0;
    private int pageW = 0;
    private int pageH = 0;

    private static final int COLOR_BORDER = 0xFF664466;
    private static final int COLOR_PAGE = 0xE0331133;
    private static final int COLOR_HEADER = 0xFFFF88CC;
    private static final int COLOR_TEXT = 0xFFDDCCDD;
    private static final int COLOR_MUTED = 0xFF887788;
    private static final int COLOR_HOVER = 0xD0443366;

    public GuideBookScreen(String[] pageKeys) {
        super(Component.translatable("gui.pleasurehorizons.guide_book.title"));
        this.pageKeys = pageKeys == null || pageKeys.length == 0
                ? new String[]{"guide.pleasurehorizons.welcome"}
                : pageKeys;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.pageW = Math.min(300, this.width - 40);
        this.pageH = Math.min(210, this.height - 60);
        this.pageX = (this.width - pageW) / 2;
        this.pageY = (this.height - pageH) / 2;

        g.fill(pageX - 2, pageY - 2, pageX + pageW + 2, pageY + pageH + 2, COLOR_BORDER);
        g.fill(pageX, pageY, pageX + pageW, pageY + pageH, COLOR_PAGE);

        g.drawString(this.font, this.title, pageX + 10, pageY + 8, COLOR_HEADER, true);
        String pageNum = Component.translatable("gui.pleasurehorizons.guide_book.page",
                this.currentPage + 1, this.pageKeys.length).getString();
        g.drawString(this.font, pageNum, pageX + pageW - this.font.width(pageNum) - 10, pageY + 8, COLOR_MUTED, true);
        g.fill(pageX + 10, pageY + 22, pageX + pageW - 10, pageY + 23, COLOR_BORDER);

        String content = Component.translatable(this.pageKeys[this.currentPage]).getString();
        String[] lines = content.split("\\n");
        int textY = pageY + 30;
        for (String line : lines) {
            if (textY > pageY + pageH - 20) break;
            g.drawString(this.font, line, pageX + 14, textY, COLOR_TEXT, true);
            textY += 12;
        }

        int btnY = pageY + pageH + 10;
        int btnW = Math.min(100, pageW / 2 - 10);
        if (this.currentPage > 0) {
            this.drawNav(g, pageX, pageX + btnW, btnY, btnW, mouseX, mouseY,
                    Component.translatable("gui.pleasurehorizons.guide_book.prev"));
        }
        if (this.currentPage < this.pageKeys.length - 1) {
            this.drawNav(g, pageX + pageW - btnW, pageX + pageW, btnY, btnW, mouseX, mouseY,
                    Component.translatable("gui.pleasurehorizons.guide_book.next"));
        }

        g.drawCenteredString(this.font,
                Component.translatable("gui.pleasurehorizons.guide_book.close"),
                this.width / 2, btnY + 28, COLOR_MUTED);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawNav(GuiGraphics g, int x1, int x2, int y, int btnW,
                         int mouseX, int mouseY, Component label) {
        boolean hovered = mouseX >= x1 && mouseX <= x2 && mouseY >= y && mouseY <= y + 20;
        g.fill(x1, y, x2, y + 20, hovered ? COLOR_HOVER : COLOR_PAGE);
        g.fill(x1, y, x2, y + 1, COLOR_BORDER);
        g.drawCenteredString(this.font, label, (x1 + x2) / 2, y + 6, COLOR_TEXT);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // No-op: the guide book is an in-game panel over the world, not a blurred menu.
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.pageW == 0) {
            this.pageW = Math.min(300, this.width - 40);
            this.pageH = Math.min(210, this.height - 60);
            this.pageX = (this.width - pageW) / 2;
            this.pageY = (this.height - pageH) / 2;
        }
        int btnY = pageY + pageH + 10;
        int btnW = Math.min(100, pageW / 2 - 10);

        if (this.currentPage > 0
                && mouseX >= pageX && mouseX <= pageX + btnW
                && mouseY >= btnY && mouseY <= btnY + 20) {
            this.currentPage--;
            return true;
        }
        if (this.currentPage < this.pageKeys.length - 1
                && mouseX >= pageX + pageW - btnW && mouseX <= pageX + pageW
                && mouseY >= btnY && mouseY <= btnY + 20) {
            this.currentPage++;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
