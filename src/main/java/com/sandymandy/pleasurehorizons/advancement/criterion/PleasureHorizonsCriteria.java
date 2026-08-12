package com.sandymandy.pleasurehorizons.advancement.criterion;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the mod's advancement triggers in the vanilla trigger-type registry. */
public final class PleasureHorizonsCriteria {
    private static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES =
            DeferredRegister.create(Registries.TRIGGER_TYPE, PleasureHorizons.MOD_ID);

    public static final DeferredHolder<CriterionTrigger<?>, TameGirlCriterion> TAME_GIRL =
            TRIGGER_TYPES.register("tame_girl", TameGirlCriterion::new);

    private PleasureHorizonsCriteria() {
    }

    public static void register(IEventBus bus) {
        TRIGGER_TYPES.register(bus);
    }
}
