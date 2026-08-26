package com.sandymandy.pleasurehorizons.client.gui.screen;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.networking.C2S.InteractionActionC2SPacket;
import com.sandymandy.pleasurehorizons.networking.C2S.SetGUIOpenStateC2SPacket;
import com.sandymandy.pleasurehorizons.relationship.AffectionData;
import com.sandymandy.pleasurehorizons.relationship.DialogueDB;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Unified dialogue/action screen for an owned girl.
 *
 * <p>Shows her name, affection and relationship tier, a greeting line, and quick actions
 * (follow/stay, sit/stand, scene picker). The heavier management actions (rename, quests,
 * send home, drop loot, work toggles) stay in the inventory / NPC editor so this panel stays
 * small and readable.</p>
 */
@OnlyIn(Dist.CLIENT)
public class InteractionScreen extends Screen {
    private static final int PANEL_W = 240;
    private static final int PANEL_H = 280;
    private static final int BTN_H = 20;
    private static final int SPACING = 24;

    private final TameableGirlEntity girl;
    private String greetingKey = "";

    private static final int COLOR_PANEL = 0xD0331133;
    private static final int COLOR_BORDER = 0xFF664466;
    private static final int COLOR_TEXT = 0xFFDDCCDD;
    private static final int COLOR_TEXT_DIM = 0xFF887788;
    private static final int COLOR_HEADER = 0xFFFF88CC;
    private static final int COLOR_HEART = 0xFFEE4466;
    private static final int COLOR_HEART_BG = 0xFF442233;

    private final List<Scene> scenes;

    public InteractionScreen(TameableGirlEntity girl) {
        super(Component.translatable("gui.pleasurehorizons.interaction.title"));
        this.girl = girl;
        this.scenes = girl.getScenes();
        refreshGreeting();
    }

    private void refreshGreeting() {
        AffectionData.AffectionLevel level = AffectionData.levelFor(this.girl.getAffection());
        this.greetingKey = DialogueDB.greetingKey(this.girl.getGirlID(), level);
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int panelX = cx - PANEL_W / 2;
        int panelY = Math.max(16, cy - PANEL_H / 2);
        int buttonX = panelX + 10;
        int buttonW = PANEL_W - 20;

        int y = panelY + 62;
        this.addRenderableWidget(Button.builder(
                Component.translatable(this.girl.isFollowing()
                        ? "gui.pleasurehorizons.interaction.stop_follow"
                        : "gui.pleasurehorizons.interaction.follow"),
                b -> this.send("follow")).bounds(buttonX, y, buttonW, BTN_H).build());
        y += SPACING;

        this.addRenderableWidget(Button.builder(
                Component.translatable(this.girl.isSitting()
                        ? "gui.pleasurehorizons.interaction.stand"
                        : "gui.pleasurehorizons.interaction.sit"),
                b -> this.send("sit")).bounds(buttonX, y, buttonW, BTN_H).build());
        y += SPACING;

        if (!this.scenes.isEmpty()) {
            this.addRenderableWidget(Button.builder(
                    Component.translatable("gui.pleasurehorizons.interaction.scenes"),
                    b -> this.openScenes()).bounds(buttonX, y, buttonW, BTN_H).build());
            y += SPACING;
        }

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.pleasurehorizons.interaction.talk"),
                b -> this.refreshGreeting()).bounds(buttonX, y, buttonW, BTN_H).build());
        y += SPACING;

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.pleasurehorizons.interaction.inventory"),
                b -> this.openInventory()).bounds(buttonX, y, buttonW, BTN_H).build());
        y += SPACING;

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.pleasurehorizons.interaction.close"),
                b -> this.onClose()).bounds(buttonX, y, buttonW, BTN_H).build());
    }

    private void send(String action) {
        PacketDistributor.sendToServer(new InteractionActionC2SPacket(this.girl.getId(), action));
    }

    private void openScenes() {
        if (this.scenes.isEmpty()) return;
        PacketDistributor.sendToServer(new SetGUIOpenStateC2SPacket(this.girl.getId(), false));
        net.minecraft.client.Minecraft.getInstance().setScreen(new GirlSceneScreen(
                this.girl.getId(), this.girl.getCurrentRelationshipLevel(),
                new net.minecraft.world.item.ItemStack(this.girl.isAttractedTo()), this.scenes));
    }

    private void openInventory() {
        // The server opens the container menu; the server-side handler on the interaction
        // action is intentionally not used here because the inventory isn't a network action.
        // Closing this screen leaves the girl's GUI-open state reset for the next interaction.
        this.onClose();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0x88000000);
        super.render(g, mouseX, mouseY, partialTick);

        int cx = this.width / 2;
        int cy = this.height / 2;
        int panelX = cx - PANEL_W / 2;
        int panelY = Math.max(16, cy - PANEL_H / 2);

        g.fill(panelX - 2, panelY - 2, panelX + PANEL_W + 2, panelY + PANEL_H + 2, COLOR_BORDER);
        g.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, COLOR_PANEL);

        // Header: name + affection tier.
        g.drawCenteredString(this.font,
                Component.translatable("gui.pleasurehorizons.interaction.header", this.girl.getGirlDisplayName()),
                cx, panelY + 8, COLOR_HEADER);
        int level = this.girl.getCurrentRelationshipLevel();
        int levelMax = this.girl.maxRelationshipLevel();
        g.drawCenteredString(this.font,
                Component.translatable("gui.pleasurehorizons.interaction.relationship",
                        characterHeart(level, levelMax), level, levelMax),
                cx, panelY + 20, COLOR_TEXT);

        // Affection bar.
        int aff = this.girl.getAffection();
        int barX = panelX + 20;
        int barY = panelY + 36;
        int barW = PANEL_W - 40;
        int barH = 8;
        int fillW = (int) ((float) aff / AffectionData.MAX_AFFECTION * barW);
        g.fill(barX, barY, barX + barW, barY + barH, COLOR_HEART_BG);
        if (fillW > 0) {
            g.fill(barX, barY, barX + fillW, barY + barH, COLOR_HEART);
            g.fill(barX, barY, barX + fillW, barY + barH / 2, 0x40FFFFFF);
        }
        // Greeting line.
        List<String> lines = wrapGreeting();
        int textY = barY + barH + 6;
        for (String line : lines) {
            g.drawString(this.font, line, panelX + 14, textY, COLOR_TEXT, true);
            textY += 11;
        }
    }

    private List<String> wrapGreeting() {
        String text = Component.translatable(this.greetingKey).getString();
        int maxW = PANEL_W - 28;
        List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (this.font.width(current + " " + word) > maxW && !current.isEmpty()) {
                lines.add(current.toString().trim());
                current.setLength(0);
            }
            if (!current.isEmpty()) current.append(" ");
            current.append(word);
        }
        if (!current.isEmpty()) lines.add(current.toString().trim());
        if (lines.isEmpty()) lines.add(text);
        return lines;
    }

    private static String characterHeart(int value, int max) {
        if (max <= 0) {
            return "\u2665";
        }
        int filled = Math.max(0, Math.min(max, value));
        return "\u2665".repeat(filled) + "\u2661".repeat(max - filled);
    }

    @Override
    public void onClose() {
        super.onClose();
        PacketDistributor.sendToServer(new SetGUIOpenStateC2SPacket(this.girl.getId(), false));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
