package com.sandymandy.pleasurehorizons.effects;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Custom mob effects added by the mod.
 *
 * <p>{@code HORNY} is a pure marker effect applied by the Horny Potion. It is intentionally a
 * dedicated effect rather than a vanilla speed+regen check so that other mods' golden apples or
 * beacon effects can never be confused with it.</p>
 */
public class PleasureHorizonsEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, PleasureHorizons.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> HORNY = EFFECTS.register("horny",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF6EB4) {
                // Pure marker effect: no per-tick logic needed.
            });

    public static Holder<MobEffect> hornyHolder() {
        return HORNY;
    }

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
