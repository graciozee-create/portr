package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.networking.C2S.InventoryButtonC2SPacket;
import com.sandymandy.pleasurehorizons.screen.InventoryButtonAction;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * The buttons shown in the girl inventory screen.
 *
 * <p>The screen is a single large menu (no tabs): every action is visible at once, grouped
 * into three sections - relationship/comfort actions, AI behaviours and per-girl settings.
 *
 * <p>Every action sends {@link InventoryButtonC2SPacket} to the server, which applies the
 * change. The Fabric original used {@code ClientPlayNetworking.send}; the NeoForge
 * equivalent is {@link PacketDistributor#sendToServer}.</p>
 */
public class InventoryButtonRegistry {

    /** Relationship and comfort actions (formerly the "Main" tab). */
    public static final List<InventoryButtonAction> BUTTONS_MAIN = List.of(
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
                    (girl, player) -> send(girl.getId(), "talk"))
    );

    /** AI behaviours that make her useful around a base (formerly the "Survival" tab). */
    public static final List<InventoryButtonAction> BUTTONS_BEHAVIOR = List.of(
            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.guardBase", 1,
                    (girl, player) -> send(girl.getId(), "guardBase")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.guardOwner", 2,
                    (girl, player) -> send(girl.getId(), "guardOwner")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.stayNearBase", 1,
                    (girl, player) -> send(girl.getId(), "stayNearBase")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.gather", 2,
                    (girl, player) -> send(girl.getId(), "gather")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.harvest", 2,
                    (girl, player) -> send(girl.getId(), "harvest")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.cook", 3,
                    (girl, player) -> send(girl.getId(), "cook")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.cycleRole", 2,
                    (girl, player) -> send(girl.getId(), "cycleRole")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.chopTrees", 2,
                    (girl, player) -> send(girl.getId(), "chopTrees")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.hunt", 3,
                    (girl, player) -> send(girl.getId(), "hunt")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.feedOwner", 3,
                    (girl, player) -> send(girl.getId(), "feedOwner")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.dropLoot", 1,
                    (girl, player) -> send(girl.getId(), "dropLoot"))
    );

    /**
     * Fine-tuning per-girl settings (formerly the "Settings" tab). Every entry passes
     * {@code opensSubscreen = true} to keep the screen OPEN after a click - closing after
     * every single knob would make adjusting twelve settings miserable. The button labels
     * are rendered stateful by {@code GirlInventoryScreen#dynamicLabel}.
     */
    public static final List<InventoryButtonAction> BUTTONS_SETTINGS = List.of(
            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.followTeleport", 2, true,
                    (girl, player) -> send(girl.getId(), "followTeleport")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.followDistance", 1, true,
                    (girl, player) -> send(girl.getId(), "followDistance")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.workPace", 1, true,
                    (girl, player) -> send(girl.getId(), "workPace")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.workRadius", 1, true,
                    (girl, player) -> send(girl.getId(), "workRadius")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.guardRange", 1, true,
                    (girl, player) -> send(girl.getId(), "guardRange")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.closeDoors", 1, true,
                    (girl, player) -> send(girl.getId(), "closeDoors")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.avoidWater", 1, true,
                    (girl, player) -> send(girl.getId(), "avoidWater")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.stayRadius", 1, true,
                    (girl, player) -> send(girl.getId(), "stayRadius")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.autoDeliver", 1, true,
                    (girl, player) -> send(girl.getId(), "autoDeliver")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.autoEquipArmor", 1, true,
                    (girl, player) -> send(girl.getId(), "autoEquipArmor")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.avoidCreepers", 1, true,
                    (girl, player) -> send(girl.getId(), "avoidCreepers")),

            new InventoryButtonAction(
                    "gui.pleasurehorizons.button.highJump", 1, true,
                    (girl, player) -> send(girl.getId(), "highJump"))
    );

    private static void send(int entityId, String actionId) {
        PacketDistributor.sendToServer(new InventoryButtonC2SPacket(entityId, actionId));
    }
}
