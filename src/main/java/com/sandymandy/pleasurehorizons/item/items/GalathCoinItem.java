package com.sandymandy.pleasurehorizons.item.items;

import com.sandymandy.pleasurehorizons.entity.girls.GalathEntity;
import com.sandymandy.pleasurehorizons.registries.GirlRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Galath's Coin: toggles Galath summon/dismiss. Right-click to summon, right-click again to dismiss.
 * Cooldown: 10 seconds (200 ticks).
 * Ported from Mine335/JennysMod1.21.1 (GalathCoinItem).
 */
public class GalathCoinItem extends Item {
    public GalathCoinItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level instanceof ServerLevel serverLevel) {
            // Check if player already has an active Galath
            GalathEntity existing = null;
            for (var entity : serverLevel.getAllEntities()) {
                if (entity instanceof GalathEntity galath
                        && galath.isTamed() && galath.isOwner(player) && galath.isAlive()) {
                    existing = galath;
                    break;
                }
            }

            if (existing != null) {
                // Dismiss
                existing.discard();
                player.getCooldowns().addCooldown(this, 200);
                player.displayClientMessage(
                        Component.translatable("message.pleasurehorizons.galath_dismissed"), true);
            } else {
                // Summon
                BlockPos pos = player.blockPosition().relative(player.getDirection(), 2);
                GalathEntity galath = GirlRegistry.GALATH.get().spawn(serverLevel, pos, MobSpawnType.SPAWN_EGG);
                if (galath != null) {
                    galath.setTamedBy(player);
                    galath.setFollowing(true);
                    galath.setPersistenceRequired();

                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                            galath.getX(), galath.getY() + 0.5D, galath.getZ(),
                            24, 0.35D, 0.6D, 0.35D, 0.08D);
                    serverLevel.playSound(null, galath.blockPosition(),
                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.2F);

                    player.getCooldowns().addCooldown(this, 200);
                    player.displayClientMessage(
                            Component.translatable("message.pleasurehorizons.galath_summoned"), true);
                }
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
