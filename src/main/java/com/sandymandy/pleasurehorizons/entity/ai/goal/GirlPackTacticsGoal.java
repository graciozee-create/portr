package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Pack tactics: when several tamed girls share an owner, they fight as a squad instead of a
 * crowd of individuals.
 *
 * <p>Pull-based target sharing: a girl with no target of her own periodically looks at what
 * her sisters nearby are fighting (their current target, or whoever hurt them) and joins the
 * nearest fight. Combined with the guard/hurt-by/attack-with-owner goals this gives focus fire
 * and "avenge a fallen sister" behaviour without any event plumbing. The goal only ever fills
 * an EMPTY target slot, so it never overrides self-defence or the bodyguard scan.</p>
 */
public class GirlPackTacticsGoal extends TargetGoal {
    private static final double ALLY_SCAN_RANGE = 16.0D;
    private static final double JOIN_RANGE_SQ = 24.0D * 24.0D;
    private static final double GIVE_UP_RANGE_SQ = 32.0D * 32.0D;
    private static final int SCAN_INTERVAL = 10;

    private final TameableGirlEntity girl;
    private int scanCooldown = 0;

    public GirlPackTacticsGoal(TameableGirlEntity girl) {
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
        if (unavailable()) return false;

        // Never steal: only fill an empty target slot.
        LivingEntity current = girl.getTarget();
        if (current != null && current.isAlive()) return false;

        LivingEntity candidate = findSharedTarget();
        if (candidate != null) {
            girl.setTarget(candidate);
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (unavailable()) return false;
        LivingEntity target = girl.getTarget();
        return target != null && target.isAlive()
                && girl.distanceToSqr(target) <= GIVE_UP_RANGE_SQ;
    }

    private boolean unavailable() {
        return !girl.isTamed()
                || girl.isSitting() || girl.isSceneActive()
                || girl.isDowned() || girl.isPassenger();
    }

    /**
     * Nearest fight among same-owner sisters: either their current target or whoever last
     * hurt them ("avenge"). Sisters, the shared owner and (optionally) creepers are never
     * adopted, and the candidate must be close enough to actually reach.
     */
    private LivingEntity findSharedTarget() {
        List<TameableGirlEntity> allies = girl.level().getEntitiesOfClass(TameableGirlEntity.class,
                girl.getBoundingBox().inflate(ALLY_SCAN_RANGE, 6.0D, ALLY_SCAN_RANGE),
                ally -> ally != girl && ally.isAlive() && ally.isTamed()
                        && girl.isOwner(ally.getOwner()));

        List<LivingEntity> candidates = new ArrayList<>();
        for (TameableGirlEntity ally : allies) {
            LivingEntity target = ally.getTarget();
            if (target != null && target.isAlive()) candidates.add(target);
            LivingEntity attacker = ally.getLastHurtByMob();
            if (attacker != null && attacker.isAlive()) candidates.add(attacker);
        }

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            if (candidate == girl || candidate instanceof TameableGirlEntity) continue;
            if (girl.isOwner(candidate)) continue;
            if (girl.isAvoidCreepersEnabled()
                    && candidate instanceof net.minecraft.world.entity.monster.Creeper) continue;
            double dist = girl.distanceToSqr(candidate);
            if (dist > JOIN_RANGE_SQ || dist >= bestDist) continue;
            best = candidate;
            bestDist = dist;
        }
        return best;
    }
}
