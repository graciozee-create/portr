package com.sandymandy.pleasurehorizons.entity.girls;

import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import com.sandymandy.pleasurehorizons.util.json.CustomGirlLoader;
import com.sandymandy.pleasurehorizons.util.variables.CustomGirlProfile;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A girl whose stats, tame item, hitbox and scenes come from a JSON profile.
 *
 * <p>Previously this class ignored profiles entirely, so every custom girl was a 20 HP
 * apple-tamed shell with no scenes. Sneak-right-clicking her with another profile's tame item
 * swaps the profile, exactly as upstream.</p>
 *
 * <p>Yarn to Mojang: {@code getBaseDimensions(EntityPose)} → {@code getDefaultDimensions(Pose)},
 * {@code EntityDimensions.changing} → {@code EntityDimensions.scalable},
 * {@code calculateDimensions()} → {@code refreshDimensions()},
 * {@code onTrackedDataSet} → {@code onSyncedDataUpdated}.</p>
 */
public class CustomGirlEntity extends SettlementGirlEntityAI {

    private static final EntityDataAccessor<String> GIRL_ID =
            SynchedEntityData.defineId(CustomGirlEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> GIRL_NAME =
            SynchedEntityData.defineId(CustomGirlEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> HITBOX_HEIGHT =
            SynchedEntityData.defineId(CustomGirlEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_PROFILE_PERMANENT =
            SynchedEntityData.defineId(CustomGirlEntity.class, EntityDataSerializers.BOOLEAN);

    private CustomGirlProfile profile = CustomGirlProfile.DEFAULT;
    private float lastHitboxHeight = CustomGirlProfile.DEFAULT.hitboxHeight();

    public CustomGirlEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GIRL_ID, CustomGirlProfile.DEFAULT.id());
        builder.define(GIRL_NAME, CustomGirlProfile.DEFAULT.name());
        builder.define(HITBOX_HEIGHT, CustomGirlProfile.DEFAULT.hitboxHeight());
        builder.define(IS_PROFILE_PERMANENT, false);
    }

    // ------------------------------------------------------------------ profile

    public CustomGirlProfile getProfile() {
        return profile != null ? profile : CustomGirlProfile.DEFAULT;
    }

    public void setProfile(CustomGirlProfile profile, boolean isPermanent) {
        if (profile == null) profile = CustomGirlProfile.DEFAULT;
        this.profile = profile;

        applyAttribute(Attributes.MAX_HEALTH, profile.maxHealth());
        applyAttribute(Attributes.MOVEMENT_SPEED, profile.movementSpeed());
        applyAttribute(Attributes.ATTACK_DAMAGE, profile.attackDamage());
        this.setHealth((float) profile.maxHealth());

        if (!this.level().isClientSide()) {
            this.entityData.set(GIRL_ID, profile.id());
            this.entityData.set(GIRL_NAME, profile.name());
            this.entityData.set(HITBOX_HEIGHT, profile.hitboxHeight());
            this.entityData.set(IS_PROFILE_PERMANENT, isPermanent);
        }

        this.lastHitboxHeight = profile.hitboxHeight();
        this.refreshDimensions();
    }

    private void applyAttribute(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                double value) {
        AttributeInstance instance = this.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    // ------------------------------------------------------- profile-driven data

    @Override
    public String getGirlID() {
        // The animation/model id must match a shipped rig; custom girls all use "default".
        return "default";
    }

    /** Display name comes from the profile, unlike the rig id. */
    public String getProfileName() {
        return this.entityData.get(GIRL_NAME);
    }

    @Override
    public Item isAttractedTo() {
        return getProfile().tameItem();
    }

    @Override
    public List<Scene> getScenes() {
        return getProfile().scenes();
    }

    @Override
    public int getSizeGUI() {
        return getProfile().guiSize();
    }

    @Override
    public float getYAxisGUI() {
        return getProfile().guiYOffset();
    }

    @Override
    public float getWeaponBoneXRotation() {
        return getProfile().weaponBoneRotation();
    }

    @Override
    protected boolean hasBlinkAnimation() {
        return false;
    }

    @Override
    public boolean hasStripAnim() {
        return false;
    }

    private float getHitBoxHeight() {
        return this.entityData.get(HITBOX_HEIGHT);
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return EntityDimensions.scalable(0.5f, getHitBoxHeight());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (HITBOX_HEIGHT.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            float currentHeight = getProfile().hitboxHeight();
            if (Math.abs(currentHeight - lastHitboxHeight) > 0.001f) {
                lastHitboxHeight = currentHeight;
                this.entityData.set(HITBOX_HEIGHT, currentHeight);
                this.refreshDimensions();
            }
        }
    }

    // ------------------------------------------------------------- interaction

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && player.isShiftKeyDown()) {
            boolean maySwitch = !this.isTamed() || this.isOwner(player);
            if (maySwitch) {
                InteractionResult result = trySwitchingProfiles(player);
                if (result == InteractionResult.SUCCESS) {
                    return result;
                }
            }
        }
        return super.mobInteract(player, hand);
    }

    private InteractionResult trySwitchingProfiles(Player player) {
        if (this.entityData.get(IS_PROFILE_PERMANENT)) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        CustomGirlProfile candidate = CustomGirlLoader.checkItem(stack.getItem());
        if (candidate == null) return InteractionResult.PASS;
        if (candidate.id().equals(getProfile().id())) return InteractionResult.PASS;

        setProfile(candidate, false);
        player.displayClientMessage(
                Component.translatable("msg.pleasurehorizons.profile_switched", candidate.name()), true);
        return InteractionResult.SUCCESS;
    }

    // --------------------------------------------------------------- save data

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("GirlProfileID", getProfile().id());
        compound.putBoolean("IsPermanent", this.entityData.get(IS_PROFILE_PERMANENT));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        String id = compound.contains("GirlProfileID") ? compound.getString("GirlProfileID") : "default";
        setProfile(CustomGirlLoader.getGirlOrDefault(id), compound.getBoolean("IsPermanent"));
    }

    @Override
    public void onTempCloneCreation(GirlEntity clone) {
        super.onTempCloneCreation(clone);
        if (clone instanceof CustomGirlEntity girl) {
            girl.setProfile(this.getProfile(), false);
        }
    }
}
