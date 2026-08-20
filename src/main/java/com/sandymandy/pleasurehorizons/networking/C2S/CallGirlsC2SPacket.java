package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * One-button "call": teleports every loaded, owned girl to the sender so they can always come
 * defend the player, no matter how far away they were left.
 */
public record CallGirlsC2SPacket() implements CustomPacketPayload {
    public static final Type<CallGirlsC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "call_girls"));

    public static final StreamCodec<ByteBuf, CallGirlsC2SPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> {}, buf -> new CallGirlsC2SPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            int called = TameableGirlEntity.callOwnedGirlsTo(player, null);
            player.displayClientMessage(Component.translatable(
                    called > 0
                            ? "msg.pleasurehorizons.girlsCalled"
                            : "commands.pleasurehorizons.girls.no_girls",
                    called), true);
        });
    }
}
