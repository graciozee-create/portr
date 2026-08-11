package com.sandymandy.pleasurehorizons.client.gui.screen.hud;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.resources.ResourceLocation;

public class SceneProgressOverlay {
    private static final Identifier SCENE_PROGRESS_BAR_TEXTURE = Identifier.of(PleasureHorizons.MOD_ID, "textures/gui/scene_progress_bar.png");
    private static final Identifier READY_TO_CUM_TEXTURE = Identifier.of(PleasureHorizons.MOD_ID, "textures/gui/cum_button.png");
    private static boolean animatingCum = false;
    private static long cumStartTime = 0L;
    private static final long CUM_ANIM_DURATION = 500; // ms

    private static boolean active = false;

    public static void setActive(boolean on) {
        active = on;
    }

    public static boolean isActive() {
        return active;
    }

    public static void triggerCumAnimation() {
        animatingCum = true;
        cumStartTime = System.currentTimeMillis();
    }

    public static void render(DrawContext context, float sceneProgress, float cumThreshold) {
        if (!active) return;

        float ratio = cumThreshold > 0 ? (sceneProgress / cumThreshold) : 0f;
        ratio = Math.min(ratio, 1f);

        int texWidth = 48;
        int texHeight = 175;
        float scale = 1f;

        int scaledWidth = (int)(texWidth * scale);
        int scaledHeight = (int)(texHeight * scale);

        int x = 36;
        int y = 10;

        // --- Background ---
        context.drawTexture(RenderPipelines.GUI_TEXTURED, SCENE_PROGRESS_BAR_TEXTURE,
                x, y,
                0, 0,
                scaledWidth, scaledHeight,
                texWidth, texHeight,
                0xFFFFFFFF);

        // --- Ready to cum message ---
        float cumScale = .4f;
        int cumXPadding = 10;
        int cumU = 0;
        int cumV = 0;
        int cropWidth = (int)(256 * cumScale);
        int cropHeight = (int)(52 * cumScale);
        int cumWidth = (int)(256 * cumScale);
        int cumHeight = (int) (106 * cumScale);
        int cumYPadding = texHeight + 5;

        if(ratio == 1f) {
            cumV = (int) (55 * cumScale);
        }

        if(!animatingCum)
            context.drawTexture(RenderPipelines.GUI_TEXTURED, READY_TO_CUM_TEXTURE,
                    cumXPadding, y + cumYPadding,
                    cumU, cumV,
                    cropWidth, cropHeight,
                    cumWidth, cumHeight,
                    0xFFFFFFFF);

        // --- Fill ---
        int insetX = (int)(8 * scale);
        int insetY = (int)(8 * scale);
        int fillWidth = scaledWidth - (insetX * 2);
        int fillHeightMax = scaledHeight - (insetY * 2);

        int fillX = x + insetX;
        int baseFillBottom = y + scaledHeight - insetY;

        if (animatingCum) {
            long elapsed = System.currentTimeMillis() - cumStartTime;
            float t = Math.min(elapsed / (float) CUM_ANIM_DURATION, 1f);

            // Y offset: move the whole bar upward over time
            int yOffset = (int)(scaledHeight * t * 2); // move up by up to 1 full bar height

            int fillY = baseFillBottom - fillHeightMax - yOffset;

            int color = 0xFFFFFFFF;
            context.fill(fillX, fillY, fillX + fillWidth, baseFillBottom - yOffset, color);

            if (t >= 1f) {
                setActive(false); // hide overlay after cum
                animatingCum = false;
            }
        } else {
            int filledHeight = (int)(ratio * fillHeightMax);
            if (filledHeight > 0) {
                int fillY = baseFillBottom - filledHeight;
                int color = 0xEFEFEFEF;
                context.fill(fillX, fillY, fillX + fillWidth, baseFillBottom, color);
            }
        }

    }

}
