package com.sandymandy.pleasurehorizons.client.gui.screen.customize;

import com.sandymandy.pleasurehorizons.client.gui.screen.CustomizeScreen;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
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

        StringWidget labelWidget = new StringWidget(layout.centerX, currentY, layout.contentWidth, 20,
                Component.literal(label + \": \" + value), Minecraft.getInstance().font);
        if (tooltipText != null) {
            labelWidget.setTooltip(Tooltip.create(Component.literal(tooltipText)));
        }
        screen.addWidget(labelWidget);
        currentY += 20;

        int btnWidth = (layout.contentWidth / 2) - 2;
        Button minusBtn = Button.builder(Component.literal(\"-\"), btn -> {
            int cur = valueGetter.get();
            int newVal = Math.max(minValue, cur - 1);
            valueSetter.accept(newVal);
            labelWidget.setMessage(Component.literal(label + \": \" + newVal));
        }).bounds(layout.centerX, currentY, btnWidth, 20).build();

        Button plusBtn = Button.builder(Component.literal(\"+\"), btn -> {
            int cur = valueGetter.get();
            int newVal = Math.min(maxValue, cur + 1);
            valueSetter.accept(newVal);
            labelWidget.setMessage(Component.literal(label + \": \" + newVal));
        }).bounds(layout.centerX + btnWidth + 4, currentY, btnWidth, 20).build();

        screen.addWidget(minusBtn);
        screen.addWidget(plusBtn);

        return currentY + 25;
    }
}
