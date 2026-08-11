package com.sandymandy.pleasurehorizons.entity.base.tamable;

import com.sandymandy.pleasurehorizons.settlement.Settlement;
import com.sandymandy.pleasurehorizons.settlement.SettlementMember;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class SettlementGirlEntityAI extends TameableGirlEntity implements SettlementMember {
    @Nullable
    private UUID settlementId;

    protected SettlementGirlEntityAI(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
    }

    @Nullable
    public UUID getSettlementId() {
        return settlementId;
    }

    @Nullable
    @Override
    public Settlement getSettlement() {
        return null;
    }

    @Override
    public void setSettlement(Settlement settlement) {}

    @Override
    public boolean hasSettlement() {
        return false;
    }
}
