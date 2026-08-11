package com.sandymandy.pleasurehorizons.registries;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.constant.dataticket.DataTicket;

import java.util.Map;

public class PleasureHorizonsDataTicketRegistry {
    public static final DataTicket<Boolean> IS_STRIPPED = DataTicket.create("is_stripped", Boolean.class);
    public static final DataTicket<Boolean> IS_IN_SCENE = DataTicket.create("is_in_scene", Boolean.class);
    public static final DataTicket<Boolean> HAS_VEHICLE = DataTicket.create("has_vehicle", Boolean.class);
    public static final DataTicket<Boolean> IS_SPRINTING = DataTicket.create("is_running", Boolean.class);
    public static final DataTicket<String> GIRL_ID = DataTicket.create("girl_id", String.class);
    public static final DataTicket<Integer> ENTITY_ID = DataTicket.create("entity_id", Integer.class);
    public static final DataTicket<Entity> GIRL_FIRST_PASSENGER = DataTicket.create("girl_first_passenger", Entity.class);
    public static final DataTicket<Float> GIRL_WEAPON_BONE_ROTATION_X = DataTicket.create("wep_bone_rot_x", Float.class);
    public static final DataTicket<ItemStack> GIRL_MAIN_HAND_STACK = DataTicket.create("girl_main_hand_stack", ItemStack.class);
    public static final DataTicket<Map<String, Boolean>> GIRL_BONE_VISIBILITY = (DataTicket<Map<String, Boolean>>) (Object) DataTicket.create("girl_bone_visibility", Map.class);
    public static final DataTicket<Map<String, Vec2f>> GIRL_BONE_UV_OFFSETS = (DataTicket<Map<String, Vec2f>>) (Object) DataTicket.create("girl_bone_uv_offsets", Map.class);
    public static final DataTicket<Map<String, ResourceLocation>> GIRL_BONE_TEXTURE_OVERRIDES = (DataTicket<Map<String, ResourceLocation>>) (Object) DataTicket.create("girl_bone_texture_overrides", Map.class);
    public static final DataTicket<Map<String, ResourceLocation>> GIRL_BONE_TEXTURE_OVERRIDES_LAYER_TWO = (DataTicket<Map<String, ResourceLocation>>) (Object) DataTicket.create("girl_bone_texture_overrides_layer_two", Map.class);
    public static final DataTicket<Map<String, ResourceLocation>> GIRL_BONE_TEXTURE_OVERRIDES_LAYER_THREE = (DataTicket<Map<String, ResourceLocation>>) (Object) DataTicket.create("girl_bone_texture_overrides_layer_three", Map.class);
    public static final DataTicket<Map<String, Integer>> GIRL_BONE_COLOR_OVERRIDES = (DataTicket<Map<String, Integer>>) (Object) DataTicket.create("girl_bone_color_overrides", Map.class);
    public static final DataTicket<Map<String, Vec3d>> GIRL_BONE_SIZE_OVERRIDES = (DataTicket<Map<String, Vec3d>>) (Object) DataTicket.create("girl_bone_size_overrides", Map.class);
    public static final DataTicket<Map<String, Vec3d>> GIRL_BONE_POSITION_OFFSET = (DataTicket<Map<String, Vec3d>>) (Object) DataTicket.create("girl_bone_pos_offset", Map.class);
    public static final DataTicket<String> PASSENGER_BONE_NAME = DataTicket.create("passenger_bone_name", String.class);
    public static final DataTicket<Float> YAW = DataTicket.create("yaw", Float.class);
    public static final DataTicket<Float> PREVIOUS_YAW = DataTicket.create("previous_yaw", Float.class);
    public static final DataTicket<Vec3d> PREVIOUS_VELOCITY = DataTicket.create("previous_velocity", Vec3d.class);


}
