package com.sandymandy.pleasurehorizons;

import com.sandymandy.pleasurehorizons.advancement.criterion.PleasureHorizonsCriteria;
import com.sandymandy.pleasurehorizons.block.PleasureHorizonsBlocks;
import com.sandymandy.pleasurehorizons.block.entity.PleasureHorizonsBlockEntities;
import com.sandymandy.pleasurehorizons.command.Commands;
import com.sandymandy.pleasurehorizons.component.PleasureHorizonsDataComponentTypes;
import com.sandymandy.pleasurehorizons.item.PleasureHorizonsItemGroups;
import com.sandymandy.pleasurehorizons.item.PleasureHorizonsItems;
import com.sandymandy.pleasurehorizons.item.PleasureHorizonsSpawnEggs;
import com.sandymandy.pleasurehorizons.networking.PleasureHorizonsPackets;
import com.sandymandy.pleasurehorizons.registries.GirlRegistry;
import com.sandymandy.pleasurehorizons.registries.PleasureHorizonsDispenserBehavior;
import com.sandymandy.pleasurehorizons.registries.PleasureHorizonsScreenHandlerRegistry;
import com.sandymandy.pleasurehorizons.registries.PleasureHorizonsTrackedDataRegistry;
import com.sandymandy.pleasurehorizons.util.json.CustomGirlLoader;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
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
    public static Map<UUID, BlockPos> usedBeds = new HashMap<>();
    public static Map<UUID, UUID> activeScenes = new HashMap<>();

    public PleasureHorizons(IEventBus modEventBus, ModContainer container, Dist dist) {
        LOGGER.info("Initializing " + MOD_NAME + " for NeoForge 1.21.1!");
        // Build marker: if you do not see this line in latest.log, the jar you launched
        // is an older build and does not contain the spawn eggs / creative tab.
        LOGGER.info("[PH] BUILD MARKER girls-port-v2 :: spawn eggs + creative tab + renderers");

        // Registries
        PleasureHorizonsItems.register(modEventBus);
        PleasureHorizonsSpawnEggs.register(modEventBus);
        PleasureHorizonsBlocks.register(modEventBus);
        PleasureHorizonsBlockEntities.register(modEventBus);
        GirlRegistry.register(modEventBus);
        PleasureHorizonsScreenHandlerRegistry.register(modEventBus);
        PleasureHorizonsDataComponentTypes.register(modEventBus);

        // Other systems
        PleasureHorizonsCriteria.register(modEventBus);
        PleasureHorizonsTrackedDataRegistry.register(modEventBus);
        PleasureHorizonsDispenserBehavior.register(modEventBus);
        PleasureHorizonsItemGroups.register(modEventBus);
        Commands.register();
        // Girl profiles reference items by id, so they must load after the item registry
        // is populated - see onServerStarting below. Loading them here would resolve every
        // tame_item to air.

        // Networking
        PleasureHorizonsPackets.register();

        // Server tick
        NeoForge.EVENT_BUS.register(this);

        if (dist == Dist.CLIENT) {
            // Freecam settings live in a CLIENT config; registering it during mod loading is
            // required because NeoForge reads the spec before client setup events run.
            com.sandymandy.pleasurehorizons.freecam.FreecamConfig.register(container);
        }
    }

    @SubscribeEvent
    public void onServerStarting(net.neoforged.neoforge.event.server.ServerStartingEvent event) {
        // Integrated servers can stop and restart in the same JVM. Runtime-only reservations
        // from the previous world must never block players or beds in the next one.
        usedBeds.clear();
        activeScenes.clear();
        CustomGirlLoader.register();
    }
}
