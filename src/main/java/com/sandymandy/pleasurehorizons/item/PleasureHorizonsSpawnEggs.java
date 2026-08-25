package com.sandymandy.pleasurehorizons.item;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.registries.GirlRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Spawn eggs for every girl.
 *
 * <p>{@link DeferredSpawnEggItem} is the NeoForge replacement for vanilla {@code SpawnEggItem}:
 * it takes a {@code Supplier<EntityType>} so the egg can be created before the entity type
 * has finished registering.</p>
 */
public class PleasureHorizonsSpawnEggs {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, PleasureHorizons.MOD_ID);

    /** Every registered egg, in registration order - used to fill the creative tab. */
    public static final List<DeferredHolder<Item, Item>> ALL_EGGS = new ArrayList<>();

    public static final DeferredHolder<Item, Item> LUCY_SPAWN_EGG =
            registerEgg("lucy_spawn_egg", GirlRegistry.LUCY::get, 0xF2C4A0, 0xE8A33D);
    public static final DeferredHolder<Item, Item> MIKA_SPAWN_EGG =
            registerEgg("mika_spawn_egg", GirlRegistry.MIKA::get, 0xF2C4A0, 0x8B4FA8);
    public static final DeferredHolder<Item, Item> MOMO_SPAWN_EGG =
            registerEgg("momo_spawn_egg", GirlRegistry.MOMO::get, 0xF2C4A0, 0xE86A8F);
    public static final DeferredHolder<Item, Item> SLIME_SPAWN_EGG =
            registerEgg("slime_spawn_egg", GirlRegistry.SLIME::get, 0x8FD86B, 0x5CA83D);
    public static final DeferredHolder<Item, Item> KOBOLD_SPAWN_EGG =
            registerEgg("kobold_spawn_egg", GirlRegistry.KOBOLD::get, 0xC97B3D, 0x6E4326);
    public static final DeferredHolder<Item, Item> COPPIE_SPAWN_EGG =
            registerEgg("coppie_spawn_egg", GirlRegistry.COPPIE::get, 0xE0794B, 0x4FBFA8);
    public static final DeferredHolder<Item, Item> ALLIE_SPAWN_EGG =
            registerEgg("allie_spawn_egg", GirlRegistry.ALLIE::get, 0xF2C4A0, 0xD4629A);
    public static final DeferredHolder<Item, Item> BIA_SPAWN_EGG =
            registerEgg("bia_spawn_egg", GirlRegistry.BIA::get, 0xE8C8A0, 0x6B8E5A);
    public static final DeferredHolder<Item, Item> GOBLIN_SPAWN_EGG =
            registerEgg("goblin_spawn_egg", GirlRegistry.GOBLIN::get, 0x8B9A6B, 0x4A5A3A);
    public static final DeferredHolder<Item, Item> GALATH_SPAWN_EGG =
            registerEgg("galath_spawn_egg", GirlRegistry.GALATH::get, 0x7B3FA0, 0xC9A03D);
    public static final DeferredHolder<Item, Item> MANGLELIE_SPAWN_EGG =
            registerEgg("manglelie_spawn_egg", GirlRegistry.MANGLELIE::get, 0xA0A0B0, 0x5050A0);
    public static final DeferredHolder<Item, Item> JENNY_SPAWN_EGG =
            registerEgg("jenny_spawn_egg", GirlRegistry.JENNY::get, 0xF2D4A0, 0xA06030);

    @SuppressWarnings("unchecked")
    private static DeferredHolder<Item, Item> registerEgg(String name,
                                                          Supplier<? extends EntityType<?>> type,
                                                          int background, int highlight) {
        DeferredHolder<Item, Item> egg = ITEMS.register(name,
                () -> new DeferredSpawnEggItem(
                        (Supplier<? extends EntityType<? extends Mob>>) type,
                        background, highlight, new Item.Properties()));
        ALL_EGGS.add(egg);
        return egg;
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
