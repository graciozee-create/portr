package com.sandymandy.pleasurehorizons.item.items;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.registries.GirlRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Tribe Egg: spawns 4 Kobolds in a cluster. All are tamed to the player.
 * Ported from Mine335/JennysMod1.21.1 (TribeEggItem).
 */
public class TribeEggItem extends Item {
    private static final int[][] OFFSETS = {{0, 0}, {1, 0}, {-1, 0}, {0, 1}};

    public TribeEggItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;

        BlockPos origin = context.getClickedPos().relative(context.getClickedFace());
        List<TameableGirlEntity> spawned = new ArrayList<>();

        for (int[] offset : OFFSETS) {
            BlockPos pos = origin.offset(offset[0], 0, offset[1]);
            TameableGirlEntity kobold = GirlRegistry.KOBOLD.get().spawn(serverLevel, pos, MobSpawnType.SPAWN_EGG);
            if (kobold == null) {
                // Rollback all spawned kobolds on failure
                spawned.forEach(TameableGirlEntity::discard);
                return InteractionResult.FAIL;
            }
            kobold.setTamedBy(player);
            kobold.setPersistenceRequired();
            kobold.setFollowing(false); // Tribe kobolds don't follow by default
            spawned.add(kobold);

            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    kobold.getX(), kobold.getY() + 0.5D, kobold.getZ(),
                    8, 0.3D, 0.4D, 0.3D, 0.05D);
        }

        serverLevel.playSound(null, origin, SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS, 1.0F, 1.2F);

        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        player.displayClientMessage(Component.translatable(
                "message.pleasurehorizons.tribe_created", spawned.size()), true);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
