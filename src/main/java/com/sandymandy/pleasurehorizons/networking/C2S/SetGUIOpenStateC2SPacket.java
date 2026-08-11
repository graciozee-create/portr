package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetGUIOpenStateC2SPacket(int entityId, boolean data) implements CustomPacketPayload {
    public static final Type<SetGUIOpenStateC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "setguiopenstate"));
    public static final StreamCodec<ByteBuf, SetGUIOpenStateC2SPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> { buf.writeVarInt(entityId); buf.writeBoolean(data); },
        buf -> new SetGUIOpenStateC2SPacket(buf.readVarInt(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Server-side handling for SetGUIOpenStateC2SPacket
        });
    }
}
