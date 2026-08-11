package com.sandymandy.pleasurehorizons.settlement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SettlementResourceData(
        float morale,
        int food,
        int materials,
        int settlementTokens
) {
    public static final SettlementResourceData DEFAULT = new SettlementResourceData(1.0f, 100, 0, 0);

    public static final Codec<SettlementResourceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("morale").orElse(1.0f).forGetter(SettlementResourceData::morale),
            Codec.INT.fieldOf("food").orElse(100).forGetter(SettlementResourceData::food),
            Codec.INT.fieldOf("materials").orElse(0).forGetter(SettlementResourceData::materials),
            Codec.INT.fieldOf("settlement_tokens").orElse(0).forGetter(SettlementResourceData::settlementTokens)
    ).apply(instance, SettlementResourceData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SettlementResourceData> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SettlementResourceData::morale,
            ByteBufCodecs.VAR_INT, SettlementResourceData::food,
            ByteBufCodecs.VAR_INT, SettlementResourceData::materials,
            ByteBufCodecs.VAR_INT, SettlementResourceData::settlementTokens,
            SettlementResourceData::new
    );

    public SettlementResourceData() {
        this(1.0f, 100, 0, 0);
    }

    public SettlementResourceData withMorale(float morale) {
        return new SettlementResourceData(morale, food, materials, settlementTokens);
    }

    public SettlementResourceData withFood(int food) {
        return new SettlementResourceData(morale, food, materials, settlementTokens);
    }

    public SettlementResourceData withMaterials(int materials) {
        return new SettlementResourceData(morale, food, materials, settlementTokens);
    }

    public SettlementResourceData withSettlementTokens(int population) {
        return new SettlementResourceData(morale, food, materials, population);
    }
}
