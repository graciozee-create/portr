package com.sandymandy.pleasurehorizons;

import com.sandymandy.pleasurehorizons.advancement.criterion.PleasureHorizonsCriteria;
import com.sandymandy.pleasurehorizons.block.PleasureHorizonsBlocks;
import com.sandymandy.pleasurehorizons.block.entity.PleasureHorizonsBlockEntities;
import com.sandymandy.pleasurehorizons.command.Commands;
import com.sandymandy.pleasurehorizons.component.PleasureHorizonsDataComponentTypes;
import com.sandymandy.pleasurehorizons.entity.ai.brain.GirlMemoryTypes;
import com.sandymandy.pleasurehorizons.item.PleasureHorizonsItemGroups;
import com.sandymandy.pleasurehorizons.item.PleasureHorizonsItems;
import com.sandymandy.pleasurehorizons.networking.PleasureHorizonsPackets;
import com.sandymandy.pleasurehorizons.registries.GirlRegistry;
import com.sandymandy.pleasurehorizons.registries.PleasureHorizonsDispenserBehavior;
import com.sandymandy.pleasurehorizons.registries.PleasureHorizonsEntities;
import com.sandymandy.pleasurehorizons.registries.PleasureHorizonsScreenHandlerRegistry;
import com.sandymandy.pleasurehorizons.registries.PleasureHorizonsSoundEventRegistry;
import com.sandymandy.pleasurehorizons.registries.PleasureHorizonsTrackedDataRegistry;
import com.sandymandy.pleasurehorizons.util.json.CustomGirlLoader;
import com.sandymandy.pleasurehorizons.util.managers.TamedGirlManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod(PleasureHorizons.MOD_ID)
public class PleasureHorizons {
    public static final String MOD_ID = "pleasurehorizons";
    public static final String MOD_NAME = "Pleasure Horizons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static Map<UUID, ResourceLocation> usedBeds = new HashMap<>();
    public static Map<UUID, UUID> activeScenes = new HashMap<>();

    public PleasureHorizons(IEventBus modEventBus, ModContainer container, Dist dist) {
        LOGGER.info("Initializing " + MOD_NAME + " for NeoForge 1.21.1!");

        // Registries
        PleasureHorizonsItems.register(modEventBus);
        PleasureHorizonsBlocks.register(modEventBus);
        PleasureHorizonsBlockEntities.register(modEventBus);
        PleasureHorizonsEntities.register(modEventBus);
        PleasureHorizonsSoundEventRegistry.register(modEventBus);
        PleasureHorizonsScreenHandlerRegistry.register(modEventBus);
        PleasureHorizonsDataComponentTypes.register(modEventBus);

        // Other systems
        GirlRegistry.register(modEventBus);
        GirlMemoryTypes.register(modEventBus);
        PleasureHorizonsCriteria.register(modEventBus);
        PleasureHorizonsTrackedDataRegistry.register(modEventBus);
        PleasureHorizonsDispenserBehavior.register();
        PleasureHorizonsItemGroups.register();
        Commands.register();
        CustomGirlLoader.register();

        // Networking
        PleasureHorizonsPackets.register();

        // Server tick
        NeoForge.EVENT_BUS.register(this);

        if (dist == Dist.CLIENT) {
            modEventBus.addListener(PleasureHorizonsClient::onClientSetup);
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        // Cleanup dead girls on server tick
        var server = event.getServer();
        if (server != null) {
            TamedGirlManager.get(server.overworld()).cleanupDeadGirls(server.overworld());
        }
    }
}
