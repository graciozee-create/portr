package com.sandymandy.pleasurehorizons.entity.girls;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import com.sandymandy.pleasurehorizons.networking.S2C.GoblinCaughtScreenS2CPacket;
import com.sandymandy.pleasurehorizons.registries.GirlRegistry;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Goblin - cave-dwelling thief with the original full thief mechanics.
 *
 * <ul>
 *   <li>Wild goblins steal gold items (tools, ingots, nuggets, blocks, apples) from nearby
 *       survival players every 15 seconds and hold them in their offhand.</li>
 *   <li>Right-clicking a goblin that has stolen goods opens the catch screen: take your stuff
 *       back, start her special scene, or make her your queen.</li>
 *   <li>Queens stay untamed and periodically birth baby thieves that carry your gold, so each
 *       stolen item gives you another catchable goblin.</li>
 *   <li>Shift + right-click lets you ride a goblin piggyback (wild or tamed).</li>
 * </ul>
 *
 * <p>It reuses the shared tamed-girl ownership model: {@code isTamed()}/{@code setTamedBy()}.
 * The thief behaviour runs only while untamed, downed/scene/passenger/sitting are all gated.</p>
 */
public class GoblinEntity extends SettlementGirlEntityAI implements PlayerRideableJumping {

    // Synced data
    private static final EntityDataAccessor<String> DATA_STOLEN_OWNER =
            SynchedEntityData.defineId(GoblinEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<ItemStack> DATA_STOLEN_ITEM =
            SynchedEntityData.defineId(GoblinEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> DATA_IS_QUEEN =
            SynchedEntityData.defineId(GoblinEntity.class, EntityDataSerializers.BOOLEAN);

    // Gold items the thief hunts (matching the original GOLD_ITEMS).
    private static final Set<Item> GOLD_ITEMS = Set.of(
            Items.GOLDEN_SWORD, Items.GOLDEN_AXE, Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_HOE,
            Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS,
            Items.GOLD_INGOT, Items.GOLD_NUGGET, Items.GOLD_BLOCK,
            Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE
    );

    private static final int STEAL_COOLDOWN_TICKS = 300;      // 15 seconds between thefts
    private static final int STEAL_RANGE_SQ = 16;             // 4 blocks
    private static final int QUEEN_SPAWN_INTERVAL = 32000;    // ~26 min between queen births
    private static final double QUEEN_SPAWN_RANGE = 64.0D;

    private int stealCooldown = 0;
    private int stealCount = 0;
    private int queenSpawnTimer = 0;
    private final List<ItemStack> stolenItems = new ArrayList<>();

    public GoblinEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        // The thief holds stolen goods so the offhand/weapon bones should stay invisible.
        if (!level.isClientSide()) {
            this.setBoneVisibility("offhand", false);
            this.setBoneVisibility("weapon", false);
            this.setBoneVisibility("weaponStart", false);
            this.setBoneVisibility("weaponEnd", false);
            this.setBoneVisibility("customHandL", false);
            this.setBoneVisibility("customHandR", false);
            this.setBoneVisibility("blocks", false);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createDefaultAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STOLEN_OWNER, "");
        builder.define(DATA_STOLEN_ITEM, ItemStack.EMPTY);
        builder.define(DATA_IS_QUEEN, false);
    }

    @Override
    public Item isAttractedTo() {
        return Items.EMERALD;
    }

    @Override
    public String getGirlID() {
        return "goblin";
    }

    @Override
    public int getSizeGUI() {
        return 29;
    }

    @Override
    public float getYAxisGUI() {
        return 0.0525F;
    }

    // ------------------------------------------------------------ thief state

    public int getStealCount() {
        return this.stealCount;
    }

    public void setStealCount(int count) {
        this.stealCount = count;
    }

    public List<ItemStack> getStolenItems() {
        return this.stolenItems;
    }

    /** Client-safe: uses the synced held-item accessor. */
    public boolean hasStolenItems() {
        return this.stealCount > 0 || !this.entityData.get(DATA_STOLEN_ITEM).isEmpty();
    }

    public boolean isQueen() {
        return this.entityData.get(DATA_IS_QUEEN);
    }

    public void setQueen(boolean queen) {
        this.entityData.set(DATA_IS_QUEEN, queen);
    }

    // ------------------------------------------------------------ tick

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        this.handleServerTick();
    }

