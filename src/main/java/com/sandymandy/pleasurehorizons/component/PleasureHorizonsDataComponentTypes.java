package com.sandymandy.pleasurehorizons.component;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;

/** Item data used by the settlement recruitment contract. */
public final class PleasureHorizonsDataComponentTypes {
    private PleasureHorizonsDataComponentTypes() {}

    private static final DeferredRegister.DataComponents COMPONENT_TYPES =
            DeferredRegister.createDataComponents(PleasureHorizons.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> SETTLEMENT_UUID =
            COMPONENT_TYPES.registerComponentType("settlement_uuid", builder -> builder
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC));

    public static void register(IEventBus bus) {
        COMPONENT_TYPES.register(bus);
    }
}
