package com.sandymandy.pleasurehorizons.util.variables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamCodecs;

import java.util.ArrayList;
import java.util.List;

public class Scene {

    private final String displayName;
    private final int requiredRelationshipLevel;
    private final SceneOptions options;
    private final SceneAnimations animations;
    private final SceneType sceneType;

    public static final Scene EMPTY = new Scene("", 0, SceneOptions.EMPTY, SceneAnimations.EMPTY, SceneType.ON_PLAYER);

    public static final PacketCodec<RegistryByteBuf, Scene> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, Scene::displayName,
            PacketCodecs.VAR_INT, Scene::requiredRelationshipLevel,
            SceneOptions.PACKET_CODEC, Scene::options,
            SceneAnimations.PACKET_CODEC, Scene::animations,
            SceneType.PACKET_CODEC, Scene::sceneType,
            Scene::new
    );

    public static final Codec<Scene> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("displayName").forGetter(Scene::displayName),
            Codec.INT.fieldOf("requiredRelationshipLevel").forGetter(Scene::requiredRelationshipLevel),
            SceneOptions.CODEC.fieldOf("options").forGetter(Scene::options),
            SceneAnimations.CODEC.fieldOf("animations").forGetter(Scene::animations),
            SceneType.CODEC.fieldOf("sceneType").forGetter(Scene::sceneType)

    ).apply(instance, Scene::new));

    private Scene(
            String displayName,
            int requiredRelationshipLevel,
            SceneOptions options,
            SceneAnimations animations,
            SceneType sceneType
            ) {
        this.displayName = displayName;
        this.requiredRelationshipLevel = requiredRelationshipLevel;
        this.options = options;
        this.animations = animations;
        this.sceneType = sceneType;
    }


    public final String displayName() {return this.displayName;}
    public final int requiredRelationshipLevel() {return this.requiredRelationshipLevel;}
    public final List<String> introAnim() {return this.animations.introAnim;}
    public final List<String> slowAnim() {return this.animations.slowAnim;}
    public final List<String> fastAnim() {return this.animations.fastAnim;}
    public final String cumAnim() {return this.animations.cumAnim;}
    public final float cumThreshold() {return this.options.cumThreshold;}
    public final boolean needsToStrip() {return this.options.needsToStrip;}
    public final boolean hidePlayer() {return this.options.hidePlayer();}
    public final SceneType sceneType() {return this.sceneType;}
    public final boolean useKeyFrameEvents() {return this.options.useKeyFrameEvents;}
    public final boolean countTowardsImpregnation() {return this.options.countTowardsImpregnation;}
    public final float bedAlignmentOffset() {return this.options.bedAlignmentOffset;}
    public final String layOnBed() {return this.animations.layOnBed;}
    public final String bedIdle() {return this.animations.bedIdle;}
    private SceneAnimations animations() {return this.animations;}
    private SceneOptions options() {return this.options;}
    public List<String> stationaryIntroAnim() { return animations.stationaryIntroAnim; }
    public String stationaryLoopAnim() { return animations.stationaryLoopAnim; }
    public int amountOfLoops() { return options.amountOfLoops; }


    public static Scene onBed(
            String name,
            int requiredRelationshipLevel,
            List<String> introAnim,
            List<String> slowAnim,
            List<String> fastAnim,
            String cumAnim,
            float cumThreshold,
            boolean needsToStrip,
            boolean useKeyFrameEvents,
            boolean countTowardsImpregnation,
            float bedAlignmentOffset,
            String layOnBed,
            String bedIdle
    ) {
        return new Scene(
                name, requiredRelationshipLevel,
                SceneOptions.of(cumThreshold, needsToStrip, useKeyFrameEvents, countTowardsImpregnation, bedAlignmentOffset),
                SceneAnimations.of(introAnim, slowAnim, fastAnim, cumAnim, layOnBed, bedIdle),
                SceneType.ON_BED
        );
    }


    public static Scene onPlayer(
            String name,
            int requiredRelationshipLevel,
            List<String> introAnim,
            List<String> slowAnim,
            List<String> fastAnim,
            String cumAnim,
            float cumThreshold,
            boolean needsToStrip,
            boolean useKeyFrameEvents,
            boolean countTowardsImpregnation

    ) {
        return new Scene(
                name, requiredRelationshipLevel,
                SceneOptions.of(cumThreshold, needsToStrip, useKeyFrameEvents, countTowardsImpregnation),
                SceneAnimations.of(introAnim, slowAnim, fastAnim, cumAnim),
                SceneType.ON_PLAYER
        );
    }

    public static Scene stationaryContact(
            String name,
            int requiredRelationshipLevel,
            List<String> introAnim,
            List<String> slowAnim,
            List<String> fastAnim,
            String cumAnim,
            float cumThreshold,
            boolean needsToStrip,
            boolean useKeyFrameEvents,
            boolean countTowardsImpregnation,
            String layDown,
            String idle
    ) {
        return new Scene(
                name, requiredRelationshipLevel,
                SceneOptions.of(cumThreshold, needsToStrip, useKeyFrameEvents, countTowardsImpregnation),
                SceneAnimations.of(introAnim, slowAnim, fastAnim, cumAnim, layDown, idle),
                SceneType.STATIONARY_CONTACT
            );
    }

    public static Scene stationaryIntro(
            String name,
            int requiredRelationshipLevel,
            List<String> stationaryIntroAnim,
            String anim,
            int amountOfLoops,
            boolean needsToStrip,
            boolean hidePlayer
            ) {
        return new Scene(
                name, requiredRelationshipLevel,
                SceneOptions.of(needsToStrip, hidePlayer, amountOfLoops),
                SceneAnimations.of(stationaryIntroAnim, anim),
                SceneType.STATIONARY_INTRO
                );

    }

    public static Scene stationary(
            String name,
            int requiredRelationshipLevel,
            String anim,
            int amountOfLoops,
            boolean needsToStrip,
            boolean hidePlayer
            ) {
        return new Scene(
                name, requiredRelationshipLevel,
                SceneOptions.of(needsToStrip, hidePlayer, amountOfLoops),
                SceneAnimations.of(new ArrayList<>(), anim),
                SceneType.STATIONARY
        );
    }

    public record SceneAnimations(
            List<String> introAnim,
            List<String> slowAnim,
            List<String> fastAnim,
            String cumAnim,
            String layOnBed,
            String bedIdle,
            List<String> stationaryIntroAnim,
            String stationaryLoopAnim
    ) {
        public static final SceneAnimations EMPTY = new SceneAnimations(new ArrayList<>(),new ArrayList<>(),new ArrayList<>(), "", "", "", new ArrayList<>(),"");

        public static final Codec<SceneAnimations> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.listOf().fieldOf("introAnim").forGetter(SceneAnimations::introAnim),
                Codec.STRING.listOf().fieldOf("slowAnim").forGetter(SceneAnimations::slowAnim),
                Codec.STRING.listOf().fieldOf("fastAnim").forGetter(SceneAnimations::fastAnim),
                Codec.STRING.fieldOf("cumAnim").forGetter(SceneAnimations::cumAnim),
                Codec.STRING.fieldOf("layOnBed").forGetter(SceneAnimations::layOnBed),
                Codec.STRING.fieldOf("bedIdle").forGetter(SceneAnimations::bedIdle),
                Codec.STRING.listOf().fieldOf("stationaryIntroAnim").forGetter(SceneAnimations::stationaryIntroAnim),
                Codec.STRING.fieldOf("stationaryLoopAnim").forGetter(SceneAnimations::stationaryLoopAnim)
        ).apply(instance, SceneAnimations::new));

        public static final PacketCodec<RegistryByteBuf, SceneAnimations> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.collection(ArrayList::new, PacketCodecs.STRING), SceneAnimations::introAnim,
                PacketCodecs.collection(ArrayList::new, PacketCodecs.STRING), SceneAnimations::slowAnim,
                PacketCodecs.collection(ArrayList::new, PacketCodecs.STRING), SceneAnimations::fastAnim,
                PacketCodecs.STRING, SceneAnimations::cumAnim,
                PacketCodecs.STRING, SceneAnimations::layOnBed,
                PacketCodecs.STRING, SceneAnimations::bedIdle,
                PacketCodecs.collection(ArrayList::new, PacketCodecs.STRING), SceneAnimations::stationaryIntroAnim,
                PacketCodecs.STRING, SceneAnimations::stationaryLoopAnim,
                SceneAnimations::new
        );

        public static SceneAnimations of(
                List<String> stationaryIntroAnim,
                String stationaryLoopAnim
        ){
            return new SceneAnimations(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), "", "", "", stationaryIntroAnim, stationaryLoopAnim);
        }

        public static SceneAnimations of(
                List<String> introAnim,
                List<String> slowAnim,
                List<String> fastAnim,
                String cumAnim,
                String layOnBed,
                String bedIdle
        ){
            return new SceneAnimations(introAnim, slowAnim, fastAnim, cumAnim, layOnBed, bedIdle, new ArrayList<>(), "");
        }

        public static SceneAnimations of(
                List<String> introAnim,
                List<String> slowAnim,
                List<String> fastAnim,
                String cumAnim
        ){
            return new SceneAnimations(introAnim, slowAnim, fastAnim, cumAnim, "", "", new ArrayList<>(), "");
        }

    }

    public record SceneOptions(
            float cumThreshold,
            boolean needsToStrip,
            boolean useKeyFrameEvents,
            boolean countTowardsImpregnation,
            boolean hidePlayer,
            float bedAlignmentOffset,
            int amountOfLoops
    ) {
        public static final SceneOptions EMPTY = new SceneOptions(0f, false, false, false, false, 0f, 0);

        public static final Codec<SceneOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("cumThreshold").forGetter(SceneOptions::cumThreshold),
                Codec.BOOL.fieldOf("needsToStrip").forGetter(SceneOptions::needsToStrip),
                Codec.BOOL.fieldOf("useKeyFrameEvents").forGetter(SceneOptions::useKeyFrameEvents),
                Codec.BOOL.fieldOf("countTowardsImpregnation").forGetter(SceneOptions::countTowardsImpregnation),
                Codec.BOOL.fieldOf("hidePlayer").forGetter(SceneOptions::hidePlayer),
                Codec.FLOAT.fieldOf("bedAlignmentOffset").forGetter(SceneOptions::bedAlignmentOffset),
                Codec.INT.fieldOf("amountOfLoops").forGetter(SceneOptions::amountOfLoops)
        ).apply(instance, SceneOptions::new));

        public static final PacketCodec<RegistryByteBuf, SceneOptions> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.FLOAT, SceneOptions::cumThreshold,
                PacketCodecs.BOOLEAN, SceneOptions::needsToStrip,
                PacketCodecs.BOOLEAN, SceneOptions::useKeyFrameEvents,
                PacketCodecs.BOOLEAN, SceneOptions::countTowardsImpregnation,
                PacketCodecs.BOOLEAN, SceneOptions::hidePlayer,
                PacketCodecs.FLOAT, SceneOptions::bedAlignmentOffset,
                PacketCodecs.INTEGER, SceneOptions::amountOfLoops,
                SceneOptions::new
        );

        public static SceneOptions of(
                float cumThreshold,
                boolean needsToStrip,
                boolean useKeyFrameEvents,
                boolean countTowardsImpregnation,
                float bedAlignmentOffset,
                int amountOfLoops
        ){
            return new SceneOptions(cumThreshold, needsToStrip, useKeyFrameEvents, countTowardsImpregnation, false,bedAlignmentOffset, amountOfLoops);
        }

        public static SceneOptions of(
                float cumThreshold,
                boolean needsToStrip,
                boolean useKeyFrameEvents,
                boolean countTowardsImpregnation,
                float bedAlignmentOffset
        ){
            return new SceneOptions(cumThreshold, needsToStrip, useKeyFrameEvents, countTowardsImpregnation, false,bedAlignmentOffset, 0);
        }

        public static SceneOptions of(
                float cumThreshold,
                boolean needsToStrip,
                boolean useKeyFrameEvents,
                boolean countTowardsImpregnation
        ){
            return new SceneOptions(cumThreshold, needsToStrip, useKeyFrameEvents, countTowardsImpregnation, false, 0, 0);
        }

        public static SceneOptions of(
                boolean needsToStrip,
                boolean hidePlayer,
                int amountOfLoops
        ){
            return new SceneOptions(0, needsToStrip, false, false, hidePlayer, 0, amountOfLoops);
        }
    }

}
