package com.sandymandy.pleasurehorizons.client.gui.screen.settlement;

import com.google.common.collect.Maps;
import com.sandymandy.pleasurehorizons.client.gui.screen.settlement.tabs.ResourcesTab;
import com.sandymandy.pleasurehorizons.client.gui.screen.settlement.tabs.SettlersTab;
import com.sandymandy.pleasurehorizons.screen.SettlementHubScreenHandler;
import com.sandymandy.pleasurehorizons.settlement.Settlement;
import com.sandymandy.pleasurehorizons.settlement.SettlementDisplay;

import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class SettlementHubScreen extends HandledScreen<SettlementHubScreenHandler> {
    private static final Identifier WINDOW_TEXTURE = Identifier.ofVanilla("textures/gui/advancements/window.png");
    private static final int WINDOW_WIDTH = 252;
    private static final int WINDOW_HEIGHT = 140;
    private static final int PAGE_X = 9;
    private static final int PAGE_Y = 18;
    private static final int PAGE_WIDTH = 234;
    private static final int PAGE_HEIGHT = 113;
    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 6;

    private final Settlement settlement;
    private final Map<String, SettlementTabWidget> tabs = Maps.newLinkedHashMap();
    @Nullable
    private SettlementTabWidget selectedTab;

    public SettlementHubScreen(SettlementHubScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.settlement = handler.getSettlement();
        this.backgroundWidth = WINDOW_WIDTH;
        this.backgroundHeight = WINDOW_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        tabs.clear();
        selectedTab = null;

        // Create tabs with proper displays
        addTab("resources", SettlementDisplay.create(
                Items.CHEST.getDefaultStack(),
                Text.literal("Resources"),
                Text.literal("Resource overview"),
                Identifier.ofVanilla("textures/gui/advancements/backgrounds/nether.png")
        )).setContentProvider(new ResourcesTab(this, settlement));

        addTab("settlers", SettlementDisplay.create(
                Items.PLAYER_HEAD.getDefaultStack(),
                Text.literal("Settlers"),
                Text.literal("Population management"),
                Identifier.ofVanilla("textures/gui/advancements/backgrounds/end.png")
        )).setContentProvider(new SettlersTab(this, settlement));

        // Select first tab
        if (!tabs.isEmpty()) {
            selectedTab = tabs.values().iterator().next();
            if (selectedTab != null) {
                int windowX = (this.width - WINDOW_WIDTH) / 2;
                int windowY = (this.height - WINDOW_HEIGHT) / 2;
                selectedTab.initContent(windowX + PAGE_X, windowY + PAGE_Y, PAGE_WIDTH, PAGE_HEIGHT);
            }
        }
    }

    private SettlementTabWidget addTab(String id, SettlementDisplay display) {
        int index = tabs.size();
        SettlementTabWidget tab = SettlementTabWidget.create(client, this, index, display);
        if (tab != null) {
            tabs.put(id, tab);
        }
        return tab;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int x = (this.width - WINDOW_WIDTH) / 2;
        int y = (this.height - WINDOW_HEIGHT) / 2;

        // Draw window and tabs
        drawWindow(context, x, y);

        // Draw tab content
        drawTabPage(context, x, y, mouseX, mouseY);

        // Draw tab tooltips
        drawTabTooltips(context, x, y, mouseX, mouseY);
    }

    private void drawWindow(DrawContext context, int x, int y) {
        // Draw window background
        context.drawTexture(RenderPipelines.GUI_TEXTURED, WINDOW_TEXTURE, x, y, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, 256, 256);

        // Draw tab backgrounds
        if (tabs.size() > 1) {
            for (SettlementTabWidget tab : tabs.values()) {
                tab.drawBackground(context, x, y, tab == selectedTab);
            }
        }

        // Draw tab icons
        if (tabs.size() > 1) {
            for (SettlementTabWidget tab : tabs.values()) {
                tab.drawIcon(context, x, y);
            }
        }

        // Draw title
        context.drawText(
                textRenderer,
                selectedTab != null ? selectedTab.getTitle() : title,
                x + TITLE_X, y + TITLE_Y,
                0x404040, false
        );
    }

    private void drawTabPage(DrawContext context, int x, int y, int mouseX, int mouseY) {
        if (selectedTab == null) {
            // No tabs = draw empty message
            context.fill(x + PAGE_X, y + PAGE_Y, x + PAGE_X + PAGE_WIDTH, y + PAGE_Y + PAGE_HEIGHT, Colors.BLACK);
            int centerX = x + PAGE_X + PAGE_WIDTH / 2;
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("No Settlement Data"), centerX, y + PAGE_Y + 40, Colors.WHITE);
            return;
        }

        // Enable scissor for content area
        context.enableScissor(
                x + PAGE_X,
                y + PAGE_Y,
                x + PAGE_X + PAGE_WIDTH,
                y + PAGE_Y + PAGE_HEIGHT
        );

        // Render tab content
        selectedTab.renderContent(context, mouseX, mouseY, 0);

        context.disableScissor();
    }

    private void drawTabTooltips(DrawContext context, int x, int y, int mouseX, int mouseY) {
        if (tabs.size() > 1) {
            for (SettlementTabWidget tab : tabs.values()) {
                if (tab.isClickOnTab(x, y, mouseX, mouseY)) {
                    context.drawTooltip(textRenderer, tab.getTitle(), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = (width - WINDOW_WIDTH) / 2;
            int y = (height - WINDOW_HEIGHT) / 2;

            // Check tab clicks
            for (SettlementTabWidget tab : tabs.values()) {
                if (tab.isClickOnTab(x, y, mouseX, mouseY)) {
                    selectTab(tab);
                    return true;
                }
            }

            // Forward to content
            if (selectedTab != null) {
                double localMouseX = mouseX - (x + PAGE_X);
                double localMouseY = mouseY - (y + PAGE_Y);
                selectedTab.mouseClicked(localMouseX, localMouseY, button);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void selectTab(SettlementTabWidget tab) {
        if (selectedTab != null) {
            selectedTab.removeContent();
        }

        selectedTab = tab;

        if (selectedTab != null) {
            int windowX = (this.width - WINDOW_WIDTH) / 2;
            int windowY = (this.height - WINDOW_HEIGHT) / 2;
            selectedTab.initContent(windowX + PAGE_X, windowY + PAGE_Y, PAGE_WIDTH, PAGE_HEIGHT);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (selectedTab != null) {
            return selectedTab.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        // Background handled by window texture
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        if (selectedTab != null) {
            selectedTab.tick();
        }
    }

    // Helper methods for tab content
    public void addContentWidget(net.minecraft.client.gui.widget.ButtonWidget widget) {
        this.addDrawableChild(widget);
    }

    public void removeContentWidget(net.minecraft.client.gui.widget.ButtonWidget widget) {
        this.remove(widget);
    }

    public net.minecraft.client.font.TextRenderer getTextRenderer() {
        return this.textRenderer;
    }

    public net.minecraft.client.MinecraftClient getClient() {
        return this.client;
    }
}
