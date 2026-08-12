package com.sandymandy.pleasurehorizons.client.gui.screen.customize;

import com.sandymandy.pleasurehorizons.client.gui.screen.CustomizeScreen;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SliderSection<T extends GirlSceneEntity> extends CustomizeSection<T> {

    private final Component label;
    private final int minValue;
    private final int maxValue;
    private final Supplier<Integer> valueGetter;
    private final Consumer<Integer> valueSetter;

    public SliderSection(T entity, T previewEntity, Component label, int minValue, int maxValue,
                         Supplier<Integer> valueGetter, Consumer<Integer> valueSetter) {
        super(entity, previewEntity);
        this.label = label;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.valueGetter = valueGetter;
        this.valueSetter = valueSetter;
    }

    public SliderSection(T entity, T previewEntity, String label, int minValue, int maxValue,
                         Supplier<Integer> valueGetter, Consumer<Integer> valueSetter) {
        this(entity, previewEntity, Component.literal(label), minValue, maxValue, valueGetter, valueSetter);
    }

    public SliderSection(T entity, T previewEntity, Component label, int minValue, int maxValue,
                         Supplier<Integer> valueGetter, Consumer<Integer> valueSetter, String tooltip) {
        this(entity, previewEntity, label, minValue, maxValue, valueGetter, valueSetter);
    }

    public SliderSection(T entity, T previewEntity, String label, int minValue, int maxValue,
                         Supplier<Integer> valueGetter, Consumer<Integer> valueSetter, String tooltip) {
        this(entity, previewEntity, Component.literal(label), minValue, maxValue, valueGetter, valueSetter);
    }

    @Override
    public void init(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int startY) {
    }

    @Override
    public int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
        int value = valueGetter.get();

        StringWidget labelWidget = new StringWidget(layout.centerX, currentY, layout.contentWidth, 20,
                Component.literal(label.getString() + \": \" + value), Minecraft.getInstance().font);
        screen.addWidget(labelWidget);
        currentY += 20;

        int btnWidth = (layout.contentWidth / 2) - 2;
        Button minusBtn = Button.builder(Component.literal(\"-\"), btn -> {
            int cur = valueGetter.get();
            int newVal = cur - 1;
            if (newVal < minValue) newVal = minValue;
            valueSetter.accept(newVal);
        }).bounds(layout.centerX, currentY, btnWidth, 20).build();

        Button plusBtn = Button.builder(Component.literal(\"+\"), btn -> {
            int cur = valueGetter.get();
            int newVal = cur + 1;
            if (newVal > maxValue) newVal = maxValue;
            valueSetter.accept(newVal);
        }).bounds(layout.centerX + btnWidth + 4, currentY, btnWidth, 20).build();

        screen.addWidget(minusBtn);
        screen.addWidget(plusBtn);

        return currentY + 25;
    }
}
