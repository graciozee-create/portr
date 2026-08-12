package com.sandymandy.pleasurehorizons.client.gui.screen.customize;

import com.sandymandy.pleasurehorizons.client.gui.screen.CustomizeScreen;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public class Vec3dInputSection<T extends GirlSceneEntity> extends CustomizeSection<T> {

    private final String label;

    public Vec3dInputSection(T entity, T previewEntity, String label,
                             java.util.function.Supplier<net.minecraft.world.phys.Vec3> valueGetter,
                             java.util.function.Consumer<net.minecraft.world.phys.Vec3> valueSetter) {
        super(entity, previewEntity);
        this.label = label;
    }

    @Override
    public void init(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int startY) {
    }

    @Override
    public int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
        StringWidget labelWidget = new StringWidget(layout.centerX, currentY, layout.contentWidth, 20,
                Component.literal(label), Minecraft.getInstance().font);
        screen.addWidget(labelWidget);
        return currentY + 20;
    }
}