    private void handleServerTick() {
        // Don't steal while occupied by a scene/carry/downed/sitting state, or being ridden.
        if (this.isSceneActive() || this.isDowned() || this.isPassenger()
                || this.isSitting() || !this.getPassengers().isEmpty()) {
            return;
        }

        if (!this.isTamed() && !this.isQueen()) {
            this.attemptSteal();
        }
        if (this.isQueen() && !this.isTamed()) {
            this.handleQueenBirth();
        }
    }

    // ------------------------------------------------------------ stealing

    private void attemptSteal() {
        if (this.stealCooldown > 0) {
            this.stealCooldown--;
            return;
        }
        this.stealCooldown = STEAL_COOLDOWN_TICKS;

        for (Player player : this.level().players()) {
            if (player.distanceToSqr(this) > STEAL_RANGE_SQ
                    || player.isCreative() || player.isSpectator()) {
                continue;
            }
            if (this.isOwner(player)) continue;

            int slot = this.findGoldItem(player.getInventory());
            if (slot == -1) continue;

            ItemStack stolen = player.getInventory().getItem(slot).split(1);
            if (stolen.isEmpty()) continue;

            this.stealCount++;
            this.stolenItems.add(stolen.copy());
            this.entityData.set(DATA_STOLEN_ITEM, stolen.copy());
            this.entityData.set(DATA_STOLEN_OWNER, player.getStringUUID());
            this.setItemSlot(EquipmentSlot.MAINHAND, stolen.copy());
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.goblin_swiped"), true);
            return;
        }
    }

