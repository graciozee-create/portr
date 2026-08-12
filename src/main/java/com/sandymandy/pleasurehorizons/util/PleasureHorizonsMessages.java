package com.sandymandy.pleasurehorizons.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Small chat helpers.
 *
 * <p>Both methods were empty stubs, so anything routed through them vanished silently.</p>
 */
public class PleasureHorizonsMessages {

    private PleasureHorizonsMessages() {
    }

    /** Sends to every player on the server; server side only. */
    public static void globalMessage(Level level, Component message) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        for (ServerPlayer player : serverLevel.players()) {
            player.displayClientMessage(message, false);
        }
    }

    public static void globalMessage(Level level, String message) {
        globalMessage(level, Component.literal(message));
    }

    public static void playerSpecificMessage(Player player, Component message) {
        if (player == null) return;
        player.displayClientMessage(message, false);
    }

    public static void playerSpecificMessage(Player player, String message) {
        playerSpecificMessage(player, Component.literal(message));
    }

    // Legacy spellings kept so existing call sites still compile.
    public static void GlobleMessage(Level level, Component message) {
        globalMessage(level, message);
    }

    public static void GlobleMessage(Level level, String message) {
        globalMessage(level, message);
    }

    public static void PlayerSpecificMessage(Player player, String message) {
        playerSpecificMessage(player, message);
    }
}
