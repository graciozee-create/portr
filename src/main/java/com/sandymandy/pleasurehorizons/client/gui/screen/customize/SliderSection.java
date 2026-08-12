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

    private final String label;

    public SliderSection(T entity, T previewEntity, String label, int minValue, int maxValue,
                         Supplier<Integer> valueGetter, Consumer<Integer> valueSetter, String tooltipText) {
        super(entity, previewEntity);
        this.label = label;
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
        StringWidget labelWidget = new StringWidget(layout.centerX, currentY, layout.contentWidth, 20,
                Component.literal(label), Minecraft.getInstance().font);
        screen.addWidget(labelWidget);
        currentY += 20;

        Button minusBtn = Button.builder(Component.literal("-"), btn -> {}).bounds(layout.centerX, currentY, 50, 20).build();
        Button plusBtn = Button.builder(Component.literal("+"), btn -> {}).bounds(layout.centerX + 54, currentY, 50, 20).build();

        screen.addWidget(minusBtn);
        screen.addWidget(plusBtn);

        return currentY + 25;
    }
}
