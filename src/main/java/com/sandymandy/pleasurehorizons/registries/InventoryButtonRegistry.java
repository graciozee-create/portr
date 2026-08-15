package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.networking.C2S.InventoryButtonC2SPacket;
import com.sandymandy.pleasurehorizons.screen.InventoryButtonAction;
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
                    "gui.pleasurehorizons.button.breakUp", 0,
                    (girl, player) -> send(girl.getId(), "breakUp")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.setBase", 1,
                    (girl, player) -> send(girl.getId(), "setBase")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.goToBase", 1,
                    (girl, player) -> send(girl.getId(), "goToBase")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.customize", 1, true,
                    (girl, player) -> send(girl.getId(), "customize")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.guardBase", 1,
                    (girl, player) -> send(girl.getId(), "guardBase")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.guardOwner", 2,
                    (girl, player) -> send(girl.getId(), "guardOwner")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.stayNearBase", 1,
                    (girl, player) -> send(girl.getId(), "stayNearBase"))
    );

    public static final List<InventoryButtonAction> BUTTONS_RIGHT = List.of(
            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.sit", 2,
                    (girl, player) -> send(girl.getId(), "sit")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.follow", 3,
                    (girl, player) -> send(girl.getId(), "follow")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.strip", 4,
                    (girl, player) -> send(girl.getId(), "stripOrDressup")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.talk", 4, true,
                    (girl, player) -> send(girl.getId(), "talk")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.gather", 2,
                    (girl, player) -> send(girl.getId(), "gather")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.harvest", 2,
                    (girl, player) -> send(girl.getId(), "harvest"))
    );

    private static void send(int entityId, String actionId) {
        PacketDistributor.sendToServer(new InventoryButtonC2SPacket(entityId, actionId));
    }
}
