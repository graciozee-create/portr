package com.sandymandy.pleasurehorizons.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.entity.base.GirlEntity;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.Optional;

/** Advancement trigger fired after a girl is successfully tamed. */
public class TameGirlCriterion extends SimpleCriterionTrigger<TameGirlCriterion.Conditions> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(PleasureHorizons.MOD_ID, "tame_girl");

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player, GirlEntity entity) {
        LootContext entityContext = EntityPredicate.createContext(player, entity);
        this.trigger(player, conditions -> conditions.matches(entityContext));
    }

    public record Conditions(Optional<ContextAwarePredicate> player,
                             Optional<ContextAwarePredicate> entity)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(Conditions::entity)
        ).apply(instance, Conditions::new));

        public boolean matches(LootContext entityContext) {
            return entity.isEmpty() || entity.get().matches(entityContext);
        }

        @Override
        public void validate(CriterionValidator validator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
            validator.validateEntity(entity, ".entity");
        }
    }
}
