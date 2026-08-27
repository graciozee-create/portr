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
    // Big-menu layout: all actions live on one screen in four button columns around the
    // inventory panel (two per side), each column headed by its section name.
    private static final int BTN_W = 80;
    private static final int BTN_H = 20;
    private static final int COL_GAP = 6;
    private static final int ROW_GAP = 4;
    private static final int COL_PAD = 10;
    private static final int HEADER_H = 14;
    // Settings are a proper list, not buttons: each row shows the name on the left and the
    // LIVE value on the right (Вкл/Выкл, Близко/Обычно/Далеко); clicking the row toggles it.
    private static final int SETTINGS_PANEL_W = 180;
    private static final int SETTINGS_ROW_H = 16;
    private static final int COLOR_SECTION_HEADER = 0xFFCC88DD;
    private static final int COLOR_PANEL_BORDER = 0xFF664466;
    private static final int COLOR_PANEL_BG = 0xF01C1018;
    private static final int COLOR_ROW_NAME = 0xFFDDCCDD;
    private static final int COLOR_VALUE_ON = 0xFF7DDD8A;
    private static final int COLOR_VALUE_OFF = 0xFF9988AA;
    private static final int COLOR_VALUE_MODE = 0xFFDDCCDD;
    /** One clickable row of the settings list (absolute coords, setting key, is-mod-row). */
    private record SettingsRow(int x, int y, int w, int h, String key, boolean mod) {
    }
    private final TameableGirlEntity girl;
    private final Player player;
    /** Section header rows (absolute screen coords + label), drawn in renderLabels. */
    private final java.util.List<int[]> sectionHeaderPositions = new java.util.ArrayList<>();
    private final java.util.List<Component> sectionHeaderLabels = new java.util.ArrayList<>();
    /** Rows of the settings list, rebuilt in init. */
    private final java.util.List<SettingsRow> settingsRows = new java.util.ArrayList<>();
    /** labelKey -> action for the settings rows (from InventoryButtonRegistry.BUTTONS_SETTINGS). */
    private final java.util.Map<String, InventoryButtonAction> settingsActions = new java.util.HashMap<>();
    private int frameCounter;
    /**
     * Clicks made on stateful setting buttons since the server state was last observed, plus
     * the server value at the time of the first pending click ("base"). Synched data only
     * arrives from the server a few ticks later, so the pending count lets the labels/tooltips
     * reflect the would-be state immediately (toggles flip per click, cycles advance per
     * click). The base is what makes the optimistic display self-correcting: the moment the
     * server value differs from the base, one pending click is considered applied and the
     * base is re-anchored to the new server value.
     */
    private final java.util.Map<String, Integer> pendingClicks = new java.util.HashMap<>();
    private final java.util.Map<String, Boolean> pendingBaseToggle = new java.util.HashMap<>();
    private final java.util.Map<String, Integer> pendingBaseMode = new java.util.HashMap<>();
    /** Setting keys that cycle through three values instead of flipping on/off. */
    private static final java.util.Set<String> MODE_SETTING_KEYS = java.util.Set.of(
            "gui.pleasurehorizons.button.followDistance",
            "gui.pleasurehorizons.button.workPace",
            "gui.pleasurehorizons.button.workRadius",
            "gui.pleasurehorizons.button.guardRange",
            "gui.pleasurehorizons.button.stayRadius");

    public GirlInventoryScreen(GirlInventoryScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.girl = handler.getGirl();
        this.player = inventory.player;
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Dynamic labels (sit/stand, on/off values, relationship gating) come from server
        // sync a few ticks after a click - the screen no longer closes after a click, so
        // refresh the widget list twice a second to keep the labels honest.
        if (++this.frameCounter % 20 == 1) {
            this.rebuildWidgets();
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        // Tooltips for the custom-drawn settings rows (vanilla renderTooltip only covers widgets).
        for (SettingsRow row : this.settingsRows) {
            if (!row.mod() && ScreenUtils.isMouseOverHere(mouseX, mouseY, row.x(), row.y(), row.w(), row.h())) {
                if (this.rowLocked(row.key())) {
                    InventoryButtonAction action = this.settingsActions.get(row.key());
                    guiGraphics.renderTooltip(this.font,
                            Component.translatable("gui.pleasurehorizons.requires_relationship",
                                    action == null ? 1 : action.requiredRelationshipLevel()),
                            mouseX, mouseY);
                } else {
                    java.util.List<Component> tip = settingsTooltip(row.key());
                    if (tip != null && tip.size() >= 2) {
                        // 1.21.1 renderTooltip takes FormattedCharSequence lines, not raw Components.
                        java.util.List<net.minecraft.util.FormattedCharSequence> lines = tip.stream()
                                .map(Component::getVisualOrderText)
                                .toList();
                        guiGraphics.renderTooltip(this.font, lines, mouseX, mouseY);
                    }
                }
            }
        }
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Stops the container names from rendering.
        for (int i = 0; i < this.sectionHeaderLabels.size(); i++) {
            int[] pos = this.sectionHeaderPositions.get(i);
            guiGraphics.drawString(this.font, this.sectionHeaderLabels.get(i), pos[0], pos[1], COLOR_SECTION_HEADER, false);
        }

        // Settings list: framed panel, one row per setting (name left, live value right).
        if (!this.settingsRows.isEmpty()) {
            SettingsRow first = this.settingsRows.get(0);
            SettingsRow last = this.settingsRows.get(this.settingsRows.size() - 1);
            int panelTop = first.y() - 4;
            int panelBottom = last.y() + last.h() + 4;
            guiGraphics.fill(first.x() - 2, panelTop - 2, first.x() + first.w() + 2, panelBottom + 2, COLOR_PANEL_BORDER);
            guiGraphics.fill(first.x() - 1, panelTop - 1, first.x() + first.w() + 1, panelBottom + 1, COLOR_PANEL_BG);

            for (SettingsRow row : this.settingsRows) {
                boolean hover = ScreenUtils.isMouseOverHere(mouseX, mouseY, row.x(), row.y(), row.w(), row.h());
                if (hover) {
                    guiGraphics.fill(row.x(), row.y(), row.x() + row.w(), row.y() + row.h(), 0x40FFFFFF);
                }
                if (row.mod()) {
                    guiGraphics.drawString(this.font,
                            Component.translatable("gui.pleasurehorizons.settings.open"),
                            row.x() + 6, row.y() + 4, COLOR_SECTION_HEADER, true);
                    continue;
                }
                boolean locked = this.rowLocked(row.key());
                guiGraphics.drawString(this.font, Component.translatable(row.key()),
                        row.x() + 6, row.y() + 4, locked ? 0xFF665566 : COLOR_ROW_NAME, true);
                Component value = this.settingValue(row.key());
                Boolean on = this.settingToggleState(row.key());
                int valueColor = locked ? 0xFF665566
                        : (on == null ? COLOR_VALUE_MODE : (on ? COLOR_VALUE_ON : COLOR_VALUE_OFF));
                guiGraphics.drawString(this.font, value,
                        row.x() + row.w() - 6 - this.font.width(value), row.y() + 4, valueColor, true);
            }
        }
    }

    /**
     * Live value for a settings row: on/off for toggles, the mode name for cycles.
     * Includes the optimistic pending-click reconciliation (see effectiveToggle/effectiveMode).
     */
    private Component settingValue(String key) {
        if (MODE_SETTING_KEYS.contains(key)) {
            int mode = effectiveMode(key, serverMode(key));
            return Component.translatable("setting.pleasurehorizons." + modeSettingBase(key) + "." + mode);
        }
        boolean on = this.settingToggleState(key);
        return Component.translatable(on ? "setting.pleasurehorizons.on" : "setting.pleasurehorizons.off");
    }

    /** Settings below the girl's relationship level are visible but locked (same gate the old buttons had). */
    private boolean rowLocked(String key) {
        InventoryButtonAction action = this.settingsActions.get(key);
        return action != null
                && girl.getCurrentRelationshipLevel() < action.requiredRelationshipLevel();
    }

    /** Current (optimistic) state of a toggle setting, or null if the key is a mode cycle. */
    private Boolean settingToggleState(String key) {
        if (MODE_SETTING_KEYS.contains(key)) {
            return null;
        }
        return switch (key) {
            case "gui.pleasurehorizons.button.followTeleport" -> effectiveToggle(key, girl.isFollowTeleportEnabled());
            case "gui.pleasurehorizons.button.closeDoors" -> effectiveToggle(key, girl.isCloseDoorsEnabled());
            case "gui.pleasurehorizons.button.avoidWater" -> effectiveToggle(key, girl.isAvoidWaterEnabled());
            case "gui.pleasurehorizons.button.autoDeliver" -> effectiveToggle(key, girl.isAutoDeliverEnabled());
            case "gui.pleasurehorizons.button.autoEquipArmor" -> effectiveToggle(key, girl.isAutoEquipArmorEnabled());
            case "gui.pleasurehorizons.button.avoidCreepers" -> effectiveToggle(key, girl.isAvoidCreepersEnabled());
            default -> effectiveToggle(key, girl.isHighJumpEnabled());
        };
    }

    /** The server value for a mode-cycle setting (followDistance, workPace, ...). */
    private int serverMode(String key) {
        return switch (key) {
            case "gui.pleasurehorizons.button.followDistance" -> girl.getFollowDistanceMode();
            case "gui.pleasurehorizons.button.workPace" -> girl.getWorkPaceMode();
            case "gui.pleasurehorizons.button.workRadius" -> girl.getWorkRadiusMode();
            case "gui.pleasurehorizons.button.guardRange" -> girl.getGuardRangeMode();
            default -> girl.getStayRadiusMode();
        };
    }

    /** "followDistance" / "workPace" / ... - the part shared with the setting.* lang keys. */
    private String modeSettingBase(String key) {
        return key.substring(key.lastIndexOf('.') + 1);
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
                        if (settingsTooltip(action.labelKey()) != null) {
                            recordPendingSettingClick(action.labelKey());
                        }
                        if (this.minecraft != null && this.minecraft.screen == this) {
                            this.rebuildWidgets();
                        }
                    }
                }
        ).bounds(x, y, buttonWidth, buttonHeight).build();

        java.util.List<Component> settingsTip = settingsTooltip(action.labelKey());
        if (settingsTip != null && settingsTip.size() >= 2 && button.active) {
            // 1.21.1 Tooltip has no List overload - first line = description, second = value.
            button.setTooltip(Tooltip.create(settingsTip.get(0), settingsTip.get(1)));
        }

        if (girl.getCurrentRelationshipLevel() < action.requiredRelationshipLevel()) {
            button.active = false;
        }

        if (!button.active) {
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
            // Right of the panel: a proper settings list (name + live value per row).
            this.drawSectionColumn(centerX - COL_PAD - 2 * BTN_W - COL_GAP, topY,
                    "gui.pleasurehorizons.section.main", InventoryButtonRegistry.BUTTONS_MAIN);
            this.drawSectionColumn(centerX - COL_PAD - BTN_W, topY,
                    "gui.pleasurehorizons.section.behavior", InventoryButtonRegistry.BUTTONS_BEHAVIOR);
            this.initSettingsList(centerX + GUI_WIDTH + COL_PAD, topY);
        }
    }

    /** Builds the settings list: header + one row per setting + a "mod settings" row. */
    private void initSettingsList(int panelX, int topY) {
        this.sectionHeaderPositions.add(new int[]{panelX, topY});
        this.sectionHeaderLabels.add(Component.translatable("gui.pleasurehorizons.section.settings"));
        this.settingsRows.clear();
        this.settingsActions.clear();
        for (InventoryButtonAction action : InventoryButtonRegistry.BUTTONS_SETTINGS) {
            this.settingsActions.put(action.labelKey(), action);
            int y = topY + HEADER_H + this.settingsRows.size() * SETTINGS_ROW_H;
            this.settingsRows.add(new SettingsRow(panelX, y, SETTINGS_PANEL_W, SETTINGS_ROW_H, action.labelKey(), false));
        }
        // Mod-wide/client options are not per-girl, so the last row opens their screen.
        int y = topY + HEADER_H + this.settingsRows.size() * SETTINGS_ROW_H;
        this.settingsRows.add(new SettingsRow(panelX, y, SETTINGS_PANEL_W, SETTINGS_ROW_H,
                "gui.pleasurehorizons.settings.open", true));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && girl != null && minecraft != null && player != null) {
            for (SettingsRow row : this.settingsRows) {
                if (ScreenUtils.isMouseOverHere(mouseX, mouseY, row.x(), row.y(), row.w(), row.h())) {
                    if (row.mod()) {
                        this.onClose();
                        net.minecraft.client.Minecraft.getInstance().setScreen(new FreecamSettingsScreen());
                    } else if (!this.rowLocked(row.key())) {
                        InventoryButtonAction action = this.settingsActions.get(row.key());
                        if (action != null) {
                            action.action().accept(girl, player);
                            recordPendingSettingClick(row.key());
                        }
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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

    /** Tooltip shown on Settings-tab buttons: description + the (optimistic) current value. */
    private java.util.List<Component> settingsTooltip(String key) {
        return switch (key) {
            case "gui.pleasurehorizons.button.followTeleport" -> List.of(
                    Component.translatable("gui.pleasurehorizons.desc.followTeleport"),
                    valueLine("setting.pleasurehorizons.on", "setting.pleasurehorizons.off",
                            effectiveToggle(key, girl.isFollowTeleportEnabled())));
            case "gui.pleasurehorizons.button.closeDoors" -> List.of(
                    Component.translatable("gui.pleasurehorizons.desc.closeDoors"),
                    valueLine("setting.pleasurehorizons.on", "setting.pleasurehorizons.off",
                            effectiveToggle(key, girl.isCloseDoorsEnabled())));
            case "gui.pleasurehorizons.button.avoidWater" -> List.of(
                    Component.translatable("gui.pleasurehorizons.desc.avoidWater"),
                    valueLine("setting.pleasurehorizons.on", "setting.pleasurehorizons.off",
                            effectiveToggle(key, girl.isAvoidWaterEnabled())));
            case "gui.pleasurehorizons.button.autoDeliver" -> List.of(
                    Component.translatable("gui.pleasurehorizons.desc.autoDeliver"),
                    valueLine("setting.pleasurehorizons.on", "setting.pleasurehorizons.off",
                            effectiveToggle(key, girl.isAutoDeliverEnabled())));
            case "gui.pleasurehorizons.button.autoEquipArmor" -> List.of(
                    Component.translatable("gui.pleasurehorizons.desc.autoEquipArmor"),
                    valueLine("setting.pleasurehorizons.on", "setting.pleasurehorizons.off",
                            effectiveToggle(key, girl.isAutoEquipArmorEnabled())));
            case "gui.pleasurehorizons.button.avoidCreepers" -> List.of(
                    Component.translatable("gui.pleasurehorizons.desc.avoidCreepers"),
                    valueLine("setting.pleasurehorizons.on", "setting.pleasurehorizons.off",
                            effectiveToggle(key, girl.isAvoidCreepersEnabled())));
            case "gui.pleasurehorizons.button.highJump" -> List.of(
                    Component.translatable("gui.pleasurehorizons.desc.highJump"),
                    valueLine("setting.pleasurehorizons.on", "setting.pleasurehorizons.off",
                            effectiveToggle(key, girl.isHighJumpEnabled())));
            case "gui.pleasurehorizons.button.followDistance" -> List.of(
                    Component.translatable("gui.pleasurehorizons.desc.followDistance"),
                    Component.translatable("gui.pleasurehorizons.currentValue",
                            Component.translatable("setting.pleasurehorizons.followDistance."
                                    + effectiveMode(key, girl.getFollowDistanceMode()))));
            case "gui.pleasurehorizons.button.workPace" -> List.of(
                    Component.translatable("gui.pleasurehorizons.desc.workPace"),
                    Component.translatable("gui.pleasurehorizons.currentValue",
                            Component.translatable("setting.pleasurehorizons.workPace."
                                    + effectiveMode(key, girl.getWorkPaceMode()))));
            case "gui.pleasurehorizons.button.workRadius" -> List.of(
                    Component.translatable("gui.pleasurehorizons.desc.workRadius"),
                    Component.translatable("gui.pleasurehorizons.currentValue",
                            Component.translatable("setting.pleasurehorizons.workRadius."
                                    + effectiveMode(key, girl.getWorkRadiusMode()))));
            case "gui.pleasurehorizons.button.guardRange" -> List.of(
                    Component.translatable("gui.pleasurehorizons.desc.guardRange"),
                    Component.translatable("gui.pleasurehorizons.currentValue",
                            Component.translatable("setting.pleasurehorizons.guardRange."
                                    + effectiveMode(key, girl.getGuardRangeMode()))));
            case "gui.pleasurehorizons.button.stayRadius" -> List.of(
                    Component.translatable("gui.pleasurehorizons.desc.stayRadius"),
                    Component.translatable("gui.pleasurehorizons.currentValue",
                            Component.translatable("setting.pleasurehorizons.stayRadius."
                                    + effectiveMode(key, girl.getStayRadiusMode()))));
            default -> null;
        };
    }

    private Component valueLine(String onKey, String offKey, boolean on) {
        return Component.translatable("gui.pleasurehorizons.currentValue",
                Component.translatable(on ? onKey : offKey));
    }

    /**
     * Bumps the pending counter for a setting, anchoring the base to the CURRENT server value
     * on the first pending click (the value captured before the server applies the new one).
     */
    private void recordPendingSettingClick(String key) {
        int n = this.pendingClicks.getOrDefault(key, 0);
        if (n == 0) {
            if (MODE_SETTING_KEYS.contains(key)) {
                this.pendingBaseMode.put(key, serverMode(key));
            } else {
                this.pendingBaseToggle.put(key, switch (key) {
                    case "gui.pleasurehorizons.button.followTeleport" -> girl.isFollowTeleportEnabled();
                    case "gui.pleasurehorizons.button.closeDoors" -> girl.isCloseDoorsEnabled();
                    case "gui.pleasurehorizons.button.avoidWater" -> girl.isAvoidWaterEnabled();
                    case "gui.pleasurehorizons.button.autoDeliver" -> girl.isAutoDeliverEnabled();
                    case "gui.pleasurehorizons.button.autoEquipArmor" -> girl.isAutoEquipArmorEnabled();
                    case "gui.pleasurehorizons.button.avoidCreepers" -> girl.isAvoidCreepersEnabled();
                    default -> girl.isHighJumpEnabled();
                });
            }
        }
        this.pendingClicks.put(key, n + 1);
    }

    /** Server state plus the clicks not yet confirmed by the server (self-correcting). */
    private boolean effectiveToggle(String key, boolean serverValue) {
        int n = this.pendingClicks.getOrDefault(key, 0);
        if (n == 0) {
            return serverValue;
        }
        boolean base = this.pendingBaseToggle.getOrDefault(key, serverValue);
        if (serverValue != base) {
            // The server applied one of the pending clicks.
            n--;
            base = serverValue;
        }
        if (n == 0) {
            this.pendingClicks.remove(key);
            this.pendingBaseToggle.remove(key);
            return serverValue;
        }
        this.pendingClicks.put(key, n);
        this.pendingBaseToggle.put(key, base);
        return n % 2 == 0 ? serverValue : !serverValue;
    }

    /** Server mode plus the pending cycle steps (self-correcting, three values). */
    private int effectiveMode(String key, int serverValue) {
        int n = this.pendingClicks.getOrDefault(key, 0);
        if (n == 0) {
            return serverValue;
        }
        int base = this.pendingBaseMode.getOrDefault(key, serverValue);
        while (serverValue != base && n > 0) {
            // The server applied one of the pending cycle steps.
            n--;
            base = (base + 1) % 3;
        }
        if (n == 0) {
            this.pendingClicks.remove(key);
            this.pendingBaseMode.remove(key);
            return serverValue;
        }
        this.pendingClicks.put(key, n);
        this.pendingBaseMode.put(key, base);
        return Math.floorMod(serverValue + n, 3);
    }

    /** Toggle buttons read the live state and show "Stop ..." when enabled. */
    private Component dynamicLabel(InventoryButtonAction action) {
        String key = action.labelKey();
        if ("gui.pleasurehorizons.button.followDistance".equals(key)) {
            int mode = effectiveMode(key, girl.getFollowDistanceMode());
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
