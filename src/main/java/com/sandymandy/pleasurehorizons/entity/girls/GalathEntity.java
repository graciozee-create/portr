package com.sandymandy.pleasurehorizons.entity.girls;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.item.PleasureHorizonsItems;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Galath - the Nether succubus boss, ported 1:1 from the original Jenny/Fapcraft mod
 * (decompiled 1.12.2 class {@code f_}).
 *
 * <p>Original behaviour (verified against the decompiled mod jar):</p>
 *
 * <ul>
 *   <li>No natural spawn: a {@code LivingSpawnEvent.CheckSpawn} handler replaces Nether
 *       wither-skeleton/blaze spawns with her (see {@code PleasureHorizons#onLivingPositionCheck}).
 *   <li>Hostile to players, but her combat is NOT standard melee: she fights with
 *       energy balls - a swing whose hitbox is active during ticks 9-30, dealing 1 damage
 *       + 1.5 knockback and <b>dodged by sneaking</b>. She is immune to fire, lava,
 *       fall damage, the void, starvation and thorns (a Nether boss).</li>
 *   <li>While she takes damage she summons wither skeletons, placed 15+ blocks away
 *       from her victim (the original keeps a UUID list of her minions).</li>
 *   <li>When beaten she does NOT die: she screams, floats down and lies paralyzed
 *       ("Galath is paralyzed! Now it's time to corrupt her - walk to her and right
 *       click her"). Any player who right-clicks her while she lies down binds her:
 *       she stands up, and the <b>soul-binding coin goes straight into the claimer's
 *       hand</b> (the old item is dropped), exactly like the original GIVE_COIN cutscene.
 *   <li>A beaten-by-environment Galath with no player to claim her simply vanishes
 *       (the original resets her ownership data on non-player death).</li>
 *   <li>After a scene the player is pulled back to her side (the original
 *       {@code GalathEntity#d} teleport).</li>
 *   <li>When bound she is a flying mount: the original "ride" option opens her flight
 *       (no gravity, the rider steers, jump/sneak for altitude) - no piggyback gallop.
 *   <li>The Manglelie threesome ("dark ritual") is also original (her post-tame menu
 *       option, Manglelie required).</li>
 * </ul>
 */
public class GalathEntity extends SettlementGirlEntityAI implements PlayerRideableJumping {

    private static final String DEFEATED_KEY = "PleasureHorizonsGalathDefeated";
    private static final int RIDE_COOLDOWN = 100;
    private static final int THREESOME_DISTANCE_SQ = 36;          // owner must stay within 6 blocks

    // ---- original 1.12.2 boss-fight tuning (decompiled f_ class) -------------
    private static final int SWING_DURATION = 35;                 // full energy-ball swing
    private static final int SWING_ACTIVE_FROM = 9;               // original: hitbox active at ad 9..30
    private static final int SWING_ACTIVE_TO = 30;
    private static final double SWING_REACH_SQ = 16.0D;           // 4 blocks to start the swing
    private static final double SWING_HIT_RADIUS = 1.4D;          // original ball hitbox 0.75 + player
    private static final int SWING_COOLDOWN_MIN = 40;
    private static final int SWING_COOLDOWN_MAX = 80;
    private static final float BALL_DAMAGE = 1.0F;                // original energy-ball damage
    private static final float BALL_KNOCKBACK = 1.5F;             // original knockback
    private static final int KO_FLY_DURATION = 80;                // KNOCK_OUT_FLY: scream + float down
    private static final int KO_DISCARD_GRACE = 100;              // unclaimed (environment) KO vanishes
    private static final int SKELETON_SUMMON_INTERVAL = 600;      // 30 s between summon bursts
    private static final int DAMAGE_FOR_SKELETONS = 30;           // summon burst every 30 damage
    private static final int MAX_WITHER_MINIONS = 3;
    private static final int WITHER_MINION_RANGE = 15;            // original: placed >15 blocks from target

    private enum KnockOutPhase { NONE, FLY, GROUND }

    private KnockOutPhase koPhase = KnockOutPhase.NONE;
    private int koTicks = 0;
    @Nullable
    private UUID koVictor;

    private int swingCooldown = 0;
    private int skeletonSummonCooldown = 0;
    private int damageAccumulated = 0;
    private int rideCooldown = 0;
    private final List<UUID> minionUUIDs = new ArrayList<>();

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

    /**
     * The original Galath has no boss-tier attributes: a standard 20 HP girl that fights
     * with 1-damage energy balls and wither-skeleton minions - she is a dodge-and-manage
     * boss, not a HP sponge.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return createDefaultAttributes()
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
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
    protected void registerCombatGoals() {
        // No standard melee/bow switch: the original fights with energy balls only.
        this.goalSelector.addGoal(2, new BallSwingGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;

        if (this.rideCooldown > 0) this.rideCooldown--;
        this.tickThreesome();

        // Bound Galath is a companion again; her boss behaviour stops.
        if (this.isTamed()) {
            this.expireMinions();
            return;
        }

        if (this.koPhase != KnockOutPhase.NONE) {
            this.tickKnockOut();
            return;
        }

        this.expireMinions();

        // Original: while she takes damage she calls in wither skeletons, and keeps
        // summoning periodically while actively fighting.
        if (this.damageAccumulated >= DAMAGE_FOR_SKELETONS && this.skeletonSummonCooldown <= 0) {
            this.damageAccumulated = 0;
            this.skeletonSummonCooldown = SKELETON_SUMMON_INTERVAL;
            this.summonWitherSkeletons();
        }

        if (this.skeletonSummonCooldown > 0) this.skeletonSummonCooldown--;

        if (this.getTarget() != null && this.skeletonSummonCooldown <= 0
                && this.random.nextFloat() < 0.005F) {
            this.skeletonSummonCooldown = SKELETON_SUMMON_INTERVAL;
            this.summonWitherSkeletons();
        }
    }

    // ------------------------------------------------------------ boss combat

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Original: a Nether boss ignores fire/lava, falling, the void, starvation
        // and thorns (her decompiled attackEntityFrom returns false for all of them).
        if (!this.isTamed() && (source.is(DamageTypes.LAVA) || source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.HOT_FLOOR)
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.OUT_OF_WORLD)
                || source.is(DamageTypes.STARVE) || source.is(DamageTypes.THORNS))) {
            return false;
        }

        boolean wasDowned = this.isDowned();
        boolean result = super.hurt(source, amount);
        if (!this.level().isClientSide() && !this.isTamed() && !wasDowned && this.isDowned()) {
            this.beginKnockOut(playerFromDamage(source));
        } else if (!this.level().isClientSide() && !this.isTamed() && !this.isDowned() && result) {
            // Wither-skeleton summoning tracks real damage taken while she is still up.
            this.damageAccumulated += (int) amount;
        }
        return result;
    }

    /**
     * Original KNOCK_OUT sequence start: she does not die - she screams
     * ("Galath is paralyzed!"), gravity is dropped and she floats down to the ground
     * where she lies until a player right-clicks her.
     */
    private void beginKnockOut(@Nullable Player victor) {
        this.koPhase = KnockOutPhase.FLY;
        this.koTicks = 0;
        this.koVictor = victor != null ? victor.getUUID() : null;
        this.setTarget(null);
        this.getNavigation().stop();
        this.clearMinions();
        this.setNoGravity(true);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY() + 1.5D, this.getZ(),
                    12, 0.4D, 0.5D, 0.4D, 0.08D);
            for (ServerPlayer nearby : serverLevel.getPlayers()) {
                if (nearby.distanceToSqr(this) <= 32.0D * 32.0D) {
                    nearby.displayClientMessage(
                            Component.translatable("msg.pleasurehorizons.galath_ko", this.getGirlDisplayName()),
                            true);
                }
            }
        }
        this.playSound(SoundEvents.ENDERMAN_DEATH, 1.0F, 0.6F);
    }

    private void tickKnockOut() {
        this.koTicks++;
        if (this.koPhase == KnockOutPhase.FLY) {
            // KNOCK_OUT_FLY: she hovers slowly toward the ground (the hover itself runs in
            // travel(), where it survives the base movement code). Original: no gravity,
            // cleared path, screaming.
            if (this.koTicks >= KO_FLY_DURATION || this.onGround()) {
                this.koPhase = KnockOutPhase.GROUND;
                this.koTicks = 0;
                this.playSound(SoundEvents.ENDERMAN_TELEPORT, 0.8F, 0.8F);
            }
        } else {
            // KNOCK_OUT_GROUND: lies paralyzed. A player must come and right-click her;
            // an unclaimed (environment-killed) boss vanishes, like the original reset.
            if (this.koVictor == null && this.koTicks >= KO_DISCARD_GRACE) {
                this.discard();
            }
        }
    }

    /** True while she lies on the ground waiting to be claimed. */
    public boolean isKnockOutGround() {
        return this.koPhase == KnockOutPhase.GROUND;
    }

    /**
     * Original GIVE_COIN: the first player to right-click the paralyzed Galath binds her.
     * She stands up, becomes tamed, and the soul-binding coin goes straight into the
     * claimer's main hand (the previous item is dropped on the ground).
     */
    private void claimFromKnockOut(Player player) {
        this.koPhase = KnockOutPhase.NONE;
        this.koTicks = 0;
        this.koVictor = null;
        this.setTarget(null);
        this.setFreeze(false);
        this.setNoGravity(false);
        this.setDowned(false);
        this.setHealth(this.getMaxHealth());
        this.setTamedBy(player);
        this.getNavigation().stop();
        this.setBasePos(this.blockPosition());
        this.clearMinions();

        markDefeatedFor(player);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.giveExperiencePoints(this.getBaseExperienceReward());
        }

        // Original: "granting him a coin to which her soul is bound" - straight to the hand.
        ItemStack previous = player.getItemInHand(InteractionHand.MAIN_HAND);
        player.setItemInHand(InteractionHand.MAIN_HAND,
                new ItemStack(PleasureHorizonsItems.GALATH_COIN.get()));
        if (!previous.isEmpty()) {
            player.drop(previous, false);
        }

        this.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.2F);
        player.displayClientMessage(
                Component.translatable("msg.pleasurehorizons.galath_bound", this.getGirlDisplayName()),
                true);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.WITCH,
                    this.getX(), this.getY() + 1.5D, this.getZ(),
                    12, 0.5D, 0.6D, 0.5D, 0.05D);
        }
    }

    /** Original: she summons wither skeletons 15+ blocks away from her victim. */
    private void summonWitherSkeletons() {
        if (this.level().isClientSide()) return;
        LivingEntity target = this.getTarget();
        double tx = target != null ? target.getX() : this.getX();
        double tz = target != null ? target.getZ() : this.getZ();

        for (int i = 0; i < 2 && this.minionUUIDs.size() < MAX_WITHER_MINIONS; i++) {
            WitherSkeleton skeleton = EntityType.WITHER_SKELETON.create(this.level());
            if (skeleton == null) continue;
            double angle = this.random.nextDouble() * Math.PI * 2;
            double dist = WITHER_MINION_RANGE + this.random.nextDouble() * 5.0D;
            double sx = tx + Math.cos(angle) * dist;
            double sz = tz + Math.sin(angle) * dist;
            skeleton.setPos(sx, this.getY(), sz);
            if (target != null) skeleton.setTarget(target);
            skeleton.setPersistenceRequired();
            this.level().addFreshEntity(skeleton);
            this.minionUUIDs.add(skeleton.getUUID());
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMOKE, sx, this.getY(), sz, 8, 0.3D, 0.5D, 0.3D, 0.05D);
            }
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY() + 1.5D, this.getZ(),
                    10, 1.0D, 0.5D, 1.0D, 0.1D);
        }
    }

    private void expireMinions() {
        this.minionUUIDs.removeIf(uuid -> {
            Entity minion = this.level().getEntity(uuid);
            return minion == null || !minion.isAlive() || minion.level() != this.level();
        });
    }

    private void clearMinions() {
        for (UUID uuid : this.minionUUIDs) {
            Entity minion = this.level().getEntity(uuid);
            if (minion != null && minion.isAlive()) {
                minion.discard();
            }
        }
        this.minionUUIDs.clear();
    }

    /**
     * The original energy-ball attack: a 35-tick swing whose hitbox is active during
     * ticks 9-30 (decompiled {@code f_#h}). 1 damage + 1.5 knockback, and a player
     * that sneaks when the ball reaches them takes no damage (the original's
     * {@code isSneaking} guard). Works underwater too, since the base melee goal was
     * replaced by this one.
     */
    private static final class BallSwingGoal extends Goal {
        private final GalathEntity galath;
        private int swingTicks = 0;
        private final Set<UUID> hitThisSwing = new HashSet<>();

        private BallSwingGoal(GalathEntity galath) {
            this.galath = galath;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.galath.getTarget();
            if (target == null || !target.isAlive()) return false;
            if (this.galath.isTamed() || this.galath.isDowned() || this.galath.isFrozenInPlace()
                    || this.galath.isSceneActive() || this.galath.isPassenger()) {
                return false;
            }
            if (this.galath.swingCooldown > 0) return false;

            if (this.galath.isInWater() && (target.isInWater() || target.isUnderWater())) {
                // Underwater the ball is a short-range splash (replaces the water melee).
                return target.distanceToSqr(this.galath) <= 6.25D;
            }
            return target.distanceToSqr(this.galath) <= SWING_REACH_SQ
                    && this.galath.hasLineOfSight(target);
        }

        @Override
        public void start() {
            this.swingTicks = 1;
            this.hitThisSwing.clear();
            this.galath.playSound(SoundEvents.WITHER_SHOOT, 0.6F, 1.6F);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.galath.getTarget();
            if (target == null || !target.isAlive()) {
                this.swingTicks = 0;
                return false;
            }
            double reachSq = this.galath.isInWater() ? 6.25D : 20.25D;
            return target.distanceToSqr(this.galath) <= reachSq;
        }

        @Override
        public void tick() {
            if (this.swingTicks <= 0) return;
            this.swingTicks++;

            if (this.swingTicks > SWING_DURATION) {
                this.swingTicks = 0;
                this.galath.swingCooldown = SWING_COOLDOWN_MIN
                        + this.galath.random.nextInt(SWING_COOLDOWN_MAX - SWING_COOLDOWN_MIN);
                return;
            }

            if (this.swingTicks >= SWING_ACTIVE_FROM && this.swingTicks <= SWING_ACTIVE_TO) {
                this.hitWithBall();
            }
        }

        private void hitWithBall() {
            Vec3 ballPos = this.galath.getEyePosition()
                    .add(this.galath.getLookAngle().scale(1.1D))
                    .add(0.0D, -0.2D, 0.0D);

            if (this.galath.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        ballPos.x, ballPos.y, ballPos.z,
                        6, 0.15D, 0.15D, 0.15D, 0.03D);
            }

            AABB reach = new AABB(ballPos).inflate(SWING_HIT_RADIUS);
            for (LivingEntity entity : this.galath.level().getEntitiesOfClass(LivingEntity.class, reach,
                    e -> e != this.galath && e.isAlive() && !(e instanceof TameableGirlEntity))) {
                if (this.hitThisSwing.contains(entity.getUUID())) continue;
                // Original: sneaking dodges the energy ball entirely.
                if (entity instanceof Player player && player.isCrouching()) continue;

                this.hitThisSwing.add(entity.getUUID());
                entity.hurt(this.galath.damageSources().mobAttack(this.galath), BALL_DAMAGE);
                Vec3 dir = entity.position().subtract(this.galath.position()).normalize();
                entity.knockback(BALL_KNOCKBACK, dir.x, dir.z);
            }
        }

        @Override
        public void stop() {
            this.swingTicks = 0;
        }
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

    // ------------------------------------------------------------ flight

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
        // Knock-out: she floats down (FLY) or lies still (GROUND). Handled here because the
        // base tick would otherwise re-apply gravity / knock the hover around.
        if (this.koPhase != KnockOutPhase.NONE) {
            if (this.koPhase == KnockOutPhase.FLY && !this.onGround()) {
                this.setDeltaMovement(0.0D, -0.03D, 0.0D);
            } else {
                this.setDeltaMovement(0.0D, 0.0D, 0.0D);
            }
            this.fallDistance = 0.0F;
            super.travel(Vec3.ZERO);
            return;
        }

        // While in the ritual or paralyzed she plants herself.
        if (this.isInThreesome || this.isFrozenInPlace()) {
            this.setDeltaMovement(0.0D, 0.0D, 0.0D);
            return;
        }

        LivingEntity rider = this.getControllingPassenger();
        if (rider != null && this.isVehicle()) {
            // Original "ride": she FLYES with the rider - no gravity, mouse steering,
            // W/S for speed, jump for altitude up, sneak for altitude down.
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
        return this.koPhase == KnockOutPhase.NONE && this.getPassengers().isEmpty()
                && passenger instanceof Player;
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

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) {
            // Optimistic client result: the owner interacts with a tamed Galath, and any
            // player may claim one lying in her knock-out.
            boolean willAct = (this.isTamed() && this.isOwner(player)) || this.isKnockOutGround();
            return willAct ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        if (!this.getOverrideAnim().isEmpty() || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        // Original: right-click the paralyzed Galath and she is bound to you.
        if (this.koPhase != KnockOutPhase.NONE) {
            if (this.koPhase == KnockOutPhase.GROUND && player.distanceToSqr(this) <= 16.0D) {
                this.claimFromKnockOut(player);
                return InteractionResult.SUCCESS;
            }
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

    /**
     * Original: after a scene ends, Galath pulls her player back to her side
     * (decompiled {@code GalathEntity#d} - teleport to a point next to her).
     */
    @Override
    public void stopScene() {
        super.stopScene();
        if (this.level().isClientSide() || !this.isTamed()) return;
        Player owner = this.getOwner() instanceof Player player ? player : null;
        if (owner == null || !owner.isAlive()) return;
        if (owner.distanceToSqr(this) > 64.0D) {
            Vec3 offset = this.getLookAngle().scale(0.4D);
            owner.teleportTo(this.getX() + offset.x,
                    this.getY() + 0.5D - owner.getEyeHeight(),
                    this.getZ() + offset.z);
            owner.setYRot(this.getYRot());
        }
    }

    /** Remember that this player earned the right to use the soul-binding coin. */
    private void markDefeatedFor(Player player) {
        player.getPersistentData().putBoolean(DEFEATED_KEY, true);
    }

    @Nullable
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
