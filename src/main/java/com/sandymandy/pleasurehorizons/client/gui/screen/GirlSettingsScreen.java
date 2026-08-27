package com.sandymandy.pleasurehorizons.client.gui.screen;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.registries.InventoryButtonRegistry;
import com.sandymandy.pleasurehorizons.screen.InventoryButtonAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Full-screen settings screen for a tamed girl, opened from the single "Настройки" button in
 * {@link GirlInventoryScreen}.
 *
 * <p>The big menu used to cram a 13-row settings list next to the 176 px inventory panel, which
 * pushed the right edge off-screen on small resolutions. The list now lives here: one row per
 * setting with the name on the left and the LIVE value on the right (Вкл/Выкл, Близко/Обычно/
 * Далеко), the same relationship gating, the same C2S toggle/cycle actions and the same
 * optimistic pending-click reconciliation the inline list had. Escape or "Назад" returns to the
 * girl's inventory (the same screen instance is re-shown, so the container stays open and the
 * pending optimistic state survives the round trip).</p>
 */
@OnlyIn(Dist.CLIENT)
public class GirlSettingsScreen extends Screen {
    // Palette of the old inline settings list (kept so the rows look familiar).
    private static final int COLOR_PANEL_BG = 0xF01C1018;
    private static final int COLOR_BORDER = 0xFF664466;
    private static final int COLOR_HEADER = 0xFFCC88DD;
    private static final int COLOR_RELATIONSHIP = 0xFF9988AA;

    private static final int ROW_H = 24;
    private static final int BTN_H = 20;
    private static final int VALUE_W = 116;
    private static final int TITLE_ZONE = 34;
    private static final int BACK_ZONE = 36;

    private final GirlInventoryScreen previous;
    private final TameableGirlEntity girl;
    private final Player player;
    private final List<InventoryButtonAction> settings = InventoryButtonRegistry.BUTTONS_SETTINGS;

    /**
     * Clicks made since the server state was last observed, plus the server value at the time of
     * the first pending click ("base"). Synched data only arrives from the server a few ticks
     * later, so the pending count lets the values reflect the would-be state immediately; the
     * base makes the optimistic display self-correcting (same mechanism as the old inline list).
     */
    private final Map<String, Integer> pendingClicks = new HashMap<>();
    private final Map<String, Boolean> pendingBaseToggle = new HashMap<>();
    private final Map<String, Integer> pendingBaseMode = new HashMap<>();
    /** Setting keys that cycle through three values instead of flipping on/off. */
    private static final Set<String> MODE_SETTING_KEYS = Set.of(
            "gui.pleasurehorizons.button.followDistance",
            "gui.pleasurehorizons.button.workPace",
            "gui.pleasurehorizons.button.workRadius",
            "gui.pleasurehorizons.button.guardRange",
            "gui.pleasurehorizons.button.stayRadius");

    private int panelW;
    private int panelH;
    private int panelX;
    private int panelY;
    private int contentX;
    private int contentW;
    private int contentHeight;
    private double scrollOffset;
    private int frameCounter;

    public GirlSettingsScreen(GirlInventoryScreen previous, TameableGirlEntity girl, Player player) {
        super(Component.translatable("gui.pleasurehorizons.settings_screen.title"));
        this.previous = previous;
        this.girl = girl;
        this.player = player;
    }

    @Override
    protected void init() {
        super.init();
        layout();
        rebuildWidgets();
    }

    private void layout() {
        this.panelW = Math.min(420, this.width - 40);
        int natural = TITLE_ZONE + (this.settings.size() + 1) * ROW_H + BACK_ZONE;
        this.panelH = Math.min(natural, this.height - 40);
        this.panelX = (this.width - panelW) / 2;
        this.panelY = Math.max(16, (this.height - panelH) / 2);
        this.contentX = panelX + 16;
        this.contentW = panelW - 32;
    }

    private int maxScroll() {
        return Math.max(0, this.contentHeight - (this.panelH - TITLE_ZONE - BACK_ZONE));
    }

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();
        this.scrollOffset = Math.max(0, Math.min(this.maxScroll(), this.scrollOffset));

        int y = panelY + TITLE_ZONE - (int) scrollOffset;
        for (InventoryButtonAction action : this.settings) {
            boolean locked = girl.getCurrentRelationshipLevel() < action.requiredRelationshipLevel();

            this.addRenderableWidget(new StringWidget(contentX, y, contentW - VALUE_W - 6, BTN_H,
                    Component.translatable(action.labelKey()), this.font));

            Button button = Button.builder(this.settingValue(action.labelKey()), b -> {
                if (!locked) {
                    action.action().accept(girl, player);
                    recordPendingSettingClick(action.labelKey());
                    this.rebuildWidgets();
                }
            }).bounds(controlX(), y, VALUE_W, BTN_H).build();
            if (locked) {
                button.active = false;
                button.setTooltip(Tooltip.create(Component.translatable(
                        "gui.pleasurehorizons.requires_relationship", action.requiredRelationshipLevel())));
            } else {
                button.setTooltip(rowTooltip(action.labelKey()));
            }
            this.addRenderableWidget(button);
            y += ROW_H;
        }

