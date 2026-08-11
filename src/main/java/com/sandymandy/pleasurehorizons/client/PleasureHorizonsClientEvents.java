package com.sandymandy.pleasurehorizons.client;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.client.render.GirlRenderer;
import com.sandymandy.pleasurehorizons.registries.GirlRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Without these registrations every girl entity exists on the server but is completely
 * invisible on the client - one of the reasons the previous port showed nothing.
 */
@EventBusSubscriber(modid = PleasureHorizons.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PleasureHorizonsClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GirlRegistry.LUCY.get(), GirlRenderer::new);
        event.registerEntityRenderer(GirlRegistry.MIKA.get(), GirlRenderer::new);
        event.registerEntityRenderer(GirlRegistry.MOMO.get(), GirlRenderer::new);
        event.registerEntityRenderer(GirlRegistry.SLIME.get(), GirlRenderer::new);
        event.registerEntityRenderer(GirlRegistry.KOBOLD.get(), GirlRenderer::new);
        event.registerEntityRenderer(GirlRegistry.COPPIE.get(), GirlRenderer::new);
        event.registerEntityRenderer(GirlRegistry.CUSTOM_GIRL.get(), GirlRenderer::new);
    }
}
