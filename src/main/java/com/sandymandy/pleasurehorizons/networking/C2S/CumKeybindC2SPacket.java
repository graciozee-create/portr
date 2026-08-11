package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CumKeybindC2SPacket(boolean pressed) implements CustomPacketPayload {
    public static final Type<CumKeybindC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "cumkeybind"));
    public static final StreamCodec<ByteBuf, CumKeybindC2SPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> { buf.writeBoolean(pressed); },
        buf -> new CumKeybindC2SPacket(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Server-side handling for CumKeybindC2SPacket
        });
    }
}
