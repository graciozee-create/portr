package com.sandymandy.pleasurehorizons.entity.base.tamable;

import com.sandymandy.pleasurehorizons.advancement.criterion.PleasureHorizonsCriteria;
import com.sandymandy.pleasurehorizons.entity.PleasureHorizonsEntityStatuses;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.item.PleasureHorizonsItems;
import com.sandymandy.pleasurehorizons.registries.PleasureHorizonsSoundEventRegistry;
import com.sandymandy.pleasurehorizons.screen.GirlInventoryScreenHandlerFactory;
import com.sandymandy.pleasurehorizons.util.PleasureHorizonsMessages;
import com.sandymandy.pleasurehorizons.util.managers.TamedGirlManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static com.sandymandy.pleasurehorizons.util.Utils.getPlayerName;
import static com.sandymandy.pleasurehorizons.util.Utils.getReadableItemName;

public abstract class TameableGirlEntity extends GirlSceneEntity implements Tameable {

    protected static final TrackedData<Byte> TAMEABLE_FLAGS = DataTracker.registerData(TameableGirlEntity.class, TrackedDataHandlerRegistry.BYTE);
    protected static final TrackedData<Optional<LazyEntityReference<LivingEntity>>> OWNER_UUID = DataTracker.registerData(
            TameableGirlEntity.class, TrackedDataHandlerRegistry.LAZY_ENTITY_REFERENCE
    );

    public List<String> giftRepliesLike() {
        return List.of("Nice", "Thank you so much");
    }
    public List<String> giftRepliesLove() {
        return List.of("Oh, Thank you", "Love you ~_^");
    }

