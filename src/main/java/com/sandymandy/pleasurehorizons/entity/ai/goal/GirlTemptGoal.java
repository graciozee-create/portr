package com.sandymandy.pleasurehorizons.entity.ai.goal;

import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import net.minecraft.world.entity.ai.goal.TemptGoal;

/**
 * Vanilla temptation with the girl-specific favourite item and scene safety checks.
 *
 * <p>Minecraft 1.21.1 takes an {@code ItemStack} predicate rather than the newer
 * {@code Ingredient} constructor used by the Fabric 1.21.6 source. The predicate resolves the
 * favourite item on demand because custom-girl profiles are assigned after entity construction.</p>
 */
public class GirlTemptGoal extends TemptGoal {
    private final GirlEntity girl;

    public GirlTemptGoal(GirlEntity girl, double speedModifier, boolean canScare) {
        super(girl, speedModifier, stack -> stack.is(girl.isAttractedTo()), canScare);
        this.girl = girl;
    }

    private boolean isUnavailable() {
        return this.girl.isPassenger()
                || this.girl.isDowned()
                || this.girl.isSceneActive()
                || this.girl.isMovementLocked();
    }

    @Override
    public boolean canUse() {
        return !isUnavailable() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !isUnavailable() && super.canContinueToUse();
    }
}
