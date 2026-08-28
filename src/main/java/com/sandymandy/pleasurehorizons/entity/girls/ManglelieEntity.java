package com.sandymandy.pleasurehorizons.entity.girls;

import com.sandymandy.pleasurehorizons.entity.base.tamable.SettlementGirlEntityAI;
import com.sandymandy.pleasurehorizons.util.variables.Scene;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Manglelie companion.
 *
 * <p>Besides her own scenes she can take part in the Galath "dark ritual" (the original
 * threesome): Galath starts it, and both girls lock in place facing each other around the
 * owner. The state here mirrors {@link GalathEntity} only as far as the ritual requires.</p>
 */
public class ManglelieEntity extends SettlementGirlEntityAI {

    private static final int THREESOME_DISTANCE_SQ = 36;           // owner must stay within 6 blocks

    // Mirrored threesome state (set from GalathEntity).
    public boolean isInThreesome = false;
    public String threesomePartnerUUID = "";
    public int threesomeTicks = 0;

    public ManglelieEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        // Hide extra bones that render unwanted visual elements (energy balls, offhand items).
        // The "coin" floating in front of Manglelie is likely energyBallL/R or offhand bone.
        if (!level.isClientSide()) {
            this.setBoneVisibility("energyBallL", false);
            this.setBoneVisibility("energyBallR", false);
            this.setBoneVisibility("offhand", false);
            this.setBoneVisibility("weapon", false);
            this.setBoneVisibility("customHandL", false);
            this.setBoneVisibility("customHandR", false);
            this.setBoneVisibility("blocks", false);
        }
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
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        this.tickThreesome();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && this.isInThreesome) {
            GalathEntity galath = this.findGalathByUUID(this.threesomePartnerUUID);
            this.clearThreesomeSelf();
            if (galath != null) galath.clearThreesomeSelf();
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isInThreesome || this.isFrozenInPlace()) {
            this.setDeltaMovement(0.0D, 0.0D, 0.0D);
            return;
        }
        super.travel(travelVector);
    }

    private void tickThreesome() {
        if (!this.isInThreesome || this.threesomePartnerUUID.isEmpty()) return;
        this.threesomeTicks++;

        GalathEntity galath = this.findGalathByUUID(this.threesomePartnerUUID);
        Player owner = this.getOwner() instanceof Player player ? player : null;
        if (galath == null || !galath.isAlive() || !galath.isInThreesome
                || owner == null || owner.distanceToSqr(this) > THREESOME_DISTANCE_SQ) {
            this.clearThreesomeSelf();
            if (galath != null) galath.clearThreesomeSelf();
            return;
        }

        this.setFreeze(true);
        if (this.threesomeTicks % 20 == 0) this.lookAt(galath, 360.0F, 360.0F);
        if (this.threesomeTicks % 10 == 0 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.WITCH,
                    this.getX(), this.getY() + 1.2D, this.getZ(),
                    3, 0.3D, 0.3D, 0.3D, 0.02D);
        }
    }

    /** Clears this Manglelie's side of the ritual (also called by Galath). */
    public void clearThreesomeSelf() {
        this.isInThreesome = false;
        this.threesomePartnerUUID = "";
        this.threesomeTicks = 0;
        this.setFreeze(false);
    }

    @Nullable
    private GalathEntity findGalathByUUID(String uuid) {
        if (uuid == null || uuid.isEmpty()) return null;
        UUID target;
        try {
            target = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
        for (GalathEntity galath : this.level().getEntitiesOfClass(GalathEntity.class,
                this.getBoundingBox().inflate(50.0D))) {
            if (galath.getUUID().equals(target) && galath.isAlive()) {
                return galath;
            }
        }
        return null;
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
