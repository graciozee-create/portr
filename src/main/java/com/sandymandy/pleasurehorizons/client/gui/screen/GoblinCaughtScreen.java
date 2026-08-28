package com.sandymandy.pleasurehorizons.client.gui.screen;

import com.sandymandy.pleasurehorizons.entity.girls.GoblinEntity;
import com.sandymandy.pleasurehorizons.networking.C2S.GoblinActionC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Catch screen shown when a player right-clicks a wild goblin carrying stolen gold.
 *
 * <p>Options: return your stuff, start her special scene, make her your queen, or walk away.
 * Choices are sent to the server so the goblin remains authoritative.</p>
 */
@OnlyIn(Dist.CLIENT)
public class GoblinCaughtScreen extends Screen {

    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 24;

    private final GoblinEntity goblin;
    private final Player player;

    public GoblinCaughtScreen(GoblinEntity goblin, Player player) {
        super(Component.translatable("gui.pleasurehorizons.goblin_caught.title"));
        this.goblin = goblin;
        this.player = player;
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(240, this.width - 40);
        int buttonWidth = panelWidth - 20;
        int panelHeight = 4 * BUTTON_SPACING + 44;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = Math.max(20, (this.height - panelHeight) / 2);

        int y = panelY + 30;
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.pleasurehorizons.goblin_caught.return"),
                        b -> this.send("return"))
                .bounds(panelX + 10, y, buttonWidth, BUTTON_HEIGHT).build());
        y += BUTTON_SPACING;

        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.pleasurehorizons.goblin_caught.scene"),
                        b -> this.send("scene"))
                .bounds(panelX + 10, y, buttonWidth, BUTTON_HEIGHT).build());
        y += BUTTON_SPACING;

        Button queen = Button.builder(
                        Component.translatable("gui.pleasurehorizons.goblin_caught.queen"),
                        b -> this.send("make_queen"))
                .bounds(panelX + 10, y, buttonWidth, BUTTON_HEIGHT).build();
        queen.active = !this.goblin.isQueen() && !this.goblin.isTamed();
        this.addRenderableWidget(queen);
        y += BUTTON_SPACING;

        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.pleasurehorizons.goblin_caught.leave"),
                        b -> this.send("dismiss"))
                .bounds(panelX + 10, y, buttonWidth, BUTTON_HEIGHT).build());
    }

    private void send(String action) {
        PacketDistributor.sendToServer(new GoblinActionC2SPacket(this.goblin.getId(), action));
        this.onClose();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // No-op: the screen draws its own dim overlay; the vanilla blurred menu must not stack.
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int panelWidth = Math.min(240, this.width - 40);
        int panelHeight = 4 * BUTTON_SPACING + 44;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = Math.max(20, (this.height - panelHeight) / 2);

        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0x66000000);
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0xFF664466);
        guiGraphics.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, 0xFF664466);

        guiGraphics.drawCenteredString(this.font,
                Component.translatable("gui.pleasurehorizons.goblin_caught.subtitle"),
                this.width / 2, panelY + 8, 0xFFDDCCDD);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
