package com.sandymandy.pleasurehorizons.entity.base.tamable;

import com.sandymandy.pleasurehorizons.entity.ai.goal.BedGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlAttackWithOwnerGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlTrackOwnerAttackerGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlFollowOwnerGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlGatherItemsGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlGuardBaseGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlGuardOwnerGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlHarvestCropsGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlSitGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlStayNearBaseGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.MoveToPlayerGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.StationaryContactGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.StopMovementGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.StripGoal;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.entity.PleasureHorizonsEntityStatuses;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import com.sandymandy.pleasurehorizons.screen.GirlInventoryScreenHandlerFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
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
        // Scene goals run before everything else so a scene cannot be interrupted by idle AI.
        this.goalSelector.addGoal(0, new StationaryContactGoal(this));
        this.goalSelector.addGoal(0, new MoveToPlayerGoal(this, 1.25D));
        this.goalSelector.addGoal(0, new BedGoal(this, 1.25D));
        this.goalSelector.addGoal(0, new StripGoal(this));
        this.goalSelector.addGoal(0, new StopMovementGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GirlSitGoal(this));
        // Combat goal is added by SettlementGirlEntityAI, which is the level that can
        // actually fire a bow (it implements RangedAttackMob).
        this.registerCombatGoals();
        this.goalSelector.addGoal(3, new GirlFollowOwnerGoal(this, 1.1D, 4.0F, 2.0F));
        this.goalSelector.addGoal(4, new GirlHarvestCropsGoal(this)); // toggleable via isHarvestEnabled
        this.goalSelector.addGoal(5, new GirlGatherItemsGoal(this)); // toggleable via isGatherEnabled
        this.goalSelector.addGoal(5, new GirlStayNearBaseGoal(this, 1.0D, 3.0F, 10.0F)); // toggleable
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        // These two are the reason a carried girl kept spinning on the spot: the vanilla
        // look goals do not know about being a passenger, so they carried on picking new
        // look targets and rotating her while she sat on the player's shoulder.
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F) {
            @Override
            public boolean canUse() {
                return !TameableGirlEntity.this.isCarried() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !TameableGirlEntity.this.isCarried() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !TameableGirlEntity.this.isCarried() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !TameableGirlEntity.this.isCarried() && super.canContinueToUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // Defend the owner: retaliate against whoever hurt them, and join their fights.
        this.targetSelector.addGoal(1, new GirlTrackOwnerAttackerGoal(this));
        this.targetSelector.addGoal(1, new GirlAttackWithOwnerGoal(this, Player.class));
        this.targetSelector.addGoal(2, new GirlGuardBaseGoal(this)); // guard base when enabled
        this.targetSelector.addGoal(2, new GirlGuardOwnerGoal(this)); // guard owner when enabled - new advanced AI
    }

    /** Overridden by weapon-capable subclasses; plain melee by default. */
    protected void registerCombatGoals() {
        this.goalSelector.addGoal(2,
                new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.2D, true));
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
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            com.sandymandy.pleasurehorizons.advancement.criterion.PleasureHorizonsCriteria
                    .TAME_GIRL.get().trigger(serverPlayer, this);
        }
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
        if (this.isDowned()) {
            // Allow carrying wounded girl on hands even when downed (rescue)
            if (player.isShiftKeyDown() && stack.isEmpty() && this.isOwner(player)) {
                return this.toggleCarry(player);
            }

            boolean isFood = stack.get(DataComponents.FOOD) != null;
            if (stack.is(this.isAttractedTo()) || isFood || stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE) || stack.is(Items.GOLDEN_CARROT)) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.setDowned(false);
                this.setHealth(this.getMaxHealth());
                this.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
                player.displayClientMessage(Component.translatable("msg.pleasurehorizons.girl_revived", this.getGirlDisplayName()), true);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.translatable("msg.pleasurehorizons.girl_downed_need_food", this.getGirlDisplayName()), true);
                return InteractionResult.SUCCESS;
            }
        }

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

            List<Component> replies = this.getCurrentRelationshipLevel() < 4
                    ? this.giftRepliesLike()
                    : this.giftRepliesLove();
            if (!replies.isEmpty()) {
                this.messageAsEntity(player, replies.get(RANDOM.nextInt(replies.size())));
            }

            this.setCurrentRelationshipLevel(this.getCurrentRelationshipLevel() + 1);
            this.playSound(SoundEvents.PLAYER_LEVELUP, 0.7F, 1.4F);
            return InteractionResult.SUCCESS;
        }

        if (!this.isSceneActive() && player.isShiftKeyDown() && stack.isEmpty()) {
            return this.toggleCarry(player);
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

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isDowned()) {
            return false;
        }
        // No fall damage while being carried on hands - princess carry should be safe
        if (this.isPassenger() && this.getVehicle() instanceof Player) {
            if (source.type().msgId().equals("fall") || source.type().msgId().equals("inWall")) {
                return false;
            }
        }
        float currentHealth = this.getHealth();
        if (amount >= currentHealth) {
            this.setDowned(true);
            this.setHealth(1.0F);
            this.getNavigation().stop();
            this.setTarget(null);
            if (this.getOwner() instanceof Player owner) {
                owner.displayClientMessage(Component.translatable("msg.pleasurehorizons.girl_heavily_wounded", this.getGirlDisplayName()), true);
            }
            return false;
        }
        return super.hurt(source, amount);
    }

    /**
     * Picks the girl up onto the player's hands, or puts her down if already carried.
     *
     * <p>This used to look like it worked but froze the girl on the carrying player's own
     * screen. The cause is vanilla entity tracking, not the mount itself:</p>
     *
     * <ul>
     *   <li>{@code ChunkMap.TrackedEntity#updatePlayer} opens with {@code if (player != this.entity)},
     *       so a player is never sent tracking updates about <em>themselves</em>. When the vehicle
     *       is a player, the {@link ClientboundSetPassengersPacket} that vanilla would normally
     *       broadcast from {@code ServerEntity#sendChanges} is therefore never delivered to the
     *       carrier's own client.</li>
     *   <li>Meanwhile {@code ServerEntity#sendChanges} stops sending position updates for an
     *       entity that {@code isPassenger()}, because passengers are positioned client-side by
     *       their vehicle.</li>
     * </ul>
     *
     * <p>So the carrier's client kept the girl at her last broadcast position with no vehicle link
     * and no further movement packets - a girl frozen in mid-air. Other players saw her carried
     * correctly, which matches the reported symptom exactly.</p>
     *
     * <p>The fix is to explicitly send the passenger list to the carrying player, since the server
     * will not do it for them. Everything else (goal suppression, gravity) already worked.</p>
     */
    protected InteractionResult toggleCarry(Player player) {
        if (this.isPassenger() && this.getVehicle() == player) {
            this.stopRiding();
            // Place her in front of the player so she does not spawn inside him.
            float yawRad = (float) Math.toRadians(player.getYRot());
            double forwardX = -Math.sin(yawRad);
            double forwardZ = Math.cos(yawRad);
            this.moveTo(player.getX() + forwardX, player.getY(), player.getZ() + forwardZ,
                    this.getYRot(), this.getXRot());
            this.setNoGravity(false);
            this.setSitting(false);
            this.syncCarryState(player);
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.girl_put_down", this.getGirlDisplayName()), true);
            return InteractionResult.SUCCESS;
        }

        if (this.isVehicle()) {
            // She must not be carrying anyone while being carried herself.
            this.ejectPassengers();
        }
        this.getNavigation().stop();
        this.setTarget(null);
        this.setSitting(false);

        // startRiding(force = true) skips canRide()/canAddPassenger(); it only fails on a
        // ride cycle. Bail out without touching gravity if the mount did not take, otherwise
        // she would float in place - the previous behaviour.
        if (!this.startRiding(player, true)) {
            return InteractionResult.FAIL;
        }

        this.setNoGravity(true);
        this.syncCarryState(player);
        player.displayClientMessage(
                Component.translatable("msg.pleasurehorizons.girl_picked_up", this.getGirlDisplayName()), true);
        return InteractionResult.SUCCESS;
    }

    /** True while she is riding a player, i.e. being carried. */
    public boolean isCarried() {
        return this.isPassenger() && this.getVehicle() instanceof Player;
    }

    /**
     * Sends the vehicle's passenger list to the vehicle player themselves.
     *
     * <p>Required because vanilla entity tracking never informs a player about their own entity,
     * so a player-as-vehicle mount is invisible to the carrier without this.</p>
     */
    private void syncCarryState(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetPassengersPacket(player));
        }
    }

    @Override
    protected boolean canAddPassenger(net.minecraft.world.entity.Entity passenger) {
        // Girls should not carry other entities while being carried themselves
        if (this.isPassenger() && this.getVehicle() instanceof Player) {
            return false;
        }
        return super.canAddPassenger(passenger);
    }

    /**
     * Where the carrier's client places her hitbox while she is carried.
     *
     * <p>The default {@code PASSENGER} attachment falls back to the vehicle's full height, which
     * for a player vehicle parks her on top of his head. Vanilla positions a passenger at
     * {@code vehicle.getPassengerRidingPosition() - passenger.getVehicleAttachmentPoint()}
     * (see {@code Entity#positionRider}), so a <em>positive</em> Y here moves her down.</p>
     *
     * <p>{@code Player} declares no {@code PASSENGER} attachment, so it falls back to
     * {@code AT_HEIGHT}, i.e. the full standing height of <b>1.8</b> - not the 1.62 eye height
     * an earlier version of this comment assumed. Subtracting 0.6 seats her origin at 1.2,
     * which is shoulder level, so the renderer does not have to lift her at all.</p>
     */
    @Override
    public net.minecraft.world.phys.Vec3 getVehicleAttachmentPoint(net.minecraft.world.entity.Entity vehicle) {
        if (vehicle instanceof Player) {
            return new net.minecraft.world.phys.Vec3(0.0, 0.6, 0.0);
        }
        return super.getVehicleAttachmentPoint(vehicle);
    }

    /** She is baggage while carried - no collision pushing against the carrier. */
    @Override
    public boolean canBeCollidedWith() {
        if (this.isPassenger() && this.getVehicle() instanceof Player) {
            return false;
        }
        return super.canBeCollidedWith();
    }

    @Override
    public void tick() {
        super.tick();
        // While being carried, ensure she stays nicely positioned and doesn't suffocate
        if (this.isPassenger() && this.getVehicle() instanceof Player player) {
            this.setNoGravity(true);
            // Suppress leftover AI motion; the vehicle positions her every tick.
            this.getNavigation().stop();
            this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);

            // Lock her rotation to the carrier. Without this she keeps her own yaw and any
            // leftover look target spins her around on the player's shoulder. All four
            // rotation fields have to be written, including the "O" (previous tick) ones,
            // or the renderer interpolates between the old and new yaw and she jitters.
            //
            // This follows the carrier's BODY yaw, not getYRot(). For a player getYRot() is
            // the head/look yaw, so using it turned her whole body every time the carrier
            // moved the mouse - she swung around to face wherever he glanced. yBodyRot only
            // changes when he actually turns his body, which is what a passenger rides with.
            float carrierYaw = player.yBodyRot;
            this.setYRot(carrierYaw);
            this.yRotO = carrierYaw;
            this.setYBodyRot(carrierYaw);
            this.yBodyRotO = carrierYaw;
            // Head follows the body, but GirlRenderer's head tracking is free to turn it
            // within its own limits, so she can still glance around while being carried.
            this.setYHeadRot(carrierYaw);
            this.yHeadRotO = carrierYaw;
            this.setXRot(0.0F);
            this.xRotO = 0.0F;

            // Drop her safely if the carrier dies or leaves, otherwise she would be stuck
            // riding a removed entity.
            if (!player.isAlive() || player.isRemoved()) {
                this.stopRiding();
                this.setNoGravity(false);
                this.syncCarryState(player);
            }
        } else if (this.isNoGravity() && !this.isSceneActive() && !this.level().isClientSide()) {
            // Failsafe: never leave her weightless once the carry has ended.
            this.setNoGravity(false);
        }
    }

    public void talkToPlayer(Player player) {
        if (this.level().isClientSide()) return;
        List<Component> replies = this.getCurrentRelationshipLevel() < 4
                ? this.giftRepliesLike()
                : this.giftRepliesLove();
        if (!replies.isEmpty()) {
            this.messageAsEntity(player, replies.get(RANDOM.nextInt(replies.size())));
        }
        this.playSound(SoundEvents.PLAYER_LEVELUP, 0.7F, 1.4F);
    }

    protected InteractionResult interactNotTamed(Player player, ItemStack stack) {
        if (stack.is(this.isAttractedTo())) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            this.tryTame(player);
            return InteractionResult.SUCCESS;
        }

        player.displayClientMessage(Component.translatable(
                "msg.pleasurehorizons.girl_ignores",
                this.isAttractedTo().getDescription().getString()), true);
        return InteractionResult.FAIL;
    }

    private void tryTame(Player player) {
        if (this.random.nextInt(3) == 0) {
            this.setTamedBy(player);
            this.getNavigation().stop();
            this.setTarget(null);
            this.setBasePos(this.blockPosition());
            this.playSound(SoundEvents.PLAYER_LEVELUP, 0.8F, 1.6F);
            player.displayClientMessage(Component.translatable(
                    "msg.pleasurehorizons.tame_success", this.getGirlDisplayName()), true);
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

    protected void messageAsEntity(Player player, Component message) {
        player.displayClientMessage(
                Component.translatable("chat.pleasurehorizons.girlSays", this.getGirlDisplayName(), message), false);
    }

    @Override
    public String getSceneDisplayName() {
        return getGirlDisplayName();
    }

    public String getGirlDisplayName() {
        if (this.hasCustomName()) {
            return this.getCustomName().getString();
        }
        String id = this.getGirlID();
        return id.isEmpty() ? "Girl" : Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }

    public List<Component> giftRepliesLike() {
        return List.of(
                Component.translatable("chat.pleasurehorizons.gift_like.1"),
                Component.translatable("chat.pleasurehorizons.gift_like.2"),
                Component.translatable("chat.pleasurehorizons.gift_like.3")
        );
    }

    public List<Component> giftRepliesLove() {
        return List.of(
                Component.translatable("chat.pleasurehorizons.gift_love.1"),
                Component.translatable("chat.pleasurehorizons.gift_love.2"),
                Component.translatable("chat.pleasurehorizons.gift_love.3")
        );
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
