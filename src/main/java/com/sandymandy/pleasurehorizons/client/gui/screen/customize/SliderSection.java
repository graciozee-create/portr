package com.sandymandy.pleasurehorizons.client.gui.screen.customize;

import com.sandymandy.pleasurehorizons.client.gui.screen.CustomizeScreen;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Ported from Fabric original (Yarn SliderWidget -> Mojang AbstractSliderButton)
 */
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

    // Overload without tooltip for compatibility
    public SliderSection(T entity, T previewEntity, String label, int minValue, int maxValue,
                         Supplier<Integer> valueGetter, Consumer<Integer> valueSetter) {
        this(entity, previewEntity, label, minValue, maxValue, valueGetter, valueSetter, null);
    }

    @Override
    public void init(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int startY) {
    }

    @Override
    public int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
        int value = valueGetter.get();
        double normalized = maxValue == minValue ? 0.0 : (double) (value - minValue) / (maxValue - minValue);
        normalized = Math.clamp(normalized, 0.0, 1.0);

        AbstractSliderButton slider = new AbstractSliderButton(layout.centerX, currentY, layout.contentWidth, 20,
                Component.literal(label + \": \" + value), normalized) {
            @Override
            protected void updateMessage() {
                int currentValue = minValue + (int) (this.value * (maxValue - minValue));
                this.setMessage(Component.literal(label + \": \" + currentValue));
            }

            @Override
            protected void applyValue() {
                int newValue = minValue + (int) (this.value * (maxValue - minValue));
                valueSetter.accept(newValue);
            }
        };

        if (tooltipText != null) {
            slider.setTooltip(Tooltip.create(Component.literal(tooltipText)));
        }

        screen.addWidget(slider);
        return currentY + 25;
    }
}
