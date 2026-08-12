package com.sandymandy.pleasurehorizons.entity.girls;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

public class MomoEntity extends SettlementGirlEntityAI {
    public MomoEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
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
        return "momo";
    }

    @Override
    public int getSizeGUI() {
        return 35;
    }

    @Override
    public float getYAxisGUI() {
        return 0.0625F;
    }

    @Override
    public List<Scene> getScenes() {
        return List.of(
                Scene.onBed("Anal", 6,
                        List.of("anal_intro"),
                        List.of("anal_slow"),
                        List.of("anal_fast"),
                        "anal_cum", 6f, true, false, false,
                        0f, "anal_lay_on_bed", "anal_bed_idle"),

                Scene.onBed("Doggy", 8,
                        List.of("prone_doggy_intro", "prone_doggy_insert"),
                        List.of("prone_doggy_slow"),
                        List.of("prone_doggy_hard1", "prone_doggy_hard2", "prone_doggy_hard3"),
                        "prone_doggy_cum", 6f, true, true, true,
                        1f, "sit_down", "sit_down_idle")
        );
    }
}
