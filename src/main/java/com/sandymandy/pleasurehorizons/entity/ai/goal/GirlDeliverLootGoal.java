package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * Auto-delivery: when {@code isAutoDeliverEnabled()} is on and the backpack is completely full,
 * she walks to her owner and hands the contents over (same path as the inventory "Give Loot"
 * button), then goes back to work with an empty backpack.
 *
 * <p>Registered at the same priority as the work goals but inserted first, so a full backpack
 * makes delivery win the tie - more gathering would be pointless anyway.</p>
 */
public class GirlDeliverLootGoal extends Goal {
    private static final double SEARCH_RANGE_SQ = 32.0D * 32.0D;
    private static final double DELIVER_DISTANCE_SQ = 2.5D * 2.5D;
    private static final int GIVE_UP_TICKS = 600;
    private static final int GIVE_UP_COOLDOWN = 200;

    private final TameableGirlEntity girl;
    private Player owner;
    private int repath = 0;
    private int elapsed = 0;
    private int cooldown = 0;

    public GirlDeliverLootGoal(TameableGirlEntity girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (!girl.isAutoDeliverEnabled() || !girl.isTamed() || !girl.isBackpackFull()) return false;
        if (forbidden()) return false;
        if (!(girl.getOwner() instanceof Player player) || !player.isAlive() || player.isSpectator()) {
            return false;
        }
        if (girl.distanceToSqr(player) > SEARCH_RANGE_SQ) return false;
        this.owner = player;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return girl.isAutoDeliverEnabled() && owner != null && owner.isAlive()
                && girl.isBackpackFull() && !forbidden();
    }

    private boolean forbidden() {
        return girl.isSitting() || girl.isSceneActive() || girl.isDowned() || girl.isPassenger();
    }

    @Override
    public void start() {
        this.repath = 0;
        this.elapsed = 0;
        girl.getNavigation().moveTo(owner, girl.workSpeedModifier());
    }

    @Override
    public void tick() {
        girl.getLookControl().setLookAt(owner, 30.0F, girl.getMaxHeadXRot());

        if (++elapsed > GIVE_UP_TICKS) {
            // Owner unreachable (flying, across water) - stop trying for a while.
            this.owner = null;
            this.cooldown = GIVE_UP_COOLDOWN;
            return;
        }

        if (girl.distanceToSqr(owner) > DELIVER_DISTANCE_SQ) {
            if (--repath <= 0) {
                repath = this.adjustedTickDelay(10);
                girl.getNavigation().moveTo(owner, girl.workSpeedModifier());
            }
            return;
        }

        girl.giveBackpackTo(owner);
        girl.playSound(SoundEvents.ITEM_PICKUP, 0.8F, 1.0F);
        owner.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(
                        "msg.pleasurehorizons.backpackDelivered", girl.getGirlDisplayName()), true);
        cooldown = GIVE_UP_COOLDOWN;
    }

    @Override
    public void stop() {
        this.owner = null;
        girl.getNavigation().stop();
        // cooldown is only ever set in tick(); stop() deliberately leaves it alone so a
        // give-up cooldown set right before the goal stops survives.
    }
}
