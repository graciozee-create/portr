package com.sandymandy.pleasurehorizons.entity.base.tamable;

import com.sandymandy.pleasurehorizons.entity.ai.goal.*;
import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import com.sandymandy.pleasurehorizons.settlement.Settlement;
import com.sandymandy.pleasurehorizons.settlement.SettlementMember;
import com.sandymandy.pleasurehorizons.util.managers.SettlementManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.world.World;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class SettlementGirlEntityAI extends TameableGirlEntity implements SmartBrainOwner<SettlementGirlEntityAI>, SettlementMember {
    @Nullable
    private UUID settlementId;

    @Nullable
    private Settlement settlementCache;

    private static final TrackedData<Boolean> SHOULD_TICK_BRAIN = DataTracker.registerData(SettlementGirlEntityAI.class, TrackedDataHandlerRegistry.BOOLEAN);
    protected SettlementGirlEntityAI(EntityType<? extends SettlementGirlEntityAI> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SHOULD_TICK_BRAIN, false);
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(0, new GirlSitGoal(this));

        if(!this.dataTracker.get(SHOULD_TICK_BRAIN)) {
            this.goalSelector.add(1, new SwimGoal(this));
            this.goalSelector.add(2, new LongDoorInteractGoal(this, true));
            this.goalSelector.add(3, new TameableGirlEscapeDangerGoal(1.5D, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
            this.goalSelector.add(4, new GirlAttackSwitchGoal(this, 1.0, 5, 6, 11));
            this.goalSelector.add(5, new ConditionalGoal(new GirlFollowOwnerGoal(this, 1D, 10.0F, 2.0F), this::isFollowing));
            this.goalSelector.add(6, new TemptGoal(this, 1D, Ingredient.ofItems(isAttractedTo()), false));
            this.goalSelector.add(7, new GirlStayNearBaseGoal(this, 1.0, 2.0F, 15.0F, 150));
            this.goalSelector.add(8, new WanderAroundGoal(this, 1.0D));
            this.goalSelector.add(9, new ConditionalGoal(new LookAtEntityGoal(this, PlayerEntity.class, 6.0F), () -> !isMovementLocked()));
            this.goalSelector.add(10, new ConditionalGoal(new LookAroundGoal(this), () -> !isMovementLocked()));
            this.targetSelector.add(1, new ConditionalGoal(new GirlTrackOwnerAttackerGoal(this), this::isFollowing));
            this.targetSelector.add(2, new ConditionalGoal(new GirlAttackWithOwnerGoal(this, SettlementGirlEntityAI.class), this::isFollowing));
            this.targetSelector.add(3, new RevengeGoal(this, PlayerEntity.class, GirlEntity.class));
        }
    }

    @Nullable
    public UUID getSettlementId() {
        return settlementId;
    }

    @Override
    public @Nullable Settlement getSettlement() {
        // Lazy load settlement from ID if needed
        if (settlementCache == null && settlementId != null && !this.getWorld().isClient()) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            SettlementManager manager = SettlementManager.get(serverWorld);
            settlementCache = manager.getSettlement(settlementId);

            // If settlement no longer exists, clear the ID
            if (settlementCache == null) {
                settlementId = null;
            }
        }
        return settlementCache;
    }

    public void setSettlementById(@Nullable UUID id) {
        this.settlementId = id;
        this.settlementCache = null; // Will be lazy-loaded
    }

    @Override
    public void setSettlement(@Nullable Settlement settlement) {
        if(settlement != null) {
            this.settlementId = settlement.getId();
            this.settlementCache = settlement;
        }
        else {
            this.settlementId = null;
            this.settlementCache = null;
        }
    }

    @Override
    protected Brain.Profile<?> createBrainProfile() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void mobTick(ServerWorld world) {
        if(this.dataTracker.get(SHOULD_TICK_BRAIN)) tickBrain(this);
    }

    @Override
    public List<? extends ExtendedSensor<? extends SettlementGirlEntityAI>> getSensors() {
        return List.of(
                new NearbyLivingEntitySensor<SettlementGirlEntityAI>()
                        .setPredicate((target, entity) ->
                                target instanceof PlayerEntity ||
                                        target instanceof IronGolemEntity ||
                                        target instanceof WolfEntity ||
                                        (target instanceof TurtleEntity turtle && turtle.isBaby() && !turtle.isSwimming())),
                new HurtBySensor<>()                // This tracks the last damage source and attacker
        );
    }

    @Override
    public BrainActivityGroup<? extends SettlementGirlEntityAI> getCoreTasks() { // These are the tasks that run all the time (usually)
        return BrainActivityGroup.coreTasks(
                new LookAtTarget<>(),                      // Have the entity turn to face and look at its current look target
                new MoveToWalkTarget<>());          // Walk towards the current walk target
    }


    @Override
    public BrainActivityGroup<? extends SettlementGirlEntityAI> getIdleTasks() { // These are the tasks that run when the mob isn't doing anything else (usually)
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<SettlementGirlEntityAI>(      // Run only one of the below behaviours, trying each one in order. Include the generic type because JavaC is silly
                        new TargetOrRetaliate<>(),            // Set the attack target and walk target based on nearby entities
                        new SetPlayerLookTarget<>(),          // Set the look target for the nearest player
                        new SetRandomLookTarget<>()),         // Set a random look target
                new OneRandomBehaviour<>(                 // Run a random task from the below options
                        new SetRandomWalkTarget<>(),          // Set a random walk target to a nearby position
                        new Idle<>().runFor(entity -> entity.getRandom().nextBetween(30, 60)))); // Do nothing for 1.5->3 seconds
    }

    @Override
    public BrainActivityGroup<? extends SettlementGirlEntityAI> getFightTasks() { // These are the tasks that handle fighting
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget<>(), // Cancel fighting if the target is no longer valid
                new SetWalkTargetToAttackTarget<>(),      // Set the walk target to the attack target
                new AnimatableMeleeAttack<>(0)); // Melee attack the target if close enough
    }

