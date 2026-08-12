package com.sandymandy.pleasurehorizons.entity.girls;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import com.sandymandy.pleasurehorizons.util.Colors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

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

    public KoboldEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createDefaultAttributes();
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
        this.entityData.set(BODY_SIZE, Math.clamp(size, 65, 115));
    }

    public int getKoboldBreastSize() {
        return this.entityData.get(KOBOLD_BREAST_SIZE);
    }

    public void setKoboldBreastSize(int size) {
        this.entityData.set(KOBOLD_BREAST_SIZE, Math.clamp(size, 60, 160));
    }

    public int getPrimaryColor() {
        return this.entityData.get(PRIMARY_COLOR);
    }

    public void setPrimaryColor(int color) {
        this.entityData.set(PRIMARY_COLOR, color);
    }

    public int getSecondaryColor() {
        return this.entityData.get(SECONDARY_COLOR);
    }

    public void setSecondaryColor(int color) {
        this.entityData.set(SECONDARY_COLOR, color);
    }

    public int getIrisColor() {
        return this.entityData.get(IRIS_COLOR);
    }

    public void setIrisColor(int color) {
        this.entityData.set(IRIS_COLOR, color);
    }

    public int getTopHornType() {
        return this.entityData.get(TOP_HORN_TYPE);
    }

    public void setTopHornType(int type) {
        this.entityData.set(TOP_HORN_TYPE, Math.clamp(type, 0, 7));
    }

    public int getBottomHornType() {
        return this.entityData.get(BOTTOM_HORN_TYPE);
    }

    public void setBottomHornType(int type) {
        this.entityData.set(BOTTOM_HORN_TYPE, Math.clamp(type, 0, 2));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("KoboldBodySize", getBodySize());
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
        if (tag.contains("KoboldBodySize")) setBodySize(tag.getInt("KoboldBodySize"));
        if (tag.contains("KoboldBreastSize")) setKoboldBreastSize(tag.getInt("KoboldBreastSize"));
        if (tag.contains("PrimaryColor")) setPrimaryColor(tag.getInt("PrimaryColor"));
        if (tag.contains("SecondaryColor")) setSecondaryColor(tag.getInt("SecondaryColor"));
        if (tag.contains("IrisColor")) setIrisColor(tag.getInt("IrisColor"));
        if (tag.contains("TopHornType")) setTopHornType(tag.getInt("TopHornType"));
        if (tag.contains("BottomHornType")) setBottomHornType(tag.getInt("BottomHornType"));
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
}

