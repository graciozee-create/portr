package com.sandymandy.pleasurehorizons.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Custom {@link EntityDataSerializer}s used by the mod.
 *
 * <p>On Fabric these were registered through {@code FabricTrackedDataRegistry}. On NeoForge
 * they are ordinary registry entries in {@code NeoForgeRegistries.ENTITY_DATA_SERIALIZERS}.</p>
 */
public class PleasureHorizonsTrackedDataRegistry {
    public static final DeferredRegister<EntityDataSerializer<?>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, PleasureHorizons.MOD_ID);

    /** {@link Vec3} stream codec. 1.21.1 has no built-in {@code Vec3.STREAM_CODEC}, so define one. */
    public static final StreamCodec<RegistryFriendlyByteBuf, Vec3> VEC3_STREAM_CODEC = StreamCodec.of(
            (buf, vec) -> {
                buf.writeDouble(vec.x);
                buf.writeDouble(vec.y);
                buf.writeDouble(vec.z);
            },
            buf -> new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
    );

    public static final Codec<Vec3> VEC3_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("x").forGetter(Vec3::x),
            Codec.DOUBLE.fieldOf("y").forGetter(Vec3::y),
            Codec.DOUBLE.fieldOf("z").forGetter(Vec3::z)
    ).apply(instance, Vec3::new));

    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Vec3>> VEC3 =
            SERIALIZERS.register("vec3", () -> EntityDataSerializer.forValueType(VEC3_STREAM_CODEC));

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }
}
