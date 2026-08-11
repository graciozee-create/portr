package com.sandymandy.pleasurehorizons.entity.girls;

import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import com.sandymandy.pleasurehorizons.entity.base.wild.WildGirlEntity;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.List;

public class SlimeEntity extends WildGirlEntity {
    public SlimeEntity(EntityType<? extends WildGirlEntity> entityType, World world) {
        super(entityType, world);
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
    public int getSizeGUI(){return 29;}

    @Override
    public float getYAxisGUI(){return 0.0525F;}

    @Override
    public float getWeaponBoneXRotation() {
        return -100f;
    }

    @Override
    public List<Scene> getScenes() {
        return List.of(
                Scene.onPlayer("Blow Job",
                        4,
                        List.of("blowjob_intro"),
                        List.of("blowjob_slow"),
                        List.of("blowjob_fast"),
                        "blowjob_cum",
                        2.5f,
                        false,
                        false,
                        false),

                Scene.stationaryContact("Doggy",
                        6,
                        List.of("doggy_intro"),
                        List.of("doggy_slow"),
                        List.of("doggy_fast"),
                        "doggy_cum",
                        4.5f,
                        true,
                        false,
                        true,
                        "doggy_lay_on_bed",
                        "doggy_bed_idle")
        );
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (source.isIn(DamageTypeTags.IS_FALL)) {
            return false;
        }
        return super.damage(world, source, amount);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return GirlEntity.createDefaultAttributes()
                .add(EntityAttributes.MAX_HEALTH, 15)
                .add(EntityAttributes.MOVEMENT_SPEED, .20)
                .add(EntityAttributes.ATTACK_DAMAGE, 2);
    }
}
