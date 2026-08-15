package com.sandymandy.pleasurehorizons.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.sandymandy.pleasurehorizons.entity.girls.CustomGirlEntity;
import com.sandymandy.pleasurehorizons.registries.GirlRegistry;
import com.sandymandy.pleasurehorizons.util.json.CustomGirlLoader;
import com.sandymandy.pleasurehorizons.util.variables.CustomGirlProfile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;

/**
 * {@code /girls} - reload custom girl profiles and spawn a custom girl by profile id.
 *
 * <p>The Fabric original also had a "refresh models" subcommand backed by
 * {@code AbstractGirlModel.refreshAllModels()}. That model-cache class is not ported, and
 * GeckoLib 4 rebuilds its cache on the normal resource reload (F3+T) anyway, so the
 * subcommand is intentionally left out rather than shipped as a no-op that looks like it works.</p>
 */
public class GirlsCommand {

    private static final SuggestionProvider<CommandSourceStack> PROFILE_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    CustomGirlLoader.LOADED_PROFILES.keySet(), builder);

    private GirlsCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("girls")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reload")
                        .executes(ctx -> reloadProfiles(ctx.getSource())))
                .then(Commands.literal("spawn")
                        .then(Commands.argument("id", StringArgumentType.string())
                                .suggests(PROFILE_SUGGESTIONS)
                                .executes(ctx -> spawnGirl(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "id"),
                                        ctx.getSource().getPosition()))
                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                        .executes(ctx -> spawnGirl(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "id"),
                                                Vec3Argument.getVec3(ctx, "pos")))))));
    }

    private static int reloadProfiles(CommandSourceStack source) {
        CustomGirlLoader.register();
        int count = CustomGirlLoader.LOADED_PROFILES.size();
        source.sendSuccess(() -> Component.translatable("commands.pleasurehorizons.girls.reloaded", count), true);
        return count;
    }

    private static int spawnGirl(CommandSourceStack source, String id, Vec3 pos) {
        ServerLevel level = source.getLevel();

        CustomGirlProfile profile = CustomGirlLoader.LOADED_PROFILES.get(id);
        if (profile == null) {
            source.sendFailure(Component.translatable("commands.pleasurehorizons.girls.unknown_profile", id));
            return 0;
        }

        CustomGirlEntity girl = GirlRegistry.CUSTOM_GIRL.get().create(level);
        if (girl == null) {
            source.sendFailure(Component.translatable("commands.pleasurehorizons.girls.spawn_failed"));
            return 0;
        }

        girl.moveTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);
        girl.finalizeSpawn(level, level.getCurrentDifficultyAt(girl.blockPosition()),
                MobSpawnType.COMMAND, null);
        girl.setProfile(profile, true);
        level.addFreshEntity(girl);

        source.sendSuccess(() -> Component.translatable(
                "commands.pleasurehorizons.girls.spawned", profile.name()), true);
        return 1;
    }
}
