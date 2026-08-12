package com.sandymandy.pleasurehorizons.client.gui.screen.customize;

import com.sandymandy.pleasurehorizons.client.gui.screen.CustomizeScreen;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ButtonSection<T extends GirlSceneEntity> extends CustomizeSection<T> {
    private final Component label;
    private final Supplier<Boolean> valueGetter;
    private final Consumer<Boolean> valueSetter;

    /** String overload kept for call sites that still pass a raw literal. */
    public ButtonSection(T entity, T previewEntity, String label, Supplier<Boolean> valueGetter, Consumer<Boolean> valueSetter) {
        this(entity, previewEntity, Component.literal(label), valueGetter, valueSetter);
    }

    public ButtonSection(T entity, T previewEntity, Component label, Supplier<Boolean> valueGetter, Consumer<Boolean> valueSetter) {
        super(entity, previewEntity);
        this.label = label;
        this.valueGetter = valueGetter;
        this.valueSetter = valueSetter;
    }

    @Override
    public int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
        StringWidget labelWidget = new StringWidget(layout.centerX, currentY, layout.contentWidth, 20,
                label, Minecraft.getInstance().font);
        screen.addWidget(labelWidget);
        currentY += 20;

        boolean current = valueGetter.get();
        ChatFormatting color = current ? ChatFormatting.GREEN : ChatFormatting.RED;

        Button toggleButton = Button.builder(
                Component.translatable(current ? "gui.pleasurehorizons.customize.true" : "gui.pleasurehorizons.customize.false")
                        .withStyle(ChatFormatting.BOLD, color),
                btn -> {
                    boolean newVal = !valueGetter.get();
                    valueSetter.accept(newVal);
                    ChatFormatting newColor = newVal ? ChatFormatting.GREEN : ChatFormatting.RED;
                    btn.setMessage(Component.translatable(newVal ? "gui.pleasurehorizons.customize.true" : "gui.pleasurehorizons.customize.false")
                            .withStyle(ChatFormatting.BOLD, newColor));
                }
        ).bounds(layout.centerX, currentY, layout.contentWidth, 20).build();

        screen.addWidget(toggleButton);
        return currentY + 20;
    }
}
