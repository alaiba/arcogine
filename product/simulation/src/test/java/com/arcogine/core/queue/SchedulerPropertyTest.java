package com.arcogine.core.queue;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Property tests ported from crates/sim-core/tests/properties.rs.
 *
 * <p>The Rust suite used the {@code proptest} crate to generate randomized
 * inputs. Here each property is exercised across many fixed RNG seeds; for a
 * given seed the generated input is reproducible, mirroring proptest's
 * shrink/replay guarantee.
 */
class SchedulerPropertyTest {

    private static final long[] SEEDS = seeds();

    private static long[] seeds() {
        long[] s = new long[256];
        for (int i = 0; i < s.length; i++) {
            s[i] = i;
        }
        return s;
    }

    private static List<Long> randomTimes(Random rng) {
        int count = 1 + rng.nextInt(99); // 1..100
        List<Long> times = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            times.add((long) rng.nextInt(10000)); // 0..10000
        }
        return times;
    }

    /** Events are always dequeued in non-decreasing time order. */
    @ParameterizedTest
    @ValueSource(longs = {0, 1, 2, 3, 7, 42, 99, 255})
    void monotonicTimeProgression(long seed) {
        Random rng = new Random(seed);
        List<Long> times = randomTimes(rng);

        Scheduler scheduler = new Scheduler();
        for (long t : times) {
            scheduler.schedule(Event.of(new SimTime(t), EventPayload.DemandEvaluation.INSTANCE));
        }

        long lastTime = 0L;
        Optional<Event> next;
        while ((next = scheduler.nextEvent()).isPresent()) {
            long time = next.get().time().ticks();
            assertTrue(time >= lastTime, "time went backwards: " + time + " < " + lastTime);
            lastTime = time;
        }
    }

    /** All scheduled events are eventually dequeued (no event loss). */
    @ParameterizedTest
    @ValueSource(longs = {0, 1, 2, 3, 7, 42, 99, 255})
    void noEventLoss(long seed) {
        Random rng = new Random(seed);
        List<Long> times = randomTimes(rng);
        int count = times.size();

        Scheduler scheduler = new Scheduler();
        for (long t : times) {
            scheduler.schedule(Event.of(new SimTime(t), EventPayload.DemandEvaluation.INSTANCE));
        }

        int dequeued = 0;
        while (scheduler.nextEvent().isPresent()) {
            dequeued++;
        }
        assertEquals(count, dequeued, "lost events: scheduled " + count + " but dequeued " + dequeued);
    }

    /** Scheduling an event at or after current time never fails. */
    @ParameterizedTest
    @ValueSource(longs = {0, 1, 2, 3, 7, 42, 99, 255})
    void schedulingAtCurrentTimeSucceeds(long seed) {
        Random rng = new Random(seed);
        long baseTime = rng.nextInt(1000); // 0..1000
        long offset = rng.nextInt(1000); // 0..1000

        Scheduler scheduler = new Scheduler();
        if (baseTime > 0) {
            scheduler.schedule(Event.of(new SimTime(baseTime), EventPayload.DemandEvaluation.INSTANCE));
            scheduler.nextEvent();
        }

        assertDoesNotThrow(() -> scheduler.schedule(
                Event.of(new SimTime(baseTime + offset), EventPayload.DemandEvaluation.INSTANCE)));
    }

    /** Exercise the full seed range for monotonicity to broaden coverage. */
    @ParameterizedTest
    @ValueSource(strings = "all-seeds")
    void monotonicAcrossAllSeeds(String label) {
        assertEquals("all-seeds", label);
        for (long seed : SEEDS) {
            Random rng = new Random(seed);
            List<Long> times = randomTimes(rng);
            Scheduler scheduler = new Scheduler();
            for (long t : times) {
                scheduler.schedule(Event.of(new SimTime(t), EventPayload.DemandEvaluation.INSTANCE));
            }
            long lastTime = 0L;
            Optional<Event> next;
            while ((next = scheduler.nextEvent()).isPresent()) {
                long time = next.get().time().ticks();
                assertTrue(time >= lastTime, "seed " + seed + ": " + time + " < " + lastTime);
                lastTime = time;
            }
        }
    }
}
