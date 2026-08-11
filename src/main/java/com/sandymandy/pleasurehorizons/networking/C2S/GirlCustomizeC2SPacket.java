package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GirlCustomizeC2SPacket(int entityId, float breastSize, float breastOffset, boolean canGetImpregnated) implements CustomPacketPayload {
    public static final Type<GirlCustomizeC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "girlcustomize"));
    public static final StreamCodec<ByteBuf, GirlCustomizeC2SPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> { buf.writeVarInt(entityId); buf.writeFloat(breastSize); buf.writeFloat(breastOffset); buf.writeBoolean(canGetImpregnated); },
        buf -> new GirlCustomizeC2SPacket(buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Server-side handling for GirlCustomizeC2SPacket
        });
    }
}