    protected TameableGirlEntity(EntityType<? extends GirlSceneEntity> entityType, World world) {
        super(entityType, world);
    }


    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TAMEABLE_FLAGS, (byte)0);
        builder.add(OWNER_UUID, Optional.empty());
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        Item itemInHand = itemStack.getItem();
        if (!this.getWorld().isClient() && this.getOverrideAnim().isEmpty() && hand.equals(Hand.MAIN_HAND)) {

            if (this.isFoodItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
                this.getNavigation().findPathTo(player, 20);
                this.eat(player, hand, itemStack);
                FoodComponent foodComponent = itemStack.get(DataComponentTypes.FOOD);
                float f = foodComponent != null ? foodComponent.nutrition() : 1.0F;
                this.heal(2.0F * f);
                return ActionResult.CONSUME;
            }

            if(itemStack.isOf(PleasureHorizonsItems.MILK_JUG_EMPTY ) && this.isStripped() && isPregnant() && this.getMilkedAmount() < 4) {
                ItemStack milkInHand = ItemUsage.exchangeStack(itemStack, player, PleasureHorizonsItems.MILK_JUG_FULL.getDefaultStack());
                player.setStackInHand(hand, milkInHand);
                this.setMilkedAmount(this.getMilkedAmount() + 1);
                return ActionResult.CONSUME;
            }

            if(itemStack.isOf(Items.POTION)) return ActionResult.FAIL;

            if (this.isTamed()) return interactTamed(player, itemStack, itemInHand);

            return interactNotTamed(player, itemStack, itemInHand);
        }
        return ActionResult.PASS;
    }


    public ActionResult interactTamed(PlayerEntity player, ItemStack itemStack, Item itemInHand) {
        if (this.isOwner(player)) {
            if (itemInHand.equals(isAttractedTo()) && getCurrentRelationshipLevel() < maxRelationshipLevel()) {
                itemStack.decrementUnlessCreative(1, player);
                player.sendMessage(Text.literal("She Liked The Gift"), true);
                if(getCurrentRelationshipLevel() < 4) messageAsEntity(this.giftRepliesLike().get(RANDOM.nextInt(this.giftRepliesLike().size())));
                else messageAsEntity(this.giftRepliesLove().get(RANDOM.nextInt(this.giftRepliesLove().size())));
                setCurrentRelationshipLevel(getCurrentRelationshipLevel() + 1);
                this.getWorld().sendEntityStatus(this, PleasureHorizonsEntityStatuses.HAPPY_PARTICLES);
                this.playSound(PleasureHorizonsSoundEventRegistry.SoundGroup.GIGGLE.getSound(this.getGirlID()));
                return ActionResult.SUCCESS_SERVER;
            }

            if(!this.isSceneActive()) {
                if (player.isSneaking()) {
                    this.setSitting(!this.isSitting());
                    this.jumping = false;
                    this.navigation.stop();
                    return ActionResult.SUCCESS_SERVER;
                                    }
                player.openHandledScreen(new GirlInventoryScreenHandlerFactory(this));
                this.setGUIOpenState(true, player);
                return ActionResult.SUCCESS_SERVER;
            }
            return ActionResult.FAIL;
        }
        player.sendMessage(Text.translatable("msg.pleasurehorizons.alreadyInRelationship"), true);
        return ActionResult.FAIL;
    }

    public ActionResult interactNotTamed(PlayerEntity player, ItemStack itemStack, Item itemInHand) {
        if (itemInHand.equals(isAttractedTo())) {
            itemStack.decrementUnlessCreative(1, player);
            this.tryTame(player);
            return ActionResult.SUCCESS_SERVER;
        }

        player.sendMessage(Text.literal("She ignores you. Maybe try giving her a " + getReadableItemName(this.isAttractedTo()) + "."), true);
        return ActionResult.FAIL;
    }


    private void tryTame(PlayerEntity player) {
        if (this.random.nextInt(3) == 0) {
            this.playSound(PleasureHorizonsSoundEventRegistry.SoundGroup.HAPPOH.getSound(this.getGirlID()));
            this.setTamedBy(player);
            this.navigation.stop();
            setTarget(null);
            this.getWorld().sendEntityStatus(this, PleasureHorizonsEntityStatuses.POSITIVE_REACTION_PARTICLES);
            player.sendMessage(Text.literal("You Asked " + getGirlDisplayName() + " Out And She Said §aYes" ), true);
            this.setBasePosHere();
        } else {
            this.getWorld().sendEntityStatus(this, PleasureHorizonsEntityStatuses.NEGATIVE_REACTION_PARTICLES);
        }
    }

    public void breakUp(PlayerEntity player) {
        if(!this.getWorld().isClient()){
            this.setTamed(false,true); // Mark the entity as untamed
            this.setOwner((LivingEntity) null);
            TamedGirlManager.get((ServerWorld) this.getWorld()).removeGirl(this.getUuid());
            this.setSitting(false);
            this.setStripped(false);
            this.setFollowing(false);
            this.dropInventory((ServerWorld) this.getWorld());
            this.setCurrentRelationshipLevel(0);
            if(!isTamed() && !isOwner(player)){
                player.sendMessage(Text.literal("§cYou Broke Up With " + getGirlDisplayName()), true);
            }
        }
    }

    public void breakUpParticles(PlayerEntity player) {
        this.breakUp(player);
        this.getWorld().sendEntityStatus(this, PleasureHorizonsEntityStatuses.ANGRY_PARTICLES);
        this.playSound(PleasureHorizonsSoundEventRegistry.SoundGroup.SADOH.getSound(this.getGirlID()));
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isInvulnerableTo(world, source)) return false;

        String damageType = source.getName();
        // If killed by /kill or void, allow normal death
        if (damageType.equals("outOfWorld") || damageType.equals("genericKill")) {
            return super.damage(world, source, amount);
        }

        if(this.isTamed() && (this.getHealth() - amount <= 0.0F) &! (damageType.equals("outOfWorld") || damageType.equals("genericKill") || isMovementLocked())) {
            this.setHealth(getMaxHealth());
            // If basePos is still null, fall back to current position

            // Send a message referencing whichever Pos we have
            PleasureHorizonsMessages.GlobleMessage(
                    this.getWorld(),
                    getPlayerName((PlayerEntity) this.getOwner()) + "'s " +
                            getGirlDisplayName() + " died and respawned at base: " +
                            this.getBasePos().getX() + ", " +
                            this.getBasePos().getY() + ", " +
                            this.getBasePos().getZ()
            );

            // Drops inventory as if she died
            this.dropInventory(world);

            teleportToBase();


            return false;
        }
        else if(isMovementLocked() &! damageType.equals("outOfWorld") || damageType.equals("genericKill")){
            if(!this.hasPassengers()){
                ((PlayerEntity)this.getOwner()).sendMessage(
                        Text.of(getGirlDisplayName() + " is busy at the moment"), true);
            }
            return false;
        }
        else{
            return super.damage(world, source, amount);
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (this.getWorld() instanceof ServerWorld serverWorld
                && serverWorld.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES)
                && this.getOwner() instanceof ServerPlayerEntity serverPlayerEntity) {
            serverPlayerEntity.sendMessage(this.getDamageTracker().getDeathMessage());
        }
        super.onDeath(damageSource);
    }

    @Override
    public void tick() {
        if (!this.getWorld().isClient()) {
            ServerWorld world = (ServerWorld) this.getWorld();

            if (this.isTamed()) {
                TamedGirlManager.get(world).registerGirl(this);
            }
            else if (TamedGirlManager.get(world).containsGirl(this.getUuid())){
                // not tamed anymore → remove
                TamedGirlManager.get(world).removeGirl(this.getUuid());
            }

            byte b = this.dataTracker.get(TAMEABLE_FLAGS);
            if (isSitting()) {
                this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 1));
            } else {
                this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & -2));
            }
        }
        super.tick();
    }

    @Override
    public void modelLogic() {
        super.modelLogic();
        this.setBoneSize("boobs", this.getBreastSize(), getBreastMinSize(), getBreastMaxSize());
        this.setBonePos("boobs", this.getBreastOffset());
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }



    public boolean isTamed() {
        return (this.dataTracker.get(TAMEABLE_FLAGS) & 4) != 0;
    }

    public void setTamed(boolean tamed, boolean updateAttributes) {
        byte b = this.dataTracker.get(TAMEABLE_FLAGS);
        if (tamed) {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 4));
        } else {
            this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & -5));
        }

        if (updateAttributes) {
            this.updateAttributesForTamed();
        }
    }

    protected void updateAttributesForTamed() {
    }

    @Nullable
    @Override
    public LazyEntityReference<LivingEntity> getOwnerReference() {
        return (LazyEntityReference)((Optional)this.dataTracker.get(OWNER_UUID)).orElse((Object)null);
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.dataTracker.set(OWNER_UUID, Optional.ofNullable(owner).map(LazyEntityReference::new));
    }

    public void setOwner(@Nullable LazyEntityReference<LivingEntity> owner) {
        this.dataTracker.set(OWNER_UUID, Optional.ofNullable(owner));
    }

    public void setTamedBy(PlayerEntity player) {
        this.setTamed(true, true);
        this.setOwner(player);
        if (player instanceof ServerPlayerEntity serverPlayerEntity) {
            PleasureHorizonsCriteria.TAME_GIRL.trigger(serverPlayerEntity, this);        }
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        return !this.isOwner(target) && super.canTarget(target);
    }

    public boolean isOwner(LivingEntity entity) {
        return entity == this.getOwner();
    }

    @Nullable
    @Override
    public Team getScoreboardTeam() {
        Team team = super.getScoreboardTeam();
        if (team != null) {
            return team;
        } else {
            if (this.isTamed()) {
                LivingEntity livingEntity = this.getTopLevelOwner();
                if (livingEntity != null) {
                    return livingEntity.getScoreboardTeam();
                }
            }

            return null;
        }
    }

    @Override
    protected boolean isInSameTeam(Entity other) {
        if (this.isTamed()) {
            LivingEntity livingEntity = this.getTopLevelOwner();
            if (other == livingEntity) {
                return true;
            }

            if (livingEntity != null) {
                return this.isTeamPlayer(other.getScoreboardTeam());
            }
        }

        return super.isInSameTeam(other);
    }

    public void tryTeleportToOwner() {
        LivingEntity livingEntity = this.getOwner();
        if (livingEntity != null) {
            this.tryTeleportNear(livingEntity.getBlockPos());
        }
    }

    public boolean shouldTryTeleportToOwner() {
        LivingEntity livingEntity = this.getOwner();
        return livingEntity != null && this.squaredDistanceTo(this.getOwner()) >= 144.0;
    }

    private void tryTeleportNear(BlockPos pos) {
        for (int i = 0; i < 10; i++) {
            int j = this.random.nextBetween(-3, 3);
            int k = this.random.nextBetween(-3, 3);
            if (Math.abs(j) >= 2 || Math.abs(k) >= 2) {
                int l = this.random.nextBetween(-1, 1);
                if (this.tryTeleportTo(pos.getX() + j, pos.getY() + l, pos.getZ() + k)) {
                    return;
                }
            }
        }
    }

    private boolean tryTeleportTo(int x, int y, int z) {
        if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        } else {
            this.refreshPositionAndAngles(x + 0.5, y, z + 0.5, this.getYaw(), this.getPitch());
            this.navigation.stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPos pos) {
        PathNodeType pathNodeType = LandPathNodeMaker.getLandNodeType(this, pos);
        if (pathNodeType != PathNodeType.WALKABLE) {
            return false;
        } else {
            BlockState blockState = this.getWorld().getBlockState(pos.down());
            if (!this.canTeleportOntoLeaves() && blockState.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockPos = pos.subtract(this.getBlockPos());
                return this.getWorld().isSpaceEmpty(this, this.getBoundingBox().offset(blockPos));
            }
        }
    }

    public final boolean cannotFollowOwner() {
        return this.isSitting() || this.hasVehicle() || this.mightBeLeashed() || this.getOwner() != null && this.getOwner().isSpectator();
    }

    protected boolean canTeleportOntoLeaves() {
        return false;
    }

    @Override
    public void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        LazyEntityReference<LivingEntity> lazyEntityReference = this.getOwnerReference();
        if (lazyEntityReference != null) {
            lazyEntityReference.writeData(view, "Owner");
        }
    }

    @Override
    public void readCustomData(ReadView view) {
        super.readCustomData(view);

        LazyEntityReference<LivingEntity> lazyEntityReference =
                LazyEntityReference.fromDataOrPlayerName(view, "Owner", this.getWorld());

        if (lazyEntityReference != null) {
            try {
                this.dataTracker.set(OWNER_UUID, Optional.of(lazyEntityReference));
                this.setTamed(true, false);
            } catch (Throwable t) {
                this.setTamed(false, true);
            }
        } else {
            this.dataTracker.set(OWNER_UUID, Optional.empty());
            this.setTamed(false, true);
        }
    }



    public class TameableGirlEscapeDangerGoal extends EscapeDangerGoal {
        public TameableGirlEscapeDangerGoal(final double speed, final TagKey<DamageType> dangerousDamageTypes) {
            super(TameableGirlEntity.this, speed, dangerousDamageTypes);
        }

        public TameableGirlEscapeDangerGoal(final double speed) {
            super(TameableGirlEntity.this, speed);
        }

        @Override
        public void tick() {
            if (!TameableGirlEntity.this.cannotFollowOwner() && TameableGirlEntity.this.shouldTryTeleportToOwner()) {
                TameableGirlEntity.this.tryTeleportToOwner();
            }

            super.tick();
        }
    }
}
