package com.sandymandy.pleasurehorizons.client.gui.screen.customize;

import com.sandymandy.pleasurehorizons.client.gui.screen.CustomizeScreen;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Vec3dInputSection<T extends GirlSceneEntity> extends CustomizeSection<T> {

    private final String label;
    private final Supplier<Vec3> valueGetter;
    private final Consumer<Vec3> valueSetter;

    private EditBox xField;
    private EditBox yField;
    private EditBox zField;

    public Vec3dInputSection(T entity, T previewEntity, String label,
                             Supplier<Vec3> valueGetter, Consumer<Vec3> valueSetter) {
        super(entity, previewEntity);
        this.label = label;
        this.valueGetter = valueGetter;
        this.valueSetter = valueSetter;
    }

    @Override
    public void init(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int startY) {
    }

    @Override
    public int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
        Vec3 currentValue = valueGetter.get();

        StringWidget labelWidget = new StringWidget(layout.centerX, currentY, layout.contentWidth, 20,
                Component.literal(label), Minecraft.getInstance().font);
        screen.addWidget(labelWidget);
        currentY += 20;

        int fieldWidth = 60;
        int fieldGap = 10;
        int totalWidth = (int) ((fieldWidth * 1.5) + fieldGap);
        int startX = layout.centerX + totalWidth;

        // X Field
        xField = new EditBox(Minecraft.getInstance().font, startX, currentY, fieldWidth, 20, Component.literal("X"));
        xField.setValue(String.valueOf(currentValue.x()));
        xField.setTooltip(Tooltip.create(Component.literal("X Offset")));
        xField.setResponder(text -> onValueChanged());
        screen.addWidget(xField);

        // Y Field
        yField = new EditBox(Minecraft.getInstance().font, startX + fieldWidth + fieldGap, currentY, fieldWidth, 20, Component.literal("Y"));
        yField.setValue(String.valueOf(currentValue.y()));
        yField.setTooltip(Tooltip.create(Component.literal("Y Offset")));
        yField.setResponder(text -> onValueChanged());
        screen.addWidget(yField);

        // Z Field
        zField = new EditBox(Minecraft.getInstance().font, startX + (fieldWidth + fieldGap) * 2, currentY, fieldWidth, 20, Component.literal("Z"));
        zField.setValue(String.valueOf(currentValue.z()));
        zField.setTooltip(Tooltip.create(Component.literal("Z Offset")));
        zField.setResponder(text -> onValueChanged());
        screen.addWidget(zField);

        return currentY + 25;
    }

    private void onValueChanged() {
        try {
            double x = Double.parseDouble(xField.getValue());
            double y = Double.parseDouble(yField.getValue());
            double z = Double.parseDouble(zField.getValue());
            valueSetter.accept(new Vec3(x, y, z));
        } catch (NumberFormatException e) {
            // Invalid input, don't update
        }
    }
}
