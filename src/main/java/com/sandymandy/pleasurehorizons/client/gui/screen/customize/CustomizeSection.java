package com.sandymandy.pleasurehorizons.client.gui.screen.customize;

import com.sandymandy.pleasurehorizons.client.gui.screen.CustomizeScreen;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;

public abstract class CustomizeSection<T extends GirlSceneEntity> {

    protected final T entity;
    protected final T previewEntity;

    public CustomizeSection(T entity, T previewEntity) {
        this.entity = entity;
        this.previewEntity = previewEntity;
    }

    /**
     * Initialize the section (called during screen init)
     */
    public abstract void init(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int startY);

    /**
     * Render the section and return the new Y position
     */
    public abstract int render(CustomizeScreen<T> screen, CustomizeScreen.LayoutConfig layout, int currentY);

    /**
     * Get spacing after this section
     */
    public int getSpacing() {
        return 10;
    }
}
