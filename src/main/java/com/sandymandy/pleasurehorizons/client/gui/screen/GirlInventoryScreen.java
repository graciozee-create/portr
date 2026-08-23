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
    private static final int TAB_MAIN = 0;
    private static final int TAB_SURVIVAL = 1;
    private static final int TAB_SETTINGS = 2;
    private final TameableGirlEntity girl;
    private final Player player;
    private int tabIndex = TAB_MAIN;
    /**
     * Clicks made on Settings-tab buttons since the screen opened. Synched data only arrives
     * from the server a few ticks later, so these counts let the labels/tooltips reflect the
     * would-be state immediately (toggles flip per click, cycles advance per click).
     */
    private final java.util.Map<String, Integer> pendingClicks = new java.util.HashMap<>();

    public GirlInventoryScreen(GirlInventoryScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.girl = handler.getGirl();
        this.player = inventory.player;
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Stops the container names from rendering
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
                        // Talk and Customize replace this screen asynchronously after the server
                        // validates the click. Closing here would send both the vanilla container
                        // close and SetGUIOpenState(false) before the replacement screen can use
                        // the interaction. That invalidates customization previews/confirmation
                        // and makes every scene-selection button fail server authorization.
                        // Settings buttons keep the screen open too: adjusting a stack of small
                        // knobs must not close and reopen the inventory eleven times.
                        if (!action.opensSubscreen()) {
                            this.onClose();
                        } else if (tabIndex == TAB_SETTINGS) {
                            pendingClicks.merge(action.labelKey(), 1, Integer::sum);
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

        int buttonHeight = 22;
        int buttonWidth = 80;

        int paddingX = 10;
        int paddingY = 4;

        int startX = centerX - (buttonWidth + paddingX);
        int startY = centerY + 15;

        if (girl.isTamed()) {
            initTabs(centerX, centerY);

            List<InventoryButtonAction> left = switch (tabIndex) {
                case TAB_SURVIVAL -> InventoryButtonRegistry.BUTTONS_SURVIVAL_LEFT;
                case TAB_SETTINGS -> InventoryButtonRegistry.BUTTONS_SETTINGS_LEFT;
                default -> InventoryButtonRegistry.BUTTONS_MAIN_LEFT;
            };
            List<InventoryButtonAction> right = switch (tabIndex) {
                case TAB_SURVIVAL -> InventoryButtonRegistry.BUTTONS_SURVIVAL_RIGHT;
                case TAB_SETTINGS -> InventoryButtonRegistry.BUTTONS_SETTINGS_RIGHT;
                default -> InventoryButtonRegistry.BUTTONS_MAIN_RIGHT;
            };

            for (int i = 0; i < left.size(); i++) {
                InventoryButtonAction action = left.get(i);
                int y = startY + i * (buttonHeight + paddingY);
                this.drawButton(dynamicLabel(action), action, startX, y, buttonWidth, buttonHeight);
            }

            for (int i = 0; i < right.size(); i++) {
                InventoryButtonAction action = right.get(i);
                int y = startY + i * (buttonHeight + paddingY);
                this.drawButton(dynamicLabel(action), action, centerX + 176 + paddingX, y, buttonWidth, buttonHeight);
            }
        }
    }

    /** Three small tab buttons above the panel; the active tab is rendered disabled. */
    private void initTabs(int centerX, int centerY) {
        int tabWidth = 84;
        int tabHeight = 16;
        int tabY = centerY - 46;
        // The row is wider than the 176px panel, so centre it on the panel.
        int rowWidth = tabWidth * 3 + 8;
        int startX = centerX - (rowWidth - GUI_WIDTH) / 2;

        Button mainTab = Button.builder(
                        Component.translatable("gui.pleasurehorizons.tab.main"),
                        b -> switchTab(TAB_MAIN))
                .bounds(startX, tabY, tabWidth, tabHeight)
                .build();
        mainTab.active = tabIndex != TAB_MAIN;
        this.addRenderableWidget(mainTab);

        Button survivalTab = Button.builder(
                        Component.translatable("gui.pleasurehorizons.tab.survival"),
                        b -> switchTab(TAB_SURVIVAL))
                .bounds(startX + tabWidth + 4, tabY, tabWidth, tabHeight)
                .build();
        survivalTab.active = tabIndex != TAB_SURVIVAL;
        this.addRenderableWidget(survivalTab);

        Button settingsTab = Button.builder(
                        Component.translatable("gui.pleasurehorizons.tab.settings"),
                        b -> switchTab(TAB_SETTINGS))
                .bounds(startX + (tabWidth + 4) * 2, tabY, tabWidth, tabHeight)
                .build();
        settingsTab.active = tabIndex != TAB_SETTINGS;
        this.addRenderableWidget(settingsTab);
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

    /** Server state plus the clicks already made in this screen session. */
    private boolean effectiveToggle(String key, boolean serverValue) {
        int extra = pendingClicks.getOrDefault(key, 0);
        return extra % 2 == 0 ? serverValue : !serverValue;
    }

    private int effectiveMode(String key, int serverValue) {
        return Math.floorMod(serverValue + pendingClicks.getOrDefault(key, 0), 3);
    }

    private void switchTab(int tab) {
        if (this.tabIndex != tab) {
            this.tabIndex = tab;
            this.rebuildWidgets();
        }
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
