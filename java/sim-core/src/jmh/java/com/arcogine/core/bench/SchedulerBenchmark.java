package com.arcogine.core.bench;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/** Ported from crates/sim-core/benches/scheduler.rs. */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class SchedulerBenchmark {

    static Event orderEvent(long tick) {
        return Event.of(SimTime.of(tick), new EventPayload.OrderCreation(new ProductId(1), 1, 10.0));
    }

    /** A scheduler freshly populated with 1000 events before each invocation. */
    @State(Scope.Thread)
    public static class PopulatedScheduler {
        Scheduler scheduler;

        @Setup(Level.Invocation)
        public void fill() {
            scheduler = new Scheduler();
            for (long i = 0; i < 1000; i++) {
                scheduler.schedule(orderEvent(i));
            }
        }
    }

    @Benchmark
    public void schedule1000Events(Blackhole bh) {
        Scheduler scheduler = new Scheduler();
        for (long i = 0; i < 1000; i++) {
            scheduler.schedule(orderEvent(i));
        }
        bh.consume(scheduler);
    }

    @Benchmark
    public void dequeue1000Events(PopulatedScheduler state, Blackhole bh) {
        while (state.scheduler.nextEvent().isPresent()) {
            // drain
        }
        bh.consume(state.scheduler);
    }

    @Benchmark
    public void interleavedScheduleDequeue(Blackhole bh) {
        Scheduler scheduler = new Scheduler();
        for (long i = 0; i < 500; i++) {
            scheduler.schedule(orderEvent(i * 2));
            if (i % 3 == 0) {
                scheduler.nextEvent();
            }
        }
        while (scheduler.nextEvent().isPresent()) {
            // drain
        }
        bh.consume(scheduler);
    }
}