        // Mod-wide/client options are not per-girl, so the last row opens their screen.
        Button modButton = Button.builder(
                Component.translatable("gui.pleasurehorizons.settings.open"),
                b -> {
                    if (this.previous != null) {
                        // Close the girl's container properly (same path as the old mod row),
                        // then swap to the mod settings screen.
                        this.previous.onClose();
                    }
                    Minecraft.getInstance().setScreen(new FreecamSettingsScreen());
                }
        ).bounds(contentX, y, contentW, BTN_H).build();
        this.addRenderableWidget(modButton);
        this.contentHeight = y + BTN_H;

        // Fixed back button at the bottom of the panel.
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.pleasurehorizons.settings_screen.back"),
                        b -> this.onClose())
                .bounds(panelX + panelW - 16 - 80, panelY + panelH - 28, 80, BTN_H).build());
    }

    private int controlX() {
        return contentX + contentW - VALUE_W;
    }

    /** Description + the (optimistic) current value, like the old inline rows. */
    private Tooltip rowTooltip(String key) {
        String base = modeSettingBase(key);
        if (MODE_SETTING_KEYS.contains(key)) {
            return Tooltip.create(
                    Component.translatable("gui.pleasurehorizons.desc." + base),
                    Component.translatable("gui.pleasurehorizons.currentValue",
                            Component.translatable("setting.pleasurehorizons." + base + "." + effectiveMode(key, serverMode(key)))));
        }
        return Tooltip.create(
                Component.translatable("gui.pleasurehorizons.desc." + base),
                Component.translatable("gui.pleasurehorizons.currentValue",
                        Component.translatable(effectiveToggle(key, serverToggle(key))
                                ? "setting.pleasurehorizons.on" : "setting.pleasurehorizons.off")));
    }

    /**
     * Live value for a row: on/off for toggles, the mode name for cycles, with the optimistic
     * pending-click reconciliation applied.
     */
    private Component settingValue(String key) {
        if (MODE_SETTING_KEYS.contains(key)) {
            int mode = effectiveMode(key, serverMode(key));
            return Component.translatable("setting.pleasurehorizons." + modeSettingBase(key) + "." + mode);
        }
        boolean on = effectiveToggle(key, serverToggle(key));
        return Component.translatable(on ? "setting.pleasurehorizons.on" : "setting.pleasurehorizons.off");
    }

    /** Server value of a toggle setting. */
    private boolean serverToggle(String key) {
        return switch (key) {
            case "gui.pleasurehorizons.button.followTeleport" -> girl.isFollowTeleportEnabled();
            case "gui.pleasurehorizons.button.closeDoors" -> girl.isCloseDoorsEnabled();
            case "gui.pleasurehorizons.button.avoidWater" -> girl.isAvoidWaterEnabled();
            case "gui.pleasurehorizons.button.autoDeliver" -> girl.isAutoDeliverEnabled();
            case "gui.pleasurehorizons.button.autoEquipArmor" -> girl.isAutoEquipArmorEnabled();
            case "gui.pleasurehorizons.button.avoidCreepers" -> girl.isAvoidCreepersEnabled();
            default -> girl.isHighJumpEnabled();
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

    /** "followDistance" / "workPace" / ... - the part shared with the setting lang keys. */
    private String modeSettingBase(String key) {
        return key.substring(key.lastIndexOf('.') + 1);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Server sync lands a few ticks after a click - refresh twice a second so the values
        // re-anchor to the confirmed server state (same cadence as the inventory screen).
        if (++this.frameCounter % 20 == 1) {
            this.rebuildWidgets();
        }

        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, COLOR_PANEL_BG);
        g.fill(panelX, panelY, panelX + panelW, panelY + 1, COLOR_BORDER);
        g.fill(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, COLOR_BORDER);
        g.fill(panelX, panelY, panelX + 1, panelY + panelH, COLOR_BORDER);
        g.fill(panelX + panelW - 1, panelY, panelX + panelW, panelY + panelH, COLOR_BORDER);

        g.drawCenteredString(this.font, this.title, panelX + panelW / 2, panelY + 8, COLOR_HEADER);
        g.drawString(this.font, Component.translatable("gui.pleasurehorizons.settings_screen.relationship",
                girl.getCurrentRelationshipLevel(), girl.maxRelationshipLevel()),
                contentX, panelY + 22, COLOR_RELATIONSHIP, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // No-op: a normal in-game panel over the world, not a blurred menu.
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (mouseX < panelX || mouseX > panelX + panelW || mouseY < panelY || mouseY > panelY + panelH) {
            return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        }
        double previous = this.scrollOffset;
        this.scrollOffset = Math.max(0, Math.min(this.maxScroll(), this.scrollOffset - deltaY * 14));
        if (previous != this.scrollOffset) {
            this.rebuildWidgets();
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * Escape (and the "Назад" button) go back to the girl's inventory instead of the game:
     * the same screen instance is re-shown, so its container is still open and the pending
     * optimistic clicks survive.
     */
    @Override
    public void onClose() {
        if (this.previous != null && this.minecraft != null) {
            this.minecraft.setScreen(this.previous);
        } else {
            super.onClose();
        }
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
                this.pendingBaseToggle.put(key, serverToggle(key));
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
}
