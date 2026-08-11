package com.sandymandy.pleasurehorizons.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import com.sandymandy.pleasurehorizons.entity.girls.CustomGirlEntity;
import com.sandymandy.pleasurehorizons.networking.S2C.RefreshModelsS2CPacket;
import com.sandymandy.pleasurehorizons.registries.GirlRegistry;
import com.sandymandy.pleasurehorizons.util.json.CustomGirlLoader;
import com.sandymandy.pleasurehorizons.util.managers.TamedGirlManager;
import com.sandymandy.pleasurehorizons.util.variables.CustomGirlProfile;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;

import static com.sandymandy.pleasurehorizons.util.Utils.getReadableItemName;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class GirlsCommand {

    // Suggestion provider for auto-complete
    private static final SuggestionProvider<ServerCommandSource> PROFILE_SUGGESTIONS = (context, builder) -> {
        CustomGirlLoader.LOADED_PROFILES.keySet().forEach(builder::suggest);
        return CompletableFuture.completedFuture(builder.build());
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {

        dispatcher.register(
                literal("girls")
                        .requires(src -> true) // anyone can run the base command

                        .then(literal("locateAll")
                                .requires(src -> true)
                                .executes(ctx -> locateAllGirls(ctx.getSource()))
                        )

                        .then(literal("refreshJiggle")
                                .requires(src -> true)
                                .executes(ctx -> refresh(ctx.getSource()))
                        )

                        .then(literal("showCustomGirlInfo")
                                .requires(src -> true)
                                .executes(ctx -> customGirlInfo(ctx.getSource()))
                        )

                        // --- spawn <girlName> ---
                        .then(literal("spawnCustom")
                                .requires(src -> src.hasPermissionLevel(2))
                                // /girl spawn <id>
                                .then(argument("id", StringArgumentType.string())
                                        .suggests(PROFILE_SUGGESTIONS)
                                        .executes(ctx -> {
                                            Vec3d pos = ctx.getSource().getPosition();
                                            String id = StringArgumentType.getString(ctx, "id");
                                            return spawnGirl(ctx.getSource(), id, pos);
                                        })
                                        // /girl spawn <id> <pos>
                                        .then(argument("pos", BlockPosArgumentType.blockPos())
                                                .executes(ctx -> {
                                                    String id = StringArgumentType.getString(ctx, "id");
                                                    BlockPos blockPos = BlockPosArgumentType.getBlockPos(ctx, "pos");
                                                    Vec3d pos = new Vec3d(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
                                                    return spawnGirl(ctx.getSource(), id, pos);
                                                })
                                        )
                                )
                        )
        );
    }

    private static int refresh(ServerCommandSource source) throws CommandSyntaxException {
        PacketDistributor.sendToPlayer(source.getPlayerOrThrow(), new RefreshModelsS2CPacket());
        source.sendFeedback(() ->
                        Text.literal("Refreshed All Loaded Girl Models"),
                false
        );

        return 1;
    }

    private static int customGirlInfo(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        if(CustomGirlLoader.REGISTERED_PROFILES.isEmpty()) {
            player.sendMessage(Text.literal("§cYou have no profiles registered."), false);
            return 0;
        }
        for (CustomGirlProfile profile : CustomGirlLoader.REGISTERED_PROFILES.values()){
            player.sendMessage(Text.of("§d"+profile.id() + " → §b" + getReadableItemName(profile.tameItem())), false);
        }
        return 1;
    }

    private static int locateAllGirls(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        ServerWorld world = player.getWorld();
        TamedGirlManager manager = TamedGirlManager.get(world);

        var owned = manager.getGirlsOwnedBy(player.getUuid());
        if (owned.isEmpty()) {
            player.sendMessage(Text.literal("§cYou have no tamed girls in this world."), false);
            return 0;
        }

        int found = 0;
        for (var entry : owned) {
            GirlEntity girl = (GirlEntity) player.getWorld().getEntity(entry);
            found++;

            Vec3d pos = girl.getPos();
            String name = girl.getGirlDisplayName();

            player.sendMessage(
                    Text.literal("§d" + name
                            + "§b → X: " + (int) pos.x
                            + " Y: " + (int) pos.y
                            + " Z: " + (int) pos.z),
                    false
            );
        }

        player.sendMessage(Text.literal("§aTotal girls found: §e" + found), false);
        return found;
    }

    private static int spawnGirl(ServerCommandSource source, String id, Vec3d pos) {
        ServerWorld world = source.getWorld();

        // Validate profile
        CustomGirlProfile profile = CustomGirlLoader.LOADED_PROFILES.get(id);
        if (profile == null) {
            source.sendError(Text.literal("Girl profile not found: " + id));
            return 0;
        }

        // Validate position
        BlockPos blockPos = BlockPos.ofFloored(pos);
        if (!world.isValid(blockPos)) {
            source.sendError(Text.literal("Invalid spawn position."));
            return 0;
        }

        // Create entity
        CustomGirlEntity girl = GirlRegistry.CUSTOM_GIRL.create(world, net.minecraft.entity.SpawnReason.COMMAND);
        if (girl == null) {
            source.sendError(Text.literal("Failed to create girl entity."));
            return 0;
        }

        // Apply profile and attributes
        girl.setProfile(profile, true);


        // Position & rotation
        girl.refreshPositionAndAngles(pos.x, pos.y, pos.z, source.getRotation().y, 0);

        // Initialize like vanilla
        girl.initialize(world, world.getLocalDifficulty(girl.getBlockPos()), net.minecraft.entity.SpawnReason.COMMAND, null);

        // Spawn entity
        if (!world.spawnEntity(girl)) {
            source.sendError(Text.literal("Failed to spawn entity in the world."));
            return 0;
        }

        source.sendFeedback(() -> Text.literal("Spawned girl: " + id), true);
        return 1;
    }


}
