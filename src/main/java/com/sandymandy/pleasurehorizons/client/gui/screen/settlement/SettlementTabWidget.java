package com.sandymandy.pleasurehorizons.client.gui.screen.settlement;

import com.sandymandy.pleasurehorizons.settlement.SettlementDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

/** The clickable tab button plus the content it owns. */
@OnlyIn(Dist.CLIENT)
public class SettlementTabWidget {
    private final Minecraft client;
    private final SettlementHubScreen screen;
    private final SettlementTabType type;
    private final int index;
    private final SettlementDisplay display;
    private final ItemStack icon;
    private final Component title;
    private final ResourceLocation background;

    @Nullable
    private SettlementTab contentProvider;

    public SettlementTabWidget(Minecraft client, SettlementHubScreen screen, SettlementTabType type,
                               int index, SettlementDisplay display) {
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
        if (provider != null) {
            provider.setBackgroundTexture(this.background);
        }
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

    public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (contentProvider != null) {
            contentProvider.render(guiGraphics, mouseX, mouseY, delta);
        }
    }

    public void tick() {
        if (contentProvider != null) {
            contentProvider.tick();
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return contentProvider != null
                && contentProvider.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (contentProvider != null) {
            contentProvider.mouseClicked(mouseX, mouseY, button);
        }
    }

    // === Tab button rendering ===

    public void drawBackground(GuiGraphics guiGraphics, int x, int y, boolean selected) {
        type.drawBackground(guiGraphics, x, y, selected, index);
    }

    public void drawIcon(GuiGraphics guiGraphics, int x, int y) {
        type.drawIcon(guiGraphics, x, y, index, icon);
    }

    public boolean isClickOnTab(int baseX, int baseY, double mouseX, double mouseY) {
        return type.isClickOnTab(baseX, baseY, index, mouseX, mouseY);
    }

    // === Accessors ===

    public Component getTitle() {
        return title;
    }

    public SettlementDisplay getDisplay() {
        return display;
    }

    public Minecraft getClient() {
        return client;
    }

    public SettlementHubScreen getScreen() {
        return screen;
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

    @Nullable
    public static SettlementTabWidget create(Minecraft client, SettlementHubScreen screen,
                                             int index, SettlementDisplay display) {
        int remaining = index;
        for (SettlementTabType tabType : SettlementTabType.values()) {
            if (remaining < tabType.getTabCount()) {
                return new SettlementTabWidget(client, screen, tabType, remaining, display);
            }
            remaining -= tabType.getTabCount();
        }
        return null;
    }
}
