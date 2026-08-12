package com.sandymandy.pleasurehorizons.entity.base;

import com.sandymandy.pleasurehorizons.util.inventory.GirlInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
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
import java.util.UUID;

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
    private static final EntityDataAccessor<Vector3f> BREAST_OFFSET =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Boolean> IS_DOWNED =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);

    // --- AI Task toggles (new advanced AI system) ---
    private static final EntityDataAccessor<Boolean> AI_GUARD_BASE =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> AI_GUARD_OWNER =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> AI_GATHER =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> AI_HARVEST =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> AI_STAY_NEAR_BASE =
            SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);

    public static final Random RANDOM = new Random();

    public Map<String, Boolean> boneVisibility = new HashMap<>();
    public Map<String, Integer> boneColorOverrides = new HashMap<>();
    /**
     * Per-bone texture overrides, drawn by {@code BoneOverrideRenderLayer}.
     *
     * <p>Client-side only and never saved: they are rebuilt from the scene state each time.
     * Three layers exist so a bone can be stacked (base skin, then overlays).</p>
     */
    public Map<String, net.minecraft.resources.ResourceLocation> boneTextureOverrides = new HashMap<>();
    public Map<String, net.minecraft.resources.ResourceLocation> boneTextureOverridesLayer2 = new HashMap<>();
    public Map<String, net.minecraft.resources.ResourceLocation> boneTextureOverridesLayer3 = new HashMap<>();
    /** Per-bone UV shift, used to pick the armour material column. Client-side only. */
    public Map<String, org.joml.Vector2f> boneUVOffsets = new HashMap<>();
    public Map<String, Vec3> boneSizeOverrides = new HashMap<>();
    public Map<String, Vec3> bonePositionOffset = new HashMap<>();
    public final Map<EquipmentSlot, Boolean> armorVisibility = new EnumMap<>(EquipmentSlot.class);

    public float passengerYOffset = -1f;
    public boolean currentLoopState = false;
    public boolean currentHoldState = false;
    public String currentAnimState = "idle";

    public final GirlInventory inventory = GirlInventory.ofSize();

    private boolean guiOpenState = false;
    @Nullable
    private Player lookAtTarget = null;

    // Preview authorization is intentionally server-only: clients receive entity ids for rendering,
    // but may not choose which entity a customization/removal packet is allowed to affect.
    @Nullable
    private UUID previewRequesterId;
    @Nullable
    private UUID activePreviewId;
    private int activePreviewEntityId = -1;
    @Nullable
    private UUID previewSourceId;

    protected GirlEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createDefaultAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.FOLLOW_RANGE, 100.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(WAITING_AT_BED, false);
        builder.define(IS_TEMPORARY, false);
        builder.define(CREATED_CLONE, false);
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
        builder.define(BASE_POS, BlockPos.ZERO);
        builder.define(OVERRIDE_ANIM, "");
        builder.define(SCENE_ANIM, "");
        builder.define(CONSUMING_STACK, Items.COOKED_BEEF.getDefaultInstance());
        builder.define(IS_DOWNED, false);
        builder.define(AI_GUARD_BASE, false);
        builder.define(AI_GUARD_OWNER, false);
        builder.define(AI_GATHER, true); // gather by default is useful and non-intrusive
        builder.define(AI_HARVEST, false);
        builder.define(AI_STAY_NEAR_BASE, false);
    }

    // ---------------------------------------------------------------- state

    public void setFollowing(boolean follow) {
        this.entityData.set(FOLLOWING, follow);
    }

    public boolean isFollowing() {
        return this.entityData.get(FOLLOWING);
    }

    // --- AI toggles ---
    public void setGuardBaseEnabled(boolean enabled) { this.entityData.set(AI_GUARD_BASE, enabled); }
    public boolean isGuardBaseEnabled() { return this.entityData.get(AI_GUARD_BASE); }

    public void setGuardOwnerEnabled(boolean enabled) { this.entityData.set(AI_GUARD_OWNER, enabled); }
    public boolean isGuardOwnerEnabled() { return this.entityData.get(AI_GUARD_OWNER); }

    public void setGatherEnabled(boolean enabled) { this.entityData.set(AI_GATHER, enabled); }
    public boolean isGatherEnabled() { return this.entityData.get(AI_GATHER); }

    public void setHarvestEnabled(boolean enabled) { this.entityData.set(AI_HARVEST, enabled); }
    public boolean isHarvestEnabled() { return this.entityData.get(AI_HARVEST); }

    public void setStayNearBaseEnabled(boolean enabled) { this.entityData.set(AI_STAY_NEAR_BASE, enabled); }
    public boolean isStayNearBaseEnabled() { return this.entityData.get(AI_STAY_NEAR_BASE); }

    public void setStripped(boolean stripped) {
        this.entityData.set(STRIPPED, stripped);
    }

    public boolean isStripped() {
        return this.entityData.get(STRIPPED);
    }

    public void setFreeze(boolean locked) {
        this.entityData.set(FROZEN_STATE, locked);
    }

    public boolean isDowned() {
        return this.entityData.get(IS_DOWNED);
    }

    public void setDowned(boolean downed) {
        this.entityData.set(IS_DOWNED, downed);
    }

    public boolean isFrozenInPlace() {
        return this.entityData.get(FROZEN_STATE);
    }

    /**
     * Movement lock is derived from its already-synchronised source states. Keeping a second
     * tracked flag here previously left it permanently false because nothing updated it; deriving
     * the value also makes transitions visible immediately on both logical sides without any
     * client-side tracked-data writes.
     */
    public boolean isMovementLocked() {
        return this.isFrozenInPlace()
                || this.isWaitingAtBed()
                || this.isSceneActive()
                || this.isWaitingForPlayer();
    }

    @Override
    public boolean isPushable() {
        return !this.isMovementLocked() && super.isPushable();
    }

    @Override
    public void push(Entity entity) {
        if (!this.isMovementLocked()) {
            super.push(entity);
        }
    }

    @Override
    public void push(double x, double y, double z) {
        if (!this.isMovementLocked()) {
            super.push(x, y, z);
        }
    }

    @Override
    public void knockback(double strength, double x, double z) {
        if (this.isMovementLocked()) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }
        super.knockback(strength, x, z);
    }

    public void setGUIOpenState(boolean state, @Nullable Player lookAt) {
        this.guiOpenState = state;
        this.lookAtTarget = lookAt;

        if (!state && !this.level().isClientSide() && this.activePreviewId != null
                && this.level() instanceof ServerLevel serverLevel) {
            if (serverLevel.getEntity(this.activePreviewId) instanceof GirlEntity preview) {
                preview.discard();
            }
            clearPreviewSession();
        }
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

    @Override
    public boolean shouldBeSaved() {
        return !isTemporary() && super.shouldBeSaved();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && isTemporary() && this.level() instanceof ServerLevel serverLevel) {
            GirlEntity source = null;
            if (this.previewSourceId != null
                    && serverLevel.getEntity(this.previewSourceId) instanceof GirlEntity sourceGirl) {
                source = sourceGirl;
            }
            boolean validSession = this.previewRequesterId != null
                    && serverLevel.getPlayerByUUID(this.previewRequesterId) != null
                    && source != null
                    && source.referencesPreview(this);
            if (!validSession) {
                if (source != null && this.getUUID().equals(source.activePreviewId)) {
                    source.clearPreviewSession();
                }
                this.discard();
            }
        }
    }

    /**
     * Shows/hides the armour bones baked into the rig and picks the right armour material.
     *
     * <p>Girls do not render vanilla armour models; every rig carries its own armour geometry
     * whose texture sheet holds one column per material. Choosing a material is therefore a
     * horizontal UV shift, and dyed leather is additionally tinted.</p>
     *
     * <p>This was an empty method in the port, so equipping armour on a girl did nothing at
     * all. Client-side only: the maps it writes are read by {@code GirlRenderer} every frame,
     * and {@code armorVisibility} arrives from the server via
     * {@code ClothingArmorVisibilityS2CPacket}.</p>
     */
    public void applyClothingAndArmor() {
        if (!this.level().isClientSide()) return;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!isGirlArmorSlot(slot)) continue;

            List<String> armorBones = getArmorBones().get(slot);
            if (armorBones != null) {
                boolean visible = this.armorVisibility.getOrDefault(slot, false);
                setBoneVisibility(armorBones, visible);

                // Leg armour covers the crotch, so the bare bone underneath must go.
                if (slot == EquipmentSlot.LEGS) {
                    setBoneVisibility("vagina", !visible);
                }
            }
            displayArmor(slot);
        }
    }

    /** Maps vanilla equipment slots onto the girl's own persistent inventory layout. */
    private static int girlInventorySlot(EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> GirlInventory.MAIN_HAND_SLOT;
            case OFFHAND -> GirlInventory.OFF_HAND_SLOT;
            case FEET -> GirlInventory.ARMOR_FEET_SLOT;
            case LEGS -> GirlInventory.ARMOR_LEGS_SLOT;
            case CHEST -> GirlInventory.ARMOR_CHEST_SLOT;
            case HEAD -> GirlInventory.ARMOR_HEAD_SLOT;
            default -> -1;
        };
    }

    /**
     * Makes the inventory-screen equipment real vanilla equipment as well.
     *
     * <p>Mob combat, GeckoLib's held-item layer and Minecraft's equipment synchronization all
     * read through this method. Without this bridge, items in the girl's equipment slots only
     * existed inside her menu and were invisible to all three systems.</p>
     */
    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        int index = girlInventorySlot(slot);
        return index < 0 ? super.getItemBySlot(slot) : this.inventory.getItem(index);
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        int index = girlInventorySlot(slot);
        if (index < 0) {
            super.setItemSlot(slot, stack);
        } else {
            this.verifyEquippedItem(stack);
            ItemStack previous = this.inventory.getItem(index);
            this.inventory.setItem(index, stack);
            this.onEquipItem(slot, previous, stack);
        }
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return List.of(
                this.inventory.getItem(GirlInventory.MAIN_HAND_SLOT),
                this.inventory.getItem(GirlInventory.OFF_HAND_SLOT));
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return List.of(
                getArmorStack(EquipmentSlot.FEET),
                getArmorStack(EquipmentSlot.LEGS),
                getArmorStack(EquipmentSlot.CHEST),
                getArmorStack(EquipmentSlot.HEAD));
    }

    @Override
    public Iterable<ItemStack> getArmorAndBodyArmorSlots() {
        return List.of(
                getArmorStack(EquipmentSlot.FEET),
                getArmorStack(EquipmentSlot.LEGS),
                getArmorStack(EquipmentSlot.CHEST),
                getArmorStack(EquipmentSlot.HEAD),
                super.getItemBySlot(EquipmentSlot.BODY));
    }

    public ItemStack getArmorStack(EquipmentSlot slot) {
        int index = girlInventorySlot(slot);
        return index < GirlInventory.ARMOR_START || index > GirlInventory.ARMOR_END
                ? ItemStack.EMPTY
                : this.inventory.getItem(index);
    }

    /**
     * Recomputes which armour pieces are worn and tells every tracking client.
     *
     * <p>Server side of {@code applyClothingAndArmor}. The visibility flags cannot be derived
     * on the client because the girl's container is not synched, so they are broadcast with
     * {@code ClothingArmorVisibilityS2CPacket}. Nothing sent this packet before, which is why
     * the armour bones never appeared.</p>
     */
    public void updateClothingAndArmor() {
        if (this.level().isClientSide()) return;

        boolean stripped = this.isStripped();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!isGirlArmorSlot(slot)) continue;
            boolean hasArmor = !this.getArmorStack(slot).isEmpty();
            this.armorVisibility.put(slot, hasArmor && !stripped);
        }

        java.util.List<Boolean> armorList = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            armorList.add(this.armorVisibility.getOrDefault(slot, false));
        }

        var packet = new com.sandymandy.pleasurehorizons.networking.S2C
                .ClothingArmorVisibilityS2CPacket(this.getId(), armorList);
        for (net.minecraft.server.level.ServerPlayer player :
                ((net.minecraft.server.level.ServerLevel) this.level()).players()) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet);
        }
    }

    /**
     * Broadcasts armour visibility only when it has actually changed.
     *
     * <p>Polled from {@code tick}, because neither the girl's container nor the stripped flag
     * fires an event when it changes. Comparing first keeps this from sending a packet per
     * girl per second forever.</p>
     */
    public void updateClothingAndArmorIfChanged() {
        if (this.level().isClientSide()) return;

        boolean stripped = this.isStripped();
        boolean changed = false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!isGirlArmorSlot(slot)) continue;
            boolean expected = !this.getArmorStack(slot).isEmpty() && !stripped;
            if (this.armorVisibility.getOrDefault(slot, false) != expected) {
                changed = true;
                break;
            }
        }
        if (changed) {
            updateClothingAndArmor();
        }
    }

    /** Which slots the rigs actually have armour geometry for. */
    protected boolean isGirlArmorSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD
                || slot == EquipmentSlot.CHEST
                || slot == EquipmentSlot.LEGS
                || slot == EquipmentSlot.FEET;
    }

    /**
     * Picks the armour material column for one slot.
     *
     * <p>The armour atlas stores the materials side by side, 9 pixels apart on a 512-wide
     * sheet - hence the 0.017578125 step. Column 0 is the default, so an unrecognised material
     * simply renders as that.</p>
     */
    private void displayArmor(EquipmentSlot slot) {
        List<String> bones = getArmorBones().get(slot);
        if (bones == null) return;

        // Girls keep their gear in their own container, not in the vanilla equipment slots,
        // so getItemBySlot would always come back empty here.
        ItemStack item = this.getArmorStack(slot);
        if (item.isEmpty()) {
            // Nothing equipped: drop any offset/tint left over from the previous item.
            clearBoneUV(bones);
            clearBoneColor(bones);
            return;
        }

        final float step = 0.017578125F;
        float u = 0.0F;

        // Matched on the item id, as upstream does, so modded armour falls back to column 0.
        String armorType = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getKey(item.getItem()).getPath().toLowerCase(java.util.Locale.ROOT);

        if (armorType.contains("diamond")) u = step;
        if (armorType.contains("gold")) u = step * 2;
        if (armorType.contains("iron")) u = step * 3;
        if (armorType.contains("copper")) u = step * 4;
        if (armorType.contains("chain")) u = step * 5;
        if (armorType.contains("leather")) {
            u = step * 6;
            overrideBoneColor(bones, getDyedArmorColor(item));
        } else {
            clearBoneColor(bones);
        }
        if (armorType.contains("turtle")) u = step * 7;

        overrideBoneUV(bones, u, 0.0F);
    }

    /** Dyed leather colour, falling back to vanilla's undyed leather brown. */
    private int getDyedArmorColor(ItemStack stack) {
        if (stack.isEmpty()) return 0xFFFFFF;

        net.minecraft.world.item.component.DyedItemColor dyed =
                stack.get(net.minecraft.core.component.DataComponents.DYED_COLOR);
        if (dyed != null) {
            return dyed.rgb();
        }
        return 0xA06540;
    }

    // ------------------------------------------------------- bone overrides
    // These mirror the Fabric original's helpers. They only make sense on the client,
    // where GirlRenderer reads the maps every frame while walking the baked model.

    public void setBoneVisibility(List<String> bones, boolean visible) {
        if (!this.level().isClientSide()) return;
        for (String bone : bones) {
            this.boneVisibility.put(bone, visible);
        }
    }

    public void setBoneVisibility(String bone, boolean visible) {
        this.setBoneVisibility(List.of(bone), visible);
    }

    public void overrideBoneColor(List<String> bones, int argb) {
        if (!this.level().isClientSide()) return;
        int withAlpha = (argb & 0xFF000000) == 0 ? (argb | 0xFF000000) : argb;
        for (String bone : bones) {
            this.boneColorOverrides.put(bone, withAlpha);
        }
    }

    public void overrideBoneColor(String bone, int argb) {
        this.overrideBoneColor(List.of(bone), argb);
    }

    /** Shifts a bone's texture coordinates, used to select the armour material column. */
    public void overrideBoneUV(List<String> bones, float uOffset, float vOffset) {
        if (!this.level().isClientSide()) return;
        for (String bone : bones) {
            this.boneUVOffsets.put(bone, new org.joml.Vector2f(uOffset, vOffset));
        }
    }

    public void clearBoneUV(List<String> bones) {
        if (!this.level().isClientSide()) return;
        for (String bone : bones) {
            this.boneUVOffsets.remove(bone);
        }
    }

    public void clearBoneColor(List<String> bones) {
        if (!this.level().isClientSide()) return;
        for (String bone : bones) {
            this.boneColorOverrides.remove(bone);
        }
    }

    public void setBonePos(String bone, float x, float y, float z) {
        this.setBonePos(bone, new Vec3(x, y, z));
    }

    public void setBonePos(String bone, Vec3 pos) {
        if (!this.level().isClientSide()) return;
        this.bonePositionOffset.put(bone, pos);
    }

    /** Sizes are authored as percentages upstream (100 = unchanged). */
    public void setBoneSize(String bone, float x, float y, float z, float min, float max) {
        if (!this.level().isClientSide()) return;
        if (min != 0 && max != 0) {
            x = Mth.clamp(x, min, max);
            y = Mth.clamp(y, min, max);
            z = Mth.clamp(z, min, max);
        }
        this.boneSizeOverrides.put(bone, new Vec3(x, y, z));
    }

    public void setBoneSize(String bone, int size, int min, int max) {
        float finalSize = size / 100f;
        if (min == 0 && max == 0) {
            setBoneSize(bone, finalSize, finalSize, finalSize, 0, 0);
            return;
        }
        setBoneSize(bone, finalSize, finalSize, finalSize, min / 100f, max / 100f);
    }

    public void setBoneSize(String bone, int size) {
        setBoneSize(bone, size, 0, 0);
    }

    public boolean isArmorVisible(EquipmentSlot slot) {
        return this.armorVisibility.getOrDefault(slot, true);
    }

    public void setArmorVisible(EquipmentSlot slot, boolean visible) {
        this.armorVisibility.put(slot, visible);
    }

    @Nullable
    public GirlEntity createTempClone(Player requester) {
        if (this.level().isClientSide() || requester == null || createdClone()) return null;

        GirlEntity clone = (GirlEntity) this.getType().create(this.level());
        if (clone == null) return null;

        clone.setTemporaryState(true);
        clone.setPos(this.getX(), 800, this.getZ());
        clone.setInvisible(true);
        clone.setInvulnerable(true);
        clone.setNoGravity(true);
        clone.previewRequesterId = requester.getUUID();
        clone.previewSourceId = this.getUUID();

        this.onTempCloneCreation(clone);

        if (!this.level().addFreshEntity(clone)) return null;

        this.previewRequesterId = requester.getUUID();
        this.activePreviewId = clone.getUUID();
        this.activePreviewEntityId = clone.getId();
        this.setCreatedCloneState(true);
        return clone;
    }

    public void onTempCloneCreation(GirlEntity clone) {
        clone.setStripped(this.isStripped());
    }

    public boolean createdClone() {
        return this.entityData.get(CREATED_CLONE);
    }

    public void setCreatedCloneState(boolean state) {
        this.entityData.set(CREATED_CLONE, state);
        if (!state && !this.level().isClientSide()) {
            clearPreviewSession();
        }
    }

    public boolean hasPreviewSession(Player requester) {
        return !this.level().isClientSide()
                && createdClone()
                && this.previewRequesterId != null
                && this.previewRequesterId.equals(requester.getUUID())
                && this.lookAtTarget != null
                && this.lookAtTarget.getUUID().equals(requester.getUUID());
    }

    public boolean referencesPreview(GirlEntity preview) {
        return preview != null
                && preview.isTemporary()
                && this.activePreviewId != null
                && this.activePreviewId.equals(preview.getUUID())
                && this.previewRequesterId != null
                && this.previewRequesterId.equals(preview.previewRequesterId)
                && this.getUUID().equals(preview.previewSourceId);
    }

    public boolean referencesPreviewEntityId(int entityId) {
        return createdClone() && this.activePreviewEntityId == entityId;
    }

    public void clearPreviewSession() {
        this.previewRequesterId = null;
        this.activePreviewId = null;
        this.activePreviewEntityId = -1;
        if (!this.level().isClientSide()) {
            this.entityData.set(CREATED_CLONE, false);
        }
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

    public void setBasePos(BlockPos pos) {
        this.entityData.set(BASE_POS, pos);
    }

    public BlockPos getBasePos() {
        return this.entityData.get(BASE_POS);
    }

    /** Marks her current position as her home base. */
    public void setBasePosHere() {
        setBasePos(this.blockPosition());
    }

    /**
     * Sends her back to her home base and sits her down there.
     *
     * <p>Upstream teleports unconditionally, but BASE_POS defaults to
     * {@link BlockPos#ZERO}; without this guard a girl whose base was never set
     * would be flung to world origin.</p>
     */
    public void teleportToBase() {
        BlockPos base = this.getBasePos();
        if (BlockPos.ZERO.equals(base)) {
            return;
        }
        setSitting(true);
        this.teleportTo(base.getX() + 0.5D, base.getY(), base.getZ() + 0.5D);
    }

    public void setBreastSize(int value) {
        this.entityData.set(BREAST_SIZE, Mth.clamp(value, getBreastMinSize(), getBreastMaxSize()));
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
        if (value == null || !Double.isFinite(value.x) || !Double.isFinite(value.y)
                || !Double.isFinite(value.z)) {
            return;
        }
        this.entityData.set(BREAST_OFFSET, new Vector3f(
                (float) Mth.clamp(value.x, -16.0D, 16.0D),
                (float) Mth.clamp(value.y, -16.0D, 16.0D),
                (float) Mth.clamp(value.z, -16.0D, 16.0D)));
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

    /**
     * Scenes this girl offers in the "Talk" menu.
     *
     * <p>Every girl overrides this with her own list; the base returns an empty list so a rig
     * without animations simply shows no options instead of crashing.</p>
     */
    public List<com.sandymandy.pleasurehorizons.util.variables.Scene> getScenes() {
        return List.of();
    }

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

    public boolean hasHugAnimation() {
        return false;
    }

    public boolean hasCarryAnimation() {
        return false;
    }

    public String getCarryAnimation() {
        if (hasCarryAnimation()) return "carry_slow1";
        if (hasHugAnimation()) return "hugidle";
        return "sit";
    }

    /**
     * Returns the highest relationship level required by this girl's scenes.
     *
     * <p>The old port only returned {@link #MAX_RELATIONSHIP_LEVEL}, which is initialised to
     * four and was never updated. Girls with later scenes therefore displayed {@code 4/4}
     * even though content could require level ten. Keep this method as a pure calculation:
     * writing synched entity data while an inventory screen calls it on the client is unsafe.
     * The tracked value is only a fallback for profile-driven girls whose JSON scenes exist
     * on the server but are not loaded on the client.</p>
     */
    public int maxRelationshipLevel() {
        try {
            List<com.sandymandy.pleasurehorizons.util.variables.Scene> scenes = getScenes();
            if (scenes == null || scenes.isEmpty()) {
                return Math.max(4, this.entityData.get(MAX_RELATIONSHIP_LEVEL));
            }

            return scenes.stream()
                    .map(com.sandymandy.pleasurehorizons.util.variables.Scene::requiredRelationshipLevel)
                    .max(Integer::compareTo)
                    .orElse(4);
        } catch (RuntimeException exception) {
            // A missing/malformed custom profile must not make relationship progress unusable.
            return Math.max(4, this.entityData.get(MAX_RELATIONSHIP_LEVEL));
        }
    }

    /** Server-only update for profile-driven girls; clients merely read the tracked fallback. */
    protected void setMaxRelationshipLevel(int value) {
        if (!this.level().isClientSide()) {
            this.entityData.set(MAX_RELATIONSHIP_LEVEL, value);
        }
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
        compound.putBoolean("Downed", isDowned());
        compound.putBoolean("Following", isFollowing());
        compound.putBoolean("Pregnant", isPregnant());
        compound.putBoolean("CanGetImpregnated", canGetImpregnated());
        compound.putBoolean("Sitting", isSitting());
        compound.putBoolean("Temporary", isTemporary());
        compound.putInt("PregnancyStage", getPregnancyStage());
        compound.putInt("RelationshipLevel", getCurrentRelationshipLevel());
        compound.putInt("BreastSize", getBreastSize());
        Vec3 breastOffset = getBreastOffset();
        compound.putDouble("BreastOffsetX", breastOffset.x);
        compound.putDouble("BreastOffsetY", breastOffset.y);
        compound.putDouble("BreastOffsetZ", breastOffset.z);
        compound.putInt("MilkedAmount", getMilkedAmount());
        compound.putLong("BasePos", getBasePos().asLong());
        // AI toggles
        compound.putBoolean("AIGuardBase", isGuardBaseEnabled());
        compound.putBoolean("AIGuardOwner", isGuardOwnerEnabled());
        compound.putBoolean("AIGather", isGatherEnabled());
        compound.putBoolean("AIHarvest", isHarvestEnabled());
        compound.putBoolean("AIStayNearBase", isStayNearBaseEnabled());

        CompoundTag inventoryTag = new CompoundTag();
        ContainerHelper.saveAllItems(inventoryTag, this.inventory.getItems(), this.registryAccess());
        compound.put("Inventory", inventoryTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setStripped(compound.getBoolean("Stripped"));
        setDowned(compound.getBoolean("Downed"));
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
        if (compound.contains("BreastOffsetX")
                && compound.contains("BreastOffsetY")
                && compound.contains("BreastOffsetZ")) {
            setBreastOffset(new Vec3(
                    compound.getDouble("BreastOffsetX"),
                    compound.getDouble("BreastOffsetY"),
                    compound.getDouble("BreastOffsetZ")));
        }
        setMilkedAmount(compound.getInt("MilkedAmount"));
        // Older port saves omitted this field. Keep ZERO as the explicit "base not set"
        // sentinel instead of inventing a home at the entity's load position.
        if (compound.contains("BasePos")) {
            setBasePos(BlockPos.of(compound.getLong("BasePos")));
        }
        if (compound.contains("AIGuardBase")) setGuardBaseEnabled(compound.getBoolean("AIGuardBase"));
        if (compound.contains("AIGuardOwner")) setGuardOwnerEnabled(compound.getBoolean("AIGuardOwner"));
        if (compound.contains("AIGather")) setGatherEnabled(compound.getBoolean("AIGather"));
        if (compound.contains("AIHarvest")) setHarvestEnabled(compound.getBoolean("AIHarvest"));
        if (compound.contains("AIStayNearBase")) setStayNearBaseEnabled(compound.getBoolean("AIStayNearBase"));

        if (compound.contains("Inventory")) {
            ContainerHelper.loadAllItems(
                    compound.getCompound("Inventory"), this.inventory.getItems(), this.registryAccess());
        }
    }
}
