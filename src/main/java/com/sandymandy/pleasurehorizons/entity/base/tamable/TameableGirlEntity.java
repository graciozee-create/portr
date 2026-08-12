package com.sandymandy.pleasurehorizons.entity.base.tamable;

import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlFollowOwnerGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlSitGoal;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.entity.PleasureHorizonsEntityStatuses;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import com.sandymandy.pleasurehorizons.screen.GirlInventoryScreenHandlerFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Taming, relationship and ownership behaviour.
 *
 * <p>1.21.1 notes: the 1.21.6 original stores the owner in a {@code LazyEntityReference} and
 * returns {@code ActionResult.SUCCESS_SERVER}. Neither exists here, so ownership is kept as an
 * {@code Optional<UUID>} synced value and the interaction results use plain
 * {@link InteractionResult#SUCCESS} / {@link InteractionResult#CONSUME}.</p>
 */
public abstract class TameableGirlEntity extends GirlSceneEntity {
    private static final EntityDataAccessor<Boolean> TAMED =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    protected TameableGirlEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TAMED, false);
        builder.define(OWNER_UUID, Optional.empty());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GirlSitGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(3, new GirlFollowOwnerGoal(this, 1.1D, 4.0F, 2.0F));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    // ------------------------------------------------------------ ownership

    public boolean isTamed() {
        return this.entityData.get(TAMED);
    }

    public void setTamed(boolean tamed) {
        this.entityData.set(TAMED, tamed);
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    public LivingEntity getOwner() {
        UUID uuid = this.getOwnerUUID();
        if (uuid == null) {
            return null;
        }
        return this.level().getPlayerByUUID(uuid);
    }

    public boolean isOwner(LivingEntity entity) {
        return entity != null && entity.getUUID().equals(this.getOwnerUUID());
    }

    public void setTamedBy(Player player) {
        this.setTamed(true);
        this.setOwnerUUID(player.getUUID());
    }

    // ------------------------------------------------------------ interaction

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.level().isClientSide()) {
            // Let the client optimistically swing/consume when the server will accept it.
            boolean willAct = this.isTamed() ? this.isOwner(player) : stack.is(this.isAttractedTo());
            return willAct ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        if (!this.getOverrideAnim().isEmpty() || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (this.isFoodItem(stack) && this.getHealth() < this.getMaxHealth()) {
            FoodProperties food = stack.get(DataComponents.FOOD);
            float nutrition = food != null ? food.nutrition() : 1.0F;
            this.heal(2.0F * nutrition);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
            return InteractionResult.CONSUME;
        }

        if (stack.is(Items.POTION)) {
            return InteractionResult.FAIL;
        }

        return this.isTamed()
                ? this.interactTamed(player, stack)
                : this.interactNotTamed(player, stack);
    }

    protected InteractionResult interactTamed(Player player, ItemStack stack) {
        if (!this.isOwner(player)) {
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.alreadyInRelationship"), true);
            return InteractionResult.FAIL;
        }

        // Gifting her favourite item raises the relationship level.
        if (stack.is(this.isAttractedTo())
                && this.getCurrentRelationshipLevel() < this.maxRelationshipLevel()) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.likedGift"), true);

            List<String> replies = this.getCurrentRelationshipLevel() < 4
                    ? this.giftRepliesLike()
                    : this.giftRepliesLove();
            if (!replies.isEmpty()) {
                this.messageAsEntity(player, replies.get(RANDOM.nextInt(replies.size())));
            }

            this.setCurrentRelationshipLevel(this.getCurrentRelationshipLevel() + 1);
            this.playSound(SoundEvents.PLAYER_LEVELUP, 0.7F, 1.4F);
            return InteractionResult.SUCCESS;
        }

        if (!this.isSceneActive() && player.isShiftKeyDown()) {
            this.setSitting(!this.isSitting());
            this.jumping = false;
            this.getNavigation().stop();
            return InteractionResult.SUCCESS;
        }

        if (!this.isSceneActive()) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new GirlInventoryScreenHandlerFactory(this), buf -> buf.writeVarInt(this.getId()));
                this.setGUIOpenState(true, player);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    protected InteractionResult interactNotTamed(Player player, ItemStack stack) {
        if (stack.is(this.isAttractedTo())) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            this.tryTame(player);
            return InteractionResult.SUCCESS;
        }

        player.displayClientMessage(Component.literal(
                "She ignores you. Maybe try giving her "
                        + this.isAttractedTo().getDescription().getString() + "."), true);
        return InteractionResult.FAIL;
    }

    private void tryTame(Player player) {
        if (this.random.nextInt(3) == 0) {
            this.setTamedBy(player);
            this.getNavigation().stop();
            this.setTarget(null);
            this.setBasePos(this.blockPosition());
            this.playSound(SoundEvents.PLAYER_LEVELUP, 0.8F, 1.6F);
            player.displayClientMessage(Component.literal(
                    "You asked " + this.getGirlDisplayName() + " out and she said §aYes"), true);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART,
                        this.getX(), this.getY() + 1.5D, this.getZ(), 7, 0.4D, 0.4D, 0.4D, 0.1D);
            }
        } else {
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                        this.getX(), this.getY() + 1.5D, this.getZ(), 5, 0.3D, 0.3D, 0.3D, 0.02D);
            }
        }
    }

    public void breakUp(Player player) {
        if (this.level().isClientSide()) {
            return;
        }
        this.setTamed(false);
        this.setOwnerUUID(null);
        this.setSitting(false);
        this.setStripped(false);
        this.setFollowing(false);
        this.setCurrentRelationshipLevel(0);
        player.displayClientMessage(
                Component.translatable("msg.pleasurehorizons.brokeUp", this.getGirlDisplayName()), true);
    }

    /**
     * Breaks up and plays the angry-particle effect on every nearby client.
     *
     * <p>Upstream also plays her "sad" voice line here. The per-girl voice groups
     * are not registered in this port yet, so that part is intentionally omitted
     * rather than guessed at.</p>
     */
    public void breakUpParticles(Player player) {
        this.breakUp(player);
        this.level().broadcastEntityEvent(this, PleasureHorizonsEntityStatuses.ANGRY_PARTICLES);
    }

    protected void messageAsEntity(Player player, String message) {
        player.displayClientMessage(
                Component.translatable("chat.pleasurehorizons.girlSays", this.getGirlDisplayName(), message), false);
    }

    public String getGirlDisplayName() {
        if (this.hasCustomName()) {
            return this.getCustomName().getString();
        }
        String id = this.getGirlID();
        return id.isEmpty() ? "Girl" : Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }

    public List<String> giftRepliesLike() {
        return List.of("Wow, for me? Thanks!", "That's so nice of you...!", "Ahah, this is great!");
    }

    public List<String> giftRepliesLove() {
        return List.of("Oh, another one? Well, you're the real gift here~.",
                "Babe, you're too nice.", "You always know what I like~.");
    }

    // ------------------------------------------------------------ misc

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !this.isTamed();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Tamed", this.isTamed());
        if (this.getOwnerUUID() != null) {
            tag.putUUID("Owner", this.getOwnerUUID());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setTamed(tag.getBoolean("Tamed"));
        if (tag.hasUUID("Owner")) {
            this.setOwnerUUID(tag.getUUID("Owner"));
        }
    }
}
