package com.sandymandy.pleasurehorizons.entity.base.wild;

import com.sandymandy.pleasurehorizons.entity.PleasureHorizonsEntityStatuses;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.networking.S2C.SceneOptionsS2CPacket;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

import java.util.List;

public abstract class WildGirlEntity extends GirlSceneEntity {
    private static final List<EquipmentSlot> EQUIPMENT_INIT_ORDER = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

    protected WildGirlEntity(EntityType<? extends GirlSceneEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        if (random.nextFloat() < (1f)) {
            this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.BOW));

            int i = random.nextInt(4);

            switch (i){
                case 0 -> this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));

                case 1 -> this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));

                case 2 -> this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));

                case 3 -> this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));

                case 4 -> this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
            }

            int num = random.nextInt(2);
            float f = 0.1F;
            if (random.nextFloat() < 0.095F) {
                num++;
            }

            if (random.nextFloat() < 0.095F) {
                num++;
            }

            if (random.nextFloat() < 0.095F) {
                num++;
            }

            boolean bl = true;

            for (EquipmentSlot equipmentSlot : EQUIPMENT_INIT_ORDER) {
                ItemStack itemStack = this.getEquippedStack(equipmentSlot);
                if (!bl && random.nextFloat() < f) {
                    break;
                }

                bl = false;
                if (itemStack.isEmpty()) {
                    Item item = getEquipmentForSlot(equipmentSlot, num);
                    if (item != null) {
                        this.equipStack(equipmentSlot, new ItemStack(item));
                    }
                }
            }
        }
        super.initEquipment(random, localDifficulty);
    }

    @Override
    public boolean useUpRelationShipLevels() {
        return true;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new TemptGoal(this, 1D, Ingredient.ofItems(isAttractedTo()), false));
        this.goalSelector.add(2, new WanderAroundGoal(this, 1.0));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(4, new LookAroundGoal(this));

        this.targetSelector.add(1, new RevengeGoal(this));
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        // no taming, no inventory, no following
        ItemStack stack = player.getStackInHand(hand);

        if (!this.getWorld().isClient() && !this.isSceneActive() && hand.equals(Hand.MAIN_HAND)) {

            if (stack.isOf(isAttractedTo())) {
                if (getCurrentRelationshipLevel() < maxRelationshipLevel()) {
                    stack.decrementUnlessCreative(1, player);
                    player.sendMessage(Text.literal("She Liked The Gift"), true);
                    setCurrentRelationshipLevel(getCurrentRelationshipLevel() + 1);
                    this.getWorld().sendEntityStatus(this, PleasureHorizonsEntityStatuses.HAPPY_PARTICLES);
                    return ActionResult.SUCCESS_SERVER;
                }
            }
            else {
                this.setGUIOpenState(true, player);
                PacketDistributor.sendToPlayer((ServerPlayerEntity) player, new SceneOptionsS2CPacket(this.getId(), this.getCurrentRelationshipLevel(), new ItemStack(isAttractedTo()), this.getScenes()));
                return ActionResult.SUCCESS;
            }

        }

        return super.interactMob(player, hand);
    }

    // Override to prevent following, sitting, etc.
    @Override
    public void setFollowing(boolean follow) {
    }

    @Override
    public void setSitting(boolean sitting) {
    }
}
