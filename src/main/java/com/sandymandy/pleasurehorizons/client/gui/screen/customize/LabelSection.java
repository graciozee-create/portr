package com.sandymandy.pleasurehorizons.client.gui.screen.customize;

import com.sandymandy.pleasurehorizons.client.gui.screen.CustomizeScreen;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import net.minecraft.network.chat.Component;

public class LabelSection<T extends GirlSceneEntity> extends CustomizeSection<T> {

    private final Component text;

    public LabelSection(T entity, T previewEntity, Component text) {
        super(entity, previewEntity);
        this.text = text;
    }

    @Override
    public void init(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int startY) {
    }

    @Override
    public int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
        screen.addWidget(new net.minecraft.client.gui.widget.TextWidget(
                layout.centerX, currentY, layout.contentWidth, 20,
                text,
                screen.getTextRenderer()
        ));
        return currentY + 20;
    }

    @Override
    public int getSpacing() {
        return 5;
    }
}
