package com.sandymandy.pleasurehorizons.entity.base;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.PleasureHorizonsEntityStatuses;
import com.sandymandy.pleasurehorizons.registries.PleasureHorizonsTrackedDataRegistry;
import com.sandymandy.pleasurehorizons.util.PleasureHorizonsLangUtils;
import com.sandymandy.pleasurehorizons.util.inventory.GirlInventory;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UseRemainderComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class GirlEntity extends PathAwareEntity implements RangedAttackMob {
    private static final TrackedData<Boolean> WAITING_AT_BED = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> IS_TEMPORARY = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> CREATED_CLONE = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> LOCKED_STATE = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> WAITING_FOR_PLAYER = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> FROZEN_STATE = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> STRIPPED = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> FOLLOWING = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> IN_SCENE = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> OVERRIDE_LOOP = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> OVERRIDE_HOLD = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> OVERRIDE_ANIM_PLAYING = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> PLAYER_MODEL_SLIM = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> HAVING_SEX = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> SITTING = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> PREGNANT = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> CAN_GET_IMPREGNATED = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<String> OVERRIDE_ANIM = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<String> SCENE_ANIM = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Integer> BREAST_SIZE = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> RELATIONSHIP_LEVEL = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> MAX_RELATIONSHIP_LEVEL = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> PREGNANCY_STAGE = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<BlockPos> BASE_POS = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
    private static final TrackedData<Vec3d> PASSENGER_BONE_POSITION = DataTracker.registerData(GirlEntity.class, PleasureHorizonsTrackedDataRegistry.VEC3D);
    private static final TrackedData<Vec3d> BREAST_OFFSET = DataTracker.registerData(GirlEntity.class, PleasureHorizonsTrackedDataRegistry.VEC3D);
    private static final TrackedData<ItemStack> CONSUMING_STACK = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<Integer> MILKED_AMOUNT = DataTracker.registerData(GirlEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final Random RANDOM = new Random();
    public Map<String, Boolean> boneVisibility = new HashMap<>();
    public Map<String, Integer> boneColorOverrides = new HashMap<>();
    public Map<String, Identifier> boneTextureOverrides = new HashMap<>();
    public Map<String, Identifier> boneTextureOverridesLayer2 = new HashMap<>();
    public Map<String, Identifier> boneTextureOverridesLayer3 = new HashMap<>();
    public Map<String, Vec3d> boneSizeOverrides = new HashMap<>();
    public Map<String, Vec3d> bonePositionOffset = new HashMap<>();
    public Map<String, Vec2f> boneUVOffsets = new HashMap<>();
    public final Map<EquipmentSlot, Boolean> armorVisibility = new EnumMap<>(EquipmentSlot.class);
    public Vec3d previousVelocity = Vec3d.ZERO;
    private static final int SPRINTING_FLAG_INDEX = 3;
    private static final int MAX_TICKS_NO_HIT = 20 * 20;
    private static final Identifier SPRINTING_SPEED_MODIFIER_ID = Identifier.of(PleasureHorizons.MOD_ID, "sprinting_speed_modifier");
    private static final EntityAttributeModifier SPRINTING_SPEED_BOOST = new EntityAttributeModifier(
            SPRINTING_SPEED_MODIFIER_ID, 1.65 - 1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );
    private int ticksSinceLastHit;
    public float previousYaw = 0;
    public float passengerYOffset = -1f;
    public boolean currentLoopState = false;
    public boolean currentHoldState = false;
    private boolean guiOpenSate = false;
    public String currentAnimState = "idle";
    public final GirlInventory inventory = GirlInventory.ofSize();
    private LivingEntity attackTarget;
    private PlayerEntity lookAtTarget;
    protected GirlEntity(EntityType<? extends GirlEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(WAITING_AT_BED, false);
        builder.add(IS_TEMPORARY, false);
        builder.add(CREATED_CLONE, false);
        builder.add(LOCKED_STATE, false);
        builder.add(FROZEN_STATE, false);
        builder.add(WAITING_FOR_PLAYER, false);
        builder.add(STRIPPED, false);
        builder.add(FOLLOWING, false);
        builder.add(IN_SCENE, false);
        builder.add(OVERRIDE_LOOP, false);
        builder.add(OVERRIDE_HOLD, false);
        builder.add(OVERRIDE_ANIM_PLAYING, false);
        builder.add(PLAYER_MODEL_SLIM, false);
        builder.add(HAVING_SEX, false);
        builder.add(SITTING, false);
        builder.add(PREGNANT,false);
        builder.add(CAN_GET_IMPREGNATED,false);
        builder.add(PREGNANCY_STAGE, 0);
        builder.add(RELATIONSHIP_LEVEL,0);
        builder.add(MAX_RELATIONSHIP_LEVEL,4);
        builder.add(BREAST_SIZE,100);
        builder.add(BREAST_OFFSET, Vec3d.ZERO);
        builder.add(PASSENGER_BONE_POSITION, Vec3d.ZERO);
        builder.add(BASE_POS, this.getBlockPos());
        builder.add(OVERRIDE_ANIM,"");
        builder.add(SCENE_ANIM,"");
        builder.add(CONSUMING_STACK, Items.COOKED_BEEF.getDefaultStack());
        builder.add(MILKED_AMOUNT, 0);
    }

    public void setFollowing(boolean follow) {
        this.dataTracker.set(FOLLOWING, follow);
    }

    public boolean isFollowing() {
        return this.dataTracker.get(FOLLOWING);
    }

    public void setStripped(boolean stripped) {
        this.dataTracker.set(STRIPPED, stripped);
    }

    public boolean isStripped() {
        return this.dataTracker.get(STRIPPED);
    }

    public void setFreeze(boolean locked) {
        this.dataTracker.set(FROZEN_STATE,locked);
    }

    public boolean isFrozenInPlace() {
        return this.dataTracker.get(FROZEN_STATE);
    }

    public void setMovementLockedState(boolean locked) {
        this.dataTracker.set(LOCKED_STATE, locked);
    }

    public boolean isMovementLocked() {
        return this.dataTracker.get(LOCKED_STATE);
    }

    public void setGUIOpenState(boolean state, @Nullable PlayerEntity lookAt) {
        guiOpenSate = state;
        lookAtTarget = lookAt;
    }

    public void setGUIOpenState(boolean state) {
        this.setGUIOpenState(state, null);
    }

    public boolean isGUIOpen() {
        return guiOpenSate;
    }

    public void setSceneState(boolean inScene) {
        this.dataTracker.set(IN_SCENE, inScene);
    }

    public boolean isSceneActive() {
        return this.dataTracker.get(IN_SCENE);
    }

    public void setOverrideAnim(String anim){
        this.dataTracker.set(OVERRIDE_ANIM, anim);
    }

    public String getOverrideAnim(){
        return this.dataTracker.get(OVERRIDE_ANIM);
    }

    public void setOverrideLoop(boolean loop){
        this.dataTracker.set(OVERRIDE_LOOP, loop);
    }

    public boolean getOverrideLoopState(){
        return this.dataTracker.get(OVERRIDE_LOOP);
    }

    public void setOverrideHold(boolean hold){
        this.dataTracker.set(OVERRIDE_HOLD, hold);
    }

    public boolean getOverrideHoldState(){
        return this.dataTracker.get(OVERRIDE_HOLD);
    }

    public boolean isWaitingAtBed(){
        return this.dataTracker.get(WAITING_AT_BED);
    }

    public void setWaitingAtBedState(boolean state){
        this.dataTracker.set(WAITING_AT_BED, state);
    }

    public boolean isTemporary(){
        return this.dataTracker.get(IS_TEMPORARY);
    }

    public void setTemporaryState(boolean state){
        this.dataTracker.set(IS_TEMPORARY, state);
    }

    public boolean createdClone(){
        return this.dataTracker.get(CREATED_CLONE);
    }

    public void setCreatedCloneState(boolean state){
        this.dataTracker.set(CREATED_CLONE, state);
    }

    public boolean isWaitingForPlayer(){
        return this.dataTracker.get(WAITING_FOR_PLAYER);
    }

    public void setWaitingForPlayerState(boolean state){
        this.dataTracker.set(WAITING_FOR_PLAYER, state);
    }

    public void setIsPlayerModelSlim(boolean isSlim){
        this.dataTracker.set(PLAYER_MODEL_SLIM, isSlim);
    }

    public boolean isPlayerModelSlim(){
        return this.dataTracker.get(PLAYER_MODEL_SLIM);
    }

    public void setHavingSex(boolean state) {
        this.dataTracker.set(HAVING_SEX, state);
    }

    public boolean isHavingSex() {
        return this.dataTracker.get(HAVING_SEX);
    }

    public void setPregnantState(boolean preggo){
        this.dataTracker.set(PREGNANT, preggo);
    }

    public boolean isPregnant(){
        return this.dataTracker.get(PREGNANT);
    }

    public void canGetImpregnatedState(boolean preggo){
        this.dataTracker.set(CAN_GET_IMPREGNATED, preggo);
    }

    public boolean canGetImpregnated(){
        return this.dataTracker.get(CAN_GET_IMPREGNATED);
    }

    public void setPregnancyStage(int num){
        num = MathHelper.clamp(num, 0, maxPregnancyStage());
        this.dataTracker.set(PREGNANCY_STAGE, num);
    }

    public int getPregnancyStage(){
        return this.dataTracker.get(PREGNANCY_STAGE);
    }

    public int getCurrentRelationshipLevel() { return this.dataTracker.get(RELATIONSHIP_LEVEL);}

    public void setCurrentRelationshipLevel(int value) { this.dataTracker.set(RELATIONSHIP_LEVEL, value);}

    public void setPassengerBonePosition(Vec3d position){
        this.dataTracker.set(PASSENGER_BONE_POSITION, position);
    }

    public Vec3d getPassengerBonePosition(){
        return this.dataTracker.get(PASSENGER_BONE_POSITION);
    }

    public void setBasePos(BlockPos block){this.dataTracker.set(BASE_POS, block);}

    public BlockPos getBasePos(){return this.dataTracker.get(BASE_POS);}

    public void setBreastSize(int value) { this.dataTracker.set(BREAST_SIZE, value); }

    public int getBreastSize() { return this.dataTracker.get(BREAST_SIZE); }

    public void setMilkedAmount(int value) { this.dataTracker.set(MILKED_AMOUNT, value); }

    public int getMilkedAmount() { return this.dataTracker.get(MILKED_AMOUNT); }

    public void setBreastOffset(Vec3d value) { this.dataTracker.set(BREAST_OFFSET, value); }

    public Vec3d getBreastOffset() { return this.dataTracker.get(BREAST_OFFSET); }

    public GirlInventory getInventory() {
        return inventory;
    }

    public Item isAttractedTo() {
        return Items.DANDELION;
    }

    public boolean useUpRelationShipLevels(){
        return false;
    }

    public String getGirlID() {
        return "null";
    }

    public String getGirlDisplayName() {
        return PleasureHorizonsLangUtils.getStringFromKey("entity.pleasurehorizons." + getGirlID());
    }

    public int getBreastMinSize() { return 25; }

    public int getBreastMaxSize() { return 150; }

    public int getSizeGUI(){return 20;}

    public float getYAxisGUI(){return 0.0625F;}

    public List<Scene> getScenes() {
        return new ArrayList<>();
    }

    public float getWeaponBoneXRotation() {return 150.0F;}

    public int getMaxBellySizeWhenPregnant() { return 450;}

    public int maxPregnancyStage(){return 3;}

    public boolean hasStripAnim() {
        return true;
    }

    public int maxRelationshipLevel() {
        try {
            // Get all scene options
            List<Scene> options = getScenes();

            // If null or empty, return default
            if (options == null || options.isEmpty()) {
                this.dataTracker.set(MAX_RELATIONSHIP_LEVEL, 4);
                return this.dataTracker.get(MAX_RELATIONSHIP_LEVEL);
            }

            int value = options.stream()
                    .map(Scene::requiredRelationshipLevel)
                    .max(Integer::compareTo)
                    .orElse(4);

            this.dataTracker.set(MAX_RELATIONSHIP_LEVEL, value);

            return this.dataTracker.get(MAX_RELATIONSHIP_LEVEL);
        } catch (Exception e) {
            // In case something unexpected happens
            this.dataTracker.set(MAX_RELATIONSHIP_LEVEL, 4);
            return this.dataTracker.get(MAX_RELATIONSHIP_LEVEL);        }
    }

    protected Map<EquipmentSlot, List<String>> getArmorBones() {
        Map<EquipmentSlot, List<String>> armor = new HashMap<>();

        armor.put(EquipmentSlot.HEAD, new ArrayList<>(List.of(
                "armorHelmet"
        )));

        armor.put(EquipmentSlot.CHEST, new ArrayList<>(List.of(
                "armorBoobs",
                "armorChest",
                "armorShoulderL",
                "armorShoulderR"
        )));

        armor.put(EquipmentSlot.LEGS, new ArrayList<>(List.of(
                "armorHip",
                "armorPantsLowL",
                "armorPantsUpL",
                "armorPantsLowR",
                "armorPantsUpR",
                "armorBootyL",
                "armorBootyR"
        )));

        armor.put(EquipmentSlot.FEET, new ArrayList<>(List.of(
                "armorShoesL",
                "armorShoesR"
        )));

        return armor;
    }

    public boolean isFoodItem(ItemStack stack) {
        return stack.isIn(ItemTags.WOLF_FOOD);
    }

    @Override
    public boolean shouldRenderName() {
        this.setCustomName(Text.of(getGirlDisplayName()));
        this.setCustomNameVisible(true);
        return true;
    }

    public void setBasePosHere(){
        setBasePos(this.getBlockPos());
    }

    public void teleportToBase() {
        setSitting(true);
        this.teleport(this.getBasePos().getX(), this.getBasePos().getY(), this.getBasePos().getZ(), false);
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return inventory.getEquipmentStack(slot);
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        inventory.setEquipmentStack(slot, stack);
    }

    @Override
    public void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        Inventories.writeData(view, this.inventory.getItems());
        view.putBoolean("SitSate", this.isSitting());
        view.putBoolean("StripState", this.isStripped());
        view.putBoolean("FollowState", this.isFollowing());
        view.putInt("RelationshipLevel", this.getCurrentRelationshipLevel());
        view.putInt("BreastSize", getBreastSize());
        view.putBoolean("CanGetImpregnated", this.canGetImpregnated());
        view.putBoolean("PregnantState", this.isPregnant());
        view.putInt("AmountOfUnprotectedSex", this.getPregnancyStage());
        view.putInt("MilkedAmount", this.getMilkedAmount());
        view.put("BreastOffset", Vec3d.CODEC, getBreastOffset());
        view.put("BasePos", BlockPos.CODEC, this.getBasePos());
        view.putBoolean("Sitting", this.isSitting());
    }

    @Override
    public void readCustomData(ReadView view) {
        super.readCustomData(view);

        var w = this.getWorld();
        if (w != null) {
            Inventories.readData(view, this.inventory.getItems());
        }

        boolean sitting = view.getBoolean("Sitting", false);

        this.setSitting(sitting);

        boolean following = view.getBoolean("FollowState", false);
        this.setFollowing(following);

        boolean stripped = view.getBoolean("StripState", false);
        this.setStripped(stripped);

        int relationship = view.getInt("RelationshipLevel", 0);
        this.setCurrentRelationshipLevel(relationship);

        this.setBasePos(view.read("BasePos", BlockPos.CODEC).orElse(new BlockPos(0,0,0)));
        this.setBreastOffset(view.read("BreastOffset", Vec3d.CODEC).orElse(Vec3d.ZERO));
        this.setBreastSize(view.getInt("BreastSize", 100));
        this.canGetImpregnatedState(view.getBoolean("CanGetImpregnated", false));
        this.setPregnantState(view.getBoolean("PregnantState", false));
        this.setPregnancyStage(view.getInt("AmountOfUnprotectedSex", 0));
        this.setMilkedAmount(view.getInt("MilkedAmount", 0));

    }


    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        return true;
    }

    public boolean isSitting() {
        return this.dataTracker.get(SITTING);
    }

    public void setSitting(boolean sitting) {
        this.setTarget(null);
        this.dataTracker.set(SITTING, sitting);
    }


    @Override
    public void onDeath(DamageSource damageSource) {
        this.removeAllPassengers();
        super.onDeath(damageSource);
    }


    @Override
    public void tick() {
        super.tick();

        previousYaw = getYaw();
        previousVelocity = getVelocity();
        this.setMovementLockedState(this.isFrozenInPlace() || this.isWaitingAtBed() || this.isSceneActive() || this.isWaitingForPlayer());
    }



    @Override
    public void setSprinting(boolean sprinting) {
        this.setFlag(SPRINTING_FLAG_INDEX, sprinting);
        EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        entityAttributeInstance.removeModifier(SPRINTING_SPEED_BOOST.id());
        if (sprinting) {
            entityAttributeInstance.addTemporaryModifier(SPRINTING_SPEED_BOOST);
        }
    }

    protected void eat(PlayerEntity player, Hand hand, ItemStack stack) {
        int i = stack.getCount();
        UseRemainderComponent useRemainderComponent = stack.get(DataComponentTypes.USE_REMAINDER);
        stack.decrementUnlessCreative(1, player);
        this.playSound(SoundEvents.ENTITY_GENERIC_EAT.value());
        this.dataTracker.set(CONSUMING_STACK, stack);
        this.getWorld().sendEntityStatus(this, PleasureHorizonsEntityStatuses.EAT_PARTICLES);
        if (useRemainderComponent != null) {
            ItemStack itemStack = useRemainderComponent.convert(stack, i, player.isInCreativeMode(), player::giveOrDropStack);
            player.setStackInHand(hand, itemStack);
        }
    }

    public Vec3d getPassengerPos() {
        boolean isZero = this.getPassengerBonePosition().isInRange(Vec3d.ZERO, 0.1);

        if(isZero || !this.isHavingSex()){
            // Default position when no bone data is available
            return this.getPos().add(0, 1, 0);
        }

        return this.getPos().add(this.getPassengerBonePosition())
                .add(0, this.passengerYOffset, 0);
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        return this.getPassengerPos();
    }

    @Override
    public Vec3d getPassengerRidingPos(Entity passenger) {
        return this.getPassengerPos();
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    protected void dropInventory(ServerWorld world) {
        super.dropInventory(world); // calls standard drop logic
        if(!isRuleEnabled((ServerWorld) this.getWorld(), GameRules.KEEP_INVENTORY)) {
            for (ItemStack stack : this.getInventory().getItems()) {
                if (!stack.isEmpty()) {
                    this.dropStack(world, stack);
                }
            }
            this.getInventory().clear();
        }
    }

    public boolean isRuleEnabled(ServerWorld world, GameRules.Key<GameRules.BooleanRule> rule) {
        return world.getGameRules().getBoolean(rule);
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        if (!this.isMovementLocked()) {
            super.pushAwayFrom(entity);
        }
    }

    @Override
    public void takeKnockback(double strength, double x, double z) {
        if (!this.isMovementLocked()) {
            super.takeKnockback(strength, x, z);
        } else {
            this.setVelocity(Vec3d.ZERO); // ensure no leftover knockback velocity
        }
    }

    @Override
    public boolean isPushable() {
        return !this.isMovementLocked();
    }


    @Override
    public void addVelocity(double dx, double dy, double dz) {
        if (!this.isMovementLocked()) {
            super.addVelocity(dx, dy, dz);
        }
    }

    @Override
    public void stopMovement() {
        super.stopMovement();
        this.setVelocity(0, this.getVelocity().y > 0 ? 0 : this.getVelocity().y, 0); // stops lateral motion
        this.setJumping(false);
        this.bodyYaw = this.getBodyYaw();

        MoveControl control = this.getMoveControl();
        if (control != null) {
            control.moveTo(this.getX(), this.getY(), this.getZ(), 0); // keep position
        }
    }

    public static DefaultAttributeContainer.Builder createDefaultAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20)
                .add(EntityAttributes.MOVEMENT_SPEED, .20)
                .add(EntityAttributes.TEMPT_RANGE, 15)
                .add(EntityAttributes.FOLLOW_RANGE, 100)
                .add(EntityAttributes.ATTACK_DAMAGE, 2);
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        ItemStack itemStack = this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
        ItemStack itemStack2 = this.getProjectileType(itemStack);
        PersistentProjectileEntity persistentProjectileEntity = this.createArrowProjectile(itemStack2, pullProgress, itemStack);
        double d = target.getX() - this.getX();
        double e = target.getBodyY(0.3333333333333333) - persistentProjectileEntity.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            ProjectileEntity.spawnWithVelocity(
                    persistentProjectileEntity, serverWorld, itemStack2, d, e + g * 0.2F, f, 1.6F, 1
            );
        }

        this.playSound(SoundEvents.ENTITY_ARROW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier, @Nullable ItemStack shotFrom) {
        return ProjectileUtil.createArrowProjectile(this, arrow, damageModifier, shotFrom);
    }

    @Override
    public void handleStatus(byte status) {
        if (status == PleasureHorizonsEntityStatuses.POSITIVE_REACTION_PARTICLES) {
            this.spawnParticles(ParticleTypes.HEART);
        }
        else if (status == PleasureHorizonsEntityStatuses.NEGATIVE_REACTION_PARTICLES) {
            this.spawnParticles(ParticleTypes.SMOKE);
        }
        else if (status == PleasureHorizonsEntityStatuses.HAPPY_PARTICLES) {
            this.spawnParticles(ParticleTypes.HAPPY_VILLAGER);
        }
        else if (status == PleasureHorizonsEntityStatuses.ANGRY_PARTICLES) {
            this.spawnParticles(ParticleTypes.ANGRY_VILLAGER);
        }
        else if (status == PleasureHorizonsEntityStatuses.EAT_PARTICLES) {
            this.spawnItemParticles(this.dataTracker.get(CONSUMING_STACK), 16);
        }
        else {
            super.handleStatus(status);
        }
    }

    protected void spawnParticles(ParticleEffect parameters) {
        for (int i = 0; i < 5; i++) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            this.getWorld().addParticleClient(parameters, this.getParticleX(1.0), this.getRandomBodyY() + 0.5f, this.getParticleZ(1.0), d, e, f);
        }
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (this.attackTarget != null) {
            ticksSinceLastHit++;

            if (ticksSinceLastHit >= MAX_TICKS_NO_HIT) {
                // Lost interest — stop attacking
                this.setTarget(null);
                attackTarget = null;
                ticksSinceLastHit = 0;
            }
        }

        if(isGUIOpen()){
            this.navigation.stop();
            if(lookAtTarget != null) getLookControl().lookAt(lookAtTarget, this.getMaxHeadRotation() + 20, this.getMaxLookPitchChange());
        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);

        if (target != null) {
            attackTarget = target;
            ticksSinceLastHit = 0; // reset countdown on new target
        } else {
            attackTarget = null;
            ticksSinceLastHit = 0;
        }
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean success = super.tryAttack(world, target);

        if (success && target == attackTarget) {
            ticksSinceLastHit = 0; // reset timer on successful hit
        }

        return success;
    }

    public GirlEntity createTempClone() {
        if(this.getWorld().isClient()) return null;

        GirlEntity clone = (GirlEntity) this.getType().create(this.getWorld(), SpawnReason.EVENT);

        clone.setTemporaryState(true);
        clone.setPosition(this.getX(), 800, this.getZ());
        clone.setInvisible(true);
        clone.setInvulnerable(true);
        clone.setNoGravity(true);

        this.onTempCloneCreation(clone);

        this.getWorld().spawnEntity(clone);
        this.setCreatedCloneState(true);
        return clone;
    }

    public void onTempCloneCreation(GirlEntity clone) {
        clone.setStripped(this.isStripped());
    }

    @Override
    public float getSoundPitch() {
        return 1.0F;
    }

    @Override
    public boolean isPersistent() {
        return true;
    }
}
