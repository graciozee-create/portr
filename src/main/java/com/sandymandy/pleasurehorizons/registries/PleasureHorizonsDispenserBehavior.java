package com.sandymandy.pleasurehorizons.registries;

import com.sandymandy.pleasurehorizons.block.PleasureHorizonsBlocks;
import com.sandymandy.pleasurehorizons.block.blocks.CarvedGirlPumpkinBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/** Registers the carved-girl-pumpkin behavior after deferred block registration has completed. */
public final class PleasureHorizonsDispenserBehavior {
    private PleasureHorizonsDispenserBehavior() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(PleasureHorizonsDispenserBehavior::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> DispenserBlock.registerBehavior(
                PleasureHorizonsBlocks.CARVED_GIRL_PUMPKIN.get(),
                new CarvedGirlPumpkinDispenseBehavior()));
    }

    private static final class CarvedGirlPumpkinDispenseBehavior extends OptionalDispenseItemBehavior {
        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack) {
            ServerLevel level = source.level();
            BlockPos targetPos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
            CarvedGirlPumpkinBlock pumpkin = PleasureHorizonsBlocks.CARVED_GIRL_PUMPKIN.get();

            if (level.isEmptyBlock(targetPos) && pumpkin.canDispense(level, targetPos)) {
                level.setBlock(targetPos, pumpkin.defaultBlockState(), 3);
                level.gameEvent(null, GameEvent.BLOCK_PLACE, targetPos);
                stack.shrink(1);
                setSuccess(true);
            } else {
                // Match vanilla carved-pumpkin behavior: if no summon pattern is present,
                // try equipping the pumpkin as a helmet rather than silently consuming it.
                setSuccess(ArmorItem.dispenseArmor(source, stack));
            }

            return stack;
        }
    }
}
