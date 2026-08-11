package com.sandymandy.pleasurehorizons.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class PleasureHorizonsMessages {
    public static void GlobleMessage(World world, Component message) {
        if (world.isClient()) return;

        Objects.requireNonNull(world.getServer())
                .getPlayerManager()
                .broadcast(message, false);
    }

    public static void GlobleMessage(World world, String message) {
        GlobleMessage(world, Component.literal(message));
    }

    public static void PlayerSpecificMessage(Player playerEntity, String messageContent){
        Component message = Component.literal(messageContent);
        playerEntity.sendMessage(message,false);
    }

}
