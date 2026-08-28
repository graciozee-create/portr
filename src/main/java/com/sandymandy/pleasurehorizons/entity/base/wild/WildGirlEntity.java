package com.sandymandy.pleasurehorizons.entity.base.wild;

import com.sandymandy.pleasurehorizons.entity.ai.goal.BedGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlMeleeAttackGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.GirlTemptGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.MoveToPlayerGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.StationaryContactGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.StopMovementGoal;
import com.sandymandy.pleasurehorizons.entity.ai.goal.StripGoal;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.item.items.GiftItem;
import com.sandymandy.pleasurehorizons.networking.S2C.SceneOptionsS2CPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Non-tameable girl behaviour: gifts buy relationship levels, scenes spend them again.
 *
 * <p>Wild girls expose no ownership inventory, carry, sit or follow controls. Their scene picker
 * is opened directly by interaction and is tied server-side to that exact player; the scene-start
 * packet performs its own authorization before trusting the selection.</p>
 */
public abstract class WildGirlEntity extends GirlSceneEntity {
    private static final List<EquipmentSlot> ARMOR_ORDER = List.of(
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

    protected WildGirlEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    private boolean unavailableForIdleAI() {
        return this.isPassenger() || this.isDowned() || this.isSceneActive()
                || this.isMovementLocked();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Scene goals must pre-empt combat and idle movement.
        this.goalSelector.addGoal(0, new StationaryContactGoal(this));
        this.goalSelector.addGoal(0, new MoveToPlayerGoal(this, 1.25D));
        this.goalSelector.addGoal(0, new BedGoal(this, 1.25D));
        this.goalSelector.addGoal(0, new StripGoal(this));
        this.goalSelector.addGoal(0, new StopMovementGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(1, new GirlTemptGoal(this, 1.0D, false));
        this.goalSelector.addGoal(2, new GirlMeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return !WildGirlEntity.this.unavailableForIdleAI() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !WildGirlEntity.this.unavailableForIdleAI() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F) {
            @Override
            public boolean canUse() {
                return !WildGirlEntity.this.unavailableForIdleAI() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !WildGirlEntity.this.unavailableForIdleAI() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !WildGirlEntity.this.unavailableForIdleAI() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !WildGirlEntity.this.unavailableForIdleAI() && super.canContinueToUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return !WildGirlEntity.this.unavailableForIdleAI() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !WildGirlEntity.this.unavailableForIdleAI() && super.canContinueToUse();
            }
        });
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || this.isDowned() || this.isPassenger()
                || this.isSceneActive() || !this.getOverrideAnim().isEmpty()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (stack.getItem() instanceof GiftItem gift
                && this.getCurrentRelationshipLevel() < this.maxRelationshipLevel()) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            this.addAffection(gift.getAffectionValue());
            this.setCurrentRelationshipLevel(this.getCurrentRelationshipLevel() + 1);
            this.playSound(SoundEvents.PLAYER_LEVELUP, 0.7F, 1.4F);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "msg.pleasurehorizons.gift_given", gift.getAffectionValue(), this.getAffection()), true);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HEART,
                        this.getX(), this.getY() + 1.5D, this.getZ(), 7,
                        0.4D, 0.4D, 0.4D, 0.1D);
            }
            return InteractionResult.CONSUME;
        }

        if (stack.is(this.isAttractedTo())
                && this.getCurrentRelationshipLevel() < this.maxRelationshipLevel()) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            this.setCurrentRelationshipLevel(this.getCurrentRelationshipLevel() + 1);
            this.playSound(SoundEvents.PLAYER_LEVELUP, 0.7F, 1.4F);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("msg.pleasurehorizons.likedGift"), true);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HEART,
                        this.getX(), this.getY() + 1.5D, this.getZ(), 7,
                        0.4D, 0.4D, 0.4D, 0.1D);
            }
            return InteractionResult.CONSUME;
        }

        if (player instanceof ServerPlayer serverPlayer && !this.getScenes().isEmpty()) {
            this.setGUIOpenState(true, player);
            PacketDistributor.sendToPlayer(serverPlayer, new SceneOptionsS2CPacket(
                    this.getId(), this.getCurrentRelationshipLevel(),
                    new ItemStack(this.isAttractedTo()), this.getScenes()));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    /** Wild scene costs are paid when a validated scene starts. */
    @Override
    public boolean useUpRelationShipLevels() {
        return true;
    }

    /** Wild girls cannot be put into tameable-only movement modes by commands or stale NBT. */
    @Override
    public void setFollowing(boolean following) {
        super.setFollowing(false);
    }

    @Override
    public void setSitting(boolean sitting) {
        super.setSitting(false);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        // Mob.finalizeSpawn does not invoke populateDefaultEquipmentSlots in 1.21.1.
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        return result;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.BOW));
        this.setItemSlot(EquipmentSlot.MAINHAND,
                new ItemStack(random.nextInt(4) == 3 ? Items.IRON_SWORD : Items.STONE_SWORD));

        int tier = random.nextInt(2);
        if (random.nextFloat() < 0.095F) tier++;
        if (random.nextFloat() < 0.095F) tier++;
        if (random.nextFloat() < 0.095F) tier++;

        boolean first = true;
        for (EquipmentSlot slot : ARMOR_ORDER) {
            if (!first && random.nextFloat() < 0.1F) {
                break;
            }
            first = false;
            if (this.getItemBySlot(slot).isEmpty()) {
                Item item = getEquipmentForSlot(slot, tier);
                if (item != null) {
                    this.setItemSlot(slot, new ItemStack(item));
                }
            }
        }

        // Preserve vanilla's small chance to fill any armor slots left empty by the early break.
        super.populateDefaultEquipmentSlots(random, difficulty);
    }
}
