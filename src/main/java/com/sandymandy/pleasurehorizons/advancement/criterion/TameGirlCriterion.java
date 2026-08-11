package com.sandymandy.pleasurehorizons.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class TameGirlCriterion extends SimpleCriterionTrigger<TameGirlCriterion.Conditions> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "tame_girl");

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player, Object entity) {
        this.trigger(player, conditions -> true);
    }

    public record Conditions(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player)
        ).apply(instance, Conditions::new));

        public boolean matches(Object lootContext) { return true; }
    }
}
