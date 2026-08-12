package com.sandymandy.pleasurehorizons.entity.girls;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class MikaEntity extends SettlementGirlEntityAI {
    public MikaEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createDefaultAttributes();
    }

    @Override
    public Item isAttractedTo() {
        return Items.POPPY;
    }

    @Override
    public String getGirlID() {
        return "mika";
    }

    @Override
    public boolean hasHugAnimation() {
        return true;
    }

    @Override
    public boolean hasCarryAnimation() {
        return true;
    }

    @Override
    public String getCarryAnimation() {
        return "carry_slow1";
    }

    @Override
    public int getSizeGUI() {
        return 20;
    }

    @Override
    public float getYAxisGUI() {
        return 0.0625F;
    }
}
