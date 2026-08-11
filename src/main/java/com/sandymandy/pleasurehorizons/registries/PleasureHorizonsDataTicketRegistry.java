package com.sandymandy.pleasurehorizons.registries;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.constant.dataticket.DataTicket;

import java.util.Map;

public class PleasureHorizonsDataTicketRegistry {
    // GeckoLib 4.x DataTicket API uses different creation - stub to compile
    public static final DataTicket<Boolean> IS_STRIPPED = new DataTicket<>("is_stripped", Boolean.class);
    public static final DataTicket<Boolean> IS_IN_SCENE = new DataTicket<>("is_in_scene", Boolean.class);
    public static final DataTicket<Boolean> HAS_VEHICLE = new DataTicket<>("has_vehicle", Boolean.class);
    public static final DataTicket<Boolean> IS_SPRINTING = new DataTicket<>("is_running", Boolean.class);
    public static final DataTicket<String> GIRL_ID = new DataTicket<>("girl_id", String.class);
    public static final DataTicket<Integer> ENTITY_ID = new DataTicket<>("entity_id", Integer.class);
    public static final DataTicket<Entity> GIRL_FIRST_PASSENGER = new DataTicket<>("girl_first_passenger", Entity.class);
    public static final DataTicket<Float> GIRL_WEAPON_BONE_ROTATION_X = new DataTicket<>("wep_bone_rot_x", Float.class);
    public static final DataTicket<ItemStack> GIRL_MAIN_HAND_STACK = new DataTicket<>("girl_main_hand_stack", ItemStack.class);
    public static final DataTicket<Map> GIRL_BONE_VISIBILITY = new DataTicket<>("girl_bone_visibility", Map.class);
    public static final DataTicket<Map> GIRL_BONE_UV_OFFSETS = new DataTicket<>("girl_bone_uv_offsets", Map.class);
    public static final DataTicket<Map> GIRL_BONE_TEXTURE_OVERRIDES = new DataTicket<>("girl_bone_texture_overrides", Map.class);
    public static final DataTicket<Map> GIRL_BONE_TEXTURE_OVERRIDES_LAYER_TWO = new DataTicket<>("girl_bone_texture_overrides_layer_two", Map.class);
    public static final DataTicket<Map> GIRL_BONE_TEXTURE_OVERRIDES_LAYER_THREE = new DataTicket<>("girl_bone_texture_overrides_layer_three", Map.class);
    public static final DataTicket<Map> GIRL_BONE_COLOR_OVERRIDES = new DataTicket<>("girl_bone_color_overrides", Map.class);
    public static final DataTicket<Map> GIRL_BONE_SIZE_OVERRIDES = new DataTicket<>("girl_bone_size_overrides", Map.class);
    public static final DataTicket<Map> GIRL_BONE_POSITION_OFFSET = new DataTicket<>("girl_bone_pos_offset", Map.class);
    public static final DataTicket<String> PASSENGER_BONE_NAME = new DataTicket<>("passenger_bone_name", String.class);
    public static final DataTicket<Float> YAW = new DataTicket<>("yaw", Float.class);
    public static final DataTicket<Float> PREVIOUS_YAW = new DataTicket<>("previous_yaw", Float.class);
    public static final DataTicket<Vec3> PREVIOUS_VELOCITY = new DataTicket<>("previous_velocity", Vec3.class);
}
