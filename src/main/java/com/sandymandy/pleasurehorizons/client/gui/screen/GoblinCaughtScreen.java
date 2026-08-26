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

    private final GoblinEntity goblin;
    private final Player player;

    public GoblinCaughtScreen(GoblinEntity goblin, Player player) {
        super(Component.translatable("gui.pleasurehorizons.goblin_caught.title"));
        this.goblin = goblin;
        this.player = player;
    }

    @Override
    protected void init() {
        int y = this.height / 4;

        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.pleasurehorizons.goblin_caught.return"),
                        b -> this.send("return"))
                .bounds(this.width / 2 - 100, y, 200, 20).build());
        y += 25;

        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.pleasurehorizons.goblin_caught.scene"),
                        b -> this.send("scene"))
                .bounds(this.width / 2 - 100, y, 200, 20).build());
        y += 25;

        Button queen = Button.builder(
                        Component.translatable("gui.pleasurehorizons.goblin_caught.queen"),
                        b -> this.send("make_queen"))
                .bounds(this.width / 2 - 100, y, 200, 20).build();
        queen.active = !this.goblin.isQueen() && !this.goblin.isTamed();
        this.addRenderableWidget(queen);
        y += 25;

        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.pleasurehorizons.goblin_caught.leave"),
                        b -> this.send("dismiss"))
                .bounds(this.width / 2 - 100, y, 200, 20).build());
    }

    private void send(String action) {
        PacketDistributor.sendToServer(new GoblinActionC2SPacket(this.goblin.getId(), action));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("gui.pleasurehorizons.goblin_caught.subtitle"),
                this.width / 2, this.height / 4 - 25, 0xFFDDCCDD);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
