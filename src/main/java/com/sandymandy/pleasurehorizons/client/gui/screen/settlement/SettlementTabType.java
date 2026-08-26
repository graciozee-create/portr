package com.sandymandy.pleasurehorizons.client.gui.screen.settlement;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placement/skin of a settlement hub tab, mirroring vanilla {@code AdvancementTabType}.
 *
 * <p>Ported from Fabric: {@code Identifier.ofVanilla} became
 * {@link ResourceLocation#withDefaultNamespace}, {@code DrawContext} became {@link GuiGraphics} and
 * {@code drawGuiTexture(RenderPipelines.GUI_TEXTURED, ...)} became {@code blitSprite}, which is the
 * 1.21.1 way of drawing a GUI sprite.</p>
 */
@OnlyIn(Dist.CLIENT)
public enum SettlementTabType {
    ABOVE(
            new Textures(
                    ResourceLocation.withDefaultNamespace("advancements/tab_above_left_selected"),
                    ResourceLocation.withDefaultNamespace("advancements/tab_above_middle_selected"),
                    ResourceLocation.withDefaultNamespace("advancements/tab_above_right_selected")
            ),
            new Textures(
                    ResourceLocation.withDefaultNamespace("advancements/tab_above_left"),
                    ResourceLocation.withDefaultNamespace("advancements/tab_above_middle"),
                    ResourceLocation.withDefaultNamespace("advancements/tab_above_right")
            ),
            28, 32, 8
    ),
    BELOW(
            new Textures(
                    ResourceLocation.withDefaultNamespace("advancements/tab_below_left_selected"),
                    ResourceLocation.withDefaultNamespace("advancements/tab_below_middle_selected"),
                    ResourceLocation.withDefaultNamespace("advancements/tab_below_right_selected")
            ),
            new Textures(
                    ResourceLocation.withDefaultNamespace("advancements/tab_below_left"),
                    ResourceLocation.withDefaultNamespace("advancements/tab_below_middle"),
                    ResourceLocation.withDefaultNamespace("advancements/tab_below_right")
            ),
            28, 32, 8
    ),
    LEFT(
            new Textures(
                    ResourceLocation.withDefaultNamespace("advancements/tab_left_top_selected"),
                    ResourceLocation.withDefaultNamespace("advancements/tab_left_middle_selected"),
                    ResourceLocation.withDefaultNamespace("advancements/tab_left_bottom_selected")
            ),
            new Textures(
                    ResourceLocation.withDefaultNamespace("advancements/tab_left_top"),
                    ResourceLocation.withDefaultNamespace("advancements/tab_left_middle"),
                    ResourceLocation.withDefaultNamespace("advancements/tab_left_bottom")
            ),
            32, 28, 5
    ),
    RIGHT(
            new Textures(
                    ResourceLocation.withDefaultNamespace("advancements/tab_right_top_selected"),
                    ResourceLocation.withDefaultNamespace("advancements/tab_right_middle_selected"),
                    ResourceLocation.withDefaultNamespace("advancements/tab_right_bottom_selected")
            ),
            new Textures(
                    ResourceLocation.withDefaultNamespace("advancements/tab_right_top"),
                    ResourceLocation.withDefaultNamespace("advancements/tab_right_middle"),
                    ResourceLocation.withDefaultNamespace("advancements/tab_right_bottom")
            ),
            32, 28, 5
    );

    private final Textures selected;
    private final Textures unselected;
    private final int width;
    private final int height;
    private final int tabCount;

    SettlementTabType(Textures selected, Textures unselected, int width, int height, int tabCount) {
        this.selected = selected;
        this.unselected = unselected;
        this.width = width;
        this.height = height;
        this.tabCount = tabCount;
    }

    public int getTabCount() {
        return this.tabCount;
    }

    public void drawBackground(GuiGraphics guiGraphics, int x, int y, boolean isSelected, int index) {
        Textures tex = isSelected ? this.selected : this.unselected;
        ResourceLocation texture;
        if (index == 0) {
            texture = tex.first();
        } else if (index == this.tabCount - 1) {
            texture = tex.last();
        } else {
            texture = tex.middle();
        }

        guiGraphics.blitSprite(texture, x + getTabX(index), y + getTabY(index), this.width, this.height);
    }

    public void drawIcon(GuiGraphics guiGraphics, int x, int y, int index, ItemStack stack) {
        int i = x + this.getTabX(index);
        int j = y + this.getTabY(index);
        switch (this) {
            case ABOVE -> {
                i += 6;
                j += 9;
            }
            case BELOW -> {
                i += 6;
                j += 6;
            }
            case LEFT -> {
                i += 10;
                j += 5;
            }
            case RIGHT -> {
                i += 6;
                j += 5;
            }
        }

        guiGraphics.renderFakeItem(stack, i, j);
    }

    public int getTabX(int index) {
        return switch (this) {
            case ABOVE, BELOW -> (this.width + 4) * index;
            case LEFT -> -this.width + 4;
            case RIGHT -> 248;
        };
    }

    public int getTabY(int index) {
        return switch (this) {
            case ABOVE -> -this.height + 4;
            case BELOW -> 136;
            case LEFT, RIGHT -> this.height * index;
        };
    }

    public boolean isClickOnTab(int baseX, int baseY, int index, double mouseX, double mouseY) {
        int i = baseX + getTabX(index);
        int j = baseY + getTabY(index);
        return mouseX > i && mouseX < i + this.width && mouseY > j && mouseY < j + this.height;
    }

    @OnlyIn(Dist.CLIENT)
    public record Textures(ResourceLocation first, ResourceLocation middle, ResourceLocation last) {
    }
}
