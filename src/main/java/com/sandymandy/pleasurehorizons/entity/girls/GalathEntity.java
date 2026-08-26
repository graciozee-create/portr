package com.sandymandy.pleasurehorizons.entity.girls;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.item.PleasureHorizonsItems;
import com.sandymandy.pleasurehorizons.networking.S2C.GalathGrabScreenS2CPacket;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Galath - a hostile mini-boss from the original Jenny/Fapcraft mod.
 *
 * <p>The original character starts as an enemy in the Nether: she is aggressive, has boss-tier
 * stats, and can only become a companion after the player defeats her. This is the full port
 * of that fight, including the wave/skeleton/grab combat extras:</p>
 *
 * <ul>
 *   <li>Boss stats: 300 HP, 8 damage, follow 64, speed 0.3, 50 XP.</li>
 *   <li>Untamed Galath hunts survival players and periodically releases a wither/weakness
 *       energy wave and summons skeletons while she is damaged.</li>
 *   <li>Every 15 seconds she can grab a nearby player: the victim has 8 seconds to mash A/D
 *       (60 escape points); success knocks them free, failure deals heavy damage.</li>
 *   <li>When defeated she vanishes, drops her soul coin + nether stars, and the winning player
 *       is marked so the coin (summon/dismiss) works in survival.</li>
 * </ul>
 */
public class GalathEntity extends SettlementGirlEntityAI implements PlayerRideableJumping {

    private static final String DEFEATED_KEY = "PleasureHorizonsGalathDefeated";
    private static final int RIDE_COOLDOWN = 100;
    private static final int THREESOME_DISTANCE_SQ = 36;          // owner must stay within 6 blocks

    // Boss combat tuning (canon values from the upstream 1.21.1 port).
    private static final int ENERGY_WAVE_INTERVAL = 200;          // 10 seconds
    private static final double ENERGY_WAVE_RANGE = 6.0D;
    private static final int SKELETON_SUMMON_INTERVAL = 600;      // 30 seconds
    private static final int DAMAGE_FOR_SKELETONS = 30;           // skeleton burst every 30 damage
    private static final int GRAB_INTERVAL = 300;                 // 15 seconds between grab attempts
    private static final int GRAB_RANGE = 3;                      // 3 block range for grab
    private static final int ESCAPE_THRESHOLD = 60;
    private static final int ESCAPE_DURATION = 160;               // 8 seconds
    private static final int GRAB_CUM_DAMAGE = 8;

    private int energyCooldown = 0;
    private int grabCooldown = 0;
    private int grabPhaseTicks = 0;
    private String grabbedPlayerUUID = "";
    private int escapeTaps = 0;
    private int skeletonSummonCooldown = 0;
    private int damageAccumulated = 0;
    private int rideCooldown = 0;

    // Manglelie threesome ("dark ritual") state, mirrored by ManglelieEntity.
    public boolean isInThreesome = false;
    public String threesomePartnerUUID = "";
    public int threesomeTicks = 0;

