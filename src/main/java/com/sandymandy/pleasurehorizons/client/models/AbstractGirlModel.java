package com.sandymandy.pleasurehorizons.client.models;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.config.ModConfig;
import com.sandymandy.pleasurehorizons.entity.base.GirlSceneEntity;
import com.sandymandy.pleasurehorizons.registries.PleasureHorizonsDataTicketRegistry;
import com.sandymandy.pleasurehorizons.util.rendering.GeoBoneExtension;
import com.sandymandy.pleasurehorizons.util.rendering.JigglePhysics;
import com.sandymandy.pleasurehorizons.util.variables.JiggleBoneConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib.animatable.processing.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class AbstractGirlModel<T extends GirlSceneEntity> extends GeoModel<T> {
    private final Map<Long, Map<String, JigglePhysics>> jiggleMapByEntity = new HashMap<>();
    private final Map<Long, Map<String, Vec3d>> defaultRotationsByEntity = new HashMap<>();
    private final Map<Long, Long> lastUpdateTimeByEntity = new HashMap<>();
    private static final double FIXED_TIMESTEP = 1.0 / 25.0;// 25Hz
    private final Map<Long, Double> timeAccumulator = new HashMap<>();
    private static final List<AbstractGirlModel<?>> MODEL_INSTANCES = new ArrayList<>();

    public AbstractGirlModel() {
        MODEL_INSTANCES.add(this);
    }

    /** Reloads ALL girl models (global wipe). */
    public static void refreshAllModels() {
        for (AbstractGirlModel<?> model : MODEL_INSTANCES) {
            model.refreshAllDefaults();
        }
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        // Check if entity is stripped
        boolean stripped = renderState.getOrDefaultGeckolibData(PleasureHorizonsDataTicketRegistry.IS_STRIPPED, false).booleanValue();
        String girlID = renderState.getOrDefaultGeckolibData(PleasureHorizonsDataTicketRegistry.GIRL_ID, "");


        // Pick the folder based on stripped/dressed state
        String folder = stripped ? "nude/" : "dressed/";

        // Use the model file provided by your getModelFile() method
        String filePath = folder + girlID;

        return Identifier.of(PleasureHorizons.MOD_ID, filePath);
    }


    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        String girlID = renderState.getOrDefaultGeckolibData(PleasureHorizonsDataTicketRegistry.GIRL_ID, "");

        String filePath = "textures/entities/" + girlID + ".png";

        return Identifier.of(PleasureHorizons.MOD_ID, filePath);
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        return Identifier.of(PleasureHorizons.MOD_ID, animatable.getGirlID());
    }


    @Override
    public void setCustomAnimations(AnimationState<T> animationState) {

        GeoBone head = getAnimationProcessor().getBone("head");
        boolean isSceneActive = animationState.getData(PleasureHorizonsDataTicketRegistry.IS_IN_SCENE);
        boolean hasVehicle = animationState.getData(PleasureHorizonsDataTicketRegistry.HAS_VEHICLE);
        boolean isSprinting = animationState.getData(PleasureHorizonsDataTicketRegistry.IS_SPRINTING);

        if (!isSceneActive && !hasVehicle && !isSprinting) {
            this.calculateJigglePhysics(animationState);
        }

        if (head != null && !isSceneActive) {
            float pitch = animationState.getData(DataTickets.ENTITY_PITCH);
            float yaw = animationState.getData(DataTickets.ENTITY_YAW);

            head.setRotX(-pitch * MathHelper.RADIANS_PER_DEGREE);
            head.setRotY(-yaw * MathHelper.RADIANS_PER_DEGREE);
        }

        GeoBone headBone = this.getAnimationProcessor().getBone("Head2");
        if (headBone != null) {
            MinecraftClient client = MinecraftClient.getInstance();

            boolean isFirstPerson = client.options.getPerspective().isFirstPerson();
            boolean isPlayerRider = client.cameraEntity == animationState.renderState().getGeckolibData(PleasureHorizonsDataTicketRegistry.GIRL_FIRST_PASSENGER);

            ((GeoBoneExtension) headBone).setHiddenWithoutHidingChildren(isFirstPerson && isPlayerRider);
        }

        GeoBone boobWindow = this.getAnimationProcessor().getBone("boobWindow");
        if (boobWindow != null) {
            boobWindow.setHidden(ModConfig.INSTANCE.girls.boobWindow);
        }

    }

    private void calculateJigglePhysics(AnimationState<T> animationState) {
        long instanceId = animationState.getData(DataTickets.ANIMATABLE_INSTANCE_ID);
        boolean inGui = MinecraftClient.getInstance().currentScreen != null;

        // Read motion data
        Vec3d velocity = inGui ? Vec3d.ZERO : animationState.getDataOrDefault(DataTickets.VELOCITY, Vec3d.ZERO);
        Vec3d prevVelocity = inGui ? Vec3d.ZERO : animationState.getDataOrDefault(PleasureHorizonsDataTicketRegistry.PREVIOUS_VELOCITY, Vec3d.ZERO);
        float currentYaw = inGui ? 0 : animationState.getDataOrDefault(PleasureHorizonsDataTicketRegistry.YAW, 0f);
        float prevYaw = inGui ? 0 : animationState.getDataOrDefault(PleasureHorizonsDataTicketRegistry.PREVIOUS_YAW, currentYaw);


        // Compute change in yaw
        float yawDelta = currentYaw - prevYaw;
        if (yawDelta > 180) yawDelta -= 360;
        if (yawDelta < -180) yawDelta += 360;

        // Compute motion-based force
        Vec3d deltaVelocity = velocity.subtract(prevVelocity);
        Vec3d inertiaForce = deltaVelocity.multiply(1.2);
        double yawInfluenceX = Math.sin(Math.toRadians(currentYaw)) * yawDelta * 0.05;
        double yawInfluenceZ = Math.cos(Math.toRadians(currentYaw)) * yawDelta * 0.05;
        inertiaForce = inertiaForce.add(yawInfluenceX, 0, yawInfluenceZ);

        // Initialize maps
        jiggleMapByEntity.putIfAbsent(instanceId, new HashMap<>());
        defaultRotationsByEntity.putIfAbsent(instanceId, new HashMap<>());
        timeAccumulator.putIfAbsent(instanceId, 0.0);

        Map<String, JigglePhysics> jiggleMap = jiggleMapByEntity.get(instanceId);
        Map<String, Vec3d> defaultRotations = defaultRotationsByEntity.get(instanceId);

        // Time tracking
        long now = System.nanoTime();
        long lastUpdate = lastUpdateTimeByEntity.getOrDefault(instanceId, now);
        double deltaSec = (now - lastUpdate) / 1_000_000_000.0;
        lastUpdateTimeByEntity.put(instanceId, now);

        // Accumulate and clamp
        double accumulator = timeAccumulator.get(instanceId) + deltaSec;
        accumulator = Math.min(accumulator, FIXED_TIMESTEP * 5); // clamp to avoid runaway

        // Fixed-step updates
        while (accumulator >= FIXED_TIMESTEP) {
            for (JiggleBoneConfig config : JIGGLE_BONES(animationState)) {
                GeoBone bone = getAnimationProcessor().getBone(config.boneName());
                if (bone == null) continue;

                defaultRotations.putIfAbsent(config.boneName(),
                        new Vec3d(bone.getRotX(), bone.getRotY(), bone.getRotZ()));

                jiggleMap.putIfAbsent(config.boneName(),
                        new JigglePhysics(config.stiffness(), config.damping()));

                jiggleMap.get(config.boneName()).update(inertiaForce);
            }
            accumulator -= FIXED_TIMESTEP;
            if (Double.isNaN(accumulator) || accumulator > 1.0) accumulator = 0.0;
        }

        // Save accumulator for interpolation
        timeAccumulator.put(instanceId, accumulator);
        double alpha = accumulator / FIXED_TIMESTEP;

        // Apply interpolated displacements
        for (JiggleBoneConfig config : JIGGLE_BONES(animationState)) {
            GeoBone bone = getAnimationProcessor().getBone(config.boneName());
            if (bone == null) continue;

            Vec3d defaultRot = defaultRotations.get(config.boneName());
            JigglePhysics jiggle = jiggleMap.get(config.boneName());
            if (defaultRot == null || jiggle == null) continue;

            Vec3d offset = jiggle.getInterpolatedDisplacement(alpha);

            bone.setRotX((float) (defaultRot.x + offset.x));
            bone.setRotY((float) (defaultRot.y + offset.y));
            bone.setRotZ((float) (defaultRot.z + offset.z));
        }
    }

    protected List<JiggleBoneConfig> JIGGLE_BONES(AnimationState<T> animationState) {
        List<JiggleBoneConfig> bones = new ArrayList<>();

        bones.add(new JiggleBoneConfig("cheekL", 0.2, 0.2));
        bones.add(new JiggleBoneConfig("cheekR", 0.2, 0.2));
        bones.add(new JiggleBoneConfig("belly", 0.3, 0.4));

        if (!animationState.getDataOrDefault(PleasureHorizonsDataTicketRegistry.IS_STRIPPED, false)) {
            bones.add(new JiggleBoneConfig("boobs", 0.2, 0.4));
        }
        else {
            bones.add(new JiggleBoneConfig("boobL", 0.2, 0.3));
            bones.add(new JiggleBoneConfig("boobR", 0.2, 0.3));
        }

        return bones;
    }

    public void refreshAllDefaults() {
        this.defaultRotationsByEntity.clear();
        this.jiggleMapByEntity.clear();
        this.timeAccumulator.clear();
        this.lastUpdateTimeByEntity.clear();
    }
}
