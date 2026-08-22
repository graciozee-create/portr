package com.sandymandy.pleasurehorizons.entity.base.tamable;

import com.sandymandy.pleasurehorizons.settlement.Settlement;
import com.sandymandy.pleasurehorizons.settlement.SettlementMember;
import com.sandymandy.pleasurehorizons.util.managers.SettlementManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Adds settlement membership and ranged (bow) capability.
 *
 * <p>{@link RangedAttackMob} is implemented here rather than on {@code GirlEntity} because only
 * the settlement-capable girls use weapons. Without it {@code GirlBowAttackGoal} had no way to
 * fire an arrow, which is why girls holding a bow just walked up and punched.</p>
 */
public abstract class SettlementGirlEntityAI extends TameableGirlEntity
        implements SettlementMember, RangedAttackMob {
    @Nullable
    private UUID settlementId;
    @Nullable
    private transient Settlement settlementCache;

    protected SettlementGirlEntityAI(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (settlementId != null) {
            compound.putUUID("SettlementId", settlementId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setSettlementById(compound.hasUUID("SettlementId") ? compound.getUUID("SettlementId") : null);
    }

    @Override
    protected void registerCombatGoals() {
        // Switches between melee and bow based on weapon, range and health.
        this.goalSelector.addGoal(2,
                new com.sandymandy.pleasurehorizons.entity.ai.goal.GirlAttackSwitchGoal(
                        this, 1.2D, 5.0F, 6.0F, 16.0F));
    }

    /**
     * Ammo for the bow. Vanilla skeletons return a plain arrow here; the inherited default
     * returns {@link ItemStack#EMPTY}, and an empty ammo stack gets baked into the arrow as
     * its pickup item. The server then dies with "Cannot encode empty ItemStack" the first
     * time such an arrow is serialized - on chunk autosave the arrow is dropped ("It will
     * not persist"), and on a portal crossing the changeDimension restore crashes the
     * whole server (user logs, crash-2026-08-21_23.20.26 and crash-2026-08-22_12.02.37).
     *
     * <p>Uses a real arrow from her backpack when she carries one, otherwise an endless
     * plain arrow like a skeleton's.</p>
     */
    @Override
    public ItemStack getProjectile(ItemStack weapon) {
        for (int i = 0; i < this.getInventory().getContainerSize(); i++) {
            ItemStack stack = this.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.world.item.ArrowItem) {
                return stack.copyWithCount(1);
            }
        }
        return new ItemStack(net.minecraft.world.item.Items.ARROW);
    }

    /**
     * Fires an arrow at the target. Mirrors vanilla {@code AbstractSkeleton#performRangedAttack}:
     * the arrow is built from the held ammo so enchantments and tipped arrows carry over.
     */
    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        ItemStack bow = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this,
                item -> item instanceof BowItem));
        ItemStack ammo = this.getProjectile(bow);

        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, ammo, pullProgress, bow);
        // Like vanilla skeleton arrows: not farmable in survival (she does not consume ammo).
        // 1.21.1 has no setPickup method - the pickup field is public.
        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333D) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        arrow.shoot(dx, dy + horizontal * 0.20000000298023224D, dz, 1.6F,
                14 - serverLevel.getDifficulty().getId() * 4);

        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F,
                1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        serverLevel.addFreshEntity(arrow);
    }

    @Nullable
    public UUID getSettlementId() {
        return settlementId;
    }

    @Nullable
    @Override
    public Settlement getSettlement() {
        if (settlementId == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return settlementCache;
        }

        Settlement stored = SettlementManager.get(serverLevel).getSettlement(settlementId);
        if (stored == null) {
            setSettlementById(null);
            return null;
        }
        settlementCache = stored;
        return settlementCache;
    }

    public void setSettlementById(@Nullable UUID id) {
        this.settlementId = id;
        this.settlementCache = null;
    }

    @Override
    public void setSettlement(@Nullable Settlement settlement) {
        this.settlementId = settlement == null ? null : settlement.getId();
        this.settlementCache = settlement;
    }

    @Override
    public void breakUp(Player player) {
        Settlement settlement = getSettlement();
        if (settlement != null) {
            settlement.removeMember(this);
        }
        super.breakUp(player);
    }

    @Override
    public void die(DamageSource source) {
        Settlement settlement = getSettlement();
        if (settlement != null) {
            settlement.removeMember(this);
        }
        super.die(source);
    }
}
