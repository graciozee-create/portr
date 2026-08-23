package com.sandymandy.pleasurehorizons.client.gui.screen.hud;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * The vertical scene-progress bar plus the "ready to cum" prompt.
 *
 * <p>Ported from Fabric: {@code DrawContext#drawTexture(RenderPipelines.GUI_TEXTURED, ...)} became
 * {@link GuiGraphics#blit}, which in 1.21.1 takes
 * {@code (texture, x, y, u, v, width, height, textureWidth, textureHeight)}.</p>
 */
@OnlyIn(Dist.CLIENT)
public class SceneProgressOverlay {
    private static final ResourceLocation SCENE_PROGRESS_BAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "textures/gui/scene_progress_bar.png");
    private static final ResourceLocation READY_TO_CUM_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "textures/gui/cum_button.png");

    private static final long CUM_ANIM_DURATION = 500L; // ms

    private static boolean animatingCum = false;
    private static long cumStartTime = 0L;
    private static boolean active = false;

    private SceneProgressOverlay() {
    }

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

    public static void render(GuiGraphics guiGraphics, float sceneProgress, float cumThreshold) {
        if (!active) return;

        float ratio = cumThreshold > 0 ? Math.min(sceneProgress / cumThreshold, 1f) : 0f;

        int texWidth = 48;
        int texHeight = 175;

        int x = 36;
        int y = 10;

        // Background bar
        guiGraphics.blit(SCENE_PROGRESS_BAR_TEXTURE, x, y, 0, 0, texWidth, texHeight, texWidth, texHeight);

        // "Ready to cum" prompt - the sheet holds two 256x52 rows, the second one lit up.
        float cumScale = .4f;
        int cropWidth = (int) (256 * cumScale);
        int cropHeight = (int) (52 * cumScale);
        int cumWidth = (int) (256 * cumScale);
        int cumHeight = (int) (106 * cumScale);
        int cumV = ratio >= 1f ? (int) (55 * cumScale) : 0;

        if (!animatingCum) {
            guiGraphics.blit(READY_TO_CUM_TEXTURE,
                    10, y + texHeight + 5,
                    0, cumV,
                    cropWidth, cropHeight,
                    cumWidth, cumHeight);
        }

        // Fill
        int inset = 8;
        int fillWidth = texWidth - (inset * 2);
        int fillHeightMax = texHeight - (inset * 2);
        int fillX = x + inset;
        int baseFillBottom = y + texHeight - inset;

        if (animatingCum) {
            long elapsed = System.currentTimeMillis() - cumStartTime;
            float t = Math.min(elapsed / (float) CUM_ANIM_DURATION, 1f);

            int yOffset = (int) (texHeight * t * 2);
            int fillY = baseFillBottom - fillHeightMax - yOffset;

            guiGraphics.fill(fillX, fillY, fillX + fillWidth, baseFillBottom - yOffset, 0xFFFFFFFF);

            if (t >= 1f) {
                setActive(false);
                animatingCum = false;
            }
        } else {
            int filledHeight = (int) (ratio * fillHeightMax);
            if (filledHeight > 0) {
                guiGraphics.fill(fillX, baseFillBottom - filledHeight, fillX + fillWidth, baseFillBottom, 0xEFEFEFEF);
            }
        }
    }
}
