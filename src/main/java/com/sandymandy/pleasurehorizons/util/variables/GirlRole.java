package com.sandymandy.pleasurehorizons.util.variables;

import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;

/**
 * A survival "shift" preset applied to a tamed girl.
 *
 * <p>Roles are convenience bundles over the individual AI toggles. Assigning a role turns on the
 * toggles it needs and clears the rest; the player can still flip individual toggles afterwards.
 * The role is stored only as a synched label for the HUD and the inventory "Next Role" button -
 * the authoritative state remains the individual toggles, which are what the goals read.</p>
 */
public enum GirlRole {
    IDLE("idle"),
    WORKER("worker"),
    GUARD("guard"),
    COOK("cook");

    private final String id;

    GirlRole(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public static GirlRole fromId(String id) {
        for (GirlRole role : values()) {
            if (role.id.equals(id)) {
                return role;
            }
        }
        return IDLE;
    }

    public GirlRole next() {
        GirlRole[] all = values();
        return all[(this.ordinal() + 1) % all.length];
    }

    /** Applies this role's preset to the girl's survival toggles. */
    public void applyTo(TameableGirlEntity girl) {
        girl.setGuardBaseEnabled(false);
        girl.setGuardOwnerEnabled(false);
        girl.setStayNearBaseEnabled(false);
        girl.setGatherEnabled(false);
        girl.setHarvestEnabled(false);
        girl.setChopTreesEnabled(false);
        girl.setFeedOwnerEnabled(false);
        girl.setCookEnabled(false);

        switch (this) {
            case WORKER -> {
                // Full production chain: gather drops, harvest crops, chop wood, cook food.
                girl.setGatherEnabled(true);
                girl.setHarvestEnabled(true);
                girl.setChopTreesEnabled(true);
                girl.setCookEnabled(true);
            }
            case GUARD -> {
                girl.setGuardBaseEnabled(true);
                girl.setGuardOwnerEnabled(true);
                girl.setStayNearBaseEnabled(true);
            }
            case COOK -> {
                girl.setCookEnabled(true);
                girl.setFeedOwnerEnabled(true);
            }
            case IDLE -> {
                // Everything cleared above.
            }
        }
    }
}
