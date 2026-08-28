package com.sandymandy.pleasurehorizons.client.gui.screen;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.networking.C2S.SetGUIOpenStateC2SPacket;
import com.sandymandy.pleasurehorizons.registries.InventoryButtonRegistry;
import com.sandymandy.pleasurehorizons.screen.GirlInventoryScreenHandler;
import com.sandymandy.pleasurehorizons.screen.InventoryButtonAction;
import com.sandymandy.pleasurehorizons.util.Colors;
import com.sandymandy.pleasurehorizons.util.ScreenUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

import static com.sandymandy.pleasurehorizons.util.PleasureHorizonsIcons.*;

public class GirlInventoryScreen extends AbstractContainerScreen<GirlInventoryScreenHandler> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "textures/gui/inventory.png");
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 170;
    // Big-menu layout: actions live in two button columns left of the inventory panel (Main,
    // Behavior); the settings moved to a full-screen list (GirlSettingsScreen), opened by a
    // single button right of the panel - the old 13-row inline list ran off-screen on small
    // resolutions.
    private static final int BTN_W = 80;
    private static final int BTN_H = 20;
    private static final int COL_GAP = 6;
    private static final int ROW_GAP = 4;
    private static final int COL_PAD = 10;
    private static final int HEADER_H = 14;
    private static final int COLOR_SECTION_HEADER = 0xFFCC88DD;
    private final TameableGirlEntity girl;
    private final Player player;
    /** Section header rows (absolute screen coords + label), drawn in renderLabels. */
    private final java.util.List<int[]> sectionHeaderPositions = new java.util.ArrayList<>();
    private final java.util.List<Component> sectionHeaderLabels = new java.util.ArrayList<>();
    private int frameCounter;

    public GirlInventoryScreen(GirlInventoryScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.girl = handler.getGirl();
        this.player = inventory.player;
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Dynamic labels (sit/stand, follow distance, ...) come from server sync a few ticks
        // after a click - the screen no longer closes after a click, so refresh the widget
        // list twice a second to keep the labels honest.
        if (++this.frameCounter % 20 == 1) {
            this.rebuildWidgets();
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Stops the container names from rendering.
        for (int i = 0; i < this.sectionHeaderLabels.size(); i++) {
            int[] pos = this.sectionHeaderPositions.get(i);
            guiGraphics.drawString(this.font, this.sectionHeaderLabels.get(i), pos[0], pos[1], COLOR_SECTION_HEADER, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int centerX = (this.width - GUI_WIDTH) / 2;
        int centerY = (this.height - GUI_HEIGHT) / 2;
        int i = this.leftPos;
        int j = this.topPos;
        guiGraphics.blit(TEXTURE, centerX, centerY, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, i + 26, j + 8, i + 75, j + 78, this.girl.getSizeGUI(), this.girl.getYAxisGUI(), mouseX, mouseY, this.girl);

        int iconY = centerY - 22; // Positioned slightly above the top edge of the GUI
        int iconSize = 18;

        int relLevel = girl.getCurrentRelationshipLevel();
        int relMax = girl.maxRelationshipLevel();
        String relText = relLevel + "/" + relMax;
        int relX = centerX;

        guiGraphics.blit(HEART_ICON, relX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
        guiGraphics.drawString(this.font, Component.literal(relText), relX + 20, iconY + 5, Colors.WHITE, true);

        if (ScreenUtils.isMouseOverHere(mouseX, mouseY, relX, iconY, 18, 18)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("screen.pleasurehorizons.girl_inventory.relationship_tooltip"), mouseX, mouseY);
        }

        int pregLevel = girl.getPregnancyStage();
        int pregMax = girl.maxPregnancyStage();
        int pregX = centerX + GUI_WIDTH - iconSize; // Aligned to the far right edge of the menu
        String pregText = pregLevel + "/" + pregMax;
        int textWidth = this.font.width(pregText);

        if (girl.canGetImpregnated()) {
            guiGraphics.blit(getPregnancyIcon(pregLevel), pregX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
            guiGraphics.drawString(this.font, Component.literal(pregText), pregX - textWidth - 5, iconY + 5, Colors.WHITE, true);

            if (ScreenUtils.isMouseOverHere(mouseX, mouseY, pregX, iconY, 18, 18)) {
                guiGraphics.renderTooltip(this.font, Component.translatable("screen.pleasurehorizons.girl_inventory.pregnancy_tooltip"), mouseX, mouseY);
            }
        }
    }

    private ResourceLocation getPregnancyIcon(int stage) {
        switch (stage) {
            case 1 -> {
                return PREGNANCY_LEVEL_ONE_ICON;
            }
            case 2 -> {
                return PREGNANCY_LEVEL_TWO_ICON;
            }
            case 3 -> {
                return PREGNANCY_LEVEL_THREE_ICON;
            }
            default -> {
                return PREGNANCY_LEVEL_ZERO_ICON;
            }
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        PacketDistributor.sendToServer(new SetGUIOpenStateC2SPacket(this.girl.getId(), false));
    }

    private void drawButton(Component label, InventoryButtonAction action, int x, int y, int buttonWidth, int buttonHeight) {
        Button button = Button.builder(
                label,
                btn -> {
                    if (girl != null && minecraft != null && player != null) {
                        action.action().accept(girl, player);
                        // Single-screen menu: clicks never close it. Talk and Customize
                        // replace this screen asynchronously after the server validates the
                        // click (closing early would invalidate the scene-selection
                        // authorization), and every other button keeps it open so a stack of
                        // toggles can be adjusted in one place.
                        if (this.minecraft != null && this.minecraft.screen == this) {
                            this.rebuildWidgets();
                        }
                    }
                }
        ).bounds(x, y, buttonWidth, buttonHeight).build();

        if (girl.getCurrentRelationshipLevel() < action.requiredRelationshipLevel()) {
            button.active = false;
            button.setTooltip(Tooltip.create(Component.translatable("gui.pleasurehorizons.requires_relationship", action.requiredRelationshipLevel())));
        }

        this.addRenderableWidget(button);
    }

    @Override
    protected void init() {
        super.init();
        int centerX = (this.width - GUI_WIDTH) / 2;
        int centerY = (this.height - GUI_HEIGHT) / 2;

        this.sectionHeaderPositions.clear();
        this.sectionHeaderLabels.clear();

        if (girl.isTamed()) {
            int topY = centerY + 8;
            // Left of the panel: two button columns (Main, Behavior).
            // Right of the panel: a single button opening the full-screen settings list -
            // the 13-row inline list used to run off-screen on small resolutions.
            this.drawSectionColumn(centerX - COL_PAD - 2 * BTN_W - COL_GAP, topY,
                    "gui.pleasurehorizons.section.main", InventoryButtonRegistry.BUTTONS_MAIN);
            this.drawSectionColumn(centerX - COL_PAD - BTN_W, topY,
                    "gui.pleasurehorizons.section.behavior", InventoryButtonRegistry.BUTTONS_BEHAVIOR);
            this.addRenderableWidget(Button.builder(
                            Component.translatable("gui.pleasurehorizons.section.settings"),
                            btn -> this.openSettingsScreen())
                    .bounds(centerX + GUI_WIDTH + COL_PAD, topY + 4, BTN_W, BTN_H)
                    .tooltip(Tooltip.create(Component.translatable("gui.pleasurehorizons.settings_screen.open")))
                    .build());
        }
    }

    /** Opens the full-screen settings list for this girl (Escape goes back to this screen). */
    private void openSettingsScreen() {
        if (girl != null && minecraft != null && player != null) {
            minecraft.setScreen(new GirlSettingsScreen(this, girl, player));
        }
    }

    /** A section header plus a vertical column of buttons under it. */
    private void drawSectionColumn(int x, int topY, String headerKey, List<InventoryButtonAction> actions) {
        this.sectionHeaderPositions.add(new int[]{x, topY});
        this.sectionHeaderLabels.add(Component.translatable(headerKey));
        int y = topY + HEADER_H;
        for (InventoryButtonAction action : actions) {
            this.drawButton(dynamicLabel(action), action, x, y, BTN_W, BTN_H);
            y += BTN_H + ROW_GAP;
        }
    }

    /** Toggle buttons read the live state and show "Stop ..." when enabled. */
    private Component dynamicLabel(InventoryButtonAction action) {
        String key = action.labelKey();
        if ("gui.pleasurehorizons.button.followDistance".equals(key)) {
            int mode = girl.getFollowDistanceMode();
            int distance = mode == 0 ? 4 : mode == 2 ? 12 : 8;
            return Component.translatable("gui.pleasurehorizons.button.followDistanceValue", distance);
        }
        if ("gui.pleasurehorizons.button.guardBase".equals(key) && girl.isGuardBaseEnabled()) {
            return Component.translatable("gui.pleasurehorizons.button.stopGuardBase");
        }
        if ("gui.pleasurehorizons.button.guardOwner".equals(key) && girl.isGuardOwnerEnabled()) {
            return Component.translatable("gui.pleasurehorizons.button.stopGuardOwner");
        }
        if ("gui.pleasurehorizons.button.stayNearBase".equals(key) && girl.isStayNearBaseEnabled()) {
            return Component.translatable("gui.pleasurehorizons.button.stopStayNearBase");
        }
        if ("gui.pleasurehorizons.button.sit".equals(key) && girl.isSitting()) {
            return Component.translatable("gui.pleasurehorizons.button.stand");
        }
        if ("gui.pleasurehorizons.button.follow".equals(key) && girl.isFollowing()) {
            return Component.translatable("gui.pleasurehorizons.button.stopFollowing");
        }
        if ("gui.pleasurehorizons.button.strip".equals(key) && girl.isStripped()) {
            return Component.translatable("gui.pleasurehorizons.button.dressUp");
        }
        if ("gui.pleasurehorizons.button.gather".equals(key) && girl.isGatherEnabled()) {
            return Component.translatable("gui.pleasurehorizons.button.stopGather");
        }
        if ("gui.pleasurehorizons.button.harvest".equals(key) && girl.isHarvestEnabled()) {
            return Component.translatable("gui.pleasurehorizons.button.stopHarvest");
        }
        if ("gui.pleasurehorizons.button.chopTrees".equals(key) && girl.isChopTreesEnabled()) {
            return Component.translatable("gui.pleasurehorizons.button.stopChopTrees");
        }
        if ("gui.pleasurehorizons.button.feedOwner".equals(key) && girl.isFeedOwnerEnabled()) {
            return Component.translatable("gui.pleasurehorizons.button.stopFeedOwner");
        }
        if ("gui.pleasurehorizons.button.cook".equals(key) && girl.isCookEnabled()) {
            return Component.translatable("gui.pleasurehorizons.button.stopCook");
        }
        if ("gui.pleasurehorizons.button.hunt".equals(key) && girl.isHuntEnabled()) {
            return Component.translatable("gui.pleasurehorizons.button.stopHunt");
        }
        if ("gui.pleasurehorizons.button.cycleRole".equals(key)) {
            return Component.translatable("gui.pleasurehorizons.button.cycleRole",
                    Component.translatable("role.pleasurehorizons." + girl.getRole().id()));
        }
        return action.label();
    }
}
