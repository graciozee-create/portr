package com.sandymandy.pleasurehorizons.client.gui.screen.settlement;

import com.sandymandy.pleasurehorizons.settlement.SettlementDisplay;

import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class SettlementTabWidget {
    private final MinecraftClient client;
    private final SettlementHubScreen screen;
    private final SettlementTabType type;
    private final int index;
    private final SettlementDisplay display;
    private final ItemStack icon;
    private final Component title;
    private final ResourceLocation background;

    @Nullable
    private SettlementTab contentProvider;

    public SettlementTabWidget(MinecraftClient client, SettlementHubScreen screen, SettlementTabType type, int index, SettlementDisplay display) {
        this.client = client;
        this.screen = screen;
        this.type = type;
        this.index = index;
        this.display = display;
        this.icon = display.getIcon();
        this.title = display.getTitle();
        this.background = display.getBackground();
    }

    public SettlementTabWidget setContentProvider(SettlementTab provider) {
        this.contentProvider = provider;
        return this;
    }

    public void initContent(int x, int y, int width, int height) {
        if (contentProvider != null) {
            contentProvider.init(x, y, width, height);
        }
    }

    public void removeContent() {
        if (contentProvider != null) {
            contentProvider.removed();
        }
    }

    public void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        if (contentProvider != null) {
            // Render tiled background first
            renderTiledBackground(context);

            // Then render content
            contentProvider.render(context, mouseX, mouseY, delta);
        }
    }

    /**
     * Render the tiled background texture for the content area
     */
    private void renderTiledBackground(DrawContext context) {
        int x = contentProvider.contentX;
        int y = contentProvider.contentY;
        int width = contentProvider.contentWidth;
        int height = contentProvider.contentHeight;

        // Calculate scroll offset
        int scrollX = (int)contentProvider.scrollX;
        int scrollY = (int)contentProvider.scrollY;

        int offsetX = scrollX % 16;
        int offsetY = scrollY % 16;

        // Draw tiled background
        for (int m = -1; m <= (width / 16) + 1; m++) {
            for (int n = -1; n <= (height / 16) + 1; n++) {
                context.drawTexture(
                        RenderPipelines.GUI_TEXTURED,
                        background,
                        x + (m * 16) - offsetX,
                        y + (n * 16) - offsetY,
                        0, 0,
                        16, 16,
                        16, 16
                );
            }
        }
    }

    public void tick() {
        if (contentProvider != null) {
            contentProvider.tick();
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (contentProvider != null) {
            return contentProvider.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        return false;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (contentProvider != null) {
            contentProvider.mouseClicked(mouseX, mouseY, button);
        }
    }

    // === Tab Button Rendering ===
    public void drawBackground(DrawContext context, int x, int y, boolean selected) {
        type.drawBackground(context, x, y, selected, index);
    }

    public void drawIcon(DrawContext context, int x, int y) {
        type.drawIcon(context, x, y, index, icon);
    }

    public boolean isClickOnTab(int baseX, int baseY, double mouseX, double mouseY) {
        return type.isClickOnTab(baseX, baseY, index, mouseX, mouseY);
    }

    // === Accessors ===
    public Component getTitle() {
        return title;
    }

    public ResourceLocation getBackground() {
        return background;
    }

    public SettlementTabType getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    // === Factory ===
    public static SettlementTabWidget create(MinecraftClient client, SettlementHubScreen screen, int index, SettlementDisplay display) {
        for (SettlementTabType tabType : SettlementTabType.values()) {
            if (index < tabType.getTabCount()) {
                return new SettlementTabWidget(client, screen, tabType, index, display);
            }
            index -= tabType.getTabCount();
        }
        return null;
    }
}
