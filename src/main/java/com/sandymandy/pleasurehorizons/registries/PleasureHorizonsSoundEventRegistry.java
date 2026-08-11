package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PleasureHorizonsSoundEventRegistry {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, PleasureHorizons.MOD_ID);

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }

    public static void registerSoundEvents() {
        PleasureHorizons.LOGGER.info("Registering sound events for " + PleasureHorizons.MOD_NAME);
    }

    protected static SoundEvent registerSound(String soundPath) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, soundPath);
        return SoundEvent.createVariableRangeEvent(id);
    }
}
