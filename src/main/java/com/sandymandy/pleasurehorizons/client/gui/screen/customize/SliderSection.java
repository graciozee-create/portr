package com.sandymandy.pleasurehorizons.client.gui.screen.customize;

import com.sandymandy.pleasurehorizons.client.gui.screen.CustomizeScreen;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SliderSection<T extends GirlSceneEntity> extends CustomizeSection<T> {

    private final Component label;

    public SliderSection(T entity, T previewEntity, Component label, int minValue, int maxValue,
                         Supplier<Integer> valueGetter, Consumer<Integer> valueSetter) {
        super(entity, previewEntity);
        this.label = label;
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
        return currentY + 25;
    }
}
