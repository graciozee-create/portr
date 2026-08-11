package com.sandymandy.pleasurehorizons.config.gui;

import com.google.common.util.concurrent.AtomicDouble;
import me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.sandymandy.pleasurehorizons.freecam.Freecam.MC;

/**
 * {@link IntegerSliderEntry} ported from {@code int} to {@code double}.
 */
class DoubleSliderEntry extends TooltipListEntry<Double> {
    private final Slider sliderWidget;
    private final ButtonWidget resetButton;
    private final AtomicDouble value;
    private final double original;
    private final int precision;
    private final double minimum;
    private final double maximum;
    private final Supplier<Double> defaultValue;
    private final List<ClickableWidget> widgets;

    DoubleSliderEntry(Text fieldName, int precision, double minimum, double maximum, double value, Text resetText, Supplier<Double> defaultValue, @Nullable Consumer<Double> save) {
        //noinspection deprecation,UnstableApiUsage
        super(fieldName, null);
        this.value = new AtomicDouble(value);
        this.original = value;
        this.defaultValue = defaultValue;
        this.maximum = maximum;
        this.minimum = minimum;
        this.precision = precision;
        this.saveCallback = save;
        this.sliderWidget = new Slider(0, 0, 152, 20, (this.value.get() - minimum) / (maximum - minimum));
        this.sliderWidget.updateMessage();
        this.resetButton = ButtonWidget.builder(resetText, widget -> this.setValue(this.defaultValue.get()))
                .width(MC.textRenderer.getWidth(resetText) + 6)
                .build();
        this.widgets = List.of(this.sliderWidget, this.resetButton);
    }

    @Override
    public Double getValue() {
        return value.get();
    }

    public void setValue(double value) {
        double clamped = MathHelper.clamp(value, minimum, maximum);
        this.value.set(clamped);
        sliderWidget.setValue((clamped - minimum) / (maximum - minimum));
        sliderWidget.updateMessage();
    }

    @Override
    public boolean isEdited() {
        return super.isEdited() || getValue() != original;
    }

    @Override
    public Optional<Double> getDefaultValue() {
        return Optional.ofNullable(defaultValue).map(Supplier::get);
    }

    @Override
    public @NotNull List<? extends Element> children() {
        return widgets;
    }

    @Override
    public List<? extends Selectable> narratables() {
        return widgets;
    }

    @Override
    public void render(DrawContext graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        Window window = MC.getWindow();
        resetButton.active = isEditable() && getDefaultValue().isPresent() && defaultValue.get() != value.get();
        resetButton.setY(y);
        sliderWidget.active = isEditable();
        sliderWidget.setY(y);

        Text name = getDisplayedFieldName();
        if (MC.textRenderer.isRightToLeft()) {
            graphics.drawTextWithShadow(MC.textRenderer, name.asOrderedText(), window.getScaledWidth() - x - MC.textRenderer.getWidth(name), y + 6, getPreferredTextColor());
            resetButton.setX(x);
            sliderWidget.setX(x + resetButton.getWidth() + 1);
        } else {
            graphics.drawTextWithShadow(MC.textRenderer, name.asOrderedText(), x, y + 6, getPreferredTextColor());
            resetButton.setX(x + entryWidth - resetButton.getWidth());
            sliderWidget.setX(x + entryWidth - 150);
        }

        sliderWidget.setWidth(150 - resetButton.getWidth() - 2);
        resetButton.render(graphics, mouseX, mouseY, delta);
        sliderWidget.render(graphics, mouseX, mouseY, delta);
    }

    private final class Slider extends SliderWidget {
        private Slider(int x, int y, int width, int height, double value) {
            super(x, y, width, height, Text.empty(), value);
        }

        @Override
        public void updateMessage() {
            NumberFormat fmt = DecimalFormat.getInstance();
            fmt.setMinimumIntegerDigits(1);
            fmt.setMinimumFractionDigits(precision);
            fmt.setMaximumFractionDigits(precision);
            setMessage(Text.literal("Value: " + fmt.format(DoubleSliderEntry.this.value.get())));
        }

        @Override
        protected void applyValue() {
            double rounded = BigDecimal.valueOf(DoubleSliderEntry.this.minimum + (DoubleSliderEntry.this.maximum - DoubleSliderEntry.this.minimum) * this.value)
                    .setScale(DoubleSliderEntry.this.precision, RoundingMode.HALF_UP)
                    .doubleValue();
            DoubleSliderEntry.this.value.set(rounded);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return DoubleSliderEntry.this.isEditable() && super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return DoubleSliderEntry.this.isEditable() && super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        public void setValue(double value) {
            this.value = value;
        }
    }
}
