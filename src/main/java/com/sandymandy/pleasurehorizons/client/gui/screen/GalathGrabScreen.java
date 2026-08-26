package com.sandymandy.pleasurehorizons.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.sandymandy.pleasurehorizons.entity.girls.GalathEntity;
import com.sandymandy.pleasurehorizons.networking.C2S.GalathGrabTapsC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Escape screen shown while Galath has grabbed the player in combat.
 *
 * <p>Player must rapidly tap A/D to fill the 60-point escape bar before 8 seconds run out.
 * Alternating between A and D gives a +2 bonus. Taps are batched to the server so the
 * release decision stays authoritative there.</p>
 */
@OnlyIn(Dist.CLIENT)
public class GalathGrabScreen extends Screen {

    private static final int MAX_ESCAPE = 60;
    private static final int MAX_TICKS = 160;

    private static final int COLOR_BG = 0xE8221122;
    private static final int COLOR_BORDER = 0xFFAA4466;
    private static final int COLOR_BAR_BG = 0xFF331133;
    private static final int COLOR_BAR_FILL = 0xFFFF4466;
    private static final int COLOR_BAR_HIGH = 0xFF44FF66;
    private static final int COLOR_TEXT = 0xFFDDCCDD;
    private static final int COLOR_DANGER = 0xFFFF2244;
    private static final int COLOR_WARNING = 0xFFFFAA44;
    private static final int COLOR_ACCENT = 0xFFCC6688;

    private final GalathEntity galath;
    private final Player player;

    private int escapeProgress = 0;
    private int tickCounter = 0;
    private boolean keyDown = false;
    private int lastKey = 0;
    private String feedbackKey = "";
    private int feedbackTimer = 0;
    private int lastSentProgress = 0;

    public GalathGrabScreen(GalathEntity galath, Player player) {
        super(Component.translatable("gui.pleasurehorizons.grab.title"));
        this.galath = galath;
        this.player = player;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // No-op: the screen draws its own dim overlay; the vanilla blurred menu must not stack.
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float intensity = 0.4F + 0.3F * (1.0F - (float) escapeProgress / MAX_ESCAPE);
        int alpha = Math.min(255, (int) (intensity * 255));
        guiGraphics.fill(0, 0, this.width, this.height, (alpha << 24) | 0x330000);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int panelW = 280;
        int panelH = 140;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        guiGraphics.fill(panelX - 2, panelY - 2, panelX + panelW + 2, panelY + panelH + 2, COLOR_BORDER);
        guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, COLOR_BG);

        String title = Component.translatable("gui.pleasurehorizons.grab.title").getString();
        guiGraphics.drawString(this.font, Component.translatable("gui.pleasurehorizons.grab.title"),
                panelX + (panelW - this.font.width(title)) / 2, panelY + 10, 0xFFFF6688, true);

        String instruction = Component.translatable("gui.pleasurehorizons.grab.instructions").getString();
        guiGraphics.drawString(this.font, Component.translatable("gui.pleasurehorizons.grab.instructions"),
                panelX + (panelW - this.font.width(instruction)) / 2, panelY + 28, COLOR_WARNING, true);

        int barX = panelX + 20;
        int barY = panelY + 50;
        int barW = panelW - 40;
        int barH = 18;
        float progress = (float) escapeProgress / MAX_ESCAPE;

        guiGraphics.fill(barX, barY, barX + barW, barY + barH, COLOR_BAR_BG);
        int fillColor = progress > 0.6F ? COLOR_BAR_HIGH : COLOR_BAR_FILL;
        int fillW = (int) (barW * progress);
        if (fillW > 0) {
            guiGraphics.fill(barX, barY, barX + fillW, barY + barH, fillColor);
            guiGraphics.fill(barX, barY, barX + fillW, barY + barH / 2, 0x40FFFFFF);
        }
        guiGraphics.fill(barX, barY, barX + barW, barY + 1, COLOR_BORDER);
        guiGraphics.fill(barX, barY + barH - 1, barX + barW, barY + barH, COLOR_BORDER);

        String progressText = escapeProgress + " / " + MAX_ESCAPE;
        guiGraphics.drawCenteredString(this.font, progressText, barX + barW / 2, barY + 4, 0xFFFFFFFF);

        int remaining = (MAX_TICKS - tickCounter) / 20;
        Component timeText = remaining > 0
                ? Component.translatable("gui.pleasurehorizons.grab.warning", remaining)
                : Component.translatable("gui.pleasurehorizons.grab.too_late");
        int timeColor = remaining > 3 ? COLOR_TEXT : COLOR_DANGER;
        guiGraphics.drawCenteredString(this.font, timeText, barX + barW / 2, barY + barH + 8, timeColor);

        if (feedbackTimer > 0 && !feedbackKey.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(feedbackKey),
                    panelX + panelW / 2, panelY + panelH - 20, COLOR_ACCENT);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.tickCounter++;

        boolean aDown = InputConstants.isKeyDown(
                Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_A);
        boolean dDown = InputConstants.isKeyDown(
                Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_D);
        boolean anyDown = aDown || dDown;

        if (anyDown && !this.keyDown) {
            int tappedKey = aDown ? 1 : 2;
            int bonus = this.lastKey != 0 && tappedKey != this.lastKey ? 1 : 0;

            this.escapeProgress += 1 + bonus;
            this.lastKey = tappedKey;
            this.keyDown = true;

            if (bonus > 0) {
                this.feedbackKey = "gui.pleasurehorizons.grab.feedback_alt";
            } else {
                this.feedbackKey = aDown
                        ? "gui.pleasurehorizons.grab.feedback_a"
                        : "gui.pleasurehorizons.grab.feedback_d";
            }
            this.feedbackTimer = 10;

            if (this.escapeProgress > MAX_ESCAPE) this.escapeProgress = MAX_ESCAPE;

            int newTaps = this.escapeProgress - this.lastSentProgress;
            if (newTaps >= 5 || this.escapeProgress >= MAX_ESCAPE) {
                PacketDistributor.sendToServer(new GalathGrabTapsC2SPacket(this.galath.getId(), newTaps));
                this.lastSentProgress = this.escapeProgress;
            }
        } else if (!anyDown) {
            this.keyDown = false;
        }

        if (this.feedbackTimer > 0) this.feedbackTimer--;

        if (this.escapeProgress >= MAX_ESCAPE) {
            int finalTaps = this.escapeProgress - this.lastSentProgress;
            if (finalTaps > 0) {
                PacketDistributor.sendToServer(new GalathGrabTapsC2SPacket(this.galath.getId(), finalTaps));
            }
            this.onClose();
        }

        if (!this.galath.isAlive()) {
            this.onClose();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_A || keyCode == GLFW.GLFW_KEY_D
                || keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER) {
            return true; // Let tick() handle detection
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return true; // Consume all movement input while grabbed
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return true; // Consume all clicks
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}
