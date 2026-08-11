package com.sandymandy.pleasurehorizons.entity.girls;

import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import com.sandymandy.pleasurehorizons.entity.base.wild.WildGirlEntity;
import com.sandymandy.pleasurehorizons.networking.S2C.OpenKoboldCustomizeScreenS2CPacket;
import com.sandymandy.pleasurehorizons.util.Colors;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class KoboldEntity extends WildGirlEntity {

    // ===== Tracked Data =====
    private static final TrackedData<Boolean> LEADER_STATE = DataTracker.registerData(KoboldEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> BODY_SIZE = DataTracker.registerData(KoboldEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> KOBOLD_BREAST_SIZE = DataTracker.registerData(KoboldEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> KOBOLD_HEALTH = DataTracker.registerData(KoboldEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> PRIMARY_COLOR = DataTracker.registerData(KoboldEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> SECONDARY_COLOR = DataTracker.registerData(KoboldEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> IRIS_COLOR = DataTracker.registerData(KoboldEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TOP_HORN_TYPE = DataTracker.registerData(KoboldEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> BOTTOM_HORN_TYPE = DataTracker.registerData(KoboldEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> HITBOX_HEIGHT = DataTracker.registerData(KoboldEntity.class, TrackedDataHandlerRegistry.FLOAT);

    // Hitbox scaling constants
    private static final float MIN_HITBOX_HEIGHT = 1.0f;   // At size 65
    private static final float MAX_HITBOX_HEIGHT = 1.75f;  // At size 115
    private static final int MIN_BODY_SIZE = 65;
    private static final int MAX_BODY_SIZE = 115;
    private static final int MIN_BREAST_SIZE = 60;
    private static final int MAX_BREAST_SIZE = 160;
    private static final int MIN_HEALTH = 4;
    private static final int MAX_HEALTH = 12;

    // Track last hitbox height to avoid unnecessary recalculations
    private float lastHitboxHeight = 1.0f;

    @Override
    protected Map<EquipmentSlot, List<String>> getArmorBones() {
        Map<EquipmentSlot, List<String>> bones = super.getArmorBones();
        bones.put(EquipmentSlot.LEGS, List.of(
                "armorHip", "armorPantsLowL", "armorPantsUpL",
                "armorPantsLowR", "armorPantsUpR", "armorBootyL",
                "armorBootyR", "armorKneeR", "armorKneeL"
        ));
        return bones;
    }

    // Bone Categories
    private List<String> primaryBones() {return List.of(
            "armL", "armR", "torsoR", "torsoL", "neck", "hip", "head",
            "hornDL2", "hornDR2", "hornDL3M", "hornDR3M", "legL", "legR"
    );}

    private List<String> secondaryBones() {return List.of(
            "frontNeck", "layer2", "layer", "vagina", "boobs", "innerCheekRL",
            "innerCheekLL", "down", "down2", "down3", "down4", "down5",
            "hornDL3S", "hornDR3S", "fuckhole"
    );}

    private List<String> irisBones() {return List.of("irisL", "irisR");}

    private List<String> ignoreBones() {
        List<String> bones = new ArrayList<>(List.of(
                "hornUR", "hornUL", "hornDR", "hornDL", "mouth", "eyes",
                "dotL", "dotR", "tailpack", "crown", "tounge"
        ));

        for (List<String> boneNames : getArmorBones().values())
        {
            bones.addAll(boneNames);
        }

        return  bones;
    }

    // ===== Horn Type Lists =====
    private final List<String> topHornType0 = List.of("hornUL0", "hornUR0");
    private final List<String> topHornType1 = List.of("hornUL1", "hornUR1");
    private final List<String> topHornType2 = List.of("hornUL2", "hornUR2");
    private final List<String> topHornType3 = List.of("hornUL3", "hornUR3");
    private final List<String> topHornType4 = List.of("hornUL4", "hornUR4");
    private final List<String> topHornType5 = List.of("hornUL5", "hornUR5");
    private final List<String> topHornType6 = List.of("hornUL6", "hornUR6");
    private final List<String> topHornType7 = List.of("hornUL7", "hornUR7");

    private final List<String> bottomHornType0 = List.of("hornDL0", "hornDR0");
    private final List<String> bottomHornType1 = List.of("hornDL1", "hornDR1");
    private final List<String> bottomHornType2 = List.of("hornDL2", "hornDR2");

    // Track if we've already applied customization
    private boolean customizationApplied = false;

    public KoboldEntity(EntityType<? extends WildGirlEntity> entityType, World world) {
        super(entityType, world);
        randomizeAppearance();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(LEADER_STATE, false);
        builder.add(BODY_SIZE, 100);
        builder.add(KOBOLD_BREAST_SIZE, 100);
        builder.add(KOBOLD_HEALTH, 6);
        builder.add(PRIMARY_COLOR, Colors.PEACH);
        builder.add(SECONDARY_COLOR, Colors.BANANA);
        builder.add(IRIS_COLOR, Colors.SKY_BLUE);
        builder.add(TOP_HORN_TYPE, 0);
        builder.add(BOTTOM_HORN_TYPE, 0);
        builder.add(HITBOX_HEIGHT, calculateHitboxHeight(100));
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

    /**
     * Calculates hitbox height based on body size
     * @param bodySize The body size (65-115)
     * @return The calculated hitbox height (1.0-1.75)
     */
    private static float calculateHitboxHeight(int bodySize) {
        // Clamp body size to valid range
        int clampedSize = Math.clamp(bodySize, MIN_BODY_SIZE, MAX_BODY_SIZE);

        // Linear interpolation between min and max heights
        // Formula: height = minHeight + (size - minSize) * (maxHeight - minHeight) / (maxSize - minSize)
        float normalizedSize = (float)(clampedSize - MIN_BODY_SIZE) / (MAX_BODY_SIZE - MIN_BODY_SIZE);
        return MathHelper.lerp(normalizedSize, MIN_HITBOX_HEIGHT, MAX_HITBOX_HEIGHT);
    }

    public void randomizeAppearance() {
        // Random Health
        int randomHealth = RANDOM.nextInt(MIN_HEALTH, MAX_HEALTH  + 1);
        this.setKoboldHealth(randomHealth);

        // Random body size
        int randomBodySize = RANDOM.nextInt(MIN_BODY_SIZE, MAX_BODY_SIZE + 1);
        this.setBodySize(randomBodySize);

        // Random color
        PatternPresets preset = PatternPresets.values()[RANDOM.nextInt(PatternPresets.values().length)];
        this.setColorPreset(preset);

        Integer irisColor = Colors.ALL_COLORS.get(RANDOM.nextInt(Colors.ALL_COLORS.size()));
        this.setIrisColor(irisColor);

        // Random horn type
        this.setTopHornType(RANDOM.nextInt(0, 8));// 0-7 inclusive
        this.setBottomHornType(RANDOM.nextInt(0, 3)); // 0-2 inclusive

        // Random breast size
        this.setKoboldBreastSize(RANDOM.nextInt(MIN_BREAST_SIZE , MAX_BREAST_SIZE + 1));
    }

    public void setColorPreset(PatternPresets preset) {
        this.dataTracker.set(PRIMARY_COLOR, preset.primary);
        this.dataTracker.set(SECONDARY_COLOR, preset.secondary);
        customizationApplied = false; // Mark for re-application
    }

    public void setLeaderState(boolean state){
        this.dataTracker.set(LEADER_STATE, state);
    }

    public void setKoboldHealth(int num) {
        int clampedSize = Math.clamp(num, MIN_HEALTH, MAX_HEALTH);
        this.dataTracker.set(KOBOLD_HEALTH, clampedSize);

        customizationApplied = false;
    }

    public void setKoboldBreastSize(int size) {
        int clampedSize = Math.clamp(size, MIN_BREAST_SIZE, MAX_BREAST_SIZE);
        this.dataTracker.set(KOBOLD_BREAST_SIZE, clampedSize);
        customizationApplied = false;
    }

    public void setBodySize(int size) {
        int clampedSize = Math.clamp(size, MIN_BODY_SIZE, MAX_BODY_SIZE);
        this.dataTracker.set(BODY_SIZE, clampedSize);

        // Update hitbox height on server
        if (!this.getWorld().isClient()) {
            float newHeight = calculateHitboxHeight(clampedSize);
            this.dataTracker.set(HITBOX_HEIGHT, newHeight);
        }

        customizationApplied = false;
    }

    public void setPrimaryColor(int color) {
        this.dataTracker.set(PRIMARY_COLOR, color);
        customizationApplied = false;
    }

    public void setSecondaryColor(int color) {
        this.dataTracker.set(SECONDARY_COLOR, color);
        customizationApplied = false;
    }

    public void setIrisColor(int color) {
        this.dataTracker.set(IRIS_COLOR, color);
        customizationApplied = false;
    }

    public void setTopHornType(int type) {
        this.dataTracker.set(TOP_HORN_TYPE, Math.clamp(type, 0, 7));
        customizationApplied = false;
    }

    public void setBottomHornType(int type) {
        this.dataTracker.set(BOTTOM_HORN_TYPE, Math.clamp(type, 0, 2));
        customizationApplied = false;
    }
    public boolean getLeaderState() { return this.dataTracker.get(LEADER_STATE);}
    public int getKoboldHealth() { return this.dataTracker.get(KOBOLD_HEALTH); }
    public int getKoboldBreastSize() { return this.dataTracker.get(KOBOLD_BREAST_SIZE); }
    public int getBodySize() { return this.dataTracker.get(BODY_SIZE); }
    public int getPrimaryColor() { return this.dataTracker.get(PRIMARY_COLOR); }
    public int getSecondaryColor() { return this.dataTracker.get(SECONDARY_COLOR); }
    public int getIrisColor() { return this.dataTracker.get(IRIS_COLOR); }
    public int getTopHornType() { return this.dataTracker.get(TOP_HORN_TYPE); }
    public int getBottomHornType() { return this.dataTracker.get(BOTTOM_HORN_TYPE); }
    private float getHitBoxHeight() { return this.dataTracker.get(HITBOX_HEIGHT); }

    @Override
    protected EntityDimensions getBaseDimensions(EntityPose pose) {
        return EntityDimensions.changing(0.5f, getHitBoxHeight());
    }

    private void applyCustomizations() {
        if (!this.getWorld().isClient()) return;

        this.setBoneVisibility(List.of("crown"), getLeaderState());

        // Apply colors
        this.overrideBoneColor(primaryBones(), getPrimaryColor());
        this.overrideBoneColor(secondaryBones(), getSecondaryColor());
        this.overrideBoneColor(irisBones(), getIrisColor());
        this.overrideBoneColor(ignoreBones(), Colors.WHITE);

        // Hide all horn types first
        this.setBoneVisibility(topHornType0, false);
        this.setBoneVisibility(topHornType1, false);
        this.setBoneVisibility(topHornType2, false);
        this.setBoneVisibility(topHornType3, false);
        this.setBoneVisibility(topHornType4, false);
        this.setBoneVisibility(topHornType5, false);
        this.setBoneVisibility(topHornType6, false);
        this.setBoneVisibility(topHornType7, false);

        this.setBoneVisibility(bottomHornType0, false);
        this.setBoneVisibility(bottomHornType1, false);
        this.setBoneVisibility(bottomHornType2, false);

        // Show selected horn type
        switch (getTopHornType()) {
            case 0 -> this.setBoneVisibility(topHornType0, true);
            case 1 -> this.setBoneVisibility(topHornType1, true);
            case 2 -> this.setBoneVisibility(topHornType2, true);
            case 3 -> this.setBoneVisibility(topHornType3, true);
            case 4 -> this.setBoneVisibility(topHornType4, true);
            case 5 -> this.setBoneVisibility(topHornType5, true);
            case 6 -> this.setBoneVisibility(topHornType6, true);
            case 7 -> this.setBoneVisibility(topHornType7, true);
        }

        switch (getBottomHornType()) {
            case 0 -> this.setBoneVisibility(bottomHornType0, true);
            case 1 -> this.setBoneVisibility(bottomHornType1, true);
            case 2 -> this.setBoneVisibility(bottomHornType2, true);
        }

        // Health
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.MAX_HEALTH))
                .setBaseValue(getKoboldHealth());
        customizationApplied = true;
    }

    /**
     * Calculates breast Z offset based on breast size
     * Size 60  → Z offset -0.875
     * Size 100 → Z offset ~0
     * Size 160 → Z offset 1.0
     */
    private static float calculateBreastZOffset(int breastSize) {
        // Clamp breast size to valid range
        int clampedSize = Math.clamp(breastSize, 60, 160);

        // Two-segment linear interpolation for more accurate positioning
        if (clampedSize <= 100) {
            // From size 60 to 100: offset goes from -0.875 to 0
            float normalizedSize = (float)(clampedSize - 60) / (100 - 60);
            return MathHelper.lerp(normalizedSize, -0.875f, 0f);
        } else {
            // From size 100 to 160: offset goes from 0 to 1.0
            float normalizedSize = (float)(clampedSize - 100) / (160 - 100);
            return MathHelper.lerp(normalizedSize, 0f, 1.0f);
        }
    }

    @Override
    public void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putBoolean("LeaderSate", getLeaderState());
        view.putInt("KoboldHealth", getKoboldHealth());
        view.putInt("KoboldBreastSize", getKoboldBreastSize());
        view.putInt("BodySize", getBodySize());
        view.putInt("PrimaryColor", getPrimaryColor());
        view.putInt("SecondaryColor", getSecondaryColor());
        view.putInt("IrisColor", getIrisColor());
        view.putInt("TopHornType", getTopHornType());
        view.putInt("BottomHornType", getBottomHornType());
    }

    @Override
    public void readCustomData(ReadView view) {
        super.readCustomData(view);
        int bodySize = view.getInt("BodySize", 100);
        this.dataTracker.set(LEADER_STATE, view.getBoolean("LeaderSate", false));
        this.dataTracker.set(KOBOLD_HEALTH, view.getInt("KoboldHealth", 6));
        this.dataTracker.set(KOBOLD_BREAST_SIZE, view.getInt("KoboldBreastSize", 100));
        this.dataTracker.set(BODY_SIZE, bodySize);
        this.dataTracker.set(HITBOX_HEIGHT, calculateHitboxHeight(bodySize));
        this.dataTracker.set(PRIMARY_COLOR, view.getInt("PrimaryColor", Colors.PEACH));
        this.dataTracker.set(SECONDARY_COLOR, view.getInt("SecondaryColor", Colors.BANANA));
        this.dataTracker.set(IRIS_COLOR, view.getInt("IrisColor", Colors.SKY_BLUE));
        this.dataTracker.set(TOP_HORN_TYPE, view.getInt("TopHornType", 0));
        this.dataTracker.set(BOTTOM_HORN_TYPE, view.getInt("BottomHornType", 0));

        this.calculateDimensions();
        customizationApplied = false; // Re-apply on load
    }

    @Override
    public void tick() {

        // Server-side: Update hitbox if body size changed
        if (!this.getWorld().isClient()) {
            float currentHeight = calculateHitboxHeight(getBodySize());
            if (Math.abs(currentHeight - lastHitboxHeight) > 0.001f) {
                lastHitboxHeight = currentHeight;
                this.dataTracker.set(HITBOX_HEIGHT, currentHeight);
                this.calculateDimensions();
            }
        }

        // Client-side: Apply customizations and bone size
        if (this.getWorld().isClient()) {
            if (!customizationApplied) {
                applyCustomizations();
            }

            // Apply body size
            this.setBoneSize("body", getBodySize());

            // Apply breast size and position offset
            int breastSize = getKoboldBreastSize();
            this.setBoneSize("boobs", breastSize, MIN_BREAST_SIZE, MAX_BREAST_SIZE);

            // Calculate and apply breast position offset based on size
            float zOffset = calculateBreastZOffset(breastSize);
            this.setBonePos("boobs", 0f, 0f, zOffset);
        }
        super.tick();
    }

    // ===== Data Tracker Changes =====

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        // Mark for re-application when any customization data changes
        if (data.equals(PRIMARY_COLOR) || data.equals(SECONDARY_COLOR) ||
                data.equals(IRIS_COLOR) || data.equals(TOP_HORN_TYPE) || data.equals(BOTTOM_HORN_TYPE)) {
            customizationApplied = false;
        }

        // Recalculate dimensions when hitbox height changes (client-side sync)
        if (data.equals(HITBOX_HEIGHT)) {
            this.calculateDimensions();
        }
    }
    // Entity Properties

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

    @Override
    public List<Scene> getScenes() {
        return List.of(
                Scene.onPlayer("Blow Job",
                        4,
                        List.of("blowjob_intro"),
                        List.of("blowjob_slow_R", "blowjob_slow_L"),
                        List.of("blowjob_fast"),
                        "blowjob_cum",
                        2.5f,
                        false,
                        false,
                        false),

                Scene.onPlayer("Anal",
                        6,
                        List.of("anal_intro"),
                        List.of("anal_slow"),
                        List.of("anal_fast"),
                        "anal_cum",
                        4.5f,
                        true,
                        true,
                        false)
        );
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return GirlEntity.createDefaultAttributes()
                .add(EntityAttributes.MAX_HEALTH, 15)
                .add(EntityAttributes.MOVEMENT_SPEED, .12)
                .add(EntityAttributes.ATTACK_DAMAGE, 2);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);

        if(stack.isOf(Items.STICK)){
            this.setGUIOpenState(true, player);
            if(!this.getWorld().isClient() && !createdClone()){
                PacketDistributor.sendToPlayer((ServerPlayerEntity) player,
                        new OpenKoboldCustomizeScreenS2CPacket(this.getId(), this.createTempClone().getId()));
            }
        }

        return super.interactMob(player, hand);
    }

    @Override
    public void onTempCloneCreation(GirlEntity clone) {
        KoboldEntity previewEntity = (KoboldEntity) clone;
        previewEntity.setBodySize(this.getBodySize());
        previewEntity.setKoboldBreastSize(this.getKoboldBreastSize());
        previewEntity.setPrimaryColor(this.getPrimaryColor());
        previewEntity.setSecondaryColor(this.getSecondaryColor());
        previewEntity.setIrisColor(this.getIrisColor());
        previewEntity.setTopHornType(this.getTopHornType());
        previewEntity.setBottomHornType(this.getBottomHornType());
        previewEntity.setLeaderState(this.getLeaderState());
        previewEntity.setKoboldHealth(this.getKoboldHealth());
    }
}
