package com.sandymandy.pleasurehorizons.settlement;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Client-visible copy of a {@link Settlement}.
 *
 * <p>The Fabric original relied on {@code ExtendedScreenHandlerFactory<Settlement>} to ship the
 * whole settlement object to the client. NeoForge menus only get a {@code RegistryFriendlyByteBuf},
 * and {@link Settlement} is a mutable server-side object, so the hub screen is fed this immutable
 * snapshot instead. Without it the client menu was constructed with {@code null} and the screen
 * could not display a single value.</p>
 */
public record SettlementSnapshot(
        String name,
        BlockPos corePos,
        int memberCount,
        int buildingCount,
        SettlementResourceData resources
) {
    public static final SettlementSnapshot EMPTY =
            new SettlementSnapshot("", BlockPos.ZERO, 0, 0, SettlementResourceData.DEFAULT);

    public static final StreamCodec<RegistryFriendlyByteBuf, SettlementSnapshot> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, SettlementSnapshot::name,
                    BlockPos.STREAM_CODEC, SettlementSnapshot::corePos,
                    ByteBufCodecs.VAR_INT, SettlementSnapshot::memberCount,
                    ByteBufCodecs.VAR_INT, SettlementSnapshot::buildingCount,
                    SettlementResourceData.PACKET_CODEC, SettlementSnapshot::resources,
                    SettlementSnapshot::new
            );

    public static SettlementSnapshot of(Settlement settlement) {
        if (settlement == null) {
            return EMPTY;
        }
        return new SettlementSnapshot(
                settlement.getName(),
                settlement.getCorePos(),
                settlement.getMembers().size(),
                settlement.getBuildingIds().size(),
                settlement.getData() == null ? SettlementResourceData.DEFAULT : settlement.getData()
        );
    }
}
