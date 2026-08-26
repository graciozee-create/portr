package com.sandymandy.pleasurehorizons.client.gui.screen;

import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import com.sandymandy.pleasurehorizons.networking.C2S.NpcEditC2SPacket;
import com.sandymandy.pleasurehorizons.relationship.AffectionData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * NPC Editor opened by the Girl Wand.
 *
 * <p>All actions are sent to the server ({@link NpcEditC2SPacket}) and the server re-validates
 * ownership, so the client screen is only a view. Buttons: talk, +10 affection, send home,
 * accept/complete quest, rename.</p>
 */
@OnlyIn(Dist.CLIENT)
public class NpcEditorScreen extends Screen {
    private final GirlEntity girl;
    private EditBox nameBox;

    private static final int COLOR_BORDER = 0xFF664466;
    private static final int COLOR_PANEL = 0xD0331133;
    private static final int COLOR_TEXT = 0xFFDDCCDD;
    private static final int COLOR_HEADER = 0xFFFF88CC;

    public NpcEditorScreen(GirlEntity girl) {
        super(Component.translatable("gui.pleasurehorizons.npc_editor.title"));
        this.girl = girl;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int bw = 140;
        int bx = cx - bw / 2;

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.pleasurehorizons.npc_editor.talk"),
                b -> this.send(NpcEditC2SPacket.Action.TALK, "")).bounds(bx, cy - 96, bw, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.pleasurehorizons.npc_editor.affection"),
                b -> this.send(NpcEditC2SPacket.Action.ADD_AFFECTION, "")).bounds(bx, cy - 72, bw, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.pleasurehorizons.npc_editor.home"),
                b -> this.send(NpcEditC2SPacket.Action.GO_HOME, "")).bounds(bx, cy - 48, bw, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.pleasurehorizons.npc_editor.accept_quest"),
                b -> this.send(NpcEditC2SPacket.Action.QUEST_ACCEPT, "")).bounds(bx, cy - 24, bw, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.pleasurehorizons.npc_editor.complete_quest"),
                b -> this.send(NpcEditC2SPacket.Action.QUEST_COMPLETE, "")).bounds(bx, cy, bw, 20).build());

        this.nameBox = new EditBox(this.font, bx, cy + 28, bw, 20,
                Component.translatable("gui.pleasurehorizons.npc_editor.name_hint"));
        this.nameBox.setMaxLength(32);
        this.addRenderableWidget(this.nameBox);

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.pleasurehorizons.npc_editor.rename"),
                b -> this.send(NpcEditC2SPacket.Action.RENAME, this.nameBox.getValue())).bounds(bx, cy + 52, bw, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.pleasurehorizons.npc_editor.close"),
                b -> this.onClose()).bounds(bx, cy + 80, bw, 20).build());
    }

    private void send(NpcEditC2SPacket.Action action, String value) {
        PacketDistributor.sendToServer(new NpcEditC2SPacket(this.girl.getId(), action, value));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);
        int cx = this.width / 2;
        int cy = this.height / 2;
        g.fill(cx - 90, cy - 112, cx + 90, cy + 116, COLOR_BORDER);
        g.fill(cx - 88, cy - 110, cx + 88, cy + 114, COLOR_PANEL);

        g.drawCenteredString(this.font,
                Component.translatable("gui.pleasurehorizons.npc_editor.header", this.girlName()),
                cx, cy - 104, COLOR_HEADER);
        g.drawCenteredString(this.font,
                Component.translatable("gui.pleasurehorizons.npc_editor.affection_display",
                        this.girl.getAffection(),
                        Component.translatable(AffectionData.levelFor(this.girl.getAffection()).labelKey)),
                cx, cy - 88, COLOR_TEXT);
        super.render(g, mouseX, mouseY, partialTick);
    }

    private String girlName() {
        if (this.girl.hasCustomName()) {
            return this.girl.getCustomName().getString();
        }
        String id = this.girl.getGirlID();
        return id.isEmpty() ? "Girl" : Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
