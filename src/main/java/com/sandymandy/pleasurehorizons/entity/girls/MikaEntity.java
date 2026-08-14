package com.sandymandy.pleasurehorizons.entity.girls;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

public class MikaEntity extends SettlementGirlEntityAI {
    public MikaEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createDefaultAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
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
    public int getSizeGUI() {
        return 25;
    }

    @Override
    public float getYAxisGUI() {
        return 0.0625F;
    }

    @Override
    public float getWeaponBoneXRotation() {
        return -80.0F;
    }

    @Override
    public List<Component> giftRepliesLike() {
        return List.of(
                Component.translatable("chat.pleasurehorizons.mika.gift_like.1"),
                Component.translatable("chat.pleasurehorizons.mika.gift_like.2"),
                Component.translatable("chat.pleasurehorizons.mika.gift_like.3")
        );
    }

    @Override
    public List<Component> giftRepliesLove() {
        return List.of(
                Component.translatable("chat.pleasurehorizons.mika.gift_love.1"),
                Component.translatable("chat.pleasurehorizons.mika.gift_love.2"),
                Component.translatable("chat.pleasurehorizons.mika.gift_love.3")
        );
    }

    @Override
    public List<Scene> getScenes() {
        return List.of(
                Scene.onPlayer("Face fuck", 6,
                        List.of("carry_intro"),
                        List.of("carry_slow1"),
                        List.of("carry_fast"),
                        "carry_cum", 2.5f, false, false, false),

                Scene.onBed("Missionary", 8,
                        List.of("missionary_intro"),
                        List.of("missionary_slow"),
                        List.of("missionary_fast"),
                        "missionary_cum", 3f, true, false, true,
                        0.5f, "sit_down", "sit_down_idle"),

                Scene.onBed("Cowgirl", 10,
                        List.of("cowgirl_intro"),
                        List.of("cowgirl_slow"),
                        List.of("cowgirl_fast"),
                        "cowgirl_cum", 3f, true, false, true,
                        0.5f, "sit_down", "sit_down_idle")
        );
    }
}
