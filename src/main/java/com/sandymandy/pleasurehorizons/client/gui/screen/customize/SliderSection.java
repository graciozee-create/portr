package com.sandymandy.pleasurehorizons.client.gui.screen.customize;

import com.sandymandy.pleasurehorizons.client.gui.screen.CustomizeScreen;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SliderSection<T extends GirlSceneEntity> extends CustomizeSection<T> {

    private final String label;
    private final int minValue;
    private final int maxValue;
    private final Supplier<Integer> valueGetter;
    private final Consumer<Integer> valueSetter;
    private final String tooltipText;

    public SliderSection(T entity, T previewEntity, String label, int minValue, int maxValue,
                         Supplier<Integer> valueGetter, Consumer<Integer> valueSetter, String tooltipText) {
        super(entity, previewEntity);
        this.label = label;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.valueGetter = valueGetter;
        this.valueSetter = valueSetter;
        this.tooltipText = tooltipText;
    }

    @Override
    public void init(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int startY) {
    }

    @Override
    public int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
        int value = valueGetter.get();

        SliderWidget slider = new SliderWidget(
                layout.centerX, currentY, layout.contentWidth, 20,
                Component.literal(label + ": " + value),
                (value - (float)minValue) / (maxValue - minValue)
        ) {
            @Override
            protected void updateMessage() {
                int currentValue = minValue + (int)(this.value * (maxValue - minValue));
                this.setMessage(Component.literal(label + ": " + currentValue));
            }

            @Override
            protected void applyValue() {
                int newValue = minValue + (int)(this.value * (maxValue - minValue));
                valueSetter.accept(newValue);
            }
        };

        if (tooltipText != null) {
            slider.setTooltip(Tooltip.of(Component.literal(tooltipText)));
        }

        screen.addWidget(slider);
        return currentY + 25;
    }
}

