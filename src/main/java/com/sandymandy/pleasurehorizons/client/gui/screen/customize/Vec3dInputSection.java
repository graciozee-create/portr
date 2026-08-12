package com.sandymandy.pleasurehorizons.client.gui.screen.customize;

import com.sandymandy.pleasurehorizons.client.gui.screen.CustomizeScreen;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Vec3dInputSection<T extends GirlSceneEntity> extends CustomizeSection<T> {

    private final String label;
    private final Supplier<Vec3> valueGetter;
    private final Consumer<Vec3> valueSetter;

    public Vec3dInputSection(T entity, T previewEntity, String label,
                             Supplier<Vec3> valueGetter,
                             Consumer<Vec3> valueSetter) {
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
        Vec3 current = valueGetter.get();

        StringWidget labelWidget = new StringWidget(layout.centerX, currentY, layout.contentWidth, 20,
                Component.literal(label), Minecraft.getInstance().font);
        screen.addWidget(labelWidget);
        currentY += 20;

        int btnW = (layout.contentWidth / 3) - 4;

        Button xMinus = Button.builder(Component.literal("X-"), b -> {}).bounds(layout.centerX, currentY, btnW, 20).build();
        Button yMinus = Button.builder(Component.literal("Y-"), b -> {}).bounds(layout.centerX + btnW + 6, currentY, btnW, 20).build();
        Button zMinus = Button.builder(Component.literal("Z-"), b -> {}).bounds(layout.centerX + (btnW + 6) * 2, currentY, btnW, 20).build();

        screen.addWidget(xMinus);
        screen.addWidget(yMinus);
        screen.addWidget(zMinus);

        return currentY + 25;
    }
}
