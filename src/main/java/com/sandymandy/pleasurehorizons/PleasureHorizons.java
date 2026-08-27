package com.sandymandy.pleasurehorizons;

import com.sandymandy.pleasurehorizons.advancement.criterion.PleasureHorizonsCriteria;
import com.sandymandy.pleasurehorizons.block.PleasureHorizonsBlocks;
import com.sandymandy.pleasurehorizons.block.entity.PleasureHorizonsBlockEntities;
import com.sandymandy.pleasurehorizons.command.Commands;
import com.sandymandy.pleasurehorizons.component.PleasureHorizonsDataComponentTypes;
import com.sandymandy.pleasurehorizons.effects.PleasureHorizonsEffects;
import com.sandymandy.pleasurehorizons.item.PleasureHorizonsItemGroups;
import com.sandymandy.pleasurehorizons.item.PleasureHorizonsItems;
import com.sandymandy.pleasurehorizons.item.PleasureHorizonsSpawnEggs;
import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.networking.PleasureHorizonsPackets;
import com.sandymandy.pleasurehorizons.registries.GirlRegistry;
import com.sandymandy.pleasurehorizons.registries.PleasureHorizonsDispenserBehavior;
import com.sandymandy.pleasurehorizons.registries.PleasureHorizonsScreenHandlerRegistry;
import com.sandymandy.pleasurehorizons.registries.PleasureHorizonsTrackedDataRegistry;
import com.sandymandy.pleasurehorizons.relationship.QuestManager;
import com.sandymandy.pleasurehorizons.util.json.CustomGirlLoader;
import com.sandymandy.pleasurehorizons.util.managers.TamedGirlRegistry;
import com.sandymandy.pleasurehorizons.util.managers.TamedGirlSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
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
    /** Shared pretty-printer for the small JSON config files this mod writes. */
    public static final com.google.gson.Gson GSON =
            new com.google.gson.GsonBuilder().setPrettyPrinting().create();
    public static Map<UUID, BlockPos> usedBeds = new HashMap<>();
    public static Map<UUID, UUID> activeScenes = new HashMap<>();

    public PleasureHorizons(IEventBus modEventBus, ModContainer container, Dist dist) {
        LOGGER.info("Initializing " + MOD_NAME + " for NeoForge 1.21.1!");
        // Build marker: if you do not see this line in latest.log, the jar you launched
        // is an older build and does not contain the spawn eggs / creative tab.
                                                        LOGGER.info("[PH] BUILD MARKER 2026-08-27 v9 :: call key follows settings");

        // Registries
        PleasureHorizonsItems.register(modEventBus);
        PleasureHorizonsSpawnEggs.register(modEventBus);
        PleasureHorizonsBlocks.register(modEventBus);
        PleasureHorizonsBlockEntities.register(modEventBus);
        GirlRegistry.register(modEventBus);
        PleasureHorizonsScreenHandlerRegistry.register(modEventBus);
        PleasureHorizonsDataComponentTypes.register(modEventBus);
        PleasureHorizonsEffects.register(modEventBus);

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

        // Server-side girl tuning (stats + self-healing); file config/pleasurehorizons-girls.toml.
        com.sandymandy.pleasurehorizons.config.GirlsConfig.register(container);

        // Server tick
        NeoForge.EVENT_BUS.register(this);

        if (dist == Dist.CLIENT) {
            // Freecam settings live in a CLIENT config; registering it during mod loading is
            // required because NeoForge reads the spec before client setup events run.
            com.sandymandy.pleasurehorizons.freecam.FreecamConfig.register(container);
            // The small UI/keybind options are a plain JSON file rather than a NeoForge spec.
            com.sandymandy.pleasurehorizons.config.ModConfig.load();
        }
    }

    @SubscribeEvent
    public void onServerStarting(net.neoforged.neoforge.event.server.ServerStartingEvent event) {
        // Integrated servers can stop and restart in the same JVM. Runtime-only reservations
        // from the previous world must never block players or beds in the next one.
        usedBeds.clear();
        activeScenes.clear();
        TameableGirlEntity.clearPendingCalls();
        // Attach the persisted tamed-girl registry so "call girls" can reach girls whose chunks
        // have not been loaded since the last restart.
        TamedGirlRegistry.attach(event.getServer().overworld().getDataStorage()
                .computeIfAbsent(TamedGirlSavedData.factory(), TamedGirlSavedData.name()));
        CustomGirlLoader.register();
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        // Complete summons whose target girl lives in a chunk that had to be force-loaded.
        for (ServerLevel level : event.getServer().getAllLevels()) {
            TameableGirlEntity.tickPendingCalls(level);
        }
        tickPassiveItems(event.getServer().getAllLevels());
    }

    /**
     * Sanitises arrows with an empty pickup item stack.
     *
     * <p>1.21.1's {@code AbstractArrow} constructor copies the firing ammo into the private
     * {@code pickupItemStack} field directly (bypassing the safe setter), and
     * {@code addAdditionalSaveData} saves that field WITHOUT an empty check. Any arrow shot
     * with empty ammo (old PH builds before the {@code getProjectile} fix, or third-party
     * ranged items) therefore kills the whole server with
     * {@code IllegalStateException: Cannot encode empty ItemStack} the first time it is
     * serialized - chunk autosave drops it, and a portal crossing crashes
     * {@code changeDimension}. On join we route an empty stack through the entity's slot 0
     * accessor, whose vanilla setter falls back to the arrow type's own default item
     * ({@code minecraft:arrow} for {@code Arrow}, spectral for {@code SpectralArrow}, and the
     * correct default for any modded {@code AbstractArrow} subclass).</p>
     */
    @SubscribeEvent
    public void onEntityJoinLevel(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow)) return;
        if (!arrow.getPickupItemStackOrigin().isEmpty()) return;
        arrow.getSlot(0).set(ItemStack.EMPTY);
        LOGGER.info("[PH] sanitized empty arrow pickup item on {} (id {})",
                arrow.getType().getDescriptionId(), arrow.getId());
    }

    /**
     * Inventory-charm effects. The healing charm heals the player's nearby girls every 2 seconds;
     * the bond bracelet is a no-op placeholder until affection decay is modelled, but is kept as
     * a real item so its tooltip matches upstream.
     */
    private void tickPassiveItems(Iterable<ServerLevel> levels) {
        for (ServerLevel level : levels) {
            if ((level.getGameTime() % 40) != 0) continue;
            for (ServerPlayer player : level.players()) {
                boolean healing = hasItem(player, PleasureHorizonsItems.HEALING_CHARM.get());
                if (!healing && !hasItem(player, PleasureHorizonsItems.BOND_BRACELET.get())) {
                    continue;
                }
                for (TameableGirlEntity girl : level.getEntitiesOfClass(TameableGirlEntity.class,
                        player.getBoundingBox().inflate(12.0D))) {
                    UUID owner = girl.getOwnerUUID();
                    if (owner != null && !owner.equals(player.getUUID())) continue;
                    if (healing && girl.isAlive() && girl.getHealth() < girl.getMaxHealth()) {
                        girl.heal(1.0F);
                    }
                }
            }
        }
    }

    private static boolean hasItem(ServerPlayer player, net.minecraft.world.item.Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) return true;
        }
        return false;
    }

    private static String girlName(GirlEntity girl) {
        if (girl.hasCustomName()) {
            return girl.getCustomName().getString();
        }
        String id = girl.getGirlID();
        return id.isEmpty() ? "Girl" : Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }

    /** Count KILL quest progress when a player kills the target mob. */
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        Level level = player.level();
        LivingEntity victim = event.getEntity();
        ResourceLocation mobId = victim.getType().builtInRegistryHolder().key().location();
        for (GirlEntity girl : level.getEntitiesOfClass(GirlEntity.class, player.getBoundingBox().inflate(64.0D))) {
            QuestManager manager = girl.getQuestManager();
            QuestManager.Quest quest = manager.activeQuest();
            if (quest == null || quest.type() != QuestManager.QuestType.KILL) continue;
            if (!manager.getOwner().equals(player.getStringUUID())) continue;
            if (manager.addKill(mobId, 1)) {
                QuestManager.Quest finished = manager.complete();
                manager.grantReward(girl, player, finished);
                player.displayClientMessage(Component.translatable(
                        "msg.pleasurehorizons.quest_kill_completed", girlName(girl),
                        finished.rewardAffection()), false);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDisconnect(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        // A disconnected player can no longer be in a scene. GirlSceneEntity.tick() already
        // clears this while the girl is loaded (getScenePlayer() resolves to null), but when her
        // chunk is unloaded the girl never ticks, so both maps below would otherwise leak until
        // the next server restart. activeScenes is keyed by player (blocks only that player);
        // usedBeds is keyed by girl (blocks that bed for everyone), so the matching bed marker
        // is dropped too.
        java.util.UUID playerId = event.getEntity().getUUID();
        java.util.UUID girlId = activeScenes.remove(playerId);
        if (girlId != null) {
            usedBeds.remove(girlId);
        }
    }
}
