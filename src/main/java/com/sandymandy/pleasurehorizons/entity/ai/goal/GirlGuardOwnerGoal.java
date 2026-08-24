package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

/**
 * Bodyguard behaviour: while {@code guardOwner} is on, the girl keeps engaging hostile mobs
 * around her owner, one after another, on a short scan cooldown.
 *
 * <p>The previous version aborted a chase as soon as the girl stepped more than 20 blocks from
 * the owner. A fleeing or kiting mob (a skeleton backing away, a creeper wandering off) therefore
 * made her stop mid-fight and turn back - the reported "she sometimes just stops". Give-up is now
 * measured from the target instead of the owner, and a cooldown re-scan picks up the next hostile
 * once the current one is dead, so she sweeps through a whole pack around the owner.</p>
 */
public class GirlGuardOwnerGoal extends TargetGoal {
    private static final double SCAN_HEIGHT = 6.0D;
    private static final double GIVE_UP_RANGE_SQ = 32.0D * 32.0D;
    private static final int SCAN_INTERVAL = 10;

    private final TameableGirlEntity girl;
    private int scanCooldown = 0;

    public GirlGuardOwnerGoal(TameableGirlEntity girl) {
        super(girl, false);
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }
        scanCooldown = SCAN_INTERVAL;
        if (!guardActive()) return false;

        LivingEntity owner = girl.getOwner();
        if (owner == null) return false;

        // Never steal a target that another (equal- or higher-priority) goal already set.
        LivingEntity current = girl.getTarget();
        if (current != null && current.isAlive()) return false;

        return pickTarget(owner);
    }

    @Override
    public boolean canContinueToUse() {
        if (!guardActive()) return false;
        LivingEntity target = girl.getTarget();
        if (target == null || !target.isAlive()) return false;
        return girl.distanceToSqr(target) <= GIVE_UP_RANGE_SQ;
    }

    private boolean guardActive() {
        return girl.isGuardOwnerEnabled()
                && !girl.isSitting() && !girl.isSceneActive()
                && !girl.isDowned() && !girl.isPassenger();
    }

    private boolean pickTarget(LivingEntity owner) {
        Mob found = findNearestMonster(owner);
        if (found != null && found.isAlive()) {
            girl.setTarget(found);
            return true;
        }
        return false;
    }

    private Mob findNearestMonster(LivingEntity owner) {
        double scanRange = girl.guardScanRange(); // per-girl "guard range" setting
        AABB box = new AABB(owner.blockPosition()).inflate(scanRange, SCAN_HEIGHT, scanRange);
        // Search for Mob + Enemy (not just Monster) so flying mobs like Phantoms are included.
        // Phantom extends FlyingMob implements Enemy, NOT Monster, so Monster.class alone
        // would skip them entirely.
        java.util.List<Mob> candidates = girl.level().getEntitiesOfClass(Mob.class, box,
                mob -> mob.isAlive() && mob instanceof Enemy
                        && !girl.isAvoidCreepersEnabled(mob));
        if (candidates.isEmpty()) return null;
        
        // Priority: mobs attacking owner first, then closest to owner.
        Mob attackingOwner = null;
        double closestAttackingDistSq = Double.MAX_VALUE;
        Mob closestToOwner = null;
        double closestDistSq = Double.MAX_VALUE;
        
        for (Mob mob : candidates) {
            double distSq = mob.distanceToSqr(owner);
            if (mob.getTarget() == owner && distSq < closestAttackingDistSq) {
                attackingOwner = mob;
                closestAttackingDistSq = distSq;
            }
            if (distSq < closestDistSq) {
                closestToOwner = mob;
                closestDistSq = distSq;
            }
        }
        
        // Prefer the mob that's actively attacking the owner; fall back to closest
        return attackingOwner != null ? attackingOwner : closestToOwner;
    }
}
