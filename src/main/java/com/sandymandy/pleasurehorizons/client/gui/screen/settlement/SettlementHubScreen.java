package com.sandymandy.pleasurehorizons.client.gui.screen.settlement;

import com.google.common.collect.Maps;
import com.sandymandy.pleasurehorizons.client.gui.screen.settlement.tabs.ResourcesTab;
import com.sandymandy.pleasurehorizons.client.gui.screen.settlement.tabs.SettlersTab;
import com.sandymandy.pleasurehorizons.screen.SettlementHubScreenHandler;
import com.sandymandy.pleasurehorizons.settlement.SettlementDisplay;
import com.sandymandy.pleasurehorizons.settlement.SettlementSnapshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Settlement hub screen, drawn in the style of the vanilla advancements window.
 *
 * <p>Ported from Fabric: {@code HandledScreen} → {@link AbstractContainerScreen},
 * {@code drawTexture(RenderPipelines.GUI_TEXTURED, ...)} → {@code GuiGraphics#blit},
 * {@code drawTooltip} → {@code renderTooltip}, {@code handledScreenTick} → {@code containerTick},
 * and the client reads a {@link SettlementSnapshot} rather than the server settlement object.</p>
 */
@OnlyIn(Dist.CLIENT)
public class SettlementHubScreen extends AbstractContainerScreen<SettlementHubScreenHandler> {
    private static final ResourceLocation WINDOW_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/advancements/window.png");
    private static final int WINDOW_WIDTH = 252;
    private static final int WINDOW_HEIGHT = 140;
    private static final int PAGE_X = 9;
    private static final int PAGE_Y = 18;
    private static final int PAGE_WIDTH = 234;
    private static final int PAGE_HEIGHT = 113;
    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 6;

    private final SettlementSnapshot settlement;
    private final Map<String, SettlementTabWidget> tabs = Maps.newLinkedHashMap();
    @Nullable
    private SettlementTabWidget selectedTab;

    public SettlementHubScreen(SettlementHubScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.settlement = handler.getSnapshot();
        this.imageWidth = WINDOW_WIDTH;
        this.imageHeight = WINDOW_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        tabs.clear();
        selectedTab = null;

        SettlementTabWidget resources = addTab("resources", SettlementDisplay.create(
                Items.CHEST.getDefaultInstance(),
                Component.translatable("gui.pleasurehorizons.settlement.resources"),
                Component.translatable("gui.pleasurehorizons.settlement.resources.desc"),
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/nether.png")
        ));
        if (resources != null) {
            resources.setContentProvider(new ResourcesTab(this, settlement));
        }

        SettlementTabWidget settlers = addTab("settlers", SettlementDisplay.create(
                Items.PLAYER_HEAD.getDefaultInstance(),
                Component.translatable("gui.pleasurehorizons.settlement.settlers"),
                Component.translatable("gui.pleasurehorizons.settlement.settlers.desc"),
                ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/end.png")
        ));
        if (settlers != null) {
            settlers.setContentProvider(new SettlersTab(this, settlement));
        }

        if (!tabs.isEmpty()) {
            selectedTab = tabs.values().iterator().next();
            if (selectedTab != null) {
                int windowX = (this.width - WINDOW_WIDTH) / 2;
                int windowY = (this.height - WINDOW_HEIGHT) / 2;
                selectedTab.initContent(windowX + PAGE_X, windowY + PAGE_Y, PAGE_WIDTH, PAGE_HEIGHT);
            }
        }
    }

    @Nullable
    private SettlementTabWidget addTab(String id, SettlementDisplay display) {
        int index = tabs.size();
        SettlementTabWidget tab = SettlementTabWidget.create(this.minecraft, this, index, display);
        if (tab != null) {
            tabs.put(id, tab);
        }
        return tab;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int x = (this.width - WINDOW_WIDTH) / 2;
        int y = (this.height - WINDOW_HEIGHT) / 2;

        drawWindow(guiGraphics, x, y);
        drawTabPage(guiGraphics, x, y, mouseX, mouseY);
        drawTabTooltips(guiGraphics, x, y, mouseX, mouseY);
    }

    private void drawWindow(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(WINDOW_TEXTURE, x, y, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, 256, 256);

        if (tabs.size() > 1) {
            for (SettlementTabWidget tab : tabs.values()) {
                tab.drawBackground(guiGraphics, x, y, tab == selectedTab);
            }
            for (SettlementTabWidget tab : tabs.values()) {
                tab.drawIcon(guiGraphics, x, y);
            }
        }

        guiGraphics.drawString(this.font,
                selectedTab != null ? selectedTab.getTitle() : this.title,
                x + TITLE_X, y + TITLE_Y, 0x404040, false);
    }

    private void drawTabPage(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        if (selectedTab == null) {
            guiGraphics.fill(x + PAGE_X, y + PAGE_Y, x + PAGE_X + PAGE_WIDTH, y + PAGE_Y + PAGE_HEIGHT, 0xFF000000);
            int centerX = x + PAGE_X + PAGE_WIDTH / 2;
            guiGraphics.drawCenteredString(this.font,
                    Component.translatable("gui.pleasurehorizons.settlement.no_data"),
                    centerX, y + PAGE_Y + 40, 0xFFFFFFFF);
            return;
        }

        guiGraphics.enableScissor(x + PAGE_X, y + PAGE_Y, x + PAGE_X + PAGE_WIDTH, y + PAGE_Y + PAGE_HEIGHT);
        selectedTab.renderContent(guiGraphics, mouseX, mouseY, 0);
        guiGraphics.disableScissor();
    }

    private void drawTabTooltips(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        if (tabs.size() > 1) {
            for (SettlementTabWidget tab : tabs.values()) {
                if (tab.isClickOnTab(x, y, mouseX, mouseY)) {
                    guiGraphics.renderTooltip(this.font, tab.getTitle(), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = (this.width - WINDOW_WIDTH) / 2;
            int y = (this.height - WINDOW_HEIGHT) / 2;

            for (SettlementTabWidget tab : tabs.values()) {
                if (tab.isClickOnTab(x, y, mouseX, mouseY)) {
                    selectTab(tab);
                    return true;
                }
            }

            if (selectedTab != null) {
                selectedTab.mouseClicked(mouseX - (x + PAGE_X), mouseY - (y + PAGE_Y), button);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void selectTab(SettlementTabWidget tab) {
        if (selectedTab != null) {
            selectedTab.removeContent();
        }

        selectedTab = tab;

        int windowX = (this.width - WINDOW_WIDTH) / 2;
        int windowY = (this.height - WINDOW_HEIGHT) / 2;
        selectedTab.initContent(windowX + PAGE_X, windowY + PAGE_Y, PAGE_WIDTH, PAGE_HEIGHT);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (selectedTab != null && selectedTab.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Background is drawn by the window texture in render().
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Titles are drawn by drawWindow().
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (selectedTab != null) {
            selectedTab.tick();
        }
    }

    // === Helpers used by tab content ===

    public void addContentWidget(AbstractWidget widget) {
        this.addRenderableWidget(widget);
    }

    public void removeContentWidget(AbstractWidget widget) {
        this.removeWidget(widget);
    }

    public net.minecraft.client.gui.Font getTextRenderer() {
        return this.font;
    }
}
