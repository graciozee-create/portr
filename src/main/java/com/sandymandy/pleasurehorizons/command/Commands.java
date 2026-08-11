package com.sandymandy.pleasurehorizons.command;

import com.mojang.brigadier.CommandDispatcher;
import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod.EventBusSubscriber(modid = PleasureHorizons.MOD_ID)
public class Commands {
    public static void register() {
        PleasureHorizons.LOGGER.info("Registering commands");
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        // Commands will be ported here
    }
}
