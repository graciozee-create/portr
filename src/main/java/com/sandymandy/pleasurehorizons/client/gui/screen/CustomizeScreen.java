package com.sandymandy.pleasurehorizons.client.gui.screen;

import com.sandymandy.pleasurehorizons.client.gui.screen.customize.CustomizeSection;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.networking.C2S.RemovePreviewEntityC2SPacket;
import com.sandymandy.pleasurehorizons.networking.C2S.SetGUIOpenStateC2SPacket;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.*;

import static net.minecraft.client.gui.screen.ingame.InventoryScreen.drawEntity;

public abstract class CustomizeScreen<T extends GirlSceneEntity> extends Screen {

    protected final int entityId;
    protected final T previewEntity;
    protected final T entity;

    protected final LayoutConfig layout = new LayoutConfig();

    protected final List<CustomizeSection<T>> sections = new ArrayList<>();

    protected final Map<String, List<ButtonWidget>> buttonGroups = new HashMap<>();
    protected final Map<ButtonWidget, String> buttonToGroup = new HashMap<>();
    protected final Map<String, ButtonWidget> selectedButtons = new HashMap<>();

    protected double scrollOffset = 0;

    public CustomizeScreen(Text title, int entityId, int previewEntityId, Class<T> entityClass) {
        super(title);
        this.entityId = entityId;

        World world = MinecraftClient.getInstance().world;
        this.previewEntity = entityClass.cast(world.getEntityById(previewEntityId));
        this.entity = entityClass.cast(world.getEntityById(entityId));
    }

    /**
     * Override this to add sections to the customization screen
     */
    protected abstract void addSections();

    /**
     * Override this to handle confirmation
     */
    protected abstract void onConfirm();

    protected abstract void applyToPreview();

    protected abstract int contentHeight();

    public <W extends Element & Drawable & Selectable> W addWidget(W widget) {
        return this.addDrawableChild(widget);
    }

    @Override
    protected void init() {
        super.init();

        this.clearChildren();
        buttonGroups.clear();
        buttonToGroup.clear();
        selectedButtons.clear();
        sections.clear();

        // Calculate layout
        layout.calculate(this.width, this.height);

        addSections();

        buildUI();
    }

    private void buildUI() {
        int currentY = layout.startY - (int)scrollOffset;

        this.addDrawableChild(new net.minecraft.client.gui.widget.TextWidget(
                layout.centerX, currentY, layout.contentWidth, 20,
                this.getTitle(),
                this.textRenderer
        ));
        currentY += 30;

        // Render each section
        for (CustomizeSection<T> section : sections) {
            section.init(this, layout, currentY);
            currentY = section.render(this, layout, currentY);
            currentY += section.getSpacing();
        }

        // Action buttons at bottom e.g confirm, cancel
        currentY = renderActionButtons(currentY);
    }

    private int renderActionButtons(int currentY) {
        int actionButtonWidth = (layout.contentWidth - 5) / 2;

        ButtonWidget confirmBtn = ButtonWidget.builder(
                Text.literal("§a§lConfirm"),
                button -> {
                    onConfirm();
                    this.close();
                }
        ).dimensions(layout.centerX, currentY, actionButtonWidth, 20).build();
        this.addDrawableChild(confirmBtn);

        ButtonWidget cancelBtn = ButtonWidget.builder(
                Text.literal("§c§lCancel"),
                button -> this.close()
        ).dimensions(layout.centerX + actionButtonWidth + 5, currentY, actionButtonWidth, 20).build();
        this.addDrawableChild(cancelBtn);

        return currentY + 30;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);

        if (previewEntity != null) {
            renderEntityPreview(context, mouseX, mouseY);
        }

        // Widgets
        super.render(context, mouseX, mouseY, delta);

        if (contentHeight() > this.height) {
            int maxScroll = Math.max(0, contentHeight() - this.height + 100);
            int scrollBarHeight = Math.max(20, (this.height * this.height) / (this.height + maxScroll));
            int scrollBarY = maxScroll > 0 ? (int)((this.height - scrollBarHeight) * (scrollOffset / maxScroll)) : 0;
            context.fill(this.width - 5, scrollBarY, this.width - 3, scrollBarY + scrollBarHeight, 0xFF808080);
        }
        if (previewEntity != null) applyToPreview();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX > layout.previewWidth) {
            int maxScroll = Math.max(0, contentHeight() - this.height + 100);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - verticalAmount * layout.scrollSpeed));
            init();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void renderEntityPreview(DrawContext context, int mouseX, int mouseY) {
        int x1 = 10;
        int y1 = 50;
        int x2 = layout.previewWidth - 10;
        int y2 = this.height - 50;

        drawEntity(context, x1, y1, x2, y2, layout.previewSize, 0.0f, mouseX, mouseY, previewEntity);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (previewEntity != null) {
            PacketDistributor.sendToServer(new RemovePreviewEntityC2SPacket(entityId, previewEntity.getId()));
        }
        super.close();
        PacketDistributor.sendToServer(new SetGUIOpenStateC2SPacket(this.entityId, false));
    }

    public ButtonWidget createSelectableButton(String groupId, Text message, int x, int y, int width, int height, ButtonWidget.PressAction onPress) {
        ButtonWidget button = ButtonWidget.builder(message, btn -> {
            selectButton(groupId, btn);
            onPress.onPress(btn);
        }).dimensions(x, y, width, height).build();

        buttonGroups.computeIfAbsent(groupId, k -> new ArrayList<>()).add(button);
        buttonToGroup.put(button, groupId);

        return button;
    }

    public void selectButton(String groupId, ButtonWidget button) {
        ButtonWidget previouslySelected = selectedButtons.get(groupId);
        if (previouslySelected != null) {
            previouslySelected.active = true;
        }

        button.active = false;
        selectedButtons.put(groupId, button);
    }

    public void markAsSelected(String groupId, ButtonWidget button) {
        button.active = false;
        selectedButtons.put(groupId, button);
    }

    public static class LayoutConfig {
        public int previewWidth;
        public int menuWidth;
        public int menuStartX;
        public int startY;
        public int contentWidth;
        public int centerX;
        public int previewSize = 100;
        public int scrollSpeed = 20;

        public void calculate(int screenWidth, int screenHeight) {
            this.previewWidth = screenWidth / 4;
            this.menuWidth = (screenWidth * 3) / 4;
            this.menuStartX = previewWidth + 20;
            this.startY = 20;
            this.contentWidth = Math.min(400, menuWidth - 40);
            this.centerX = menuStartX + (menuWidth - contentWidth) / 2;
        }
    }
}
