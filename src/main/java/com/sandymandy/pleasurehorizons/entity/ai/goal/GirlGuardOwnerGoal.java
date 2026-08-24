package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
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
        Monster found = findNearestMonster(owner);
        if (found != null && found.isAlive()) {
            girl.setTarget(found);
            return true;
        }
        return false;
    }

    private Monster findNearestMonster(LivingEntity owner) {
        double scanRange = girl.guardScanRange(); // per-girl "guard range" setting
        AABB box = new AABB(owner.blockPosition()).inflate(scanRange, SCAN_HEIGHT, scanRange);
        java.util.List<Monster> candidates = girl.level().getEntitiesOfClass(Monster.class, box,
                monster -> monster.isAlive() && !girl.isAvoidCreepersEnabled(monster));
        if (candidates.isEmpty()) return null;
        
        // Priority: mobs attacking owner first, then closest to owner.
        // This ensures she protects the owner from immediate threats instead of chasing
        // a random zombie 10 blocks away while a skeleton shoots him in the face.
        Monster attackingOwner = null;
        double closestAttackingDistSq = Double.MAX_VALUE;
        Monster closestToOwner = null;
        double closestDistSq = Double.MAX_VALUE;
        
        for (Monster mob : candidates) {
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
