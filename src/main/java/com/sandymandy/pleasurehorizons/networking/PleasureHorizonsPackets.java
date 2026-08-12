package com.sandymandy.pleasurehorizons.networking;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import com.sandymandy.pleasurehorizons.networking.C2S.*;
import com.sandymandy.pleasurehorizons.networking.S2C.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = PleasureHorizons.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class PleasureHorizonsPackets {

    public static void register() {
        PleasureHorizons.LOGGER.info("Registering packets");
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ThrustKeybindC2SPacket.TYPE, ThrustKeybindC2SPacket.STREAM_CODEC, ThrustKeybindC2SPacket::handle);
        registrar.playToServer(CumKeybindC2SPacket.TYPE, CumKeybindC2SPacket.STREAM_CODEC, CumKeybindC2SPacket::handle);
        registrar.playToServer(InventoryButtonC2SPacket.TYPE, InventoryButtonC2SPacket.STREAM_CODEC, InventoryButtonC2SPacket::handle);
        registrar.playToServer(AnimationSyncC2SPacket.TYPE, AnimationSyncC2SPacket.STREAM_CODEC, AnimationSyncC2SPacket::handle);
        registrar.playToServer(AnimationFinishC2SPacket.TYPE, AnimationFinishC2SPacket.STREAM_CODEC, AnimationFinishC2SPacket::handle);
        registrar.playToServer(ScenePhaseSyncC2SPacket.TYPE, ScenePhaseSyncC2SPacket.STREAM_CODEC, ScenePhaseSyncC2SPacket::handle);
        registrar.playToServer(SetGUIOpenStateC2SPacket.TYPE, SetGUIOpenStateC2SPacket.STREAM_CODEC, SetGUIOpenStateC2SPacket::handle);
        registrar.playToServer(SoundEventSyncC2SPacket.TYPE, SoundEventSyncC2SPacket.STREAM_CODEC, SoundEventSyncC2SPacket::handle);
        registrar.playToServer(StartSceneC2SPacket.TYPE, StartSceneC2SPacket.STREAM_CODEC, StartSceneC2SPacket::handle);
        registrar.playToServer(StopSceneOnServerC2SPacket.TYPE, StopSceneOnServerC2SPacket.STREAM_CODEC, StopSceneOnServerC2SPacket::handle);
        registrar.playToServer(GirlCustomizeC2SPacket.TYPE, GirlCustomizeC2SPacket.STREAM_CODEC, GirlCustomizeC2SPacket::handle);
        registrar.playToServer(KoboldCustomizeC2SPacket.TYPE, KoboldCustomizeC2SPacket.STREAM_CODEC, KoboldCustomizeC2SPacket::handle);
        registrar.playToServer(RemovePreviewEntityC2SPacket.TYPE, RemovePreviewEntityC2SPacket.STREAM_CODEC, RemovePreviewEntityC2SPacket::handle);
        registrar.playToClient(ClothingArmorVisibilityS2CPacket.TYPE, ClothingArmorVisibilityS2CPacket.STREAM_CODEC, ClothingArmorVisibilityS2CPacket::handle);
        registrar.playToClient(SceneOptionsS2CPacket.TYPE, SceneOptionsS2CPacket.STREAM_CODEC, SceneOptionsS2CPacket::handle);
        registrar.playToClient(PlayCumHudAnimationS2CPacket.TYPE, PlayCumHudAnimationS2CPacket.STREAM_CODEC, PlayCumHudAnimationS2CPacket::handle);
        registrar.playToClient(OpenCustomizeScreenS2CPacket.TYPE, OpenCustomizeScreenS2CPacket.STREAM_CODEC, OpenCustomizeScreenS2CPacket::handle);
        registrar.playToClient(PlayAttackAnimationS2CPacket.TYPE, PlayAttackAnimationS2CPacket.STREAM_CODEC, PlayAttackAnimationS2CPacket::handle);
        registrar.playToClient(RunAnimEventsS2CPacket.TYPE, RunAnimEventsS2CPacket.STREAM_CODEC, RunAnimEventsS2CPacket::handle);
        registrar.playToClient(OpenKoboldCustomizeScreenS2CPacket.TYPE, OpenKoboldCustomizeScreenS2CPacket.STREAM_CODEC, OpenKoboldCustomizeScreenS2CPacket::handle);
    }
}
