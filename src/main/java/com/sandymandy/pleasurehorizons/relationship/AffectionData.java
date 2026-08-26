package com.sandymandy.pleasurehorizons.relationship;

import net.minecraft.nbt.CompoundTag;

/**
 * Per-character affection data attached to a {@code GirlEntity} and persisted in NBT.
 *
 * <p>Affection runs 0..{@link #MAX_AFFECTION} and is raised by gifts, by the Girl Wand's
 * "+10 affection" editor action, and by quest rewards. It maps to a tier used by dialogue
 * and by the scene locks already present in {@code GirlEntity}.</p>
 */
public class AffectionData {
    public static final int MAX_AFFECTION = 100;

    private static final String TAG_AFFECTION = "Affection";
    private static final String TAG_DAILY_GIFTS = "DailyGifts";
    private static final String TAG_LAST_GIFT_DAY = "LastGiftDay";
    private static final String TAG_LAST_DECAY_DAY = "LastDecayDay";
    private static final String TAG_UNLOCKED_SCENES = "UnlockedScenes";
    private static final String TAG_OWNER = "AffectionOwner";

    private int affection = 0;
    private int dailyGifts = 0;
    private long lastGiftDay = 0;
    private long lastDecayDay = 0;
    private int unlockedScenes = 0;
    private String ownerUUID = "";

    public int getAffection() {
        return this.affection;
    }

    public int getDailyGifts() {
        return this.dailyGifts;
    }

    public long getLastGiftDay() {
        return this.lastGiftDay;
    }

    public String getOwnerUUID() {
        return this.ownerUUID;
    }

    public void setOwner(String uuid) {
        this.ownerUUID = uuid == null ? "" : uuid;
    }

    /** Add affection, clamped to [0, max]; returns true if the value changed. */
    public boolean addAffection(int amount, int max) {
        int old = this.affection;
        this.affection = Math.max(0, Math.min(max, this.affection + amount));
        return this.affection != old;
    }

    /** Check if this girl can still receive a gift on the given Minecraft day. */
    public boolean canGiveGift(long currentDay, int dailyLimit) {
        if (currentDay != this.lastGiftDay) {
            this.dailyGifts = 0;
        }
        return this.dailyGifts < dailyLimit;
    }

    /** Record a gift given on the given Minecraft day. */
    public void recordGift(long currentDay) {
        if (currentDay != this.lastGiftDay) {
            this.dailyGifts = 0;
            this.lastGiftDay = currentDay;
        }
        this.dailyGifts++;
    }

    /** Apply daily affection decay; once per Minecraft day at most. */
    public void applyDecay(long currentDay, double decayAmount) {
        if (this.affection <= 0 || currentDay <= this.lastDecayDay) {
            return;
        }
        this.lastDecayDay = currentDay;
        this.affection = Math.max(0, this.affection - (int) Math.round(decayAmount));
    }

    public boolean isSceneUnlocked(int sceneBit) {
        return (this.unlockedScenes & (1 << sceneBit)) != 0;
    }

    public void unlockScene(int sceneBit) {
        this.unlockedScenes |= (1 << sceneBit);
    }

    /** Lower-to-upper tolerance tier used by dialogue and editor display. */
    public AffectionLevel getLevel() {
        return levelFor(this.affection);
    }

    /** Tier lookup for a raw affection value (used client-side on the synced accessor). */
    public static AffectionLevel levelFor(int affection) {
        if (affection >= 80) return AffectionLevel.INTIMATE;
        if (affection >= 60) return AffectionLevel.CLOSE;
        if (affection >= 30) return AffectionLevel.FRIENDLY;
        if (affection >= 10) return AffectionLevel.ACQUAINTED;
        return AffectionLevel.STRANGER;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_AFFECTION, this.affection);
        tag.putInt(TAG_DAILY_GIFTS, this.dailyGifts);
        tag.putLong(TAG_LAST_GIFT_DAY, this.lastGiftDay);
        tag.putLong(TAG_LAST_DECAY_DAY, this.lastDecayDay);
        tag.putInt(TAG_UNLOCKED_SCENES, this.unlockedScenes);
        tag.putString(TAG_OWNER, this.ownerUUID);
        return tag;
    }

    public void fromNBT(CompoundTag tag) {
        if (tag.contains(TAG_AFFECTION)) this.affection = tag.getInt(TAG_AFFECTION);
        if (tag.contains(TAG_DAILY_GIFTS)) this.dailyGifts = tag.getInt(TAG_DAILY_GIFTS);
        if (tag.contains(TAG_LAST_GIFT_DAY)) this.lastGiftDay = tag.getLong(TAG_LAST_GIFT_DAY);
        if (tag.contains(TAG_LAST_DECAY_DAY)) this.lastDecayDay = tag.getLong(TAG_LAST_DECAY_DAY);
        if (tag.contains(TAG_UNLOCKED_SCENES)) this.unlockedScenes = tag.getInt(TAG_UNLOCKED_SCENES);
        if (tag.contains(TAG_OWNER)) this.ownerUUID = tag.getString(TAG_OWNER);
    }

    public enum AffectionLevel {
        STRANGER("dialogue.pleasurehorizons.tier.stranger"),
        ACQUAINTED("dialogue.pleasurehorizons.tier.acquainted"),
        FRIENDLY("dialogue.pleasurehorizons.tier.friendly"),
        CLOSE("dialogue.pleasurehorizons.tier.close"),
        INTIMATE("dialogue.pleasurehorizons.tier.intimate");

        public final String labelKey;

        AffectionLevel(String labelKey) {
            this.labelKey = labelKey;
        }
    }
}
