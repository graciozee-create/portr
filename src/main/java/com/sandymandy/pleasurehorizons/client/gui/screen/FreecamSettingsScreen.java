package com.sandymandy.pleasurehorizons.client.gui.screen;

import com.sandymandy.pleasurehorizons.config.ModConfig;
import com.sandymandy.pleasurehorizons.freecam.FreecamConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Consumer;

/**
 * In-game settings screen for the client-side Pleasure Horizons options (freecam, girls'
 * flat shading and the thrust-key behaviour).
 *
 * <p>It mirrors the compact section layout used by the customize screens, but with a normal
 * transparent background: the world stays visible behind the centered panel instead of a blurred
 * menu. Every change is written through {@link FreecamConfig} (the .toml) or {@link ModConfig}
 * (the small JSON file) immediately, so the values survive a restart and are picked up live.</p>
 */
@OnlyIn(Dist.CLIENT)
public class FreecamSettingsScreen extends Screen {

    private static final int COLOR_PANEL = 0xD0331133;
    private static final int COLOR_BORDER = 0xFF664466;
    private static final int COLOR_HEADER = 0xFFFF88CC;

    private static final int ROW_H = 24;
    private static final int SECTION_H = 16;
    private static final int BTN_H = 20;

    private int panelW;
    private int panelH;
    private int panelX;
    private int panelY;
    private int contentX;
    private int contentW;
    private int valueW = 116;

    private double scrollOffset = 0;
    private int contentHeight = 0;

    public FreecamSettingsScreen() {
        super(Component.translatable("text.autoconfig.pleasurehorizons.title"));
    }

    @Override
    protected void init() {
        super.init();
        layout();
        rebuildWidgets();
    }

    private void layout() {
        this.panelW = Math.min(390, this.width - 40);
        this.panelH = Math.min(360, this.height - 40);
        this.panelX = (this.width - panelW) / 2;
        this.panelY = Math.max(16, (this.height - panelH) / 2);
        this.contentX = panelX + 16;
        this.contentW = panelW - 32;
    }

    private int maxScroll() {
        return Math.max(0, this.contentHeight - (this.panelH - 42));
    }

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();
        this.scrollOffset = Math.max(0, Math.min(this.maxScroll(), this.scrollOffset));

        int y = panelY + 30 - (int) scrollOffset;

        y = section(Minecraft.getInstance(), y, "text.autoconfig.pleasurehorizons.category.freecam");
        y = sliderRow(y, "text.autoconfig.pleasurehorizons.option.movement.horizontalSpeed",
                0.0, 10.0, () -> FreecamConfig.INSTANCE.horizontalSpeed,
                FreecamConfig::setHorizontalSpeed);
        y = sliderRow(y, "text.autoconfig.pleasurehorizons.option.movement.verticalSpeed",
                0.0, 10.0, () -> FreecamConfig.INSTANCE.verticalSpeed,
                FreecamConfig::setVerticalSpeed);

        y = section(Minecraft.getInstance(), y, "text.autoconfig.pleasurehorizons.option.visual");
        y = enumRow(y, "text.autoconfig.pleasurehorizons.option.visual.perspective",
                FreecamConfig.Perspective.values(),
                () -> FreecamConfig.INSTANCE.perspective,
                this::perspectiveKey,
                FreecamConfig::setPerspective);
        y = boolRow(y, "text.autoconfig.pleasurehorizons.option.visual.hidePlayer",
                () -> FreecamConfig.INSTANCE.hidePlayer, FreecamConfig::setHidePlayer);
        y = boolRow(y, "text.autoconfig.pleasurehorizons.option.visual.showHand",
                () -> FreecamConfig.INSTANCE.showHand, FreecamConfig::setShowHand);
        y = boolRow(y, "text.autoconfig.pleasurehorizons.option.visual.showSubmersion",
                () -> FreecamConfig.INSTANCE.showSubmersion, FreecamConfig::setShowSubmersion);

        y = section(Minecraft.getInstance(), y, "text.autoconfig.pleasurehorizons.option.utility");
        y = boolRow(y, "text.autoconfig.pleasurehorizons.option.utility.disableOnDamage",
                () -> FreecamConfig.INSTANCE.disableOnDamage, FreecamConfig::setDisableOnDamage);
        y = boolRow(y, "text.autoconfig.pleasurehorizons.option.utility.allowInteract",
                () -> FreecamConfig.INSTANCE.allowInteract, FreecamConfig::setAllowInteract);
        y = enumRow(y, "text.autoconfig.pleasurehorizons.option.utility.interactionMode",
                FreecamConfig.InteractionMode.values(),
                () -> FreecamConfig.INSTANCE.interactionMode,
                this::interactionModeKey,
                FreecamConfig::setInteractionMode);

        y = section(Minecraft.getInstance(), y, "text.autoconfig.pleasurehorizons.option.notification");
        y = boolRow(y, "text.autoconfig.pleasurehorizons.option.notification.notifyFreecam",
                () -> FreecamConfig.INSTANCE.notifyFreecam, FreecamConfig::setNotifyFreecam);
        y = boolRow(y, "text.autoconfig.pleasurehorizons.option.notification.notifyTripod",
                () -> FreecamConfig.INSTANCE.notifyTripod, FreecamConfig::setNotifyTripod);

        y = section(Minecraft.getInstance(), y, "text.autoconfig.pleasurehorizons.option.girls");
        y = boolRow(y, "text.autoconfig.pleasurehorizons.option.girls.disableShading",
                () -> ModConfig.INSTANCE.girls.disableShading,
                value -> ModConfig.INSTANCE.setDisableShading(value));

