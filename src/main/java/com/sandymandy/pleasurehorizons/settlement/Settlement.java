package com.sandymandy.pleasurehorizons.settlement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import com.sandymandy.pleasurehorizons.settlement.building.SettlementBuilding;
import com.sandymandy.pleasurehorizons.util.managers.SettlementBuildingManager;
import com.sandymandy.pleasurehorizons.util.managers.SettlementManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Persistent state for one settlement. Mutations are routed through methods that mark its manager dirty. */
public class Settlement {
    private final UUID id;
    private final UUID owner;
    private final String name;
    private final BlockPos corePos;
    private final List<UUID> members = new ArrayList<>();
    private final List<BlockPos> buildingIds = new ArrayList<>();
    private SettlementResourceData data = SettlementResourceData.DEFAULT;
    @Nullable
    private transient SettlementManager manager;

    public static final Codec<Settlement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(Settlement::getId),
            UUIDUtil.CODEC.fieldOf("owner").forGetter(Settlement::getOwner),
            Codec.STRING.fieldOf("name").forGetter(Settlement::getName),
            BlockPos.CODEC.fieldOf("core_pos").forGetter(Settlement::getCorePos),
            UUIDUtil.CODEC.listOf().optionalFieldOf("members", List.of()).forGetter(Settlement::getMembers),
            BlockPos.CODEC.listOf().optionalFieldOf("building_ids", List.of()).forGetter(Settlement::getBuildingIds),
            SettlementResourceData.CODEC.optionalFieldOf("resources", SettlementResourceData.DEFAULT)
                    .forGetter(Settlement::getData)
    ).apply(instance, (id, owner, name, corePos, members, buildingIds, resources) -> {
        Settlement settlement = new Settlement(id, owner, name, corePos);
        settlement.members.addAll(members);
        settlement.buildingIds.addAll(buildingIds);
        settlement.data = resources;
        return settlement;
    }));

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
    public List<UUID> getMembers() { return List.copyOf(members); }
    public List<BlockPos> getBuildingIds() { return List.copyOf(buildingIds); }
    public SettlementResourceData getData() { return data; }

    public boolean hasMember(UUID girlId) {
        return members.contains(girlId);
    }

    public void addMember(SettlementGirlEntityAI girl) {
        if (girl == null) return;
        boolean added = false;
        if (!members.contains(girl.getUUID())) {
            members.add(girl.getUUID());
            added = true;
        }
        girl.setSettlement(this);
        if (added) {
            markDirty();
        }
    }

    public void removeMember(SettlementGirlEntityAI girl) {
        if (girl == null) return;
        boolean removed = members.remove(girl.getUUID());
        if (this.id.equals(girl.getSettlementId())) {
            girl.setSettlement(null);
        }
        if (removed) {
            markDirty();
        }
    }

    /** Clears this settlement UUID from every currently loaded girl before the settlement is deleted. */
    public void invalidateLoadedMembers(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof SettlementGirlEntityAI girl && id.equals(girl.getSettlementId())) {
                    girl.setSettlement(null);
                }
            }
        }
        if (!members.isEmpty()) {
            members.clear();
            markDirty();
        }
    }

    public void addBuilding(BlockPos doorPos, SettlementBuilding building, ServerLevel level) {
        if (!buildingIds.contains(doorPos)) {
            buildingIds.add(doorPos.immutable());
        }
        SettlementBuildingManager.get(level).addBuilding(building);
        markDirty();
    }

    public void removeBuilding(BlockPos doorPos, ServerLevel level) {
        buildingIds.remove(doorPos);
        SettlementBuildingManager.get(level).removeBuilding(doorPos);
        markDirty();
    }

    public void setMorale(float morale) {
        if (!Float.isFinite(morale)) return;
        data = data.withMorale(Math.max(0.0F, Math.min(1.0F, morale)));
        markDirty();
    }

    public void addResources(int amount) {
        data = data.withMaterials(Math.max(0, data.materials() + amount));
        markDirty();
    }

    public void attachManager(SettlementManager manager) {
        this.manager = manager;
    }

    private void markDirty() {
        if (manager != null) {
            manager.setDirty();
        }
    }
}
