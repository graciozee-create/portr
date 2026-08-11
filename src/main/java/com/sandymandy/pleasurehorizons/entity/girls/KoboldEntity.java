package com.sandymandy.pleasurehorizons.entity.girls;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class KoboldEntity extends SettlementGirlEntityAI {
    public KoboldEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createDefaultAttributes();
    }

    @Override
    public Item isAttractedTo() {
        return Items.RAW_IRON;
    }

    @Override
    public String getGirlID() {
        return "kobold";
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
