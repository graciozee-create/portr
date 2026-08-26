package com.sandymandy.pleasurehorizons.client.gui.screen;

import com.sandymandy.pleasurehorizons.networking.C2S.SetGUIOpenStateC2SPacket;
import com.sandymandy.pleasurehorizons.networking.C2S.StartSceneC2SPacket;
import com.sandymandy.pleasurehorizons.util.ScreenUtils;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

import static com.sandymandy.pleasurehorizons.util.PleasureHorizonsIcons.HEART_ICON;

/**
 * Scene selection screen, opened by the "Talk" inventory button.
 *
 * <p>Ported from Fabric: {@code ButtonWidget.builder(...).dimensions(...)} became
 * {@code Button.builder(...).bounds(...)}, {@code Tooltip.of} became {@link Tooltip#create},
 * {@code addDrawableChild} became {@code addRenderableWidget}, {@code close()} became
 * {@code onClose()}, {@code shouldPause} became {@code isPauseScreen} and
 * {@code ClientPlayNetworking.send} became {@link PacketDistributor#sendToServer}.</p>
 *
 * <p>The scene is identified by its display name over the wire; the server resolves it against
 * {@code girl.getScenes()} so a tampered client cannot inject an arbitrary scene definition.</p>
 */
@OnlyIn(Dist.CLIENT)
public class GirlSceneScreen extends Screen {
    private final int entityId;
    private final int currentRelationshipLevel;
    private final ItemStack attractedTo;
    private final List<Scene> scenes;

    public GirlSceneScreen(int entityId, int currentRelationshipLevel, ItemStack attractedTo, List<Scene> scenes) {
        super(Component.translatable("gui.pleasurehorizons.scene.title"));
        this.entityId = entityId;
        this.currentRelationshipLevel = currentRelationshipLevel;
        this.attractedTo = attractedTo;
        this.scenes = scenes;
    }

    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 24;

    @Override
    protected void init() {
        // The panel is centred and sized to the scene list (with a static cap), so a handful of
        // buttons no longer sprawl across the top half of the screen.
        int panelWidth = Math.min(280, this.width - 40);
        int panelHeight = Math.max(70, this.scenes.size() * BUTTON_SPACING + 40);
        int panelX = (this.width - panelWidth) / 2;
        int panelY = Math.max(20, (this.height - panelHeight) / 2 - 20);
        int buttonWidth = panelWidth - 20;
        int buttonX = panelX + 10;

        int y = panelY + 32;
        for (Scene scene : this.scenes) {
            Button button = Button.builder(sceneLabel(scene), b -> {
                PacketDistributor.sendToServer(new StartSceneC2SPacket(this.entityId, scene.displayName()));
                this.onClose();
            }).bounds(buttonX, y, buttonWidth, BUTTON_HEIGHT).build();

            if (this.currentRelationshipLevel < scene.requiredRelationshipLevel()) {
                button.active = false;
                button.setTooltip(Tooltip.create(Component.translatable(
                        "gui.pleasurehorizons.requires_relationship", scene.requiredRelationshipLevel())));
            }

            this.addRenderableWidget(button);
            y += BUTTON_SPACING;
        }

        if (this.scenes.isEmpty()) {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.pleasurehorizons.scene.none"),
                            b -> this.onClose())
                    .bounds(buttonX, y, buttonWidth, BUTTON_HEIGHT).build());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int panelWidth = Math.min(280, this.width - 40);
        int panelHeight = Math.max(70, this.scenes.size() * BUTTON_SPACING + 40);
        int centerX = this.width / 2;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = Math.max(20, (this.height - panelHeight) / 2 - 20);

        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0x66000000);
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0xFF664466);
        guiGraphics.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, 0xFF664466);

        guiGraphics.drawCenteredString(this.font,
                Component.translatable("gui.pleasurehorizons.scene.relationship", this.currentRelationshipLevel),
                centerX, panelY + 12, 0xFFFFDDEE);

        int iconY = panelY + 6;
        int itemX = centerX - 30;
        guiGraphics.renderItem(this.attractedTo, itemX, iconY);

        int heartX = centerX - 10;
        guiGraphics.blit(HEART_ICON, heartX, iconY, 0, 0, 18, 18, 18, 18);

        if (ScreenUtils.isMouseOverHere(mouseX, mouseY, itemX, iconY, 16, 16)) {
            guiGraphics.renderTooltip(this.font, this.attractedTo.getHoverName(), mouseX, mouseY);
        }
    }

    private Component sceneLabel(Scene scene) {
        String key = scene.localizedNameKey();
        return I18n.exists(key) ? Component.translatable(key) : Component.literal(scene.displayName());
    }

    @Override
    public void onClose() {
        super.onClose();
        PacketDistributor.sendToServer(new SetGUIOpenStateC2SPacket(this.entityId, false));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
