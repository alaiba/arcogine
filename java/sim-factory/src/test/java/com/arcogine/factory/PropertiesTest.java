package com.arcogine.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.jobs.Job;
import com.arcogine.factory.jobs.JobStore;
import com.arcogine.factory.machines.Machine;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Property tests for sim-factory invariants. Ported from
 * crates/sim-factory/tests/properties.rs (proptest) to JUnit 5 parameterized /
 * randomized-seed tests covering the same input ranges.
 */
class PropertiesTest {

    private static final long SEED = 0x5EED_F00DL;

    // job_current_step_never_exceeds_total:
    //   total_steps in 1..=10, completions in 0..=15
    static Stream<Arguments> jobStepInputs() {
        Random rng = new Random(SEED);
        List<Arguments> args = new ArrayList<>();
        for (int totalSteps = 1; totalSteps <= 10; totalSteps++) {
            for (int completions = 0; completions <= 15; completions++) {
                args.add(Arguments.of(totalSteps, completions));
            }
        }
        // A handful of randomized cases within the same ranges.
        for (int i = 0; i < 32; i++) {
            args.add(Arguments.of(rng.nextInt(10) + 1, rng.nextInt(16)));
        }
        return args.stream();
    }

    @ParameterizedTest
    @MethodSource("jobStepInputs")
    void jobCurrentStepNeverExceedsTotal(int totalSteps, int completions) {
        Job job = new Job(new JobId(1), new ProductId(1), 1, totalSteps, SimTime.ZERO);
        for (int i = 0; i < completions; i++) {
            if (job.isComplete()) {
                break;
            }
            try {
                job.start(new MachineId(1));
            } catch (RuntimeException ignored) {
                // mirrors `let _ = job.start(...)` — error tolerated
            }
            try {
                job.completeStep(new SimTime(i + 1L));
            } catch (RuntimeException ignored) {
                // mirrors `let _ = job.complete_step(...)` — error tolerated
            }
        }
        assertTrue(job.currentStep() <= totalSteps);
    }

    // machine_active_jobs_never_exceeds_concurrency:
    //   concurrency in 1..=5, job_count in 0..=20
    static Stream<Arguments> concurrencyInputs() {
        Random rng = new Random(SEED + 1);
        List<Arguments> args = new ArrayList<>();
        for (int concurrency = 1; concurrency <= 5; concurrency++) {
            for (int jobCount = 0; jobCount <= 20; jobCount++) {
                args.add(Arguments.of(concurrency, jobCount));
            }
        }
        for (int i = 0; i < 32; i++) {
            args.add(Arguments.of(rng.nextInt(5) + 1, rng.nextInt(21)));
        }
        return args.stream();
    }

    @ParameterizedTest
    @MethodSource("concurrencyInputs")
    void machineActiveJobsNeverExceedsConcurrency(int concurrency, int jobCount) {
        Machine machine = new Machine(new MachineId(1), "M1", concurrency, null, 0);
        int started = 0;
        for (int i = 0; i < jobCount; i++) {
            JobId jid = new JobId(i + 1L);
            if (machine.canAcceptJob()) {
                machine.startJob(jid);
                started += 1;
            }
        }
        assertTrue(machine.activeJobs().size() <= concurrency);
        assertEquals(Math.min(concurrency, jobCount), started);
    }

    // queue_fifo_order: count in 0..=20
    static Stream<Arguments> fifoCounts() {
        return IntStream.rangeClosed(0, 20).mapToObj(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("fifoCounts")
    void queueFifoOrder(int count) {
        Machine machine = new Machine(new MachineId(1), "M1", 1, null, 0);
        machine.startJob(new JobId(0));

        List<JobId> ids = new ArrayList<>();
        for (long id = 1; id <= count; id++) {
            ids.add(new JobId(id));
        }
        for (JobId id : ids) {
            machine.enqueueJob(id);
        }
        List<JobId> dequeued = new ArrayList<>();
        java.util.Optional<JobId> next;
        while ((next = machine.dequeueJob()).isPresent()) {
            dequeued.add(next.get());
        }
        assertEquals(ids, dequeued);
    }

    // no_lost_jobs: created in 1..=20
    static Stream<Arguments> createdCounts() {
        return IntStream.rangeClosed(1, 20).mapToObj(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("createdCounts")
    void noLostJobs(int created) {
        JobStore store = new JobStore();
        for (int i = 0; i < created; i++) {
            store.createJob(new ProductId(1), 1, 2, SimTime.ZERO);
        }

        long active = store.activeJobs().count();
        long completed = store.completedJobs().count();
        long total = store.allJobs().count();

        assertEquals(created, total);
        assertEquals(total, active + completed);
    }

    @RepeatedTest(64)
    void randomizedJobStepInvariant() {
        Random rng = new Random();
        int totalSteps = rng.nextInt(10) + 1;
        int completions = rng.nextInt(16);
        Job job = new Job(new JobId(1), new ProductId(1), 1, totalSteps, SimTime.ZERO);
        for (int i = 0; i < completions; i++) {
            if (job.isComplete()) {
                break;
            }
            try {
                job.start(new MachineId(1));
                job.completeStep(new SimTime(i + 1L));
            } catch (RuntimeException ignored) {
                // tolerated, mirrors proptest's ignored Results
            }
        }
        assertTrue(job.currentStep() <= totalSteps);
    }
}
