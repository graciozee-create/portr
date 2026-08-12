package com.sandymandy.pleasurehorizons.command;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Command registration.
 *
 * <p>Fabric used {@code CommandRegistrationCallback}; NeoForge dispatches
 * {@link RegisterCommandsEvent} on the game bus instead.</p>
 */
@EventBusSubscriber(modid = PleasureHorizons.MOD_ID)
public class Commands {

    public static void register() {
        // Registration happens through the event below.
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        GirlsCommand.register(event.getDispatcher());
    }
}
