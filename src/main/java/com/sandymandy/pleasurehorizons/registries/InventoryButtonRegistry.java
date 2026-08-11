package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.networking.C2S.InventoryButtonC2SPacket;
import com.sandymandy.pleasurehorizons.screen.InventoryButtonAction;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.network.chat.Component;

import java.util.List;

public class InventoryButtonRegistry {
    public static final List<InventoryButtonAction> BUTTONS_LEFT = List.of(
            new InventoryButtonAction(Component.literal("Break Up"), 0,(girl, player) -> {
                PacketDistributor.sendToServer(new InventoryButtonC2SPacket(girl.getId(), "breakUp"));
            }),

            new InventoryButtonAction(Component.literal("Set Base Here"), 1,(girl, player) -> {
                PacketDistributor.sendToServer(new InventoryButtonC2SPacket(girl.getId(), "setBase"));
            }),

            new InventoryButtonAction(Component.literal("Go To Base"), 1,(girl, player) -> {
                PacketDistributor.sendToServer(new InventoryButtonC2SPacket(girl.getId(), "goToBase"));
            }),

            new InventoryButtonAction(Component.literal("Customize"), 1,(girl, player) -> {
                PacketDistributor.sendToServer(new InventoryButtonC2SPacket(girl.getId(), "customize"));
            })
    );

    public static final List<InventoryButtonAction> BUTTONS_RIGHT = List.of(

            new InventoryButtonAction(Component.literal("Sit"), 2,(girl, player) -> {
                PacketDistributor.sendToServer(new InventoryButtonC2SPacket(girl.getId(), "sit"));
            }),

            new InventoryButtonAction(Component.literal("Follow Me"), 3,(girl, player) -> {
                PacketDistributor.sendToServer(new InventoryButtonC2SPacket(girl.getId(), "follow"));
            }),

            new InventoryButtonAction(Component.literal("Strip"), 4,(girl, player) -> {
                PacketDistributor.sendToServer(new InventoryButtonC2SPacket(girl.getId(), "stripOrDressup"));
            }),

            new InventoryButtonAction(Component.literal("Talk"),4 , (girl, player) -> {
                PacketDistributor.sendToServer(new InventoryButtonC2SPacket(girl.getId(), "talk"));
            })
    );
}