        y = section(Minecraft.getInstance(), y, "text.autoconfig.pleasurehorizons.option.keybinds");
        y = boolRow(y, "text.autoconfig.pleasurehorizons.option.keybinds.holdThrust",
                () -> ModConfig.INSTANCE.keybinds.holdThrust,
                value -> ModConfig.INSTANCE.setHoldThrust(value));

        this.contentHeight = y + 30 - (panelY + 30 - (int) scrollOffset);

        int actionY = panelY + panelH - 28;
        int actionW = (contentW - 6) / 2;
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.pleasurehorizons.button.confirm"),
                        b -> this.onClose())
                .bounds(panelX + 16, actionY, actionW, BTN_H).build());
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.pleasurehorizons.button.cancel"),
                        b -> this.onClose())
                .bounds(panelX + 16 + actionW + 6, actionY, actionW, BTN_H).build());
    }

    private int section(Minecraft mc, int y, String key) {
        this.addRenderableWidget(new StringWidget(contentX, y, contentW, SECTION_H,
                Component.translatable(key), mc.font));
        return y + SECTION_H;
    }

    private int boolRow(int y, String key, java.util.function.Supplier<Boolean> getter, Consumer<Boolean> setter) {
        TextWidgetLabel label = textLabel(contentX, y, Component.translatable(key));
        this.addRenderableWidget(label.widget);

        Component message = current(getter.get());
        Button button = Button.builder(message, b -> {
            setter.accept(!getter.get());
            this.rebuildWidgets();
        }).bounds(controlX(), y, valueW, BTN_H).build();
        this.addRenderableWidget(button);
        return y + ROW_H;
    }

    private int sliderRow(int y, String key, double min, double max,
                          java.util.function.Supplier<Double> getter, Consumer<Double> setter) {
        TextWidgetLabel label = textLabel(contentX, y, Component.translatable(key));
        this.addRenderableWidget(label.widget);

        int bx = controlX();
        int btnW = Math.max(20, (valueW - 56) / 2);
        int valueX = bx + btnW + 2;
        int valueW = this.valueW - 2 * btnW - 4;

        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> {
            double next = Math.max(min, Math.round(getter.get() * 20.0) / 20.0 - 0.1);
            setter.accept(next);
            this.rebuildWidgets();
        }).bounds(bx, y, btnW, BTN_H).build());

        this.addRenderableWidget(new StringWidget(valueX, y, valueW, BTN_H,
                Component.literal(String.format(java.util.Locale.ROOT, "%.1f", getter.get())), this.font));

        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            double next = Math.min(max, Math.round(getter.get() * 20.0) / 20.0 + 0.1);
            setter.accept(next);
            this.rebuildWidgets();
        }).bounds(bx + btnW + 2 + valueW + 2, y, btnW, BTN_H).build());

        return y + ROW_H;
    }

    private <T> int enumRow(int y, String key, T[] values,
                            java.util.function.Supplier<T> getter,
                            java.util.function.Function<T, Component> labelProvider,
                            Consumer<T> setter) {
        TextWidgetLabel label = textLabel(contentX, y, Component.translatable(key));
        this.addRenderableWidget(label.widget);

        T current = getter.get();
        Component message = labelProvider.apply(current);
        Button button = Button.builder(message, b -> {
            int index = java.util.Arrays.asList(values).indexOf(current);
            T next = values[(index + 1) % values.length];
            setter.accept(next);
            this.rebuildWidgets();
        }).bounds(controlX(), y, this.valueW, BTN_H).build();
        this.addRenderableWidget(button);
        return y + ROW_H;
    }

    private int controlX() {
        return contentX + contentW - valueW;
    }

    private Component current(boolean value) {
        return Component.translatable(value ? "setting.pleasurehorizons.on" : "setting.pleasurehorizons.off");
    }

    private Component perspectiveKey(FreecamConfig.Perspective value) {
        String suffix = switch (value) {
            case FIRST_PERSON -> "firstPerson";
            case THIRD_PERSON -> "thirdPerson";
            case THIRD_PERSON_MIRROR -> "thirdPersonMirror";
            case INSIDE -> "inside";
        };
        return Component.translatable("text.autoconfig.pleasurehorizons.option.visual.perspective." + suffix);
    }

    private Component interactionModeKey(FreecamConfig.InteractionMode value) {
        String suffix = value == FreecamConfig.InteractionMode.CAMERA ? "camera" : "player";
        return Component.translatable("text.autoconfig.pleasurehorizons.option.utility.interactionMode." + suffix);
    }

    private TextWidgetLabel textLabel(int x, int y, Component text) {
        int labelW = contentW - valueW - 6;
        StringWidget widget = new StringWidget(x, y, labelW, BTN_H, text, this.font);
        return new TextWidgetLabel(widget);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, COLOR_PANEL);
        g.fill(panelX, panelY, panelX + panelW, panelY + 1, COLOR_BORDER);
        g.fill(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, COLOR_BORDER);
        g.fill(panelX, panelY, panelX + 1, panelY + panelH, COLOR_BORDER);
        g.fill(panelX + panelW - 1, panelY, panelX + panelW, panelY + panelH, COLOR_BORDER);

        g.drawCenteredString(this.font, this.title, panelX + panelW / 2, panelY + 8, COLOR_HEADER);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // No-op: this is a normal in-game panel over the world, not a blurred menu.
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        // Only eat the scroll while the cursor is over the panel, so scrolling the world or
        // other UI behind the screen keeps working.
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

    private record TextWidgetLabel(StringWidget widget) {
    }
}
