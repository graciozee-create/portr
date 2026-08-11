package com.sandymandy.pleasurehorizons.entity.base;

import com.sandymandy.pleasurehorizons.util.inventory.GirlInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Base class for every girl entity.
 *
 * <p>Ported from the Fabric original. Notable platform differences:</p>
 * <ul>
 *     <li>{@code TrackedData} → {@link EntityDataAccessor}, {@code DataTracker} → {@link SynchedEntityData}</li>
 *     <li>{@code ReadView}/{@code WriteView} (1.21.6) → {@link CompoundTag} (1.21.1)</li>
 *     <li>{@code RangedAttackMob} is not implemented here; ranged behaviour lives in the AI subclasses</li>
 * </ul>
 */
public abstract class GirlEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> WAITING_AT_BED =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_TEMPORARY =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CREATED_CLONE =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LOCKED_STATE =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> WAITING_FOR_PLAYER =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FROZEN_STATE =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STRIPPED =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FOLLOWING =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IN_SCENE =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> OVERRIDE_LOOP =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> OVERRIDE_HOLD =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> OVERRIDE_ANIM_PLAYING =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PLAYER_MODEL_SLIM =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAVING_SEX =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SITTING =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PREGNANT =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CAN_GET_IMPREGNATED =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> OVERRIDE_ANIM =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> SCENE_ANIM =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> BREAST_SIZE =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RELATIONSHIP_LEVEL =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_RELATIONSHIP_LEVEL =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PREGNANCY_STAGE =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MILKED_AMOUNT =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> BASE_POS =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<ItemStack> CONSUMING_STACK =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.ITEM_STACK);

    // NOTE: deliberately uses the vanilla VECTOR3 serializer rather than a custom one.
    // defineId() runs during class initialisation, which can happen before a modded
    // serializer registry is populated - that would hard-crash on startup.
    private static final EntityDataAccessor<Vector3f> PASSENGER_BONE_POSITION =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Vector3f> BREAST_OFFSET =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.VECTOR3);

    public static final Random RANDOM = new Random();

    public Map<String, Boolean> boneVisibility = new HashMap<>();
    public Map<String, Integer> boneColorOverrides = new HashMap<>();
    public Map<String, Vec3> boneSizeOverrides = new HashMap<>();
    public Map<String, Vec3> bonePositionOffset = new HashMap<>();
    public final Map<EquipmentSlot, Boolean> armorVisibility = new EnumMap<>(EquipmentSlot.class);

    public Vec3 previousVelocity = Vec3.ZERO;
    public float previousYaw = 0;
    public float passengerYOffset = -1f;
    public boolean currentLoopState = false;
    public boolean currentHoldState = false;
    public String currentAnimState = "idle";

    public final GirlInventory inventory = GirlInventory.ofSize();

    private boolean guiOpenState = false;
    @Nullable
    private Player lookAtTarget = null;

    protected GirlEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createDefaultAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(WAITING_AT_BED, false);
        builder.define(IS_TEMPORARY, false);
        builder.define(CREATED_CLONE, false);
        builder.define(LOCKED_STATE, false);
        builder.define(FROZEN_STATE, false);
        builder.define(WAITING_FOR_PLAYER, false);
        builder.define(STRIPPED, false);
        builder.define(FOLLOWING, false);
        builder.define(IN_SCENE, false);
        builder.define(OVERRIDE_LOOP, false);
        builder.define(OVERRIDE_HOLD, false);
        builder.define(OVERRIDE_ANIM_PLAYING, false);
        builder.define(PLAYER_MODEL_SLIM, false);
        builder.define(HAVING_SEX, false);
        builder.define(SITTING, false);
        builder.define(PREGNANT, false);
        builder.define(CAN_GET_IMPREGNATED, false);
        builder.define(PREGNANCY_STAGE, 0);
        builder.define(RELATIONSHIP_LEVEL, 0);
        builder.define(MAX_RELATIONSHIP_LEVEL, 4);
        builder.define(BREAST_SIZE, 100);
        builder.define(MILKED_AMOUNT, 0);
        builder.define(BREAST_OFFSET, new Vector3f());
        builder.define(PASSENGER_BONE_POSITION, new Vector3f());
        builder.define(BASE_POS, BlockPos.ZERO);
        builder.define(OVERRIDE_ANIM, "");
        builder.define(SCENE_ANIM, "");
        builder.define(CONSUMING_STACK, Items.COOKED_BEEF.getDefaultInstance());
    }

    // ---------------------------------------------------------------- state

    public void setFollowing(boolean follow) {
        this.entityData.set(FOLLOWING, follow);
    }

    public boolean isFollowing() {
        return this.entityData.get(FOLLOWING);
    }

    public void setStripped(boolean stripped) {
        this.entityData.set(STRIPPED, stripped);
    }

    public boolean isStripped() {
        return this.entityData.get(STRIPPED);
    }

    public void setFreeze(boolean locked) {
        this.entityData.set(FROZEN_STATE, locked);
    }

    public boolean isFrozenInPlace() {
        return this.entityData.get(FROZEN_STATE);
    }

    public void setMovementLockedState(boolean locked) {
        this.entityData.set(LOCKED_STATE, locked);
    }

    public boolean isMovementLocked() {
        return this.entityData.get(LOCKED_STATE);
    }

    public void setGUIOpenState(boolean state, @Nullable Player lookAt) {
        this.guiOpenState = state;
        this.lookAtTarget = lookAt;
    }

    public void setGUIOpenState(boolean state) {
        this.setGUIOpenState(state, null);
    }

    public boolean isGUIOpen() {
        return this.guiOpenState;
    }

    @Nullable
    public Player getLookAtTarget() {
        return this.lookAtTarget;
    }

    public void setSceneState(boolean inScene) {
        this.entityData.set(IN_SCENE, inScene);
    }

    public boolean isSceneActive() {
        return this.entityData.get(IN_SCENE);
    }

    public void setOverrideAnim(String anim) {
        this.entityData.set(OVERRIDE_ANIM, anim);
    }

    public String getOverrideAnim() {
        return this.entityData.get(OVERRIDE_ANIM);
    }

    public void setOverrideLoop(boolean loop) {
        this.entityData.set(OVERRIDE_LOOP, loop);
    }

    public boolean getOverrideLoopState() {
        return this.entityData.get(OVERRIDE_LOOP);
    }

    public void setOverrideHold(boolean hold) {
        this.entityData.set(OVERRIDE_HOLD, hold);
    }

    public boolean getOverrideHoldState() {
        return this.entityData.get(OVERRIDE_HOLD);
    }

    public void setOverrideAnimPlaying(boolean playing) {
        this.entityData.set(OVERRIDE_ANIM_PLAYING, playing);
    }

    public boolean isOverrideAnimPlaying() {
        return this.entityData.get(OVERRIDE_ANIM_PLAYING);
    }

    public void setSceneAnim(String anim) {
        this.entityData.set(SCENE_ANIM, anim);
    }

    public String getSceneAnim() {
        return this.entityData.get(SCENE_ANIM);
    }

    public boolean isWaitingAtBed() {
        return this.entityData.get(WAITING_AT_BED);
    }

    public void setWaitingAtBedState(boolean state) {
        this.entityData.set(WAITING_AT_BED, state);
    }

    public boolean isTemporary() {
        return this.entityData.get(IS_TEMPORARY);
    }

    public void setTemporaryState(boolean state) {
        this.entityData.set(IS_TEMPORARY, state);
    }

    public boolean createdClone() {
        return this.entityData.get(CREATED_CLONE);
    }

    public void setCreatedCloneState(boolean state) {
        this.entityData.set(CREATED_CLONE, state);
    }

    public boolean isWaitingForPlayer() {
        return this.entityData.get(WAITING_FOR_PLAYER);
    }

    public void setWaitingForPlayerState(boolean state) {
        this.entityData.set(WAITING_FOR_PLAYER, state);
    }

    public void setIsPlayerModelSlim(boolean isSlim) {
        this.entityData.set(PLAYER_MODEL_SLIM, isSlim);
    }

    public boolean isPlayerModelSlim() {
        return this.entityData.get(PLAYER_MODEL_SLIM);
    }

    public void setHavingSex(boolean state) {
        this.entityData.set(HAVING_SEX, state);
    }

    public boolean isHavingSex() {
        return this.entityData.get(HAVING_SEX);
    }

    public void setSitting(boolean sitting) {
        this.entityData.set(SITTING, sitting);
    }

    public boolean isSitting() {
        return this.entityData.get(SITTING);
    }

    public void setPregnantState(boolean pregnant) {
        this.entityData.set(PREGNANT, pregnant);
    }

    public boolean isPregnant() {
        return this.entityData.get(PREGNANT);
    }

    public void canGetImpregnatedState(boolean value) {
        this.entityData.set(CAN_GET_IMPREGNATED, value);
    }

    public boolean canGetImpregnated() {
        return this.entityData.get(CAN_GET_IMPREGNATED);
    }

    public void setPregnancyStage(int num) {
        this.entityData.set(PREGNANCY_STAGE, Mth.clamp(num, 0, maxPregnancyStage()));
    }

    public int getPregnancyStage() {
        return this.entityData.get(PREGNANCY_STAGE);
    }

    public int getCurrentRelationshipLevel() {
        return this.entityData.get(RELATIONSHIP_LEVEL);
    }

    public void setCurrentRelationshipLevel(int value) {
        this.entityData.set(RELATIONSHIP_LEVEL, value);
    }

    public void setPassengerBonePosition(Vec3 position) {
        this.entityData.set(PASSENGER_BONE_POSITION,
                new Vector3f((float) position.x, (float) position.y, (float) position.z));
    }

    public Vec3 getPassengerBonePosition() {
        Vector3f v = this.entityData.get(PASSENGER_BONE_POSITION);
        return new Vec3(v.x(), v.y(), v.z());
    }

    public void setBasePos(BlockPos pos) {
        this.entityData.set(BASE_POS, pos);
    }

    public BlockPos getBasePos() {
        return this.entityData.get(BASE_POS);
    }

    public void setBreastSize(int value) {
        this.entityData.set(BREAST_SIZE, value);
    }

    public int getBreastSize() {
        return this.entityData.get(BREAST_SIZE);
    }

    public void setMilkedAmount(int value) {
        this.entityData.set(MILKED_AMOUNT, value);
    }

    public int getMilkedAmount() {
        return this.entityData.get(MILKED_AMOUNT);
    }

    public void setBreastOffset(Vec3 value) {
        this.entityData.set(BREAST_OFFSET,
                new Vector3f((float) value.x, (float) value.y, (float) value.z));
    }

    public Vec3 getBreastOffset() {
        Vector3f v = this.entityData.get(BREAST_OFFSET);
        return new Vec3(v.x(), v.y(), v.z());
    }

    public void setConsumingStack(ItemStack stack) {
        this.entityData.set(CONSUMING_STACK, stack);
    }

    public ItemStack getConsumingStack() {
        return this.entityData.get(CONSUMING_STACK);
    }

    // ------------------------------------------------------------ behaviour

    public GirlInventory getInventory() {
        return this.inventory;
    }

    public Item isAttractedTo() {
        return Items.DANDELION;
    }

    public boolean useUpRelationShipLevels() {
        return false;
    }

    /** Identifier used to pick models, textures and animations. */
    public abstract String getGirlID();

    public int getBreastMinSize() {
        return 25;
    }

    public int getBreastMaxSize() {
        return 150;
    }

    public int getSizeGUI() {
        return 20;
    }

    public float getYAxisGUI() {
        return 0.0625F;
    }

    public float getWeaponBoneXRotation() {
        return 150.0F;
    }

    public int getMaxBellySizeWhenPregnant() {
        return 450;
    }

    public int maxPregnancyStage() {
        return 3;
    }

    public boolean hasStripAnim() {
        return true;
    }

    public int maxRelationshipLevel() {
        return this.entityData.get(MAX_RELATIONSHIP_LEVEL);
    }

    public void setMaxRelationshipLevel(int value) {
        this.entityData.set(MAX_RELATIONSHIP_LEVEL, value);
    }

    protected Map<EquipmentSlot, List<String>> getArmorBones() {
        Map<EquipmentSlot, List<String>> armor = new HashMap<>();
        armor.put(EquipmentSlot.HEAD, new ArrayList<>(List.of("armorHelmet")));
        armor.put(EquipmentSlot.CHEST, new ArrayList<>(List.of(
                "armorBoobs", "armorChest", "armorShoulderL", "armorShoulderR")));
        armor.put(EquipmentSlot.LEGS, new ArrayList<>(List.of(
                "armorHip", "armorPantsLowL", "armorPantsUpL", "armorPantsLowR",
                "armorPantsUpR", "armorBootyL", "armorBootyR")));
        armor.put(EquipmentSlot.FEET, new ArrayList<>(List.of("armorShoesL", "armorShoesR")));
        return armor;
    }

    public boolean isFoodItem(ItemStack stack) {
        return stack.is(net.minecraft.tags.ItemTags.WOLF_FOOD);
    }

    // ----------------------------------------------------------------- save

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Stripped", isStripped());
        compound.putBoolean("Following", isFollowing());
        compound.putBoolean("Pregnant", isPregnant());
        compound.putBoolean("CanGetImpregnated", canGetImpregnated());
        compound.putBoolean("Sitting", isSitting());
        compound.putBoolean("Temporary", isTemporary());
        compound.putInt("PregnancyStage", getPregnancyStage());
        compound.putInt("RelationshipLevel", getCurrentRelationshipLevel());
        compound.putInt("BreastSize", getBreastSize());
        compound.putInt("MilkedAmount", getMilkedAmount());

        CompoundTag inventoryTag = new CompoundTag();
        ContainerHelper.saveAllItems(inventoryTag, this.inventory.getItems(), this.registryAccess());
        compound.put("Inventory", inventoryTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setStripped(compound.getBoolean("Stripped"));
        setFollowing(compound.getBoolean("Following"));
        setPregnantState(compound.getBoolean("Pregnant"));
        canGetImpregnatedState(compound.getBoolean("CanGetImpregnated"));
        setSitting(compound.getBoolean("Sitting"));
        setTemporaryState(compound.getBoolean("Temporary"));
        setPregnancyStage(compound.getInt("PregnancyStage"));
        setCurrentRelationshipLevel(compound.getInt("RelationshipLevel"));
        if (compound.contains("BreastSize")) {
            setBreastSize(compound.getInt("BreastSize"));
        }
        setMilkedAmount(compound.getInt("MilkedAmount"));

        if (compound.contains("Inventory")) {
            ContainerHelper.loadAllItems(
                    compound.getCompound("Inventory"), this.inventory.getItems(), this.registryAccess());
        }
    }
}