//    @Override
//    public List<Activity> getActivityPriorities() {
//        return List.of(
//                GirlActivities.SEX,
//                GirlActivities.FOLLOW,
//                Activity.IDLE
//        );
//    }

//    @Override
//    public Map<Activity, BrainActivityGroup<? extends GirlEntityAI>> getAdditionalTasks() {
//        Map<Activity, BrainActivityGroup<? extends GirlEntityAI>> map = new HashMap<>();
//
//
//        map.put(GirlActivities.FOLLOW, new BrainActivityGroup<>(GirlActivities.FOLLOW)
//                .priority(15)
//                .behaviours(new FollowOwnerTask())
//        );
//
//        return map;
//    }

    @Override
    public void tick() {
        this.dataTracker.set(SHOULD_TICK_BRAIN, !(isMovementLocked() || isSitting() || this.targetBedPos != null || this.isFollowing()) && this.hasSettlement());
        super.tick();
    }

    @Override
    public void writeCustomData(WriteView view) {
        super.writeCustomData(view);

        if (settlementId != null) {
            view.put("SettlementId", Uuids.CODEC, settlementId);
        }
    }

    @Override
    public void readCustomData(ReadView view) {
        super.readCustomData(view);

        Optional<UUID> id = view.read("SettlementId", Uuids.CODEC);
        if (id.isPresent()) {
            setSettlementById(id.get());
        } else {
            setSettlementById(null);
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (this.getWorld() instanceof ServerWorld && this.hasSettlement()) {
            this.getSettlement().removeMember(this);
        }
        super.onDeath(damageSource);
    }

    @Override
    public void breakUp(PlayerEntity player) {
        if(!player.getWorld().isClient() && this.hasSettlement()) this.getSettlement().removeMember(this);
        super.breakUp(player);
    }
}
