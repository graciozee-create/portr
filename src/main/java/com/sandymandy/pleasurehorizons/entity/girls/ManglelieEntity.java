package com.sandymandy.pleasurehorizons.entity.girls;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

public class ManglelieEntity extends SettlementGirlEntityAI {
    public ManglelieEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createDefaultAttributes();
    }

    @Override
    public Item isAttractedTo() {
        return Items.GHAST_TEAR;
    }

    @Override
    public String getGirlID() {
        return "manglelie";
    }

    @Override
    public int getSizeGUI() {
        return 29;
    }

    @Override
    public float getYAxisGUI() {
        return 0.0525F;
    }

    @Override
    public boolean hasStripAnim() {
        return false; // Manglelie's animation set has no strip animation
    }

    @Override
    public List<Scene> getScenes() {
        return List.of(
                Scene.onBed("Bed", 0,
                        List.of("bed_slow"),
                        List.of("bed_slow"),
                        List.of("bed_slow"),
                        "bed_slow", 3f, true, true, true,
                        0f, "bed_slow", "bed_slow"),
                Scene.stationary("Double Holding", 2, "double_holding", 2, true, true)
        );
    }
}
