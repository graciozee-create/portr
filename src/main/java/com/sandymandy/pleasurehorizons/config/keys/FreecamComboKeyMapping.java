package com.sandymandy.pleasurehorizons.config.keys;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;


class FreecamComboKeyMapping extends FreecamKeyMapping {

    private final Runnable action;
    private final HoldAction holdAction;
    private final long maxHoldTicks;

    private long sinceLastUse = 0;
    private long sinceLastPress = 0;
    private long queued = 0;

    FreecamComboKeyMapping(String translationKey, InputUtil.Type type, int code, Runnable action, HoldAction holdAction, long maxHoldTicks) {
        super(translationKey, type, code);
        this.action = action;
        this.holdAction = holdAction;
        this.maxHoldTicks = maxHoldTicks;
    }

    @Override
    public void tick() {
        // Bump tick counters
        sinceLastUse++;
        sinceLastPress++;

        if (isPressed()) {
            // Handle combo actions
            if (holdAction.run()) {
                markUsed();
            }
        } else {
            // Handle key-up actions
            while (dequeue()) {
                action.run();
            }
        }
    }

    /**
     * Override {@link KeyBinding#setPressed(boolean) vanilla's setter}, so we can
     * invoke {@link #keyUp()} and {@link #keyDown()} when the state changes.
     */
    @Override
    public void setPressed(boolean down) {
        if (down != isPressed()) {
            if (down) {
                keyDown();
            } else {
                keyUp();
            }
        }
        super.setPressed(down);
    }

    private void keyDown() {
        sinceLastPress = 0;
    }

    private void keyUp() {
        // Queue an action run if this keyup met criteria
        if (sinceLastUse > sinceLastPress && sinceLastPress < maxHoldTicks) {
            queued++;
        }
        markUsed();
    }

    /**
     * Should be called whenever this mapping is considered "used".
     * <p>
     * E.g. combo-key actions returning {@code true} or key-up actions being queued.
     */
    private void markUsed() {
        sinceLastUse = 0;
    }

    /**
     * Consumes a {@link #queued} run.
     *
     * @return whether a run was queued.
     */
    private boolean dequeue() {
        if (queued < 1) {
            return false;
        }
        queued--;
        return true;
    }
}
