package com.sandymandy.pleasurehorizons.settlement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import com.sandymandy.pleasurehorizons.settlement.building.BuildingScanner;
import com.sandymandy.pleasurehorizons.settlement.building.BuildingType;
import com.sandymandy.pleasurehorizons.settlement.building.SettlementBuilding;
import com.sandymandy.pleasurehorizons.util.Utils;
import com.sandymandy.pleasurehorizons.util.managers.SettlementBuildingManager;
import com.sandymandy.pleasurehorizons.util.managers.SettlementManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

public class Settlement {
    private final UUID id;
    private final UUID owner;
    private final String name;
    private final BlockPos corePos;
    private SettlementResourceData data;
    private final BuildingScanner scanner = new BuildingScanner(this);
    private final List<UUID> members = new ArrayList<>();
    private final List<BlockPos> buildingIds = new ArrayList<>();
    private SettlementManager manager; // For marking dirty
    // === CODEC ===
    public static final Codec<Settlement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("id").forGetter(Settlement::getId),
            Uuids.CODEC.fieldOf("owner").forGetter(Settlement::getOwner),
            Codec.STRING.fieldOf("name").forGetter(Settlement::getName),
            BlockPos.CODEC.fieldOf("corePos").forGetter(Settlement::getCorePos),
            Codec.list(Uuids.CODEC).fieldOf("members").orElse(List.of()).forGetter(Settlement::getMembers),
            Codec.list(BlockPos.CODEC).fieldOf("buildingIds").orElse(List.of()).forGetter(Settlement::getBuildingIds),
            SettlementResourceData.CODEC.fieldOf("data").forGetter(Settlement::getData)
    ).apply(instance, (id, owner, name, pos, members, bIds, data) -> {
        Settlement s = new Settlement(id, owner, name, pos);
        s.members.addAll(members);
        s.buildingIds.addAll(bIds);
        s.data = data;
        return s;
    }));

    // === PACKET_CODEC ===
    public static final PacketCodec<RegistryByteBuf, Settlement> PACKET_CODEC = new PacketCodec<>() {
        @Override
        public Settlement decode(RegistryByteBuf buf) {
            UUID id = buf.readUuid();
            UUID owner = buf.readUuid();
            String name = buf.readString();
            BlockPos pos = buf.readBlockPos();

            int memberCount = buf.readVarInt();
            List<UUID> members = new ArrayList<>(memberCount);
            for (int i = 0; i < memberCount; i++) members.add(buf.readUuid());

            int buildingCount = buf.readVarInt();
            List<BlockPos> buildings = new ArrayList<>(buildingCount);
            for (int i = 0; i < buildingCount; i++) buildings.add(buf.readBlockPos());

            SettlementResourceData data = SettlementResourceData.PACKET_CODEC.decode(buf);

            Settlement s = new Settlement(id, owner, name, pos);
            s.members.addAll(members);
            s.buildingIds.addAll(buildings);
            s.data = data;

            return s;
        }

        @Override
        public void encode(RegistryByteBuf buf, Settlement settlement) {
            buf.writeUuid(settlement.getId());
            buf.writeUuid(settlement.getOwner());
            buf.writeString(settlement.getName());
            buf.writeBlockPos(settlement.getCorePos());

            buf.writeVarInt(settlement.getMembers().size());
            for (UUID uuid : settlement.getMembers()) buf.writeUuid(uuid);

            buf.writeVarInt(settlement.getBuildingIds().size());
            for (BlockPos pos : settlement.getBuildingIds()) buf.writeBlockPos(pos);

            SettlementResourceData.PACKET_CODEC.encode(buf, settlement.getData());


        }
    };

    public Settlement(UUID id, UUID owner, String name, BlockPos corePos) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.corePos = corePos;
        this.data = SettlementResourceData.DEFAULT;
    }

    public UUID getId() { return id; }
    public UUID getOwner() { return owner; }
    public String getName() { return name; }
    public BlockPos getCorePos() { return corePos; }
    public SettlementResourceData getData() { return data; }

    // === Member handling ===
    public List<UUID> getMembers() {
        return List.copyOf(members);
    }

    public boolean hasMember(UUID girlId) {
        return members.contains(girlId);
    }

    public void addMember(SettlementGirlEntityAI girl) {
        if (!members.contains(girl.getUuid())) {
            members.add(girl.getUuid());
            girl.setSettlement(this);
        }
        markDirty();
    }

    public void removeMember(SettlementGirlEntityAI girl) {
        members.remove(girl.getUuid());
        girl.setSettlement(null);
        markDirty();
    }

    public void setMorale(float morale) {
        data = data.withMorale(morale);
        markDirty();
    }
    public void addResources(int amount) {
        data = data.withMaterials(data.materials() + amount);
        markDirty();
    }

    // === Ticking ===
    public void tick(World world) {
        // morale decay, food consumption, random events, etc.
        // Example:
        if (world.getTime() % 24000 == 0) { // daily tick
            float newMorale = Math.max(0, data.morale() - 0.01f);
            data = data.withMorale(newMorale);
            markDirty();
        }

    }

    public void registerBuilding(World world, BlockPos doorPos, Direction tagFacing, BlockPos tagPos, BuildingType type, PlayerEntity player){
        BlockPos scanFrom = Utils.getBlockBehind(doorPos, tagFacing);
        this.scanner.scanForBuilding(world, scanFrom, doorPos, tagPos, type, player);
    }

    public List<SettlementBuilding> getBuildings(ServerWorld world) {
        SettlementBuildingManager manager = SettlementBuildingManager.get(world);
        return buildingIds.stream()
                .map(manager::getBuilding)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<BlockPos> getBuildingIds() {
        return this.buildingIds;
    }

    public void addBuilding(BlockPos pos, SettlementBuilding building, ServerWorld world) {
        if (!buildingIds.contains(pos)) {
            SettlementBuildingManager.get(world).registerBuildings(building);
            buildingIds.add(pos);
        }
        markDirty();
    }

    public void removeBuilding(BlockPos pos, ServerWorld world) {
        SettlementBuildingManager.get(world).removeBuilding(pos);
        buildingIds.remove(pos);
        markDirty();
    }

    public BuildingScanner getScanner(){
        return this.scanner;
    }

    public void setManager(SettlementManager manager) {
        this.manager = manager;
    }

    private void markDirty() {
        if (this.manager != null) {
            this.manager.markDirty();
        }
    }
}
