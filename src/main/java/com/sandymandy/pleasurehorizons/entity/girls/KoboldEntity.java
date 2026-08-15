package com.sandymandy.pleasurehorizons.entity.girls;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import com.sandymandy.pleasurehorizons.util.Colors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KoboldEntity extends SettlementGirlEntityAI {

    private static final EntityDataAccessor<Integer> BODY_SIZE =
            SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> KOBOLD_BREAST_SIZE =
            SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PRIMARY_COLOR =
            SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SECONDARY_COLOR =
            SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> IRIS_COLOR =
            SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TOP_HORN_TYPE =
            SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BOTTOM_HORN_TYPE =
            SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> HITBOX_HEIGHT =
            SynchedEntityData.defineId(KoboldEntity.class, EntityDataSerializers.FLOAT);

    private static final float MIN_HITBOX_HEIGHT = 1.0f;   // at body size 65
    private static final float MAX_HITBOX_HEIGHT = 1.75f;  // at body size 115
    public static final int MIN_BODY_SIZE = 65;
    public static final int MAX_BODY_SIZE = 115;
    public static final int MIN_BREAST_SIZE = 60;
    public static final int MAX_BREAST_SIZE = 160;
    private static final int MIN_HEALTH = 4;
    private static final int MAX_HEALTH = 12;

    /** Avoids recomputing dimensions every tick when nothing changed. */
    private float lastHitboxHeight = 1.0f;
    /** Client-side flag: bone colours/visibility only need reapplying when something changed. */
    private boolean customizationApplied = false;

    public KoboldEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createDefaultAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.12)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    /**
     * Randomise only genuinely new server-side spawns. Doing this in the constructor, as the
     * Fabric version does, would also randomise NBT-loaded and client-created entity instances.
     */
    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        randomizeAppearance(level.getRandom());
        return result;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BODY_SIZE, 100);
        builder.define(KOBOLD_BREAST_SIZE, 100);
        builder.define(PRIMARY_COLOR, Colors.PEACH);
        builder.define(SECONDARY_COLOR, Colors.BANANA);
        builder.define(IRIS_COLOR, Colors.SKY_BLUE);
        builder.define(TOP_HORN_TYPE, 0);
        builder.define(BOTTOM_HORN_TYPE, 0);
        builder.define(HITBOX_HEIGHT, calculateHitboxHeight(100));
    }

    @Override
    public Item isAttractedTo() {
        return Items.RAW_IRON;
    }

    @Override
    public String getGirlID() {
        return "kobold";
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
    public float getWeaponBoneXRotation() {
        return -100f;
    }

    @Override
    public boolean hasStripAnim() {
        return false;
    }

    // ----- getters/setters for customization -----

    public int getBodySize() {
        return this.entityData.get(BODY_SIZE);
    }

    public void setBodySize(int size) {
        int clamped = Mth.clamp(size, MIN_BODY_SIZE, MAX_BODY_SIZE);
        this.entityData.set(BODY_SIZE, clamped);
        if (!this.level().isClientSide()) {
            this.entityData.set(HITBOX_HEIGHT, calculateHitboxHeight(clamped));
        }
        customizationApplied = false;
    }

    public int getKoboldBreastSize() {
        return this.entityData.get(KOBOLD_BREAST_SIZE);
    }

    public void setKoboldBreastSize(int size) {
        this.entityData.set(KOBOLD_BREAST_SIZE, Mth.clamp(size, MIN_BREAST_SIZE, MAX_BREAST_SIZE));
        customizationApplied = false;
    }

    public int getPrimaryColor() {
        return this.entityData.get(PRIMARY_COLOR);
    }

    public void setPrimaryColor(int color) {
        this.entityData.set(PRIMARY_COLOR, color);
        customizationApplied = false;
    }

    public int getSecondaryColor() {
        return this.entityData.get(SECONDARY_COLOR);
    }

    public void setSecondaryColor(int color) {
        this.entityData.set(SECONDARY_COLOR, color);
        customizationApplied = false;
    }

    public int getIrisColor() {
        return this.entityData.get(IRIS_COLOR);
    }

    public void setIrisColor(int color) {
        this.entityData.set(IRIS_COLOR, color);
        customizationApplied = false;
    }

    public int getTopHornType() {
        return this.entityData.get(TOP_HORN_TYPE);
    }

    public void setTopHornType(int type) {
        this.entityData.set(TOP_HORN_TYPE, Mth.clamp(type, 0, 7));
        customizationApplied = false;
    }

    public int getBottomHornType() {
        return this.entityData.get(BOTTOM_HORN_TYPE);
    }

    public void setBottomHornType(int type) {
        this.entityData.set(BOTTOM_HORN_TYPE, Mth.clamp(type, 0, 2));
        customizationApplied = false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BodySize", getBodySize());
        tag.putInt("KoboldBreastSize", getKoboldBreastSize());
        tag.putInt("PrimaryColor", getPrimaryColor());
        tag.putInt("SecondaryColor", getSecondaryColor());
        tag.putInt("IrisColor", getIrisColor());
        tag.putInt("TopHornType", getTopHornType());
        tag.putInt("BottomHornType", getBottomHornType());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("BodySize")) {
            setBodySize(tag.getInt("BodySize"));
        } else if (tag.contains("KoboldBodySize")) {
            // Compatibility with worlds saved by earlier NeoForge port builds.
            setBodySize(tag.getInt("KoboldBodySize"));
        }
        if (tag.contains("KoboldBreastSize")) setKoboldBreastSize(tag.getInt("KoboldBreastSize"));
        if (tag.contains("PrimaryColor")) setPrimaryColor(tag.getInt("PrimaryColor"));
        if (tag.contains("SecondaryColor")) setSecondaryColor(tag.getInt("SecondaryColor"));
        if (tag.contains("IrisColor")) setIrisColor(tag.getInt("IrisColor"));
        if (tag.contains("TopHornType")) setTopHornType(tag.getInt("TopHornType"));
        if (tag.contains("BottomHornType")) setBottomHornType(tag.getInt("BottomHornType"));
        this.entityData.set(HITBOX_HEIGHT, calculateHitboxHeight(getBodySize()));
        this.refreshDimensions();
        customizationApplied = false;
    }

    public enum PatternPresets {
        PEACH_BANANA(Colors.PEACH, Colors.BANANA),
        BLUE_WHITE(Colors.BLUE, Colors.WHITE),
        RED_ORANGE(Colors.RED, Colors.ORANGE),
        GREEN_LIME(Colors.GREEN, Colors.LIME),
        PURPLE_PINK(Colors.PURPLE, Colors.PINK),
        GRAY_DARK(Colors.GRAY, Colors.DARK_GRAY),
        CYAN_TEAL(Colors.CYAN, Colors.TEAL);

        public final int primary;
        public final int secondary;

        PatternPresets(int primary, int secondary) {
            this.primary = primary;
            this.secondary = secondary;
        }
    }

    @Override
    public List<Scene> getScenes() {
        return List.of(
                Scene.onPlayer("Blow Job", 4,
                        List.of("blowjob_intro"),
                        List.of("blowjob_slow_R", "blowjob_slow_L"),
                        List.of("blowjob_fast"),
                        "blowjob_cum", 2.5f, false, false, false),

                Scene.onPlayer("Anal", 6,
                        List.of("anal_intro"),
                        List.of("anal_slow"),
                        List.of("anal_fast"),
                        "anal_cum", 4.5f, true, true, false)
        );
    }

    // ------------------------------------------------------- appearance logic

    private float getHitBoxHeight() {
        return this.entityData.get(HITBOX_HEIGHT);
    }

    /** Linear ramp from 1.0 blocks tall at size 65 to 1.75 at size 115. */
    private static float calculateHitboxHeight(int bodySize) {
        int clamped = Mth.clamp(bodySize, MIN_BODY_SIZE, MAX_BODY_SIZE);
        float normalized = (float) (clamped - MIN_BODY_SIZE) / (MAX_BODY_SIZE - MIN_BODY_SIZE);
        return Mth.lerp(normalized, MIN_HITBOX_HEIGHT, MAX_HITBOX_HEIGHT);
    }

    /**
     * Breast bone Z offset so larger sizes do not clip into the torso.
     * Two segments: 60 -> -0.875, 100 -> 0, 160 -> 1.0.
     */
    private static float calculateBreastZOffset(int breastSize) {
        int clamped = Mth.clamp(breastSize, MIN_BREAST_SIZE, MAX_BREAST_SIZE);
        if (clamped <= 100) {
            return Mth.lerp((float) (clamped - 60) / 40f, -0.875f, 0f);
        }
        return Mth.lerp((float) (clamped - 100) / 60f, 0f, 1.0f);
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        // 1.21.1 renamed getBaseDimensions -> getDefaultDimensions; scaling() replaces changing().
        return EntityDimensions.scalable(0.5f, getHitBoxHeight());
    }

    public void setColorPreset(PatternPresets preset) {
        this.entityData.set(PRIMARY_COLOR, preset.primary);
        this.entityData.set(SECONDARY_COLOR, preset.secondary);
        customizationApplied = false;
    }

    private void randomizeAppearance(RandomSource random) {
        int health = MIN_HEALTH + random.nextInt(MAX_HEALTH - MIN_HEALTH + 1);
        AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(health);
            this.setHealth(health);
        }

        setBodySize(MIN_BODY_SIZE + random.nextInt(MAX_BODY_SIZE - MIN_BODY_SIZE + 1));
        PatternPresets[] presets = PatternPresets.values();
        setColorPreset(presets[random.nextInt(presets.length)]);
        setIrisColor(Colors.ALL_COLORS.get(random.nextInt(Colors.ALL_COLORS.size())));
        setTopHornType(random.nextInt(8));
        setBottomHornType(random.nextInt(3));
        setKoboldBreastSize(MIN_BREAST_SIZE + random.nextInt(MAX_BREAST_SIZE - MIN_BREAST_SIZE + 1));
    }

    // Bone groups tinted with the primary / secondary / iris colour.
    private static List<String> primaryBones() {
        return List.of("armL", "armR", "torsoR", "torsoL", "neck", "hip", "head",
                "hornDL2", "hornDR2", "hornDL3M", "hornDR3M", "legL", "legR");
    }

    private static List<String> secondaryBones() {
        return List.of("frontNeck", "layer2", "layer", "vagina", "boobs", "innerCheekRL",
                "innerCheekLL", "down", "down2", "down3", "down4", "down5",
                "hornDL3S", "hornDR3S", "fuckhole");
    }

    private static List<String> irisBones() {
        return List.of("irisL", "irisR");
    }

    private List<String> ignoreBones() {
        List<String> bones = new java.util.ArrayList<>(List.of(
                "hornUR", "hornUL", "hornDR", "hornDL", "mouth", "eyes",
                "dotL", "dotR", "tailpack", "crown", "tounge"));
        for (List<String> boneNames : getArmorBones().values()) {
            bones.addAll(boneNames);
        }
        return bones;
    }

    private static List<String> topHornBones(int type) {
        return List.of("hornUL" + type, "hornUR" + type);
    }

    private static List<String> bottomHornBones(int type) {
        return List.of("hornDL" + type, "hornDR" + type);
    }

    @Override
    protected java.util.Map<net.minecraft.world.entity.EquipmentSlot, List<String>> getArmorBones() {
        java.util.Map<net.minecraft.world.entity.EquipmentSlot, List<String>> bones = super.getArmorBones();
        bones.put(net.minecraft.world.entity.EquipmentSlot.LEGS, List.of(
                "armorHip", "armorPantsLowL", "armorPantsUpL",
                "armorPantsLowR", "armorPantsUpR", "armorBootyL",
                "armorBootyR", "armorKneeR", "armorKneeL"));
        return bones;
    }

    /** Client-only: pushes colours and horn visibility into the bone override maps. */
    private void applyCustomizations() {
        if (!this.level().isClientSide()) return;

        overrideBoneColor(primaryBones(), getPrimaryColor());
        overrideBoneColor(secondaryBones(), getSecondaryColor());
        overrideBoneColor(irisBones(), getIrisColor());
        overrideBoneColor(ignoreBones(), Colors.WHITE);

        for (int i = 0; i <= 7; i++) {
            setBoneVisibility(topHornBones(i), i == getTopHornType());
        }
        for (int i = 0; i <= 2; i++) {
            setBoneVisibility(bottomHornBones(i), i == getBottomHornType());
        }

        customizationApplied = true;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (PRIMARY_COLOR.equals(key) || SECONDARY_COLOR.equals(key) || IRIS_COLOR.equals(key)
                || TOP_HORN_TYPE.equals(key) || BOTTOM_HORN_TYPE.equals(key)) {
            customizationApplied = false;
        }
        if (HITBOX_HEIGHT.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            float currentHeight = calculateHitboxHeight(getBodySize());
            if (Math.abs(currentHeight - lastHitboxHeight) > 0.001f) {
                lastHitboxHeight = currentHeight;
                this.entityData.set(HITBOX_HEIGHT, currentHeight);
                this.refreshDimensions();
            }
            return;
        }

        if (!customizationApplied) {
            applyCustomizations();
        }

        setBoneSize("body", getBodySize());

        int breastSize = getKoboldBreastSize();
        setBoneSize("boobs", breastSize, MIN_BREAST_SIZE, MAX_BREAST_SIZE);
        setBonePos("boobs", 0f, 0f, calculateBreastZOffset(breastSize));
    }
}
