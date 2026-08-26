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

/**
 * Jenny - the original companion from the Jenny Mod.
 * Tamed with a Diamond.
 */
public class JennyEntity extends SettlementGirlEntityAI {
    public JennyEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createDefaultAttributes();
    }

    @Override
    public Item isAttractedTo() {
        return Items.DIAMOND;
    }

    @Override
    public String getGirlID() {
        return "jenny";
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
    public List<Scene> getScenes() {
        return List.of(
                Scene.onPlayer("Blowjob", 0,
                        List.of("blowjobintro"),
                        List.of("blowjobsuck"),
                        List.of("blowjobthrust"),
                        "blowjobcum", 3f, false, false, false),

                Scene.onBed("Doggy", 2,
                        List.of("doggyintro"),
                        List.of("doggyslow"),
                        List.of("doggythrust"),
                        "doggycum", 4f, true, true, true,
                        0f, "doggy_lay_on_bed", "doggy_bed_idle")
        );
    }
}
