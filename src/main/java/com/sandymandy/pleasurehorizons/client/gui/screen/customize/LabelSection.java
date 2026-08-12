package com.sandymandy.pleasurehorizons.client.gui.screen.customize;

import com.sandymandy.pleasurehorizons.client.gui.screen.CustomizeScreen;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public class LabelSection<T extends GirlSceneEntity> extends CustomizeSection<T> {

    private final Component text;

    public LabelSection(T entity, T previewEntity, Component text) {
        super(entity, previewEntity);
        this.text = text;
    }

    public LabelSection(T entity, T previewEntity, String text) {
        this(entity, previewEntity, Component.literal(text));
    }

    @Override
    public void init(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int startY) {
    }

    @Override
    public int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
        StringWidget widget = new StringWidget(layout.centerX, currentY, layout.contentWidth, 20, text, Minecraft.getInstance().font);
        screen.addWidget(widget);
        return currentY + 20;
    }

    @Override
    public int getSpacing() {
        return 5;
    }
}
