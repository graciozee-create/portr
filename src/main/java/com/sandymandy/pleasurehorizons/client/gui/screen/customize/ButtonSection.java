package com.sandymandy.pleasurehorizons.client.gui.screen.customize;

import com.sandymandy.pleasurehorizons.client.gui.screen.CustomizeScreen;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.sandymandy.pleasurehorizons.util.Utils.getFirstLetterCapitalized;

public class ButtonSection<T extends GirlSceneEntity> extends CustomizeSection<T> {
    private final String label;
    private final Supplier<Boolean> valueGetter;
    private final Consumer<Boolean> valueSetter;
    private ButtonWidget toggleButton; // Store reference to update it
    Formatting textColor;
    public ButtonSection(T entity, T previewEntity, String label, Supplier<Boolean> valueGetter, Consumer<Boolean> valueSetter) {
        super(entity, previewEntity);
        this.label = label;
        this.valueGetter = valueGetter;
        this.valueSetter = valueSetter;
    }

    @Override
    public void init(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int startY) {
        textColor = this.valueGetter.get() ? Formatting.GREEN : Formatting.RED;
    }

    @Override
    public int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
        screen.addWidget(new net.minecraft.client.gui.widget.TextWidget(
                layout.centerX, currentY, layout.contentWidth, 20,
                Text.literal(label),
                screen.getTextRenderer()
        ));
        currentY += 20;


        toggleButton = ButtonWidget.builder(
                Text.literal(getFirstLetterCapitalized(String.valueOf(this.valueGetter.get()))).formatted(Formatting.BOLD, textColor),
                button -> {
                    this.valueSetter.accept(!this.valueGetter.get());
                    textColor = this.valueGetter.get() ? Formatting.GREEN : Formatting.RED;
                    button.setMessage(Text.literal(getFirstLetterCapitalized(String.valueOf(this.valueGetter.get()))).formatted(Formatting.BOLD, textColor));
                }
        ).dimensions(layout.centerX, currentY, layout.contentWidth, 20).build();

        screen.addWidget(toggleButton);

        return currentY + 20;
    }
}
