package com.sandymandy.pleasurehorizons.client.gui.screen.settlement;

import com.sandymandy.pleasurehorizons.settlement.Settlement;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for settlement tab content.
 * Each tab handles its own rendering and interaction within the content area.
 */
public abstract class SettlementTab {
    protected final SettlementHubScreen screen;
    protected final Settlement settlement;
    protected final List<ButtonWidget> widgets = new ArrayList<>();

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

    public SettlementTab(SettlementHubScreen screen, Settlement settlement) {
        this.screen = screen;
        this.settlement = settlement;
    }

    /**
     * Initialize the tab - called when tab becomes active
     */
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

    /**
     * Set the background texture for this tab
     */
    public void setBackgroundTexture(ResourceLocation texture) {
        this.backgroundTexture = texture;
    }

    /**
     * Create widgets for this tab - override in subclasses
     */
    protected abstract void createWidgets();

    /**
     * Get the title of this tab (not currently used, but kept for consistency)
     */
    public abstract Component getTitle();

    /**
     * Update scroll bounds based on content size
     */
    protected void updateScrollBounds() {
        // Override to set maxScrollX and maxScrollY
        maxScrollX = 0;
        maxScrollY = 0;
    }

    /**
     * Render tab content with tiled background and scrolling
     */
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render tiled background
        if (backgroundTexture != null) {
            renderTiledBackground(context);
        }

        // Render content with scroll offset
        context.getMatrices().pushMatrix();
        context.getMatrices().translate((float)(contentX - scrollX), (float)(contentY - scrollY));

        renderContent(context, mouseX, mouseY, delta);

        context.getMatrices().popMatrix();
    }

    /**
     * Render the tiled background texture
     */
    private void renderTiledBackground(DrawContext context) {
        int x = contentX;
        int y = contentY;
        int width = contentWidth;
        int height = contentHeight;

        // Calculate scroll offset for parallax effect
        int offsetX = ((int)scrollX) % 16;
        int offsetY = ((int)scrollY) % 16;

        // Draw tiled background
        for (int m = -1; m <= (width / 16) + 1; m++) {
            for (int n = -1; n <= (height / 16) + 1; n++) {
                context.drawTexture(
                        RenderPipelines.GUI_TEXTURED,
                        backgroundTexture,
                        x + (m * 16) - offsetX,
                        y + (n * 16) - offsetY,
                        0, 0,
                        16, 16,
                        16, 16
                );
            }
        }
    }

    /**
     * Render the actual content - override in subclasses
     */
    protected abstract void renderContent(DrawContext context, int mouseX, int mouseY, float delta);

    /**
     * Handle mouse scrolling
     */
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Check if mouse is over content area
        if (mouseX >= contentX && mouseX < contentX + contentWidth &&
                mouseY >= contentY && mouseY < contentY + contentHeight) {

            scrollY = Math.max(0, Math.min(maxScrollY, scrollY - verticalAmount * 10));
            scrollX = Math.max(0, Math.min(maxScrollX, scrollX - horizontalAmount * 10));

            return true;
        }
        return false;
    }

    /**
     * Handle mouse clicks - forward to widgets if needed
     */
    public void mouseClicked(double mouseX, double mouseY, int button) {
        // Override if you need custom click handling
        // Widgets are automatically handled by the screen
    }

    /**
     * Tick method for animations or updates
     */
    public void tick() {
        // Override if needed
    }

    /**
     * Clean up when tab is deselected
     */
    public void removed() {
        for (ButtonWidget widget : widgets) {
            screen.removeContentWidget(widget);
        }
        widgets.clear();
    }

    /**
     * Helper to add a widget to both local list and screen
     */
    protected void addWidget(ButtonWidget widget) {
        widgets.add(widget);
        screen.addContentWidget(widget);
    }
}