    private int findGoldItem(Inventory inventory) {
        List<Integer> goldSlots = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && GOLD_ITEMS.contains(stack.getItem())) {
                goldSlots.add(i);
            }
        }
        if (goldSlots.isEmpty()) return -1;
        return goldSlots.get(this.random.nextInt(goldSlots.size()));
    }

    public void returnStolenItems(ServerPlayer player) {
        if (this.stealCount <= 0 && this.stolenItems.isEmpty()) return;

        for (ItemStack stack : this.stolenItems) {
            if (!stack.isEmpty()) {
                this.spawnAtLocation(stack.copy());
            }
        }
        ItemStack held = this.getMainHandItem();
        if (!held.isEmpty()) {
            this.spawnAtLocation(held.copy());
        }

        this.stealCount = 0;
        this.stolenItems.clear();
        this.entityData.set(DATA_STOLEN_ITEM, ItemStack.EMPTY);
        this.entityData.set(DATA_STOLEN_OWNER, "");
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        player.displayClientMessage(
                Component.translatable("msg.pleasurehorizons.goblin_recovered"), true);
    }

    // ------------------------------------------------------------ queen

    private void handleQueenBirth() {
        if (this.queenSpawnTimer < QUEEN_SPAWN_INTERVAL) {
            this.queenSpawnTimer++;
            return;
        }
        this.queenSpawnTimer = 0;

        Player nearest = this.level().getNearestPlayer(this, QUEEN_SPAWN_RANGE);
        if (nearest == null || nearest.isCreative() || nearest.isSpectator()) return;

        int slot = this.findGoldItem(nearest.getInventory());
        if (slot == -1) return;

        ItemStack taken = nearest.getInventory().removeItem(slot, 1);
        if (taken.isEmpty()) return;

        this.spawnBabyThief(nearest, taken);
    }

    private void spawnBabyThief(Player player, ItemStack taken) {
        GoblinEntity baby = GirlRegistry.GOBLIN.get().create(this.level());
        if (baby == null) return;

        double angle = this.random.nextDouble() * Math.PI * 2.0D;
        double dist = 1.0D + this.random.nextDouble();
        baby.moveTo(player.getX() + Math.cos(angle) * dist, player.getY(),
                player.getZ() + Math.sin(angle) * dist);
        baby.setStealCount(1);
        baby.stolenItems.add(taken.copy());
        baby.entityData.set(DATA_STOLEN_ITEM, taken.copy());
        baby.entityData.set(DATA_STOLEN_OWNER, player.getStringUUID());
        baby.setItemSlot(EquipmentSlot.MAINHAND, taken.copy());
        baby.setPersistenceRequired();
        this.level().addFreshEntity(baby);

        player.displayClientMessage(
                Component.translatable("msg.pleasurehorizons.goblin_baby_stole", taken.getHoverName()),
                true);
    }

    // ------------------------------------------------------------ interaction

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) {
            // Optimistic client result: a catchable goblin and piggyback are both accepted
            // server-side; everything else falls through to the base screen/feeding flow.
            boolean caught = !this.isTamed() && this.hasStolenItems();
            boolean ride = player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND;
            if ((caught || ride) && !this.isDowned() && !this.isSceneActive()) {
                return InteractionResult.SUCCESS;
            }
            return super.mobInteract(player, hand);
        }

        if (!this.getOverrideAnim().isEmpty() || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            return this.toggleRide(player);
        }

        // A goblin carrying your gold can be caught: open the catch screen -
        // unless the player holds her taming item. The screen used to swallow
        // every non-sneak click, which made taming impossible in practice
        // (a goblin near a player with any gold item in the inventory always
        // had stolen goods, so the emerald feed path was never reached).
        if (!this.isTamed() && this.hasStolenItems()
                && !player.getItemInHand(hand).is(this.isAttractedTo())) {
            this.openCaughtScreen(player);
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    public void openCaughtScreen(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        this.getNavigation().stop();
        PacketDistributor.sendToPlayer(serverPlayer,
                new GoblinCaughtScreenS2CPacket(this.getId(), true));
    }

    /**
     * Handle one choice from the client catch screen.
     * Allowed actions: {@code return}, {@code scene}, {@code make_queen}, {@code dismiss}.
     */
    public void handleCatchAction(ServerPlayer player, String action) {
        switch (action) {
            case "return" -> this.returnStolenItems(player);
            case "scene" -> this.startScene(player, BREEDING_SCENE);
            case "make_queen" -> {
                if (!this.isQueen()) {
                    this.returnStolenItems(player);
                    this.setQueen(true);
                    this.setPersistenceRequired();
                    player.displayClientMessage(
                            Component.translatable("msg.pleasurehorizons.goblin_queen"), true);
                }
            }
            default -> player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.goblin_unknown"), true);
        }
        PacketDistributor.sendToPlayer(player,
                new GoblinCaughtScreenS2CPacket(this.getId(), false));
    }

    @Override
    public void setTamedBy(Player player) {
        super.setTamedBy(player);
        // The catch screen (the only way to retrieve stolen gold) only opens
        // for UNTAMED goblins, so taming a thief who still carries your gold
        // must return it on the spot or it would be stuck in her forever.
        if (this.hasStolenItems() && player instanceof ServerPlayer serverPlayer) {
            this.returnStolenItems(serverPlayer);
        }
    }

    private InteractionResult toggleRide(Player player) {
        if (this.isDowned() || this.isSceneActive() || this.isPassenger()) {
            return InteractionResult.PASS;
        }
        if (this.getPassengers().isEmpty()) {
            if (player.isPassenger()) {
                player.displayClientMessage(
                        Component.translatable("msg.pleasurehorizons.goblin_already_riding"), true);
                return InteractionResult.FAIL;
            }
            this.setFreeze(false);
            player.startRiding(this);
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.goblin_mounted"), true);
            return InteractionResult.SUCCESS;
        }
        if (this.getFirstPassenger() == player) {
            player.stopRiding();
            player.displayClientMessage(
                    Component.translatable("msg.pleasurehorizons.goblin_dismounted"), true);
            return InteractionResult.SUCCESS;
        }
        this.ejectPassengers();
        return InteractionResult.SUCCESS;
    }

    // ------------------------------------------------------------ riding

    @Override
    public boolean canAddPassenger(net.minecraft.world.entity.Entity passenger) {
        if (this.isSceneActive() || this.isDowned()) return false;
        return this.getPassengers().isEmpty() && passenger instanceof Player
                && super.canAddPassenger(passenger);
    }

    /** Fixed piggyback seat (matches the bound Galath): a 1.5-high goblin with
     *  a 0.85*bh offset would put the rider floating above her head. */
    @Override
    public net.minecraft.world.phys.Vec3 getPassengerRidingPosition(net.minecraft.world.entity.Entity passenger) {
        return new net.minecraft.world.phys.Vec3(0.0D, 0.8D, 0.0D);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity passenger = this.getFirstPassenger();
        return passenger instanceof Player player ? player : null;
    }

    /**
     * Piggyback steering, same pattern as the bound Galath: while a player
     * rides, the rider's WASD/space/sneak drive the goblin and {@code travel}
     * becomes the final authority on per-tick movement, so the shared
     * tamed-girl goals underneath cannot fight the steering.
     */
    @Override
    public void travel(Vec3 travelVector) {
        LivingEntity rider = this.getControllingPassenger();
        if (rider != null && this.isVehicle()) {
            float forward = rider.zza;
            float strafe = rider.xxa;
            boolean sneak = rider.isShiftKeyDown();
            this.setYRot(rider.getYRot());
            this.yRotO = this.getYRot();
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
            Vec3 lookVec = rider.getLookAngle();
            double hSpeed = 0.5D;
            double vSpeed = 0.3D;
            double mx = 0.0D;
            double my = 0.0D;
            double mz = 0.0D;
            if (strafe != 0.0F) {
                mx = -lookVec.z * hSpeed * strafe;
                mz = lookVec.x * hSpeed * strafe;
            }
            if (forward != 0.0F) {
                mx += lookVec.x * hSpeed * forward;
                mz += lookVec.z * hSpeed * forward;
            }
            if (this.jumping) {
                my = vSpeed;
                this.jumping = false;
            } else if (sneak) {
                my = -vSpeed;
            }
            this.setDeltaMovement(mx, my, mz);
            this.hasImpulse = true;
            this.fallDistance = 0.0F;
            super.travel(Vec3.ZERO);
            return;
        }
        super.travel(travelVector);
    }

    @Override
    public boolean isNoGravity() {
        return this.getControllingPassenger() != null || super.isNoGravity();
    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public void onPlayerJump(int jumpPower) {
        this.jumping = true;
    }

    @Override
    public void handleStartJump(int jumpPower) {
        this.jumping = true;
    }

    @Override
    public void handleStopJump() {
    }

    @Override
    public int getJumpCooldown() {
        return 0;
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return 1.0F;
    }

    // ------------------------------------------------------------ persistence

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("stolenOwner", this.entityData.get(DATA_STOLEN_OWNER));
        tag.putBoolean("isQueen", this.isQueen());
        tag.putInt("stealCount", this.stealCount);
        tag.putInt("queenSpawnTimer", this.queenSpawnTimer);

        ItemStack stolenItem = this.entityData.get(DATA_STOLEN_ITEM);
        if (!stolenItem.isEmpty()) {
            tag.put("stolenItem", stolenItem.saveOptional(this.level().registryAccess()));
        }

        CompoundTag itemsTag = new CompoundTag();
        itemsTag.putInt("count", this.stolenItems.size());
        for (int i = 0; i < this.stolenItems.size(); i++) {
            itemsTag.put("item_" + i, this.stolenItems.get(i).saveOptional(this.level().registryAccess()));
        }
        tag.put("stolenItems", itemsTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_STOLEN_OWNER, tag.getString("stolenOwner"));
        this.setQueen(tag.getBoolean("isQueen"));
        this.stealCount = tag.getInt("stealCount");
        this.queenSpawnTimer = tag.getInt("queenSpawnTimer");

        this.stolenItems.clear();
        if (tag.contains("stolenItems")) {
            CompoundTag itemsTag = tag.getCompound("stolenItems");
            int count = itemsTag.getInt("count");
            for (int i = 0; i < count; i++) {
                if (itemsTag.contains("item_" + i)) {
                    ItemStack stack = ItemStack.parseOptional(this.level().registryAccess(),
                            itemsTag.getCompound("item_" + i));
                    if (!stack.isEmpty()) this.stolenItems.add(stack);
                }
            }
        }
        if (tag.contains("stolenItem")) {
            ItemStack stolenItem = ItemStack.parseOptional(this.level().registryAccess(),
                    tag.getCompound("stolenItem"));
            if (!stolenItem.isEmpty()) {
                this.entityData.set(DATA_STOLEN_ITEM, stolenItem);
                this.setItemSlot(EquipmentSlot.MAINHAND, stolenItem.copy());
                if (!this.stolenItems.contains(stolenItem)) {
                    this.stolenItems.add(stolenItem.copy());
                }
            }
        }
    }

    @Override
    public List<Scene> getScenes() {
        return List.of(
                Scene.stationary("Breeding", 0, "breeding", 2, true, true)
        );
    }

    private static final String BREEDING_SCENE = "Breeding";
}
