package com.sandymandy.pleasurehorizons.client.gui.screen.settlement;

import com.sandymandy.pleasurehorizons.settlement.SettlementSnapshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for settlement hub tab content.
 *
 * <p>Ported from Fabric: {@code DrawContext} → {@link GuiGraphics},
 * {@code context.getMatrices().pushMatrix()/translate(x, y)} → {@code guiGraphics.pose().pushPose()}
 * plus a three-argument translate, {@code ButtonWidget} → {@link AbstractWidget} (so tabs may add
 * any widget type) and {@code Text} → {@link Component}. The tab reads a
 * {@link SettlementSnapshot} instead of the live {@code Settlement}, since the client never has the
 * server-side object.</p>
 */
@OnlyIn(Dist.CLIENT)
public abstract class SettlementTab {
    protected final SettlementHubScreen screen;
    protected final SettlementSnapshot settlement;
    protected final List<AbstractWidget> widgets = new ArrayList<>();

    protected int contentX;
    protected int contentY;
    protected int contentWidth;
    protected int contentHeight;

    protected double scrollX = 0;
    protected double scrollY = 0;
    protected double maxScrollX = 0;
    protected double maxScrollY = 0;

    @Nullable
    private ResourceLocation backgroundTexture;

    protected SettlementTab(SettlementHubScreen screen, SettlementSnapshot settlement) {
        this.screen = screen;
        this.settlement = settlement == null ? SettlementSnapshot.EMPTY : settlement;
    }

    /** Called when the tab becomes active. */
    public void init(int x, int y, int width, int height) {
        this.contentX = x;
        this.contentY = y;
        this.contentWidth = width;
        this.contentHeight = height;
        this.scrollX = 0;
        this.scrollY = 0;

        createWidgets();
        updateScrollBounds();
    }

    public void setBackgroundTexture(ResourceLocation texture) {
        this.backgroundTexture = texture;
    }

    protected abstract void createWidgets();

    public abstract Component getTitle();

    protected void updateScrollBounds() {
        maxScrollX = 0;
        maxScrollY = 0;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (backgroundTexture != null) {
            renderTiledBackground(guiGraphics, backgroundTexture);
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float) (contentX - scrollX), (float) (contentY - scrollY), 0.0F);

        renderContent(guiGraphics, mouseX, mouseY, delta);

        guiGraphics.pose().popPose();
    }

    protected void renderTiledBackground(GuiGraphics guiGraphics, ResourceLocation texture) {
        int offsetX = ((int) scrollX) % 16;
        int offsetY = ((int) scrollY) % 16;

        for (int m = -1; m <= (contentWidth / 16) + 1; m++) {
            for (int n = -1; n <= (contentHeight / 16) + 1; n++) {
                guiGraphics.blit(texture,
                        contentX + (m * 16) - offsetX,
                        contentY + (n * 16) - offsetY,
                        0, 0, 16, 16, 16, 16);
            }
        }
    }

    protected abstract void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta);

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= contentX && mouseX < contentX + contentWidth
                && mouseY >= contentY && mouseY < contentY + contentHeight) {
            scrollY = Math.max(0, Math.min(maxScrollY, scrollY - verticalAmount * 10));
            scrollX = Math.max(0, Math.min(maxScrollX, scrollX - horizontalAmount * 10));
            return true;
        }
        return false;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        // Widgets are handled by the screen; override for custom hit testing.
    }

    public void tick() {
    }

    /** Called when the tab is deselected - drops every widget it registered on the screen. */
    public void removed() {
        for (AbstractWidget widget : widgets) {
            screen.removeContentWidget(widget);
        }
        widgets.clear();
    }

    protected void addWidget(AbstractWidget widget) {
        widgets.add(widget);
        screen.addContentWidget(widget);
    }
}
