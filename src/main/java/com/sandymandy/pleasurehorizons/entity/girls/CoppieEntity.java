package com.sandymandy.pleasurehorizons.entity.girls;

import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.world.World;

import java.util.List;

public class CoppieEntity extends SettlementGirlEntityAI {

    public CoppieEntity(EntityType<? extends SettlementGirlEntityAI> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public Item isAttractedTo() {
        return Items.COPPER_INGOT;
    }

    @Override
    public String getGirlID() {
        return "coppie";
    }

    @Override
    public int getSizeGUI(){return 35;}

    @Override
    public List<Scene> getScenes() {
        return List.of(
                Scene.onBed("Anal",
                        6,
                        List.of("anal_intro"),
                        List.of("anal_slow"),
                        List.of("anal_fast"),
                        "anal_cum",
                        6f,
                        true,
                        false,
                        false,
                        0f,
                        "anal_lay_on_bed",
                        "anal_bed_idle"),

                Scene.onBed("Doggy",
                        8,
                        List.of("prone_doggy_intro", "prone_doggy_insert"),
                        List.of("prone_doggy_slow"),
                        List.of("prone_doggy_hard1","prone_doggy_hard2","prone_doggy_hard3"),
                        "prone_doggy_cum",
                        6f,
                        true,
                        true,
                        true,
                        1f,
                        "sit_down",
                        "sit_down_idle")
        );
    }

    @Override
    public List<String> giftRepliesLike() {
        return List.of("This golem accepts this… thing.", "This golem will accept the gift accordingly.", "Why are you giving this golem gifts, master?");
    }

    @Override
    public List<String> giftRepliesLove() {
        return List.of("This golem… no… I appreciate this, master.", "This really means alot to me master.", "Are you sure you don’t want to use my sexual functions, master?");
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return GirlEntity.createDefaultAttributes()
                .add(EntityAttributes.MAX_HEALTH, 15)
                .add(EntityAttributes.MOVEMENT_SPEED, .20)
                .add(EntityAttributes.ATTACK_DAMAGE, 2);
    }
}
