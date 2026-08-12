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

public record ThrustKeybindC2SPacket(boolean held) implements CustomPacketPayload {
    public static final Type<ThrustKeybindC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "thrustkeybindc2spacket"));

    public static final StreamCodec<ByteBuf, ThrustKeybindC2SPacket> STREAM_CODEC =
            ByteBufCodecs.BOOL.map(ThrustKeybindC2SPacket::new, ThrustKeybindC2SPacket::held);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Entity vehicle = ctx.player().getVehicle();
            if (vehicle instanceof GirlSceneEntity girl) {
                girl.setThrusting(this.held());
            }
        });
    }
}
