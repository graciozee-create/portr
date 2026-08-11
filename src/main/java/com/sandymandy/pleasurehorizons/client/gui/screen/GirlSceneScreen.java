package com.sandymandy.pleasurehorizons.client.gui.screen;

import com.sandymandy.pleasurehorizons.networking.C2S.SetGUIOpenStateC2SPacket;
import com.sandymandy.pleasurehorizons.networking.C2S.StartSceneC2SPacket;
import com.sandymandy.pleasurehorizons.util.ScreenUtils;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Colors;

import java.util.List;

import static com.sandymandy.pleasurehorizons.util.PleasureHorizonsIcons.HEART_ICON;

public class GirlSceneScreen extends Screen {
    private final int entityId;
    private final int currentRelationshipLevel;
    private final ItemStack attractedTo;
    private final List<Scene> scene;

    public GirlSceneScreen(int entityId, int currentRelationshipLevel, ItemStack attractedTo, List<Scene> scene) {
        super(Text.literal("Scene Options"));
        this.entityId = entityId;
        this.currentRelationshipLevel = currentRelationshipLevel;
        this.attractedTo = attractedTo;
        this.scene = scene;
    }

    @Override
    protected void init() {
        int y = this.height / 4;
        for (Scene scene : this.scene) {
            ButtonWidget buttonWidget = ButtonWidget.builder(Text.of(scene.displayName()), button -> {
                PacketDistributor.sendToServer(new StartSceneC2SPacket(
                        this.entityId,
                        scene
                ));
                this.close();
            }).dimensions(this.width / 2 - 100, y, 200, 20).build();

            if (this.currentRelationshipLevel < scene.requiredRelationshipLevel()) {
                buttonWidget.active = false; // disables and grays out
            }

            if (!buttonWidget.active) {
                buttonWidget.setTooltip(Tooltip.of(Text.literal("Requires Relationship Level " + scene.requiredRelationshipLevel())));
            }

            this.addDrawableChild(buttonWidget);
            y += 25;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Calculate base positions
        int iconY = this.height / 4 - 30;
        int centerX = this.width / 2;

        // Draw item (attracted to) first - positioned to the left
        int itemX = centerX - 30; // 30 pixels left of center
        context.drawItem(this.attractedTo, itemX, iconY);

        // Draw relationship icon after the item
        int heartX = centerX - 10; // 10 pixels left of center
        context.drawTexture(RenderPipelines.GUI_TEXTURED, HEART_ICON, heartX, iconY, 0, 0, 18, 18, 18, 18);

        // Draw the relationship level number next to the heart
        context.drawText(textRenderer,
                String.valueOf(currentRelationshipLevel),
                heartX + 20, iconY + 4, Colors.WHITE, true);

        if (ScreenUtils.isMouseOverHere(mouseX, mouseY, itemX, iconY, 16, 16)) {
            // Draw tooltip with item name
            context.drawTooltip(textRenderer,
                    this.attractedTo.getName(),
                    mouseX, mouseY);
        }
    }

    @Override
    public void close() {
        super.close();
        PacketDistributor.sendToServer(new SetGUIOpenStateC2SPacket(this.entityId,false));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
