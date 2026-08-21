package com.sandymandy.pleasurehorizons.entity.base.tamable;

import com.sandymandy.pleasurehorizons.entity.ai.goal.BedGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlAttackWithOwnerGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlTrackOwnerAttackerGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlFollowOwnerGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlGatherItemsGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlGuardBaseGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlGuardOwnerGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlChopTreesGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlCookGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlFeedOwnerGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlHarvestCropsGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlHuntGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlOpenDoorGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlSelfHealGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlSitGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlStayNearBaseGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlTemptGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.MoveToPlayerGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.StationaryContactGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.StopMovementGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.StripGoal;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.entity.PleasureHorizonsEntityStatuses;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import com.sandymandy.pleasurehorizons.screen.GirlInventoryScreenHandlerFactory;
import com.sandymandy.pleasurehorizons.util.inventory.GirlInventory;
import com.sandymandy.pleasurehorizons.util.managers.TamedGirlRegistry;
import com.sandymandy.pleasurehorizons.util.variables.GirlRole;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
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
import java.util.function.Consumer;

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
    private static final EntityDataAccessor<Boolean> CHOP_TREES =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FEED_OWNER =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> COOK =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HUNT =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> ROLE =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.STRING);
    // ---- fine-tuning settings (Settings tab). Booleans default to the pre-settings behaviour,
    // ---- except followTeleport which matches vanilla tamed pets. Modes: 0 = low, 1 = normal,
    // ---- 2 = high; the derived helpers below translate them into gameplay numbers.
    private static final EntityDataAccessor<Boolean> FOLLOW_TELEPORT =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CLOSE_DOORS =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> AVOID_WATER =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> AUTO_DELIVER =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> AUTO_EQUIP_ARMOR =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> AVOID_CREEPERS =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HIGH_JUMP =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> FOLLOW_DISTANCE_MODE =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WORK_PACE_MODE =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WORK_RADIUS_MODE =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GUARD_RANGE_MODE =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STAY_RADIUS_MODE =
            SynchedEntityData.defineId(TameableGirlEntity.class, EntityDataSerializers.INT);
    // The girl is carried directly in front of the carrier, facing them (GirlRenderer yaw-flips
    // the model 180°). The forward offset puts her center just in front of the player's front
    // face, and the positive vertical offset holds her up at chest height instead of letting her
    // sink toward the ground.
    private static final double CARRY_FORWARD_OFFSET = 0.45D;
    private static final double CARRY_VERTICAL_OFFSET = 0.10D;
    /** Beyond this gap to a followed owner the entity-level failsafe teleports her over. */
    private static final double FAR_FOLLOW_TELEPORT_SQ = 32.0D * 32.0D;

    /** Last backpack fill broadcast, so the HUD status only syncs when it actually changes. */
    private int lastSentBackpackSlots = -1;

    /** Carrier's sneak state, used to put her down on a fresh sneak press while carried. */
    private boolean carrierSneaking = false;

    /**
     * Game time of the last tracking resync sent for this girl. Recent teleports schedule
     * follow-up resyncs so a wedged client copy (visible again only after relogging) is
     * rebuilt within seconds instead of requiring a relog.
     */
    private long lastTrackingResyncTick = Long.MIN_VALUE;

    protected TameableGirlEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        // Let her path through (and open) wooden doors like a villager, so following/guarding
        // the owner does not leave her stuck behind closed doors.
        if (this.getNavigation() instanceof GroundPathNavigation navigation) {
            navigation.setCanOpenDoors(true);
        }
        // Make survival tasks route around water instead of wading in and swimming slowly.
        // Following still crosses water: GirlFollowOwnerGoal temporarily zeroes this malus.
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER, 8.0F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TAMED, false);
        builder.define(OWNER_UUID, Optional.empty());
        builder.define(CHOP_TREES, false);
        builder.define(FEED_OWNER, false);
        builder.define(COOK, false);
        builder.define(HUNT, false);
        builder.define(ROLE, GirlRole.IDLE.id());
        builder.define(FOLLOW_TELEPORT, true);
        builder.define(CLOSE_DOORS, true);
        builder.define(AVOID_WATER, true);
        builder.define(AUTO_DELIVER, false);
        builder.define(AUTO_EQUIP_ARMOR, false);
        builder.define(AVOID_CREEPERS, false);
        builder.define(HIGH_JUMP, false);
        builder.define(FOLLOW_DISTANCE_MODE, 1);
        builder.define(WORK_PACE_MODE, 1);
        builder.define(WORK_RADIUS_MODE, 1);
        builder.define(GUARD_RANGE_MODE, 1);
        builder.define(STAY_RADIUS_MODE, 1);
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
        this.goalSelector.addGoal(1, new GirlOpenDoorGoal(this));
        // Self-healing declares no flags, so it never preempts anything; it just eats when hurt.
        this.goalSelector.addGoal(3, new GirlSelfHealGoal(this));
        // Combat goal is added by SettlementGirlEntityAI, which is the level that can
        // actually fire a bow (it implements RangedAttackMob).
        this.registerCombatGoals();
        // Following is the fallback, not the default: every survival task above this priority
        // (harvest/gather/chop/feed/cook/stay-near-base) preempts it whenever it has work to do,
        // so a girl with a role assigned both works and follows her owner when idle. It sits at
        // the same priority as tempt/stroll but is registered first, so it still wins that tie.
        this.goalSelector.addGoal(6, new GirlFollowOwnerGoal(this, 1.1D, 4.0F, 2.0F));
        // Delivery outranks the other work goals on a priority tie (registered first): once the
        // backpack is full, more gathering is pointless, so she takes the loot to her owner.
        this.goalSelector.addGoal(4, new com.sandymandy.pleasurehorizons.entity.ai.goal.GirlDeliverLootGoal(this));
        this.goalSelector.addGoal(4, new GirlHarvestCropsGoal(this)); // toggleable via isHarvestEnabled
        this.goalSelector.addGoal(5, new GirlGatherItemsGoal(this)); // toggleable via isGatherEnabled
        this.goalSelector.addGoal(5, new GirlChopTreesGoal(this)); // toggleable via isChopTreesEnabled
        this.goalSelector.addGoal(5, new GirlFeedOwnerGoal(this)); // toggleable via isFeedOwnerEnabled
        this.goalSelector.addGoal(5, new GirlCookGoal(this)); // toggleable via isCookEnabled
        this.goalSelector.addGoal(5, new GirlStayNearBaseGoal(this, 1.0D, 3.0F, 10.0F)); // toggleable
        this.goalSelector.addGoal(6, new GirlTemptGoal(this, 1.0D, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        // These two are the reason a carried girl kept spinning on the spot: the vanilla
        // look goals do not know about being a passenger, so they carried on picking new
        // look targets and rotating her while the player carried her.
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
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                // Friendly fire (a swept sword, an AoE, an accidental hit) marks the owner or a
                // sister as the last attacker; never retaliate against either.
                LivingEntity lastHurt = TameableGirlEntity.this.getLastHurtByMob();
                if (lastHurt != null && (TameableGirlEntity.this.isOwner(lastHurt)
                        || lastHurt instanceof TameableGirlEntity)) {
                    return false;
                }
                return super.canUse();
            }

            @Override
            protected boolean canAttack(@Nullable LivingEntity target, net.minecraft.world.entity.ai.targeting.TargetingConditions conditions) {
                if (target != null && (TameableGirlEntity.this.isOwner(target)
                        || target instanceof TameableGirlEntity)) {
                    return false;
                }
                return super.canAttack(target, conditions);
            }
        });
        // Defend the owner: retaliate against whoever hurt them, and join their fights.
        this.targetSelector.addGoal(1, new GirlTrackOwnerAttackerGoal(this));
        this.targetSelector.addGoal(1, new GirlAttackWithOwnerGoal(this, Player.class, TameableGirlEntity.class));
        // Owner guard is checked before base guard, so a girl with both toggles on (the GUARD
        // role enables both) defends her owner first and only falls back to guarding the base
        // when no hostile threatens the owner.
        this.targetSelector.addGoal(2, new GirlGuardOwnerGoal(this));
        this.targetSelector.addGoal(2, new GirlGuardBaseGoal(this)); // guard base when enabled
        // Pack tactics sits above hunting: shared fights with sisters beat hunting a cow, and
        // the goal never preempts an existing target, it only fills an empty slot.
        this.targetSelector.addGoal(2, new com.sandymandy.pleasurehorizons.entity.ai.goal.GirlPackTacticsGoal(this));
        // Hunting is the lowest-priority target source: hostiles always take precedence.
        this.targetSelector.addGoal(2, new GirlHuntGoal(this));
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
        // Keep the summon registry in sync: remember a tamed girl's location, drop her when she
        // is released, and ignore the client (the registry is server-only).
        if (!this.level().isClientSide()) {
            if (uuid == null) {
                TamedGirlRegistry.remove(this.getUUID());
            } else {
                TamedGirlRegistry.update(this);
            }
        }
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

    // ------------------------------------------------------------ summoning

    /**
     * Teleports her to her owner (same dimension). Used by the "call girls" keybind and
     * {@code /girls call}, so a tamed girl can always reach and defend the player no matter how
     * far away she was left. Only loaded girls can be teleported - an unloaded entity does not
     * exist in memory, so she is skipped if her chunk is not loaded.
     */
    public boolean callToOwner(Player owner) {
        if (this.level().isClientSide() || owner == null || this.isSceneActive()) {
            return false;
        }

        // A manual summon (G key / /girls call) must always do something, so unlike the
        // automatic follow-teleport it falls back to a spot above the owner.
        BlockPos target = findSafeSpotNear(this, owner);
        if (target == null) {
            target = owner.blockPosition().above();
        }
        double x = target.getX() + 0.5D;
        double y = target.getY();
        double z = target.getZ() + 0.5D;
        this.stopRiding();
        this.getNavigation().stop();
        this.setTarget(null);
        this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        this.setNoGravity(false);
        this.resetFallDistance();
        this.teleportTo(x, y, z);
        if (owner instanceof ServerPlayer serverPlayer) {
            resyncTo(serverPlayer);
        }
        return true;
    }

    /**
     * Standable spot BESIDE the player - never her own column. The old version returned the
     * owner's feet block first, so teleported girls materialised inside the player: their
     * AABB ends up wrapped around the camera, which vanilla still renders, but shader and
     * culling mods (Iris + EntityCulling/MoreCulling) cull such an entity outright - the
     * reported "she teleports to me and fights, but is completely invisible, no shadow".
     * Rings of 1, then 2 blocks; small vertical scan per column; collision-checked.
     */
    private static BlockPos findSafeSpotNear(TameableGirlEntity girl, Player player) {
        BlockPos center = player.blockPosition();
        // Vanilla pets never land within 2 blocks of the owner (TamableAnimal#
        // teleportToAroundBlockPos requires |dx|>=2 or |dz|>=2) and require a WALKABLE spot
        // with solid, non-leaf ground. Rings of 2, then 3 blocks; vertical wiggle of one.
        for (int radius = 2; radius <= 3; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    for (int y : new int[]{0, 1, -1}) {
                        BlockPos candidate = center.offset(dx, y, dz);
                        if (isWalkableTeleportSpot(girl, candidate)
                                && girl.level().noCollision(girl, girl.getBoundingBox().move(
                                candidate.getX() + 0.5D - girl.getX(),
                                candidate.getY() - girl.getY(),
                                candidate.getZ() + 0.5D - girl.getZ()))) {
                            return candidate;
                        }
                    }
                }
            }
        }
        // Strict pass failed (owner on a bridge, in a boat, swimming): fall back to any
        // collision-free spot so she still ARRIVES - standing in water or on a narrow ledge
        // beats being stranded behind, which reads in-game as "she never teleports".
        for (int radius = 2; radius <= 4; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    BlockPos candidate = center.offset(dx, 0, dz);
                    if (girl.level().noCollision(girl, girl.getBoundingBox().move(
                            candidate.getX() + 0.5D - girl.getX(),
                            candidate.getY() - girl.getY(),
                            candidate.getZ() + 0.5D - girl.getZ()))) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    /** Vanilla TamableAnimal#canTeleportTo: walkable path type, no leaves underfoot. */
    private static boolean isWalkableTeleportSpot(TameableGirlEntity girl, BlockPos pos) {
        net.minecraft.world.level.pathfinder.PathType pathType =
                net.minecraft.world.level.pathfinder.WalkNodeEvaluator.getPathTypeStatic(girl, pos);
        if (pathType != net.minecraft.world.level.pathfinder.PathType.WALKABLE) {
            return false;
        }
        net.minecraft.world.level.block.state.BlockState below = girl.level().getBlockState(pos.below());
        return !(below.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock);
    }

    /**
     * Teleports the girl right next to the player, used by follow-teleport (vanilla tamed-pet
     * behaviour: never get permanently lost behind terrain, water or a cliff).
     */
    /**
     * Deterministic client-side rebuild of this entity for one player: removes any stale
     * client copy, then re-sends the exact pairing flow vanilla uses when an entity enters
     * view distance (add + equipment + position, plus our custom pairing payloads).
     */
    private void resyncTo(ServerPlayer player) {
        this.lastTrackingResyncTick = this.level().getGameTime();
        com.sandymandy.pleasurehorizons.PleasureHorizons.LOGGER.info(
                "[tracking] resync girl {} ({}) -> {}", this.getId(), this.getGirlID(),
                player.getGameProfile().getName());
        int id = this.getId();
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket(id));
        // Full public constructor: id, uuid, position, rotations, type, data, velocity, head yaw.
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundAddEntityPacket(
                id, this.getUUID(), this.getX(), this.getY(), this.getZ(),
                this.getXRot(), this.getYRot(), this.getType(), 0,
                this.getDeltaMovement(), (double) this.getYHeadRot()));
        java.util.List<net.minecraft.network.syncher.SynchedEntityData.DataValue<?>> data =
                this.getEntityData().getNonDefaultValues();
        if (data != null) {
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket(id, data));
        }
        player.connection.send(new ClientboundTeleportEntityPacket(this));
        List<com.mojang.datafixers.util.Pair<EquipmentSlot, ItemStack>> equipment = new java.util.ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR || slot.getType() == EquipmentSlot.Type.HAND) {
                equipment.add(com.mojang.datafixers.util.Pair.of(slot, this.getItemBySlot(slot)));
            }
        }
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket(id, equipment));
        // Custom pairing payloads (same content sendPairingData would deliver).
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                currentClothingAndArmorPacket(),
                new com.sandymandy.pleasurehorizons.networking.S2C.GirlStatusS2CPacket(id, usedBackpackSlots()));
    }

    public boolean teleportNear(Player player) {
        if (this.level().isClientSide() || this.level() != player.level() || this.isSceneActive()) {
            return false;
        }
        BlockPos target = findSafeSpotNear(this, player);
        if (target == null) {
            // Vanilla TamableAnimal behaviour: with no walkable spot around the owner a
            // teleport simply does not happen (she keeps pathing on foot) - a forced landing
            // into a bad spot is exactly how she used to end up inside the player.
            return false;
        }
        double x = target.getX() + 0.5D;
        double y = target.getY();
        double z = target.getZ() + 0.5D;
        double distanceBeforeSq = this.distanceToSqr(player);
        this.getNavigation().stop();
        this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        // Entity#teleportTo does NOT reset fall distance; a girl teleported mid-fall
        // (routine with high jump on) would land already "fallen" and take the damage.
        this.resetFallDistance();
        this.teleportTo(x, y, z);
        if (player instanceof ServerPlayer serverPlayer) {
            if (distanceBeforeSq > 64.0D * 64.0D) {
                // Long hop: rebuild the owner's client-side copy outright. A stale or wedged
                // tracker (async-tracking mods like c2me) otherwise leaves her invisible and
                // unclickable while the server keeps playing her - remove + full re-pair is
                // the same packet flow as her leaving and re-entering view distance, so it is
                // safe everywhere and heals whatever the tracker missed.
                resyncTo(serverPlayer);
            } else {
                serverPlayer.connection.send(new ClientboundTeleportEntityPacket(this));
            }
        }
        return true;
    }

    /** Matches a user-supplied name against her custom name or her rig id (case-insensitive). */
    public boolean matchesName(String name) {
        if (name == null || name.isEmpty()) {
            return true;
        }
        if (this.hasCustomName() && this.getCustomName().getString().equalsIgnoreCase(name)) {
            return true;
        }
        return this.getGirlID().equalsIgnoreCase(name);
    }

    @Override
    public void remove(net.minecraft.world.entity.Entity.RemovalReason reason) {
        // Chunk unload goes through Entity#setRemoved directly (not through remove()), so the
        // registry keeps her last known position and the call can still reach her. Only actual
        // removal (death, discard) drops the entry.
        if (!this.level().isClientSide() && reason.shouldDestroy()) {
            TamedGirlRegistry.remove(this.getUUID());
        }
        super.remove(reason);
    }

    /**
     * Teleports every owned girl to the player. Pass a non-empty {@code name} to limit the call
     * to a single girl matching her custom name or rig id. Returns the count of girls that will
     * be summoned.
     *
     * <p>Loaded girls are teleported immediately. Girls in unloaded chunks are looked up in the
     * server-side {@link TamedGirlRegistry}, their chunk is force-loaded, and they are teleported
     * once the chunk actually loads (see {@link #tickPendingCalls}).</p>
     */
    public static int callOwnedGirlsTo(ServerPlayer owner, @Nullable String name) {
        int called = 0;
        ServerLevel level = owner.serverLevel();

        // Loaded girls: teleport now.
        for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
            if (entity instanceof TameableGirlEntity girl
                    && girl.isTamed()
                    && girl.isOwner(owner)
                    && girl.matchesName(name)
                    && girl.callToOwner(owner)) {
                called++;
            }
        }

        // Unloaded girls: force-load their chunk and teleport once they load.
        for (TamedGirlRegistry.Entry entry : TamedGirlRegistry.ownedBy(owner.getUUID(), name)) {
            if (level.getEntity(entry.girlId()) != null) {
                continue; // already handled above as a loaded entity
            }
            if (!level.dimension().equals(entry.dimension())) {
                continue; // cannot cross dimensions; she will be reachable from her own dimension
            }

            net.minecraft.world.level.ChunkPos chunk = new net.minecraft.world.level.ChunkPos(
                    net.minecraft.util.Mth.floor(entry.x()) >> 4,
                    net.minecraft.util.Mth.floor(entry.z()) >> 4);
            level.setChunkForced(chunk.x, chunk.z, true);
            PENDING_CALLS.add(new PendingCall(entry.girlId(), owner.getUUID(),
                    level.dimension(), chunk, level.getGameTime() + 100));
            called++;
        }
        return called;
    }

    /** A summon request waiting for a force-loaded chunk to actually load. */
    private record PendingCall(UUID girlId, UUID ownerId,
                               net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,
                               net.minecraft.world.level.ChunkPos chunk, long deadlineTick) {
    }

    private static final java.util.List<PendingCall> PENDING_CALLS = new java.util.ArrayList<>();

    /** Completes pending summons for one level; called from the server tick. */
    public static void tickPendingCalls(ServerLevel level) {
        if (PENDING_CALLS.isEmpty()) {
            return;
        }

        java.util.Iterator<PendingCall> it = PENDING_CALLS.iterator();
        while (it.hasNext()) {
            PendingCall call = it.next();
            if (!level.dimension().equals(call.dimension())) {
                continue;
            }

            net.minecraft.world.entity.Entity entity = level.getEntity(call.girlId());
            if (entity instanceof TameableGirlEntity girl && girl.isTamed()) {
                ServerPlayer owner = level.getServer().getPlayerList().getPlayer(call.ownerId());
                if (owner != null && girl.isOwner(owner)) {
                    girl.callToOwner(owner);
                }
                level.setChunkForced(call.chunk().x, call.chunk().z, false);
                it.remove();
            } else if (level.getGameTime() > call.deadlineTick()) {
                level.setChunkForced(call.chunk().x, call.chunk().z, false);
                it.remove();
            }
        }
    }

    /** Drops every pending summon (e.g. when the integrated server restarts). */
    public static void clearPendingCalls() {
        PENDING_CALLS.clear();
    }

    // ------------------------------------------------- survival utility toggles

    public void setChopTreesEnabled(boolean enabled) {
        this.entityData.set(CHOP_TREES, enabled);
    }

    public boolean isChopTreesEnabled() {
        return this.entityData.get(CHOP_TREES);
    }

    public void setFeedOwnerEnabled(boolean enabled) {
        this.entityData.set(FEED_OWNER, enabled);
    }

    public boolean isFeedOwnerEnabled() {
        return this.entityData.get(FEED_OWNER);
    }

    public void setCookEnabled(boolean enabled) {
        this.entityData.set(COOK, enabled);
    }

    public boolean isCookEnabled() {
        return this.entityData.get(COOK);
    }

    public void setHuntEnabled(boolean enabled) {
        this.entityData.set(HUNT, enabled);
    }

    public boolean isHuntEnabled() {
        return this.entityData.get(HUNT);
    }

    /** Role label for the HUD and the inventory "Next Role" button. */
    public GirlRole getRole() {
        return GirlRole.fromId(this.entityData.get(ROLE));
    }    /**
     * Assigns a role: applies its toggle preset and records the label.
     * Server-only - toggles are server-owned synched data.
     */
    public void setRole(GirlRole role) {
        if (this.level().isClientSide()) return;
        if (role == null) role = GirlRole.IDLE;
        this.entityData.set(ROLE, role.id());
        role.applyTo(this);
    }

    public void cycleRole() {
        if (this.level().isClientSide()) return;
        this.setRole(this.getRole().next());
    }

    // -------------------------------------------------------- fine-tune settings

    public boolean isFollowTeleportEnabled() {
        return this.entityData.get(FOLLOW_TELEPORT);
    }

    public void setFollowTeleportEnabled(boolean enabled) {
        this.entityData.set(FOLLOW_TELEPORT, enabled);
    }

    public boolean isCloseDoorsEnabled() {
        return this.entityData.get(CLOSE_DOORS);
    }

    public void setCloseDoorsEnabled(boolean enabled) {
        this.entityData.set(CLOSE_DOORS, enabled);
    }

    public boolean isAvoidWaterEnabled() {
        return this.entityData.get(AVOID_WATER);
    }

    /** Also updates the live pathfinding malus, so the change applies without a relog. */
    public void setAvoidWaterEnabled(boolean enabled) {
        this.entityData.set(AVOID_WATER, enabled);
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER, enabled ? 8.0F : 0.0F);
    }

    public boolean isAutoDeliverEnabled() {
        return this.entityData.get(AUTO_DELIVER);
    }

    public void setAutoDeliverEnabled(boolean enabled) {
        this.entityData.set(AUTO_DELIVER, enabled);
    }

    public boolean isAutoEquipArmorEnabled() {
        return this.entityData.get(AUTO_EQUIP_ARMOR);
    }

    public void setAutoEquipArmorEnabled(boolean enabled) {
        this.entityData.set(AUTO_EQUIP_ARMOR, enabled);
    }

    public boolean isAvoidCreepersEnabled() {
        return this.entityData.get(AVOID_CREEPERS);
    }

    public void setAvoidCreepersEnabled(boolean enabled) {
        this.entityData.set(AVOID_CREEPERS, enabled);
    }

    public boolean isHighJumpEnabled() {
        return this.entityData.get(HIGH_JUMP);
    }

    /**
     * High-jump toggle: raises the jump-strength attribute so every jump (path hops, leaving
     * water, leaping at a target) carries her about 4-5 blocks up. The softened landing below
     * keeps her own leaps from hurting her; real cliffs stay dangerous.
     */
    public void setHighJumpEnabled(boolean enabled) {
        this.entityData.set(HIGH_JUMP, enabled);
        var jump = this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH);
        if (jump != null) {
            jump.setBaseValue(enabled ? 0.8D : 0.42D);
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        if (this.isHighJumpEnabled()) {
            // A 0.8-power jump lands from ~5-6 blocks up; discount that first so her own leaps
            // are free while genuine cliffs still hurt.
            fallDistance = Math.max(0.0F, fallDistance - 8.0F);
        }
        return super.causeFallDamage(fallDistance, multiplier, source);
    }

    /**
     * Target filter for the guard goals: when "avoid creepers" is on, creepers are skipped so
     * she never triggers an explosion next to herself (or the base).
     */
    public boolean isAvoidCreepersEnabled(net.minecraft.world.entity.monster.Monster monster) {
        return this.isAvoidCreepersEnabled() && monster instanceof net.minecraft.world.entity.monster.Creeper;
    }

    public int getFollowDistanceMode() {
        return this.entityData.get(FOLLOW_DISTANCE_MODE);
    }

    public void setFollowDistanceMode(int mode) {
        this.entityData.set(FOLLOW_DISTANCE_MODE, Math.floorMod(mode, 3));
    }

    public int getWorkPaceMode() {
        return this.entityData.get(WORK_PACE_MODE);
    }

    public void setWorkPaceMode(int mode) {
        this.entityData.set(WORK_PACE_MODE, Math.floorMod(mode, 3));
    }

    public int getWorkRadiusMode() {
        return this.entityData.get(WORK_RADIUS_MODE);
    }

    public void setWorkRadiusMode(int mode) {
        this.entityData.set(WORK_RADIUS_MODE, Math.floorMod(mode, 3));
    }

    public int getGuardRangeMode() {
        return this.entityData.get(GUARD_RANGE_MODE);
    }

    public void setGuardRangeMode(int mode) {
        this.entityData.set(GUARD_RANGE_MODE, Math.floorMod(mode, 3));
    }

    public int getStayRadiusMode() {
        return this.entityData.get(STAY_RADIUS_MODE);
    }

    public void setStayRadiusMode(int mode) {
        this.entityData.set(STAY_RADIUS_MODE, Math.floorMod(mode, 3));
    }

    /** Follow gap before she starts catching up: close / normal / far. */
    public float followStartDistance() {
        return switch (this.getFollowDistanceMode()) {
            case 0 -> 3.0F;
            case 2 -> 8.0F;
            default -> 5.0F;
        };
    }

    /** Follow distance at which catching up stops: close / normal / far. */
    public float followStopDistance() {
        return switch (this.getFollowDistanceMode()) {
            case 0 -> 1.5F;
            case 2 -> 4.0F;
            default -> 2.5F;
        };
    }

    /** Movement speed multiplier for the work goals: calm / normal / fast. */
    public double workSpeedModifier() {
        return switch (this.getWorkPaceMode()) {
            case 0 -> 1.0D;
            case 2 -> 1.6D;
            default -> 1.3D;
        };
    }

    /** Scale for the work goals' scan ranges (harvest/chop/gather/cook): compact / normal / wide. */
    public double workRadiusScale() {
        return switch (this.getWorkRadiusMode()) {
            case 0 -> 0.65D;
            case 2 -> 1.5D;
            default -> 1.0D;
        };
    }

    /** Guard-owner scan range: near / normal / wide. */
    public double guardScanRange() {
        return switch (this.getGuardRangeMode()) {
            case 0 -> 8.0D;
            case 2 -> 20.0D;
            default -> 12.0D;
        };
    }

    /** Stay-near-base outer radius: close / normal / far. */
    public double stayNearBaseMaxDistance() {
        return switch (this.getStayRadiusMode()) {
            case 0 -> 6.0D;
            case 2 -> 20.0D;
            default -> 10.0D;
        };
    }

    /** Stay-near-base inner radius (she stops approaching once inside): close / normal / far. */
    public double stayNearBaseMinDistance() {
        return switch (this.getStayRadiusMode()) {
            case 0 -> 2.0D;
            case 2 -> 6.0D;
            default -> 3.0D;
        };
    }

    /** True when every backpack slot holds something (triggers auto-delivery when enabled). */
    public boolean isBackpackFull() {
        return this.usedBackpackSlots() >= GirlInventory.BACKPACK_END - GirlInventory.BACKPACK_START + 1;
    }

    /** How many backpack slots currently hold an item; drives the HUD fill indicator. */
    public int usedBackpackSlots() {
        int used = 0;
        for (int i = GirlInventory.BACKPACK_START; i <= GirlInventory.BACKPACK_END; i++) {
            if (!this.inventory.getItem(i).isEmpty()) {
                used++;
            }
        }
        return used;
    }

    /**
     * Hands her gathered backpack contents to a player.
     *
     * <p>{@code Inventory#add} mutates the passed stack down to whatever did not fit, so the
     * leftover stays in her backpack. This keeps loot flowing to the owner without dropping it
     * on the ground or silently deleting a full inventory.</p>
     */
    public void giveBackpackTo(Player player) {
        if (this.level().isClientSide()) return;

        GirlInventory inv = this.getInventory();
        for (int i = GirlInventory.BACKPACK_START; i <= GirlInventory.BACKPACK_END; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            player.getInventory().add(stack);
            inv.setItem(i, stack);
        }
    }

    /**
     * Auto-equip: moves strictly better armour pieces from the backpack into the matching armour
     * slot (one piece per check). Fills empty slots first and swaps only when the candidate's
     * raw defence is strictly higher.
     */
    private void autoEquipArmorFromBackpack() {
        GirlInventory inv = this.getInventory();
        for (int i = GirlInventory.BACKPACK_START; i <= GirlInventory.BACKPACK_END; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof net.minecraft.world.item.ArmorItem armor)) {
                continue;
            }
            EquipmentSlot slot = armor.getEquipmentSlot();
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
                continue;
            }
            ItemStack current = this.getItemBySlot(slot);
            boolean better = current.isEmpty()
                    || (current.getItem() instanceof net.minecraft.world.item.ArmorItem currentArmor
                        && armor.getDefense() > currentArmor.getDefense());
            if (better) {
                inv.setItem(i, current.copy());
                this.setItemSlot(slot, stack);
                return; // one swap per check keeps it readable in-game (piece by piece)
            }
        }
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
        // Administrative removal and falling out of the world must remain able to remove a girl;
        // neither movement lock nor the downed-state substitution should intercept them.
        if (source.is(DamageTypes.GENERIC_KILL) || source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return super.hurt(source, amount);
        }
        // The owner always has authority over her life: their damage passes straight through, so
        // they can finish off even a downed girl. Mobs instead only knock her down (below) and
        // cannot land the killing blow.
        if (this.isDamageFromOwner(source)) {
            return super.hurt(source, amount);
        }
        // Squad mates never harm each other. A sister's stray sword swing, sweep edge or arrow
        // deals no damage at all - which also means no lastHurtByMob entry, so HurtByTarget can
        // never start a vendetta between two girls.
        if (source.getEntity() instanceof TameableGirlEntity) {
            return false;
        }
        if (this.isDowned() || this.isMovementLocked()) {
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

    /** True when the damage was dealt by her owner (covers both melee and owner-fired arrows). */
    private boolean isDamageFromOwner(DamageSource source) {
        net.minecraft.world.entity.Entity attacker = source.getEntity();
        return attacker != null
                && attacker != this
                && this.getOwnerUUID() != null
                && attacker.getUUID().equals(this.getOwnerUUID());
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
            this.putDown(player);
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

        // Picking her up requires sneaking, so remember that the carrier is already sneaking:
        // only a fresh sneak press later will put her down, not the held one.
        this.carrierSneaking = true;

        this.setNoGravity(true);
        this.syncCarryState(player);
        player.displayClientMessage(
                Component.translatable("msg.pleasurehorizons.girl_picked_up", this.getGirlDisplayName()), true);
        return InteractionResult.SUCCESS;
    }

    /**
     * Dismounts her from the carrier and places her in front of him.
     *
     * <p>Called both by shift+right-click and by a fresh sneak press while carried. Sneaking is
     * the reliable fallback because in first person the framed model is rendered offset from her
     * actual hitbox, which made her very hard to target with the crosshair.</p>
     */
    private void putDown(Player player) {
        this.stopRiding();
        float yawRad = (float) Math.toRadians(player.getYRot());
        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);
        this.moveTo(player.getX() + forwardX, player.getY(), player.getZ() + forwardZ,
                this.getYRot(), this.getXRot());
        this.setNoGravity(false);
        this.setSitting(false);
        this.carrierSneaking = false;
        this.syncCarryState(player);
        player.displayClientMessage(
                Component.translatable("msg.pleasurehorizons.girl_put_down", this.getGirlDisplayName()), true);
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
     * Defines the complete carry position relative to a player vehicle.
     *
     * <p>Vanilla positions a passenger at
     * {@code vehicle.getPassengerRidingPosition() - passenger.getVehicleAttachmentPoint()}.
     * A player's riding point is at the top of its current dimensions, while the normal vehicle
     * point on a passenger is its feet. Returning feet here therefore puts the girl on the
     * player's head. The old renderer tried to compensate after the fact with another
     * translation, a {@code 0.62} scale and render-time entity movement; those overlapping
     * coordinate systems caused the reported hovering and shrinking.</p>
     *
     * <p>The vertical component aligns the full-size hitboxes, then lowers her slightly into a
     * supported hold. The horizontal component places her directly in front of the carrier, with
     * her front turned toward them (the renderer yaw-flips the model 180°). Because this is the
     * sole positional calculation, normal passenger ticking keeps the server, carrier and
     * observers on the same coordinates.</p>
     */
    @Override
    public net.minecraft.world.phys.Vec3 getVehicleAttachmentPoint(net.minecraft.world.entity.Entity vehicle) {
        if (vehicle instanceof Player player) {
            float yawRadians = player.yBodyRot * ((float) Math.PI / 180.0F);
            // Forward vector in the carrier's facing direction. With yaw=0 the carrier faces
            // south (+Z), so forward = ( -sin, cos ) = (0, 1). She sits directly in front.
            double forwardX = -Math.sin(yawRadians);
            double forwardZ = Math.cos(yawRadians);
            double offsetX = forwardX * CARRY_FORWARD_OFFSET;
            double offsetZ = forwardZ * CARRY_FORWARD_OFFSET;
            double centeredHeight = (vehicle.getBbHeight() + this.getBbHeight()) * 0.5D;

            // Entity#positionRider subtracts this vector, hence the negated desired X/Z offset
            // and subtraction of the desired (negative/downward) vertical offset.
            return new net.minecraft.world.phys.Vec3(
                    -offsetX, centeredHeight - CARRY_VERTICAL_OFFSET, -offsetZ);
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

    /**
     * Includes the backpack fill in the initial tracking bundle so the HUD status panel is
     * correct for a player who starts tracking an already-loaded girl.
     */
    @Override
    public void sendPairingData(ServerPlayer serverPlayer, Consumer<CustomPacketPayload> bundleBuilder) {
        super.sendPairingData(serverPlayer, bundleBuilder);
        bundleBuilder.accept(new com.sandymandy.pleasurehorizons.networking.S2C.GirlStatusS2CPacket(
                this.getId(), usedBackpackSlots()));
    }

    /** Broadcasts the backpack fill to trackers only when it changed (polled like armour). */
    public void updateBackpackStatusIfChanged() {
        if (this.level().isClientSide()) return;
        int used = usedBackpackSlots();
        if (used != lastSentBackpackSlots) {
            lastSentBackpackSlots = used;
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(
                    this, new com.sandymandy.pleasurehorizons.networking.S2C.GirlStatusS2CPacket(
                            this.getId(), used));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.isTamed() && this.tickCount % 40 == 0) {
            // Refresh her last known location so the summon registry can reach her after her
            // chunk unloads.
            TamedGirlRegistry.update(this);
        }
        if (!this.level().isClientSide() && this.tickCount % 20 == 0) {
            updateBackpackStatusIfChanged();
        }
        if (!this.level().isClientSide() && this.isTamed()
                && this.isAutoEquipArmorEnabled() && this.tickCount % 100 == 0) {
            this.autoEquipArmorFromBackpack();
        }
        // Follow-teleport failsafe, independent of which goal currently owns the MOVE flag:
        // the follow goal alone cannot cover the case where a higher-priority work goal is
        // running (or the goal was stopped by its own navigation.isDone() check), which is
        // exactly when girls used to get stranded at "hard distances". Same guards as the
        // goal: no teleport while sitting, downed, in a scene, carried, or off-world.
        if (!this.level().isClientSide() && this.isTamed() && this.isFollowing()
                && this.isFollowTeleportEnabled() && (this.tickCount & 31) == 0
                && !this.isSitting() && !this.isDowned() && !this.isSceneActive()
                && !this.isPassenger()
                && this.getOwner() instanceof Player owner
                && owner.level() == this.level() && !owner.isSpectator()
                && !owner.isFallFlying()
                && this.distanceToSqr(owner) > FAR_FOLLOW_TELEPORT_SQ) {
            this.teleportNear(owner);
        }
        // Self-heal for a wedged client copy ("visible only after relogging"): after a
        // teleport we repeat the deterministic client rebuild a few times. Each retry costs
        // one frame of flicker at most; a healthy client just re-receives its own state.
        if (!this.level().isClientSide() && this.isTamed() && this.tickCount % 20 == 0
                && this.lastTrackingResyncTick != Long.MIN_VALUE
                && this.getOwner() instanceof ServerPlayer owner
                && owner.level() == this.level()
                && this.distanceToSqr(owner) < 64.0D * 64.0D) {
            long since = this.level().getGameTime() - this.lastTrackingResyncTick;
            if (since == 60L || since == 140L || since == 260L) {
                resyncTo(owner);
            }
        }
        // While being carried, ensure she stays nicely positioned and doesn't suffocate
        if (this.isPassenger() && this.getVehicle() instanceof Player player) {
            this.setNoGravity(true);
            // Suppress leftover AI motion; the vehicle positions her every tick.
            this.getNavigation().stop();
            this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);

            // Lock her rotation to the carrier. Without this she keeps her own yaw and any
            // leftover look target spins her around while carried. All four
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
                return;
            }

            // A fresh sneak press while carried puts her down. The crosshair is unreliable in
            // first person (the framed model is drawn offset from her real hitbox), so sneaking
            // is the dependable way to release her.
            if (!this.level().isClientSide()) {
                boolean sneaking = player.isShiftKeyDown();
                if (sneaking && !this.carrierSneaking) {
                    this.putDown(player);
                    return;
                }
                this.carrierSneaking = sneaking;
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
        tag.putBoolean("ChopTrees", this.isChopTreesEnabled());
        tag.putBoolean("FeedOwner", this.isFeedOwnerEnabled());
        tag.putBoolean("Cook", this.isCookEnabled());
        tag.putBoolean("Hunt", this.isHuntEnabled());
        tag.putString("Role", this.getRole().id());
        tag.putBoolean("FollowTeleport", this.isFollowTeleportEnabled());
        tag.putBoolean("CloseDoors", this.isCloseDoorsEnabled());
        tag.putBoolean("AvoidWater", this.isAvoidWaterEnabled());
        tag.putBoolean("AutoDeliver", this.isAutoDeliverEnabled());
        tag.putBoolean("AutoEquipArmor", this.isAutoEquipArmorEnabled());
        tag.putBoolean("AvoidCreepers", this.isAvoidCreepersEnabled());
        tag.putBoolean("HighJump", this.isHighJumpEnabled());
        tag.putInt("FollowDistanceMode", this.getFollowDistanceMode());
        tag.putInt("WorkPaceMode", this.getWorkPaceMode());
        tag.putInt("WorkRadiusMode", this.getWorkRadiusMode());
        tag.putInt("GuardRangeMode", this.getGuardRangeMode());
        tag.putInt("StayRadiusMode", this.getStayRadiusMode());
        if (this.getOwnerUUID() != null) {
            tag.putUUID("Owner", this.getOwnerUUID());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setTamed(tag.getBoolean("Tamed"));
        if (tag.contains("ChopTrees")) this.setChopTreesEnabled(tag.getBoolean("ChopTrees"));
        if (tag.contains("FeedOwner")) this.setFeedOwnerEnabled(tag.getBoolean("FeedOwner"));
        if (tag.contains("Cook")) this.setCookEnabled(tag.getBoolean("Cook"));
        if (tag.contains("Hunt")) this.setHuntEnabled(tag.getBoolean("Hunt"));
        // The role is only a label; the individual toggles above are the authoritative state, so
        // re-applying the preset here would clobber whatever the player saved.
        if (tag.contains("Role")) this.entityData.set(ROLE, GirlRole.fromId(tag.getString("Role")).id());
        if (tag.contains("FollowTeleport")) this.setFollowTeleportEnabled(tag.getBoolean("FollowTeleport"));
        if (tag.contains("CloseDoors")) this.setCloseDoorsEnabled(tag.getBoolean("CloseDoors"));
        if (tag.contains("AvoidWater")) this.setAvoidWaterEnabled(tag.getBoolean("AvoidWater"));
        if (tag.contains("AutoDeliver")) this.setAutoDeliverEnabled(tag.getBoolean("AutoDeliver"));
        if (tag.contains("AutoEquipArmor")) this.setAutoEquipArmorEnabled(tag.getBoolean("AutoEquipArmor"));
        if (tag.contains("AvoidCreepers")) this.setAvoidCreepersEnabled(tag.getBoolean("AvoidCreepers"));
        if (tag.contains("HighJump")) this.setHighJumpEnabled(tag.getBoolean("HighJump"));
        if (tag.contains("FollowDistanceMode")) this.setFollowDistanceMode(tag.getInt("FollowDistanceMode"));
        if (tag.contains("WorkPaceMode")) this.setWorkPaceMode(tag.getInt("WorkPaceMode"));
        if (tag.contains("WorkRadiusMode")) this.setWorkRadiusMode(tag.getInt("WorkRadiusMode"));
        if (tag.contains("GuardRangeMode")) this.setGuardRangeMode(tag.getInt("GuardRangeMode"));
        if (tag.contains("StayRadiusMode")) this.setStayRadiusMode(tag.getInt("StayRadiusMode"));
        if (tag.hasUUID("Owner")) {
            this.setOwnerUUID(tag.getUUID("Owner"));
        }
    }
}
