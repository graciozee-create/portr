package com.sandymandy.pleasurehorizons.client.gui.screen;

import com.sandymandy.pleasurehorizons.client.gui.screen.customize.CustomizeSection;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.networking.C2S.RemovePreviewEntityC2SPacket;
import com.sandymandy.pleasurehorizons.networking.C2S.SetGUIOpenStateC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public abstract class CustomizeScreen<T extends GirlSceneEntity> extends Screen {

    protected final Component screenTitle;
    protected final int entityId;
    private final int previewSessionEntityId;
    protected final T previewEntity;
    protected final T entity;

    protected final LayoutConfig layout = new LayoutConfig();

    protected final List<CustomizeSection<T>> sections = new ArrayList<>();

    protected final Map<String, List<Button>> buttonGroups = new HashMap<>();
    protected final Map<Button, String> buttonToGroup = new HashMap<>();
    protected final Map<String, Button> selectedButtons = new HashMap<>();

    protected double scrollOffset = 0;

    public CustomizeScreen(Component title, int entityId, int previewEntityId, Class<T> entityClass) {
        super(title);
        this.screenTitle = title;
        this.entityId = entityId;
        this.previewSessionEntityId = previewEntityId;

        net.minecraft.client.multiplayer.ClientLevel world = Minecraft.getInstance().level;
        net.minecraft.world.entity.Entity source = world != null ? world.getEntity(entityId) : null;
        this.entity = entityClass.isInstance(source) ? entityClass.cast(source) : null;

        // The server-side temporary entity is an authorization/session marker, not the model
        // rendered in this screen. Waiting for its vanilla spawn packet raced the custom open-
        // screen payload, and changing its SynchedEntityData here also wrote server-owned state
        // from the client. A detached entity of the same type is deterministic, immediately
        // available and safe to mutate exclusively for the local preview.
        T localPreview = null;
        if (world != null && this.entity != null) {
            net.minecraft.world.entity.Entity created = this.entity.getType().create(world);
            if (entityClass.isInstance(created)) {
                localPreview = entityClass.cast(created);
                this.entity.onTempCloneCreation(localPreview);
            }
        }
        this.previewEntity = localPreview;
    }

    protected abstract void addSections();

    protected abstract void onConfirm();

    protected abstract void applyToPreview();

    protected abstract int contentHeight();

    @Override
    protected void init() {
        super.init();

        this.clearWidgets();
        buttonGroups.clear();
        buttonToGroup.clear();
        selectedButtons.clear();
        sections.clear();

        layout.calculate(this.width, this.height);

        addSections();

        buildUI();
    }

    private void buildUI() {
        int currentY = layout.startY - (int) scrollOffset;
        currentY += 30;

        for (CustomizeSection<T> section : sections) {
            currentY = section.render(this, layout, currentY);
            currentY += section.getSpacing();
        }

        currentY = renderActionButtons(currentY);
    }

    private int renderActionButtons(int currentY) {
        int actionButtonWidth = (layout.contentWidth - 5) / 2;

        Button confirmBtn = Button.builder(
                Component.translatable("gui.pleasurehorizons.button.confirm"),
                button -> {
                    onConfirm();
                    this.onClose();
                }
        ).bounds(layout.centerX, currentY, actionButtonWidth, 20).build();
        this.addRenderableWidget(confirmBtn);

        Button cancelBtn = Button.builder(
                Component.translatable("gui.pleasurehorizons.button.cancel"),
                button -> this.onClose()
        ).bounds(layout.centerX + actionButtonWidth + 5, currentY, actionButtonWidth, 20).build();
        this.addRenderableWidget(cancelBtn);

        return currentY + 30;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);

        if (previewEntity != null) {
            renderEntityPreview(guiGraphics, mouseX, mouseY);
        }

        guiGraphics.drawCenteredString(this.font, this.screenTitle, layout.centerX + layout.contentWidth / 2, layout.startY - (int) scrollOffset, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (contentHeight() > this.height) {
            int maxScroll = Math.max(0, contentHeight() - this.height + 100);
            int scrollBarHeight = Math.max(20, (this.height * this.height) / (this.height + maxScroll));
            int scrollBarY = maxScroll > 0 ? (int) ((this.height - scrollBarHeight) * (scrollOffset / maxScroll)) : 0;
            guiGraphics.fill(this.width - 5, scrollBarY, this.width - 3, scrollBarY + scrollBarHeight, 0xFF808080);
        }
        if (previewEntity != null) applyToPreview();
    }

    private void renderEntityPreview(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x1 = 10;
        int y1 = 50;
        int x2 = layout.previewWidth - 10;
        int y2 = this.height - 50;

        if (previewEntity != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, x1, y1, x2, y2, layout.previewSize, 0.0f, mouseX, mouseY, previewEntity);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.previewEntity != null) {
            // Detached preview entities are not part of ClientLevel's tick list. Advancing their
            // age keeps GeckoLib idle animations moving without running mob AI or touching any
            // tracked world entity.
            this.previewEntity.tickCount++;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        // This id belongs to the server-owned preview session marker. The detached local model
        // intentionally has no network identity and must never be sent to the server.
        PacketDistributor.sendToServer(
                new RemovePreviewEntityC2SPacket(entityId, this.previewSessionEntityId));
        super.onClose();
        PacketDistributor.sendToServer(new SetGUIOpenStateC2SPacket(this.entityId, false));
    }

    public Button createSelectableButton(String groupId, Component message, int x, int y, int width, int height, java.util.function.Consumer<Button> onPress) {
        Button button = Button.builder(message, btn -> {
            selectButton(groupId, btn);
            onPress.accept(btn);
        }).bounds(x, y, width, height).build();

        buttonGroups.computeIfAbsent(groupId, k -> new ArrayList<>()).add(button);
        buttonToGroup.put(button, groupId);

        return button;
    }

    public void selectButton(String groupId, Button button) {
        Button previouslySelected = selectedButtons.get(groupId);
        if (previouslySelected != null) {
            previouslySelected.active = true;
        }

        button.active = false;
        selectedButtons.put(groupId, button);
    }

    public void markAsSelected(String groupId, Button button) {
        button.active = false;
        selectedButtons.put(groupId, button);
    }

    public net.minecraft.client.gui.components.AbstractWidget addWidget(net.minecraft.client.gui.components.AbstractWidget widget) {
        return this.addRenderableWidget(widget);
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
