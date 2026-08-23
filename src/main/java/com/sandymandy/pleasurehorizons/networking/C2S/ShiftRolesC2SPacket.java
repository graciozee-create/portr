package com.sandymandy.pleasurehorizons.networking.C2S;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.tamable.TameableGirlEntity;
import com.sandymandy.pleasurehorizons.util.variables.GirlRole;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

/**
 * One-button "shift switch": advances every tamed girl owned by the sender to the next role.
 *
 * <p>All owned girls move to the same next shift (the role after the first girl's current one),
 * so a mixed setup snaps back into a uniform worker/guard/cook shift. Only loaded girls are
 * affected, exactly like any other AI change.</p>
 */
public record ShiftRolesC2SPacket() implements CustomPacketPayload {
    public static final Type<ShiftRolesC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "shift_roles"));

    public static final StreamCodec<ByteBuf, ShiftRolesC2SPacket> STREAM_CODEC =
            StreamCodec.of((buf, packet) -> {}, buf -> new ShiftRolesC2SPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            List<TameableGirlEntity> girls = player.level().getEntitiesOfClass(
                    TameableGirlEntity.class,
                    new AABB(player.blockPosition()).inflate(128.0D),
                    girl -> girl.isTamed() && girl.isOwner(player));

            if (girls.isEmpty()) return;

            GirlRole next = girls.get(0).getRole().next();
            for (TameableGirlEntity girl : girls) {
                girl.setRole(next);
            }

            player.displayClientMessage(Component.translatable(
                    "msg.pleasurehorizons.shiftApplied",
                    Component.translatable("role.pleasurehorizons." + next.id())), true);
        });
    }
}
