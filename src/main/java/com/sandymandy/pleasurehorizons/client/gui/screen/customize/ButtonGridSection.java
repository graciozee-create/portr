package com.sandymandy.pleasurehorizons.client.gui.screen.customize;

import com.sandymandy.pleasurehorizons.client.gui.screen.CustomizeScreen;
import com.sandymandy.pleasurehorizons.client.gui.screen.CustomizeScreen.LayoutConfig;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ButtonGridSection<T extends GirlSceneEntity, V> extends CustomizeSection<T> {

    private final String title;
    private final String groupId;
    private final V[] options;
    private final int columns;
    private final Function<V, Component> labelProvider;
    private final Consumer<V> onSelect;
    private final Supplier<V> currentValue;

    public ButtonGridSection(T entity, T previewEntity, String title, String groupId, V[] options, int columns,
                             Function<V, Component> labelProvider, Consumer<V> onSelect, Supplier<V> currentValue) {
        super(entity, previewEntity);
        this.title = title;
        this.groupId = groupId;
        this.options = options;
        this.columns = columns;
        this.labelProvider = labelProvider;
        this.onSelect = onSelect;
        this.currentValue = currentValue;
    }

    @Override
    public void init(CustomizeScreen<T> screen, LayoutConfig layout, int startY) {
    }

    @Override
    public int render(CustomizeScreen<T> screen, LayoutConfig layout, int currentY) {
        screen.addWidget(new net.minecraft.client.gui.widget.TextWidget(
                layout.centerX, currentY, layout.contentWidth, 20,
                Component.literal(title),
                screen.getTextRenderer()
        ));
        currentY += 20;

        // Calculate button dimensions
        int totalGaps = (columns - 1) * 5;
        int buttonWidth = (layout.contentWidth - totalGaps) / columns;
        int rows = (options.length + columns - 1) / columns;

        V current = currentValue.get();

        for (int i = 0; i < options.length; i++) {
            int row = i / columns;
            int col = i % columns;
            int btnX = layout.centerX + (col * (buttonWidth + 5));
            int btnY = currentY + (row * 25);

            V option = options[i];

            ButtonWidget button = screen.createSelectableButton(
                    groupId,
                    labelProvider.apply(option),
                    btnX, btnY, buttonWidth, 20,
                    btn -> onSelect.accept(option)
            );

            screen.addWidget(button);

            // Mark as selected if current
            if (option.equals(current)) {
                screen.markAsSelected(groupId, button);
            }
        }

        return currentY + (rows * 25) + 10;
    }
}
