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

/**
 * Galath - a powerful boss-tier companion from the Jenny Mod.
 * Has higher HP and damage than regular girls. Tamed with a Netherite Ingot.
 */
public class GalathEntity extends SettlementGirlEntityAI {
    public GalathEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        // Hide accessory bones that render unwanted visual elements.
        // The "coin" bone renders a floating coin in front of Galath by default.
        if (!level.isClientSide()) {
            this.setBoneVisibility("coin", false);
            this.setBoneVisibility("energyBallL", false);
            this.setBoneVisibility("energyBallR", false);
            this.setBoneVisibility("offhand", false);
            this.setBoneVisibility("weapon", false);
            this.setBoneVisibility("weaponStart", false);
            this.setBoneVisibility("weaponEnd", false);
            this.setBoneVisibility("customHandL", false);
            this.setBoneVisibility("customHandR", false);
            this.setBoneVisibility("customHead", false);
            this.setBoneVisibility("customShoeL", false);
            this.setBoneVisibility("customShoeR", false);
            this.setBoneVisibility("blocks", false);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createDefaultAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D);
    }

    @Override
    public Item isAttractedTo() {
        return Items.NETHERITE_INGOT;
    }

    @Override
    public String getGirlID() {
        return "galath";
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
                Scene.onBed("Bed", 0,
                        List.of("bed_back"),
                        List.of("bed_fast", "bed_fast1", "bed_fast2"),
                        List.of("bed_fast"),
                        "bed_cum", 4f, true, true, true,
                        0f, "bed_back", "bed_back")
        );
    }
}
