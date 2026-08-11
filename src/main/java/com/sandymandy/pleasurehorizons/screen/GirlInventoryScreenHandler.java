package com.sandymandy.pleasurehorizons.screen;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.PleasureHorizonsClient;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.util.inventory.GirlInventory;
import com.sandymandy.pleasurehorizons.util.inventory.slot.ExclusiveSlot;
import com.sandymandy.pleasurehorizons.util.inventory.slot.InclusiveSlot;
import com.sandymandy.pleasurehorizons.util.inventory.slot.PublicArmorSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Map;

import static com.sandymandy.pleasurehorizons.registries.PleasureHorizonsScreenHandlerRegistry.GIRL_INVENTORY_SCREEN_HANDLER;

public class GirlInventoryScreenHandler extends AbstractContainerMenu {
    private final Inventory inventory;
    private final TameableGirlEntity girl;
    public static final ResourceLocation EMPTY_HELMET_SLOT_TEXTURE = ResourceLocation.withDefaultNamespace( "container/slot/helmet");
    public static final ResourceLocation EMPTY_CHESTPLATE_SLOT_TEXTURE = ResourceLocation.withDefaultNamespace( "container/slot/chestplate");
    public static final ResourceLocation EMPTY_LEGGINGS_SLOT_TEXTURE = ResourceLocation.withDefaultNamespace( "container/slot/leggings");
    public static final ResourceLocation EMPTY_BOOTS_SLOT_TEXTURE = ResourceLocation.withDefaultNamespace( "container/slot/boots");
    public static final ResourceLocation EMPTY_SWORD_TEXTURE = ResourceLocation.withDefaultNamespace( "container/slot/sword");
    public static final ResourceLocation EMPTY_BOW_TEXTURE = ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID,"container/slot/bow");
    public static final Map<EquipmentSlot, ResourceLocation> EMPTY_ARMOR_SLOT_TEXTURES = Map.of(
            EquipmentSlot.FEET,
            EMPTY_BOOTS_SLOT_TEXTURE,
            EquipmentSlot.LEGS,
            EMPTY_LEGGINGS_SLOT_TEXTURE,
            EquipmentSlot.CHEST,
            EMPTY_CHESTPLATE_SLOT_TEXTURE,
            EquipmentSlot.HEAD,
            EMPTY_HELMET_SLOT_TEXTURE
    );
    public static final EquipmentSlot[] EQUIPMENT_SLOT_ORDER = new EquipmentSlot[]{
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    // The codec-compatible constructor
    public GirlInventoryScreenHandler(int syncId, Inventory playerInventory, PleasureHorizonsClient.GirlScreenData data) {
        this(syncId, playerInventory, data.entityId());
    }

    // This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
    // and can therefore directly provide it as an argument. This inventory will then be synced to the client.
    public GirlInventoryScreenHandler(int syncId, Inventory playerInventory, int girlId) {
        super(GIRL_INVENTORY_SCREEN_HANDLER, syncId);
        Player player = playerInventory.player;
        World world = player.getWorld();

        Entity entity = world.getEntityById(girlId);
        if (!(entity instanceof TameableGirlEntity girlEntity)) {
            throw new IllegalStateException("Girl not found or mismatched entity ID");
        }
        this.girl = girlEntity;

        Inventory inventory;

        inventory = girl.getInventory(); // ← Use your custom implementation, not a copy

        this.inventory = inventory;

        checkSize(inventory, GirlInventory.TOTAL_SLOTS);


        // ───── Backpack slots (4x3) = indices 5..16 ─────Add commentMore actions
            int slotIndex = GirlInventory.BACKPACK_START; // 5
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                this.addSlot(new Slot(inventory, slotIndex++, 98 + col * 18, 6 + row * 18));
            }
        }

        // ───── Main Hand Slot = index 0 ─────
        this.addSlot(new ExclusiveSlot(inventory, GirlInventory.MAIN_HAND_SLOT, 116, 63, EMPTY_SWORD_TEXTURE, Items.BOW));
        this.addSlot(new InclusiveSlot(inventory, GirlInventory.OFF_HAND_SLOT, 134, 63, EMPTY_BOW_TEXTURE, Items.BOW));

        for (int i = 0; i < 4; i++) {
            EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_ORDER[i];
            ResourceLocation identifier = EMPTY_ARMOR_SLOT_TEXTURES.get(equipmentSlot);
            this.addSlot(new PublicArmorSlot(inventory, girl, equipmentSlot,
                    GirlInventory.ARMOR_END - i, 8, 6 + i * 18, identifier));
        }

        /*
        * Armor end = ARMOR_HEAD_SLOT
        *
        * ARMOR_FEET_SLOT = 1;
        * ARMOR_LEGS_SLOT = 2;
        * ARMOR_CHEST_SLOT = 3;
        * ARMOR_HEAD_SLOT = 4;
        *
        *
        * */

        int m;
        int l;
        // The player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }
        // The player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }

    }

    @Override
    public boolean canUse(Player player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(Player player, int index) {
        return ItemStack.EMPTY;
    }


    public TameableGirlEntity getGirl(){
        return this.girl;
    }


}
