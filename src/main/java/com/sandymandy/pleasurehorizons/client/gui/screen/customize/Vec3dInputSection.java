package com.sandymandy.pleasurehorizons.client.gui.screen.customize;

import com.sandymandy.pleasurehorizons.client.gui.screen.CustomizeScreen;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Vec3dInputSection<T extends GirlSceneEntity> extends CustomizeSection<T> {

    private final String label;
    private final Supplier<Vec3d> valueGetter;
    private final Consumer<Vec3d> valueSetter;

    private TextFieldWidget xField;
    private TextFieldWidget yField;
    private TextFieldWidget zField;

    public Vec3dInputSection(T entity, T previewEntity, String label,
                             Supplier<Vec3d> valueGetter, Consumer<Vec3d> valueSetter) {
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
        Vec3d currentValue = valueGetter.get();

        // Label
        screen.addWidget(new net.minecraft.client.gui.widget.TextWidget(
                layout.centerX, currentY, layout.contentWidth, 20,
                Text.literal(label),
                screen.getTextRenderer()
        ));
        currentY += 20;

        int fieldWidth = 60;
        int fieldGap = 10;
        int totalWidth = (int) ((fieldWidth * 1.5) + fieldGap);
        int startX = layout.centerX  + totalWidth;

        // X Field
        xField = new TextFieldWidget(screen.getTextRenderer(), startX, currentY, fieldWidth, 20, Text.literal("X"));
        xField.setText(String.valueOf(currentValue.getX()));
        xField.setTooltip(Tooltip.of(Text.literal("X Offset")));
        xField.setChangedListener(text -> onValueChanged());
        screen.addWidget(xField);

        // Y Field
        yField = new TextFieldWidget(screen.getTextRenderer(), startX + fieldWidth + fieldGap, currentY, fieldWidth, 20, Text.literal("Y"));
        yField.setText(String.valueOf(currentValue.getY()));
        yField.setTooltip(Tooltip.of(Text.literal("Y Offset")));
        yField.setChangedListener(text -> onValueChanged());
        screen.addWidget(yField);

        // Z Field
        zField = new TextFieldWidget(screen.getTextRenderer(), startX + (fieldWidth + fieldGap) * 2, currentY, fieldWidth, 20, Text.literal("Z"));
        zField.setText(String.valueOf(currentValue.getZ()));
        zField.setTooltip(Tooltip.of(Text.literal("Z Offset")));
        zField.setChangedListener(text -> onValueChanged());
        screen.addWidget(zField);

        return currentY + 25;
    }

    private void onValueChanged() {
        try {
            double x = Double.parseDouble(xField.getText());
            double y = Double.parseDouble(yField.getText());
            double z = Double.parseDouble(zField.getText());

            valueSetter.accept(new Vec3d(x, y, z));
        } catch (NumberFormatException e) {
            // Invalid input, don't update
        }
    }

}
