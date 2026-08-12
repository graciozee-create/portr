package com.sandymandy.pleasurehorizons.client.gui.screen.customize;

import com.sandymandy.pleasurehorizons.client.gui.screen.CustomizeScreen;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ButtonSection<T extends GirlSceneEntity> extends CustomizeSection<T> {
    private final String label;
    private final Supplier<Boolean> valueGetter;
    private final Consumer<Boolean> valueSetter;
    private ChatFormatting textColor;

    public ButtonSection(T entity, T previewEntity, String label, Supplier<Boolean> valueGetter, Consumer<Boolean> valueSetter) {
        super(entity, previewEntity);
        this.label = label;
        this.valueGetter = valueGetter;
        this.valueSetter = valueSetter;
    }

    @Override
    public void init(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int startY) {
        textColor = this.valueGetter.get() ? ChatFormatting.GREEN : ChatFormatting.RED;
    }

    @Override
    public int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
        // Label above button - Fabric used TextWidget, we use StringWidget
        StringWidget labelWidget = new StringWidget(layout.centerX, currentY, layout.contentWidth, 20,
                Component.literal(label), net.minecraft.client.Minecraft.getInstance().font);
        screen.addWidget(labelWidget);
        currentY += 20;

        boolean current = valueGetter.get();
        textColor = current ? ChatFormatting.GREEN : ChatFormatting.RED;
        String boolStr = current ? "True" : "False";

        Button toggleButton = Button.builder(
                Component.literal(boolStr).withStyle(ChatFormatting.BOLD, textColor),
                btn -> {
                    boolean newVal = !valueGetter.get();
                    valueSetter.accept(newVal);
                    ChatFormatting newColor = newVal ? ChatFormatting.GREEN : ChatFormatting.RED;
                    btn.setMessage(Component.literal(newVal ? "True" : "False").withStyle(ChatFormatting.BOLD, newColor));
                }
        ).bounds(layout.centerX, currentY, layout.contentWidth, 20).build();

        screen.addWidget(toggleButton);

        return currentY + 20;
    }
}
