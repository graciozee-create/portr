package com.sandymandy.pleasurehorizons.settlement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.UUIDUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Settlement {
    private final UUID id;
    private final UUID owner;
    private final String name;
    private final BlockPos corePos;
    private final List<UUID> members = new ArrayList<>();
    private final List<BlockPos> buildingIds = new ArrayList<>();
    private SettlementResourceData data = new SettlementResourceData();

    public static final Codec<Settlement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(Settlement::getId),
            UUIDUtil.CODEC.fieldOf("owner").forGetter(Settlement::getOwner),
            Codec.STRING.fieldOf("name").forGetter(Settlement::getName),
            BlockPos.CODEC.fieldOf("corePos").forGetter(Settlement::getCorePos)
    ).apply(instance, (id, owner, name, pos) -> new Settlement(id, owner, name, pos)));

    public static final StreamCodec<RegistryFriendlyByteBuf, Settlement> PACKET_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, Settlement::getId,
            UUIDUtil.STREAM_CODEC, Settlement::getOwner,
            ByteBufCodecs.STRING_UTF8, Settlement::getName,
            BlockPos.STREAM_CODEC, Settlement::getCorePos,
            Settlement::new
    );

    public Settlement(UUID id, UUID owner, String name, BlockPos corePos) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.corePos = corePos;
    }

    public UUID getId() { return id; }
    public UUID getOwner() { return owner; }
    public String getName() { return name; }
    public BlockPos getCorePos() { return corePos; }
    public List<UUID> getMembers() { return members; }
    public List<BlockPos> getBuildingIds() { return buildingIds; }
    public SettlementResourceData getData() { return data; }

    public void addMember(com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI girl) {
        if (girl != null) members.add(girl.getUUID());
    }

    public void removeMember(com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI girl) {
        if (girl != null) members.remove(girl.getUUID());
    }
}
