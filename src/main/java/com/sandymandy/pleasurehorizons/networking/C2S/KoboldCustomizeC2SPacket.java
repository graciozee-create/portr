package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record KoboldCustomizeC2SPacket(int entityId, float bodySize, float breastSize, int primaryColor, int secondaryColor, int irisColor, int topHornType, int bottomHornType) implements CustomPacketPayload {
    public static final Type<KoboldCustomizeC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "koboldcustomize"));
    public static final StreamCodec<ByteBuf, KoboldCustomizeC2SPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> { buf.writeVarInt(entityId); buf.writeFloat(bodySize); buf.writeFloat(breastSize); buf.writeVarInt(primaryColor); buf.writeVarInt(secondaryColor); buf.writeVarInt(irisColor); buf.writeVarInt(topHornType); buf.writeVarInt(bottomHornType); },
        buf -> new KoboldCustomizeC2SPacket(buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Server-side handling for KoboldCustomizeC2SPacket
        });
    }
}
