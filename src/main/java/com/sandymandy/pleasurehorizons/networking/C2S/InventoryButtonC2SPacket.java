package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record InventoryButtonC2SPacket(int entityId, String actionId) implements CustomPacketPayload {
    public static final Type<InventoryButtonC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "inventorybutton"));
    public static final StreamCodec<ByteBuf, InventoryButtonC2SPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> { buf.writeVarInt(entityId); buf.writeUtf(actionId); },
        buf -> new InventoryButtonC2SPacket(buf.readVarInt(), buf.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Server-side handling for InventoryButtonC2SPacket
        });
    }
}
