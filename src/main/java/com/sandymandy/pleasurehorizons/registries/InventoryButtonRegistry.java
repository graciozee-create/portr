package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.networking.C2S.InventoryButtonC2SPacket;
import com.sandymandy.pleasurehorizons.screen.InventoryButtonAction;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * The buttons shown in the girl inventory screen.
 *
 * <p>Every action previously had an empty body, so the buttons did nothing at all.
 * They now send {@link InventoryButtonC2SPacket} to the server, which applies the
 * change. The Fabric original used {@code ClientPlayNetworking.send}; the NeoForge
 * equivalent is {@link PacketDistributor#sendToServer}.</p>
 */
public class InventoryButtonRegistry {

    public static final List<InventoryButtonAction> BUTTONS_LEFT = List.of(
            new InventoryButtonAction(
                    Component.translatable("gui.pleasurehorizons.button.breakUp"), 0,
                    (girl, player) -> send(girl.getId(), "breakUp")),

            new InventoryButtonAction(
                    Component.translatable("gui.pleasurehorizons.button.setBase"), 1,
                    (girl, player) -> send(girl.getId(), "setBase")),

            new InventoryButtonAction(
                    Component.translatable("gui.pleasurehorizons.button.goToBase"), 1,
                    (girl, player) -> send(girl.getId(), "goToBase")),

            new InventoryButtonAction(
                    Component.translatable("gui.pleasurehorizons.button.customize"), 1,
                    (girl, player) -> send(girl.getId(), "customize"))
    );

    public static final List<InventoryButtonAction> BUTTONS_RIGHT = List.of(
            new InventoryButtonAction(
                    Component.translatable("gui.pleasurehorizons.button.sit"), 2,
                    (girl, player) -> send(girl.getId(), "sit")),

            new InventoryButtonAction(
                    Component.translatable("gui.pleasurehorizons.button.follow"), 3,
                    (girl, player) -> send(girl.getId(), "follow")),

            new InventoryButtonAction(
                    Component.translatable("gui.pleasurehorizons.button.strip"), 4,
                    (girl, player) -> send(girl.getId(), "stripOrDressup")),

            new InventoryButtonAction(
                    Component.translatable("gui.pleasurehorizons.button.talk"), 4,
                    (girl, player) -> send(girl.getId(), "talk"))
    );

    private static void send(int entityId, String actionId) {
        PacketDistributor.sendToServer(new InventoryButtonC2SPacket(entityId, actionId));
    }
}