    public GalathEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        // Hide accessory bones that render unwanted visual elements.
        // The "coin" bone renders a floating coin in front of Galath by default.
        if (!level.isClientSide()) {
            this.setBoneVisibility("coin", false);
            this.setBoneVisibility("energyBallL", false);
            this.setBoneVisibility("energyBallR", false);
            this.setBoneVisibility("offhand", false);
            this.setBoneVisibility("weapon", false);
            this.setBoneVisibility("weaponStart", false);
            this.setBoneVisibility("weaponEnd", false);
            this.setBoneVisibility("customHandL", false);
            this.setBoneVisibility("customHandR", false);
            this.setBoneVisibility("customHead", false);
            this.setBoneVisibility("customShoeL", false);
            this.setBoneVisibility("customShoeR", false);
            this.setBoneVisibility("blocks", false);
        }
    }

    /** Boss-tier stats, mirroring the original {@code GalathEntity#createBossAttributes()}. */
    public static AttributeSupplier.Builder createAttributes() {
        return createDefaultAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    public Item isAttractedTo() {
        return Items.NETHERITE_INGOT;
    }

    @Override
    public String getGirlID() {
        return "galath";
    }

    @Override
    public int getSizeGUI() {
        return 29;
    }

    @Override
    public float getYAxisGUI() {
        return 0.0525F;
    }

    @Override
    public int getBaseExperienceReward() {
        return 50;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Untamed Galath hunts survival players until she is beaten. The shared hierarchy's
        // owner/guard/hunt goals never fire without an owner, so adding this goal turns her
        // into the original's "hostile succubus" without touching the other girls.
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return GalathEntity.this.shouldAggroPlayer() && super.canUse();
            }
        });
    }

    private boolean shouldAggroPlayer() {
        return !this.isTamed() && !this.isDowned()
                && !this.isSceneActive() && !this.isPassenger() && !this.isSitting();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;

        if (this.rideCooldown > 0) this.rideCooldown--;
        this.tickThreesome();

        // Bound Galath is a companion again; her boss attacks stop.
        if (this.isTamed()) return;

        // Active combat grab takes priority over the other attacks.
        if (!this.grabbedPlayerUUID.isEmpty()) {
            this.tickActiveGrab();
            return;
        }

        // Damage-accumulated skeleton burst.
        if (this.damageAccumulated >= DAMAGE_FOR_SKELETONS && this.skeletonSummonCooldown <= 0) {
            this.damageAccumulated = 0;
            this.skeletonSummonCooldown = SKELETON_SUMMON_INTERVAL;
            this.summonSkeletons();
        }

        this.tickEnergyWave();
        this.tickGrabAttempt();

        if (this.skeletonSummonCooldown > 0) this.skeletonSummonCooldown--;

        // Random skeleton summon while actively fighting (1% per tick).
        if (this.getTarget() != null && this.skeletonSummonCooldown <= 0
                && this.random.nextFloat() < 0.01F) {
            this.skeletonSummonCooldown = SKELETON_SUMMON_INTERVAL;
            this.summonSkeletons();
        }
    }

    private void tickEnergyWave() {
        if (this.energyCooldown > 0) {
            this.energyCooldown--;
            return;
        }
        this.energyCooldown = ENERGY_WAVE_INTERVAL;
        LivingEntity target = this.getTarget();
        if (target == null) return;

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY() + 1.5D, this.getZ(),
                    10, 1.0D, 0.5D, 1.0D, 0.12D);
        }

        AABB area = this.getBoundingBox().inflate(ENERGY_WAVE_RANGE);
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && !(e instanceof TameableGirlEntity))) {
            if (entity.getType().getCategory().isFriendly()) continue;
            entity.hurt(this.damageSources().magic(), 4.0F);
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, true));
        }
    }

    private void tickGrabAttempt() {
        if (this.grabCooldown > 0) {
            this.grabCooldown--;
            return;
        }
        if (this.getTarget() == null) return;

        this.grabCooldown = GRAB_INTERVAL;
        Player nearestPlayer = null;
        double nearestDist = (double) GRAB_RANGE * GRAB_RANGE;
        for (Player player : this.level().getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(GRAB_RANGE))) {
            if (player.isCreative() || player.isSpectator()) continue;
            double dist = player.distanceToSqr(this);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearestPlayer = player;
            }
        }
        if (nearestPlayer != null) {
            this.startGrab(nearestPlayer);
        }
    }

    private void tickActiveGrab() {
        Player grabbed = this.grabbedPlayer();
        if (grabbed == null || !grabbed.isAlive() || grabbed.isCreative() || grabbed.isSpectator()) {
            this.releaseGrab(null);
            return;
        }

        this.grabPhaseTicks++;

        if (this.grabPhaseTicks >= ESCAPE_DURATION) {
            grabbed.hurt(this.damageSources().mobAttack(this), GRAB_CUM_DAMAGE);
            grabbed.hurt(this.damageSources().magic(), 4.0F);
            Vec3 knockback = grabbed.position().subtract(this.position()).normalize();
            grabbed.knockback(2.0D, knockback.x, knockback.z);
            this.releaseGrab(grabbed);
            return;
        }

        // Light damage over time while she holds the victim.
        if (this.grabPhaseTicks % 20 == 0) {
            grabbed.hurt(this.damageSources().mobAttack(this), 1.0F + this.random.nextFloat());
        }

        Vec3 lockPos = this.position().add(this.getLookAngle().scale(1.0D));
        grabbed.teleportTo(lockPos.x, this.getY(), lockPos.z);
        grabbed.setYRot(this.getYRot());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean wasDowned = this.isDowned();
        boolean result = super.hurt(source, amount);
        if (!this.level().isClientSide() && !this.isTamed() && !wasDowned && this.isDowned()) {
            this.onDefeated(source);
        } else if (!this.level().isClientSide() && !this.isTamed() && result) {
            // Skeleton summon tracks real damage taken while the boss is still alive.
            this.damageAccumulated += (int) amount;
        }
        return result;
    }

    private void onDefeated(DamageSource source) {
        Player player = playerFromDamage(source);
        this.setTarget(null);
        this.getNavigation().stop();

        // If she goes down mid-grab, release the victim and close the escape screen.
        if (!this.grabbedPlayerUUID.isEmpty()) {
            Player grabbed = this.grabbedPlayer();
            this.grabbedPlayerUUID = "";
            this.grabPhaseTicks = 0;
            this.escapeTaps = 0;
            if (grabbed instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer,
                        new GalathGrabScreenS2CPacket(this.getId(), false));
            }
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY() + 1.5D, this.getZ(),
                    12, 0.4D, 0.5D, 0.4D, 0.08D);
            if (player != null) {
                // Canon flow: only a player's victory grants the soul-binding coin (and a
                // handful of nether stars). Environmental deaths drop no reward.
                markDefeatedFor(player);
                this.spawnAtLocation(new ItemStack(PleasureHorizonsItems.GALATH_COIN.get()));
                int stars = 1 + this.random.nextInt(3);
                this.spawnAtLocation(new ItemStack(Items.NETHER_STAR, stars));
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.giveExperiencePoints(this.getBaseExperienceReward());
                }
            }
        }
        this.playSound(SoundEvents.WITHER_SPAWN, 0.8F, 0.6F);

        if (player != null) {
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.galath_defeated", this.getGirlDisplayName()),
                    true);
        }

        // Canon flow: she is gone after losing; the dropped coin is what summons the bound Galath.
        this.discard();
    }

    /** Summon 2-3 skeletons that fight for Galath. */
    private void summonSkeletons() {
        if (this.level().isClientSide()) return;

        int count = 2 + this.random.nextInt(2);
        for (int i = 0; i < count; i++) {
            Skeleton skeleton = EntityType.SKELETON.create(this.level());
            if (skeleton == null) continue;
            double angle = this.random.nextDouble() * Math.PI * 2;
            double dist = 2.0D + this.random.nextDouble() * 3.0D;
            double sx = this.getX() + Math.cos(angle) * dist;
            double sz = this.getZ() + Math.sin(angle) * dist;
            skeleton.setPos(sx, this.getY(), sz);
            LivingEntity target = this.getTarget();
            if (target != null) skeleton.setTarget(target);
            skeleton.setPersistenceRequired();
            this.level().addFreshEntity(skeleton);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        sx, this.getY(), sz, 8, 0.3D, 0.5D, 0.3D, 0.05D);
            }
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY() + 1.5D, this.getZ(),
                    10, 1.0D, 0.5D, 1.0D, 0.1D);
        }
    }

    private void startGrab(Player player) {
        this.grabbedPlayerUUID = player.getUUID().toString();
        this.grabPhaseTicks = 0;
        this.escapeTaps = 0;
        this.getNavigation().stop();
        Vec3 lockPos = this.position().add(this.getLookAngle().scale(1.0D));
        player.teleportTo(lockPos.x, this.getY(), lockPos.z);
        player.setYRot(this.getYRot());
        player.displayClientMessage(
                Component.translatable("msg.pleasurehorizons.galath_grabbed"), true);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer,
                    new GalathGrabScreenS2CPacket(this.getId(), true));
        }
    }

    /** Receives batched escape-tap packets from the client. */
    public void onEscapeTap(int taps) {
        if (this.grabbedPlayerUUID.isEmpty() || this.isTamed()) return;
        this.escapeTaps += Math.max(1, Math.min(taps, 10));
        if (this.escapeTaps >= ESCAPE_THRESHOLD) {
            Player grabbed = this.grabbedPlayer();
            this.releaseGrab(grabbed);
        }
    }

    private void releaseGrab(@Nullable Player player) {
        if (player != null) {
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.galath_escaped"), true);
            Vec3 knockback = player.position().subtract(this.position()).normalize();
            player.knockback(1.5D, knockback.x, knockback.z);
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer,
                        new GalathGrabScreenS2CPacket(this.getId(), false));
            }
        }
        this.grabbedPlayerUUID = "";
        this.grabPhaseTicks = 0;
        this.escapeTaps = 0;
        this.grabCooldown = GRAB_INTERVAL / 2;
    }

    public boolean isGrabbingPlayer() {
        return !this.grabbedPlayerUUID.isEmpty();
    }

    // ------------------------------------------------------------ threesome

    private void tickThreesome() {
        if (!this.isInThreesome || this.threesomePartnerUUID.isEmpty()) return;
        this.threesomeTicks++;

        ManglelieEntity mang = this.findManglelieByUUID(this.threesomePartnerUUID);
        Player owner = this.getOwner() instanceof Player player ? player : null;
        if (mang == null || !mang.isAlive() || !mang.isInThreesome
                || owner == null || owner.distanceToSqr(this) > THREESOME_DISTANCE_SQ) {
            this.exitThreesome();
            return;
        }

        this.setFreeze(true);
        if (this.threesomeTicks % 20 == 0) this.lookAt(mang, 360.0F, 360.0F);
        if (this.threesomeTicks % 10 == 0 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.WITCH,
                    this.getX(), this.getY() + 1.2D, this.getZ(),
                    3, 0.3D, 0.3D, 0.3D, 0.02D);
        }
    }

    /** Initiate the Manglelie + Galath "dark ritual". */
    public void startThreesome(ManglelieEntity mang, Player player) {
        if (this.level().isClientSide()) return;
        this.isInThreesome = true;
        this.threesomePartnerUUID = mang.getUUID().toString();
        this.threesomeTicks = 0;

        mang.isInThreesome = true;
        mang.threesomePartnerUUID = this.getUUID().toString();
        mang.threesomeTicks = 0;

        this.setFreeze(true);
        mang.setFreeze(true);
        if (this.getNavigation() != null) this.getNavigation().stop();
        if (mang.getNavigation() != null) mang.getNavigation().stop();

        Vec3 playerPos = player.position();
        this.teleportTo(playerPos.x - 1.0D, playerPos.y, playerPos.z);
        mang.teleportTo(playerPos.x + 1.0D, playerPos.y, playerPos.z);
        this.lookAt(mang, 360.0F, 360.0F);
        mang.lookAt(this, 360.0F, 360.0F);

        player.displayClientMessage(
                Component.translatable("msg.pleasurehorizons.galath_ritual_start"), true);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.WITCH,
                    this.getX(), this.getY() + 1.2D, this.getZ(),
                    12, 0.6D, 0.5D, 0.6D, 0.05D);
        }
    }

    /** Clears this Galath's side of the ritual (also called back by Manglelie). */
    public void clearThreesomeSelf() {
        this.isInThreesome = false;
        this.threesomePartnerUUID = "";
        this.threesomeTicks = 0;
        this.setFreeze(false);
    }

    private void exitThreesome() {
        ManglelieEntity mang = this.findManglelieByUUID(this.threesomePartnerUUID);
        this.clearThreesomeSelf();
        if (mang != null) mang.clearThreesomeSelf();
    }

    @Nullable
    private ManglelieEntity findManglelieByUUID(String uuid) {
        if (uuid == null || uuid.isEmpty()) return null;
        UUID target;
        try {
            target = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
        for (ManglelieEntity mang : this.level().getEntitiesOfClass(ManglelieEntity.class,
                this.getBoundingBox().inflate(50.0D))) {
            if (mang.getUUID().equals(target) && mang.isAlive()) {
                return mang;
            }
        }
        return null;
    }

    @Nullable
    private ManglelieEntity findNearbyManglelie(Player player) {
        for (ManglelieEntity mang : this.level().getEntitiesOfClass(ManglelieEntity.class,
                this.getBoundingBox().inflate(3.0D))) {
            if (mang.isTamed() && mang.isOwner(player) && mang.isAlive()) {
                return mang;
            }
        }
        return null;
    }

    /** Toggle riding for a bound Galath (Shift + empty hand). */
    private InteractionResult toggleRide(Player player) {
        if (this.getPassengers().isEmpty()) {
            if (this.rideCooldown <= 0) {
                player.startRiding(this);
                rideCooldown = RIDE_COOLDOWN;
                player.displayClientMessage(
                        Component.translatable("msg.pleasurehorizons.galath_mounted"), true);
                return InteractionResult.SUCCESS;
            }
        } else if (this.getFirstPassenger() == player) {
            player.stopRiding();
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.galath_dismounted"), true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void travel(Vec3 travelVector) {
        // While holding a victim (or inside a ritual) she plants herself.
        if (this.isGrabbingPlayer() || this.isInThreesome || this.isFrozenInPlace()) {
            this.setDeltaMovement(0.0D, 0.0D, 0.0D);
            return;
        }

        LivingEntity rider = this.getControllingPassenger();
        if (rider != null && this.isVehicle()) {
            float forward = rider.zza;
            float strafe = rider.xxa;
            boolean sneak = rider.isShiftKeyDown();
            this.setYRot(rider.getYRot());
            this.yRotO = this.getYRot();
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
            Vec3 lookVec = rider.getLookAngle();
            double hSpeed = 0.5D;
            double vSpeed = 0.3D;
            double mx = 0.0D;
            double my = 0.0D;
            double mz = 0.0D;
            if (strafe != 0.0F) {
                mx = -lookVec.z * hSpeed * strafe;
                mz = lookVec.x * hSpeed * strafe;
            }
            if (forward != 0.0F) {
                mx += lookVec.x * hSpeed * forward;
                mz += lookVec.z * hSpeed * forward;
            }
            if (this.jumping) {
                my = vSpeed;
                this.jumping = false;
            } else if (sneak) {
                my = -vSpeed;
            }
            this.setDeltaMovement(mx, my, mz);
            this.hasImpulse = true;
            this.fallDistance = 0.0F;
            super.travel(Vec3.ZERO);
            return;
        }
        super.travel(travelVector);
    }

    @Override
    public boolean isNoGravity() {
        return this.getControllingPassenger() != null || super.isNoGravity();
    }

    @Override
    public boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty() && passenger instanceof Player;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity passenger = this.getFirstPassenger();
        return passenger instanceof Player player ? player : null;
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return 1.0F;
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity passenger) {
        return new Vec3(0.0D, 0.8D, 0.0D);
    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public void onPlayerJump(int jumpPower) {
        this.jumping = true;
    }

    @Override
    public void handleStartJump(int jumpPower) {
        this.jumping = true;
    }

    @Override
    public void handleStopJump() {
    }

    @Override
    public int getJumpCooldown() {
        return 0;
    }

    @Nullable
    private Player grabbedPlayer() {
        if (this.grabbedPlayerUUID.isEmpty()) return null;
        try {
            return this.level().getPlayerByUUID(UUID.fromString(this.grabbedPlayerUUID));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) {
            // Optimistic client result: only the owner may interact with a tamed Galath.
            boolean willAct = this.isTamed() && this.isOwner(player);
            return willAct ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        if (!this.getOverrideAnim().isEmpty() || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (this.isTamed()) {
            if (this.isOwner(player) && player.isShiftKeyDown()) {
                if (player.getItemInHand(hand).isEmpty()) {
                    return this.toggleRide(player);
                }
                if (this.isInThreesome) {
                    this.exitThreesome();
                    return InteractionResult.SUCCESS;
                }
                ManglelieEntity mang = this.findNearbyManglelie(player);
                if (mang != null && !mang.isInThreesome) {
                    this.startThreesome(mang, player);
                    return InteractionResult.SUCCESS;
                }
            }
            return super.mobInteract(player, hand);
        }

        // Untamed Galath cannot be lured with her favourite item - she must be defeated first.
        player.displayClientMessage(
                Component.translatable("msg.pleasurehorizons.galath_defeat_hint", this.getGirlDisplayName()),
                true);
        return InteractionResult.FAIL;
    }

    /** Remember that this player earned the right to use the soul-binding coin. */
    private void markDefeatedFor(Player player) {
        player.getPersistentData().putBoolean(DEFEATED_KEY, true);
    }

    @org.jetbrains.annotations.Nullable
    private Player playerFromDamage(DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker instanceof Player player) {
            return player;
        }
        if (attacker instanceof TameableGirlEntity girl) {
            return girl.getOwner() instanceof Player owner ? owner : null;
        }
        return null;
    }

    @Override
    public List<Scene> getScenes() {
        return List.of(
                Scene.onBed("Bed", 0,
                        List.of("bed_back"),
                        List.of("bed_fast", "bed_fast1", "bed_fast2"),
                        List.of("bed_fast"),
                        "bed_cum", 4f, true, true, true,
                        0f, "bed_back", "bed_back")
        );
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        // A boss should stay until she is fought and claimed, not despawn like a passive spawn.
        return false;
    }
}
