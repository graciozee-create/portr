package com.sandymandy.pleasurehorizons.networking.S2C;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClothingArmorVisibilityS2CPacket(int entityId, boolean[] armor) implements CustomPacketPayload {
    public static final Type<ClothingArmorVisibilityS2CPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "clothing_armor_visibility"));
    public static final StreamCodec<ByteBuf, ClothingArmorVisibilityS2CPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> {
            buf.writeVarInt(packet.entityId);
            buf.writeVarInt(packet.armor.length);
            for (boolean b : packet.armor) buf.writeBoolean(b);
        },
        buf -> {
            int entityId = buf.readVarInt();
            int len = buf.readVarInt();
            boolean[] armor = new boolean[len];
            for (int i = 0; i < len; i++) armor[i] = buf.readBoolean();
            return new ClothingArmorVisibilityS2CPacket(entityId, armor);
        }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Client-side handling
        });
    }
}
