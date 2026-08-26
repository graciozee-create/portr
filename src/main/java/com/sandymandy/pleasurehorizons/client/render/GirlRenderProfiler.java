package com.sandymandy.pleasurehorizons.client.render;

import com.sandymandy.pleasurehorizons.PleasureHorizons;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * Frame-level profiler for the girl renderer.
 *
 * <p>In singleplayer the integrated server ticks on the client thread, so a heavy CLIENT frame
 * shows up in latest.log as "Can't keep up! Is the server overloaded?" exactly like a slow
 * server tick. If a frame spends 100ms+ drawing girls this logs it (max one line per 5 s) so
 * the freeze can be attributed to rendering instead of server AI.</p>
 */
@EventBusSubscriber(modid = PleasureHorizons.MOD_ID, value = Dist.CLIENT)
public final class GirlRenderProfiler {

    private static final LongAdder FRAME_NANOS = new LongAdder();
    private static final AtomicInteger FRAME_RENDERS = new AtomicInteger();
    private static long lastSlowLogMs = 0L;

    private GirlRenderProfiler() {
    }

    /** Called from {@link GirlRenderer#postRender} with the pass start timestamp. */
    public static void record(long startNanos) {
        FRAME_NANOS.add(System.nanoTime() - startNanos);
        FRAME_RENDERS.incrementAndGet();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        long nanos = FRAME_NANOS.sumThenReset();
        int renders = FRAME_RENDERS.getAndSet(0);
        long ms = nanos / 1_000_000L;
        if (ms >= 100L) {
            long now = System.currentTimeMillis();
            if (now - lastSlowLogMs >= 5000L) {
                lastSlowLogMs = now;
                PleasureHorizons.LOGGER.warn(
                        "[PH] SLOW-CLIENT girl render: {}ms for {} girl render(s) in this frame",
                        ms, renders);
            }
        }
    }
}
