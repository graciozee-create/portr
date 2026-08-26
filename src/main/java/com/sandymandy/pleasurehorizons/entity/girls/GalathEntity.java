package com.sandymandy.pleasurehorizons.entity.girls;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.item.PleasureHorizonsItems;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Galath - a hostile mini-boss from the original Jenny/Fapcraft mod.
 *
 * <p>The original character starts as an enemy in the Nether: she is aggressive, has boss-tier
 * stats, and can only become a companion after the player defeats her. This port keeps that
 * flow while reusing the shared tamed-girl ownership model:</p>
 *
 * <ul>
 *   <li>While untamed and standing, Galath targets nearby survival players.</li>
 *   <li>When defeated she vanishes, drops her {@link PleasureHorizonsItems#GALATH_COIN} and a
 *       handful of nether stars, and the winning survival player is marked as the one who beat
 *       her - the only state that unlocks the coin.</li>
 *   <li>{@code GalathCoinItem} remains a soul-bound summon/dismiss item, but in survival it is
 *       locked until the player has actually defeated Galath; creative keeps the old behavior.</li>
 * </ul>
 */
public class GalathEntity extends SettlementGirlEntityAI {

    private static final String DEFEATED_KEY = "PleasureHorizonsGalathDefeated";

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

    /**
     * Defeating (lethal damage) triggers the boss reward. The shared hierarchy converts lethal
     * damage into the DOWNED state; the one-time transition below is what drops the coin and
     * removes her, matching the original "she dies and drops her coin" flow.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean wasDowned = this.isDowned();
        boolean result = super.hurt(source, amount);
        if (!this.level().isClientSide() && !this.isTamed() && !wasDowned && this.isDowned()) {
            this.onDefeated(source);
        }
        return result;
    }

    private void onDefeated(DamageSource source) {
        Player player = playerFromDamage(source);
        this.setTarget(null);
        this.getNavigation().stop();

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
