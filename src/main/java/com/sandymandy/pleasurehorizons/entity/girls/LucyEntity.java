package com.sandymandy.pleasurehorizons.entity.girls;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.world.World;

import java.util.List;

public class LucyEntity extends SettlementGirlEntityAI {

    public LucyEntity(EntityType<? extends SettlementGirlEntityAI> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public Item isAttractedTo() {
        return Items.ALLIUM;
    }

    @Override
    public String getGirlID() {
        return "lucy";
    }

    @Override
    public int getSizeGUI(){return 29;}

    @Override
    public float getYAxisGUI(){return 0.0525F;}

    @Override
    public List<Scene> getScenes() {
        return List.of(

                Scene.stationary("Masturbation",
                        4,
                        "masturbating",
                        4,
                        true,
                        true),

                Scene.onPlayer("Paizuri",
                        6,
                        List.of("paizuri_intro"),
                        List.of("paizuri_slow"),
                        List.of("paizuri_fast"),
                        "paizuri_cum",
                        4,
                        true,
                        false,
                        false),


                Scene.onPlayer("Blow Job",
                        8,
                        List.of("blowjob_intro"),
                        List.of("blowjob_slow"),
                        List.of("blowjob_fast"),
                        "blowjob_cum",
                        2.5f,
                        false,
                        false,
                        false),

                Scene.onBed("Doggy",
                        10,
                        List.of("doggy_intro"),
                        List.of("doggy_slow"),
                        List.of("doggy_fast1", "doggy_fast2"),
                        "doggy_cum",
                        4.5f,
                        true,
                        true,
                        true,
                        0f,
                        "doggy_lay_on_bed",
                        "doggy_bed_idle")
                );
    }

    @Override
    public List<String> giftRepliesLike() {
        return List.of("Wow, for me? Thanks!", "An allium? That’s so nice of you…!", "Ahah, this is great!");
    }

    @Override
    public List<String> giftRepliesLove() {
        return List.of("Oh, another one? Well, you’re the real gift here~.", "Babe, you’re too nice. And hot~.", "Stop giving me gifts and just fuck me already~!");
    }
}
