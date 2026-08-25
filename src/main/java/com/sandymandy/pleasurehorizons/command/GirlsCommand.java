package com.sandymandy.pleasurehorizons.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.entity.girls.CustomGirlEntity;
import com.sandymandy.pleasurehorizons.registries.GirlRegistry;
import com.sandymandy.pleasurehorizons.util.json.CustomGirlLoader;
import com.sandymandy.pleasurehorizons.util.variables.CustomGirlProfile;
import com.sandymandy.pleasurehorizons.util.variables.GirlRole;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    private static final SuggestionProvider<CommandSourceStack> TYPE_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    List.of("lucy", "mika", "momo", "slime", "kobold", "coppie"), builder);

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
                                                Vec3Argument.getVec3(ctx, "pos"))))))
                .then(Commands.literal("summon")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests(TYPE_SUGGESTIONS)
                                .executes(ctx -> summonGirl(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "type"),
                                        ctx.getSource().getPosition()))
                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                        .executes(ctx -> summonGirl(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "type"),
                                                Vec3Argument.getVec3(ctx, "pos"))))))
                .then(Commands.literal("role")
                        .then(Commands.argument("role", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        List.of("idle", "worker", "guard", "cook"), builder))
                                .executes(ctx -> applyRole(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "role")))))
                .then(Commands.literal("call")
                        .executes(ctx -> callGirls(ctx.getSource(), null))
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(ctx -> callGirls(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "name"))))));
    }

    private static int reloadProfiles(CommandSourceStack source) {
        CustomGirlLoader.register();
        int count = CustomGirlLoader.LOADED_PROFILES.size();
        source.sendSuccess(() -> Component.translatable("commands.pleasurehorizons.girls.reloaded", count), true);
        return count;
    }

    private static int summonGirl(CommandSourceStack source, String type, Vec3 pos) {
        ServerLevel level = source.getLevel();
        net.minecraft.world.entity.EntityType<?> entityType = switch (type.toLowerCase()) {
            case "lucy" -> GirlRegistry.LUCY.get();
            case "mika" -> GirlRegistry.MIKA.get();
            case "momo" -> GirlRegistry.MOMO.get();
            case "slime" -> GirlRegistry.SLIME.get();
            case "kobold" -> GirlRegistry.KOBOLD.get();
            case "coppie" -> GirlRegistry.COPPIE.get();
            default -> null;
        };
        if (entityType == null) {
            source.sendFailure(Component.translatable(
                    "commands.pleasurehorizons.girls.unknown_type", type));
            return 0;
        }
        net.minecraft.world.entity.Entity entity = entityType.create(level);
        if (!(entity instanceof TameableGirlEntity girl)) {
            source.sendFailure(Component.translatable("commands.pleasurehorizons.girls.spawn_failed"));
            return 0;
        }
        girl.moveTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);
        if (girl instanceof net.minecraft.world.entity.Mob mob) {
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(girl.blockPosition()),
                    MobSpawnType.COMMAND, null);
        }
        // Command spawn must persist - prevent vanilla checkDespawn from removing her
        girl.setPersistenceRequired();
        level.addFreshEntity(girl);
        source.sendSuccess(() -> Component.translatable(
                "commands.pleasurehorizons.girls.summoned", type), true);
        return 1;
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
        // A command spawn is a deliberate placement. Without this the untamed girl is
        // CREATURE-category + removeWhenFarAway, so vanilla checkDespawn() discards her
        // once the spawner walks >128 blocks away.
        girl.setPersistenceRequired();
        level.addFreshEntity(girl);

        source.sendSuccess(() -> Component.translatable(
                "commands.pleasurehorizons.girls.spawned", profile.name()), true);
        return 1;
    }

    /** Applies one survival role to every loaded girl owned by the commanding player. */
    private static int applyRole(CommandSourceStack source, String roleId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("commands.pleasurehorizons.girls.players_only"));
            return 0;
        }

        GirlRole role = GirlRole.fromId(roleId);
        List<TameableGirlEntity> girls = player.level().getEntitiesOfClass(
                TameableGirlEntity.class,
                new AABB(player.blockPosition()).inflate(128.0D),
                girl -> girl.isTamed() && girl.isOwner(player));

        if (girls.isEmpty()) {
            source.sendFailure(Component.translatable("commands.pleasurehorizons.girls.no_girls"));
            return 0;
        }

        for (TameableGirlEntity girl : girls) {
            girl.setRole(role);
        }

        source.sendSuccess(() -> Component.translatable(
                "commands.pleasurehorizons.girls.role_applied",
                Component.translatable("role.pleasurehorizons." + role.id()), girls.size()), true);
        return girls.size();
    }

    /** Teleports every loaded, owned girl to the commanding player (optionally a named one). */
    private static int callGirls(CommandSourceStack source, @Nullable String name) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("commands.pleasurehorizons.girls.players_only"));
            return 0;
        }

        TameableGirlEntity.CallResult result = TameableGirlEntity.callOwnedGirlsTo(player, name);
        if (result.total() == 0) {
            source.sendFailure(Component.translatable("commands.pleasurehorizons.girls.no_girls"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable(
                "commands.pleasurehorizons.girls.call_result",
                result.teleported(), result.queued()), true);
        return result.total();
    }
}
