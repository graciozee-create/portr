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
 * Allie - a companion from the original Jenny Mod, ported to our architecture.
 * She uses the same AI/goals as other girls (harvest, chop, cook, guard, etc.)
 * but has her own model, textures, and animations from the Jenny Mod assets.
 */
public class AllieEntity extends SettlementGirlEntityAI {
    public AllieEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createDefaultAttributes();
    }

    @Override
    public Item isAttractedTo() {
        // Allie's lamp is her special item; for taming she likes golden apples
        return Items.GOLDEN_APPLE;
    }

    @Override
    public String getGirlID() {
        return "allie";
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
    public List<Component> giftRepliesLike() {
        return List.of(
                Component.translatable("chat.pleasurehorizons.allie.gift_like.1"),
                Component.translatable("chat.pleasurehorizons.allie.gift_like.2"),
                Component.translatable("chat.pleasurehorizons.allie.gift_like.3")
        );
    }

    @Override
    public List<Component> giftRepliesLove() {
        return List.of(
                Component.translatable("chat.pleasurehorizons.allie.gift_love.1"),
                Component.translatable("chat.pleasurehorizons.allie.gift_love.2"),
                Component.translatable("chat.pleasurehorizons.allie.gift_love.3")
        );
    }

    @Override
    public List<Scene> getScenes() {
        // Scenes will be added later once animations are verified in-game.
        // The model has 142 bones including steve (partner rig), so partner
        // scenes should work once animation IDs are mapped.
        return List.of();
    }
}
