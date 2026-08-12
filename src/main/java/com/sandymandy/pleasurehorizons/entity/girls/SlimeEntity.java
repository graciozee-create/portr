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

public class SlimeEntity extends SettlementGirlEntityAI {
    public SlimeEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createDefaultAttributes();
    }

    @Override
    public Item isAttractedTo() {
        return Items.SLIME_BALL;
    }

    @Override
    public String getGirlID() {
        return "slime";
    }

    @Override
    public int getSizeGUI() {
        return 20;
    }

    @Override
    public float getYAxisGUI() {
        return 0.0625F;
    }

    @Override
    public List<Scene> getScenes() {
        return List.of(
                Scene.onPlayer("Blow Job", 4,
                        List.of("blowjob_intro"),
                        List.of("blowjob_slow"),
                        List.of("blowjob_fast"),
                        "blowjob_cum", 2.5f, false, false, false),

                Scene.stationaryContact("Doggy", 6,
                        List.of("doggy_intro"),
                        List.of("doggy_slow"),
                        List.of("doggy_fast"),
                        "doggy_cum", 4.5f, true, false, true,
                        "doggy_lay_on_bed", "doggy_bed_idle")
        );
    }
}
