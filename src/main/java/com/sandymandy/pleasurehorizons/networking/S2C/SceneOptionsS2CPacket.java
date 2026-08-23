package com.sandymandy.pleasurehorizons.networking.S2C;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Sends the scene list to the client so the "Talk" button can open {@code GirlSceneScreen}.
 *
 * <p>Previously an empty no-op record, which is why the Talk button did nothing.</p>
 *
 * <p>The handler intentionally goes through {@code ClientPacketHandlers} by reflection: importing
 * a client-only class from a common packet class would crash a dedicated server on class load.</p>
 */
public record SceneOptionsS2CPacket(int entityId, int currentRelationshipLevel, ItemStack attractedTo,
                                    List<Scene> options) implements CustomPacketPayload {

    public static final Type<SceneOptionsS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "scene_options"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SceneOptionsS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SceneOptionsS2CPacket::entityId,
                    ByteBufCodecs.VAR_INT, SceneOptionsS2CPacket::currentRelationshipLevel,
                    ItemStack.OPTIONAL_STREAM_CODEC, SceneOptionsS2CPacket::attractedTo,
                    ByteBufCodecs.<RegistryFriendlyByteBuf, Scene, List<Scene>>collection(
                            ArrayList::new, Scene.PACKET_CODEC), SceneOptionsS2CPacket::options,
                    SceneOptionsS2CPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                Class.forName("com.sandymandy.pleasurehorizons.client.networking.ClientPacketHandlers")
                        .getMethod("handleSceneOptions", int.class, int.class, ItemStack.class, List.class)
                        .invoke(null, this.entityId(), this.currentRelationshipLevel(),
                                this.attractedTo(), this.options());
            } catch (Exception e) {
                PleasureHorizons.LOGGER.error("Failed to open scene options screen", e);
            }
        });
    }
}
