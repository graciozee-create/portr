package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CumKeybindC2SPacket(boolean pressed) implements CustomPacketPayload {
    public static final Type<CumKeybindC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "cumkeybindc2spacket"));

    public static final StreamCodec<ByteBuf, CumKeybindC2SPacket> STREAM_CODEC =
            ByteBufCodecs.BOOL.map(CumKeybindC2SPacket::new, CumKeybindC2SPacket::pressed);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Entity vehicle = ctx.player().getVehicle();
            if (vehicle instanceof GirlSceneEntity girl
                    && this.pressed()
                    && girl.acceptsSceneInputFrom(ctx.player())) {
                girl.tryTriggerCum();
            }
        });
    }
}
