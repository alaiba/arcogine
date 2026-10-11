package com.arcogine.research.experiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.process.CommandResult;
import com.arcogine.factory.process.FactoryRuntime;
import com.arcogine.factory.process.JobObservation;
import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeEventType;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.factory.process.RuntimeObservationMetadata;
import com.arcogine.factory.process.RuntimePerformanceObservation;
import com.arcogine.factory.process.RuntimeRunState;
import com.arcogine.research.experiment.LinearRoutingFamily.Resource;
import com.arcogine.research.experiment.LinearRoutingFamily.Step;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import com.arcogine.types.RunId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

/**
 * Research-custody discriminators for the analytics ownership Phase 2 revision after the captured
 * execution-evidence reconciliation, the removal of inert scheduler markers and the removal of the
 * per-resource busy counter. Every expected value was registered by hand before this class existed.
 *
 * <p>The class keeps three research-local roles apart on purpose. A <em>driver</em> owns the runtime
 * and records its own call outcomes; a <em>holder bundle</em> is what some party retained (a basis
 * observation, supported changes, observations, optionally the driver's closure statement); and a
 * <em>definition</em> is a stateless function of a bundle that decides for itself whether the bundle
 * suffices for its claim. None of this is production semantics, an evidence format or an analytics
 * API, and no definition here evaluates a dispatch, ranking or cascade rule.
 */
class PostExecutionEvidenceDiscriminatorExperiment {

    private static final String LOG = "[analytics-post-execution-evidence] ";
    private static final long UNBOUNDED = Long.MAX_VALUE;

    // ======================================================================== research-local roles

    /** One bounded-advancement call the driver made, and its outcome. */
    record Call(long target, long budget, long processed, boolean faulted) {}

    /**
     * A driver's attributable closure statement: who says it, for what scope, the calls it rests on,
     * how far it excludes effective input, and the post-advancement observation it is bound to.
     */
    record Closure(String attestor, String scope, List<Call> calls, long inputExcludedThrough,
            Optional<RuntimeObservation> bound) {

        /** The inclusive target through which successful exhaustion is demonstrated, if any. */
        Optional<Long> exhaustedThrough() {
            if (calls.isEmpty() || calls.stream().anyMatch(Call::faulted)) {
                return Optional.empty();
            }
            Call last = calls.getLast();
            return last.budget() > 0 && last.processed() < last.budget() ? Optional.of(last.target()) : Optional.empty();
        }
    }

    /** What one party holds about one run. */
    record Bundle(RunId runId, RuntimeObservation basis, List<RuntimeEventEnvelope> changes,
            List<RuntimeObservation> observations, Optional<Closure> closure) {

        Bundle {
            changes = List.copyOf(changes);
            observations = List.copyOf(observations);
        }

        Bundle withoutClosure() {
            return new Bundle(runId, basis, changes, observations, Optional.empty());
        }

        Bundle withClosure(Closure replacement) {
            return new Bundle(runId, basis, changes, observations, Optional.of(replacement));
        }
    }

    /** A definition's outcome: a value with the proof it rests on, or a refusal naming why. */
    sealed interface Outcome<T> {
        record Value<T>(T value, String proof) implements Outcome<T> {}

        record Refused<T>(String reason) implements Outcome<T> {}
    }

    private record Proof(long prefixThrough, String description) {}

    /**
     * The shared half-open interval rule of the runtime contract, applied to one bundle and one claim
     * {@code [a,b)}: a basis at or before {@code a}, then a producer witness change, a producer
     * observation, or a valid bound closure, each with gap-free held changes through its proof sequence.
     */
    static Optional<Proof> prove(Bundle bundle, long a, long b, List<String> reasons) {
        long t0 = bundle.basis().metadata().currentTime().value();
        if (t0 > a) {
            reasons.add("basis time " + t0 + " is after the interval start " + a + "; pre-join history is unavailable");
            return Optional.empty();
        }
        long s0 = bundle.basis().metadata().latestEventSequence();
        Map<Long, RuntimeEventEnvelope> held = held(bundle);

        Optional<RuntimeEventEnvelope> witness = held.values().stream()
                .filter(change -> change.simulationTime().value() >= b)
                .min((x, y) -> Long.compare(x.sequence(), y.sequence()));
        if (witness.isPresent()) {
            long k = witness.get().sequence();
            List<String> missing = missing(held, s0, k - 1);
            if (missing.isEmpty()) {
                return Optional.of(new Proof(k - 1, "producer witness: change " + k + " at time "
                        + witness.get().simulationTime().value()));
            }
            reasons.add("witness change " + k + " held but changes " + missing + " are missing");
        } else {
            reasons.add("no held change at time >= " + b);
        }

        for (RuntimeObservation observation : bundle.observations()) {
            if (!observation.metadata().runId().equals(bundle.runId())
                    || observation.metadata().currentTime().value() < b) {
                continue;
            }
            long cursor = observation.metadata().latestEventSequence();
            List<String> missing = missing(held, s0, cursor);
            if (missing.isEmpty()) {
                return Optional.of(new Proof(cursor, "producer observation: cursor " + cursor + " at time "
                        + observation.metadata().currentTime().value()));
            }
            reasons.add("observation at time " + observation.metadata().currentTime().value() + " has cursor " + cursor
                    + " but changes " + missing + " are missing");
        }

        if (bundle.closure().isEmpty()) {
            reasons.add("no controller closure");
            return Optional.empty();
        }
        Closure closure = bundle.closure().get();
        if (closure.bound().isEmpty()) {
            reasons.add("closure by " + closure.attestor() + " is not bound to a post-advancement observation");
            return Optional.empty();
        }
        RuntimeObservation bound = closure.bound().get();
        if (!bound.metadata().runId().equals(bundle.runId())) {
            reasons.add("closure is bound to another run");
            return Optional.empty();
        }
        Optional<Long> through = closure.exhaustedThrough();
        if (through.isEmpty()) {
            reasons.add("closure by " + closure.attestor() + " does not demonstrate successful exhaustion: " + closure.calls());
            return Optional.empty();
        }
        if (through.get() < b) {
            reasons.add("closure demonstrates exhaustion only through " + through.get());
            return Optional.empty();
        }
        if (closure.inputExcludedThrough() < b) {
            reasons.add("closure excludes effective input only through " + closure.inputExcludedThrough());
            return Optional.empty();
        }
        long cursor = bound.metadata().latestEventSequence();
        List<String> missing = missing(held, s0, cursor);
        if (!missing.isEmpty()) {
            reasons.add("closure is bound to cursor " + cursor + " but changes " + missing + " are missing");
            return Optional.empty();
        }
        return Optional.of(new Proof(cursor, "controller closure by " + closure.attestor() + " (" + closure.scope()
                + "), exhausted through " + through.get() + ", bound to (" + cursor + ","
                + bound.metadata().currentTime().value() + ")"));
    }

    private static Map<Long, RuntimeEventEnvelope> held(Bundle bundle) {
        Map<Long, RuntimeEventEnvelope> held = new TreeMap<>();
        bundle.changes().stream()
                .filter(change -> change.runId().equals(bundle.runId()))
                .forEach(change -> held.put(change.sequence(), change));
        return held;
    }

    private static List<String> missing(Map<Long, RuntimeEventEnvelope> held, long after, long through) {
        List<String> ranges = new ArrayList<>();
        long from = -1;
        for (long sequence = after + 1; sequence <= through + 1; sequence++) {
            boolean absent = sequence <= through && !held.containsKey(sequence);
            if (absent && from < 0) {
                from = sequence;
            } else if (!absent && from >= 0) {
                ranges.add(from == sequence - 1 ? Long.toString(from) : from + ".." + (sequence - 1));
                from = -1;
            }
        }
        return ranges;
    }

    private static List<RuntimeEventEnvelope> prefix(Bundle bundle, Proof proof) {
        long s0 = bundle.basis().metadata().latestEventSequence();
        return held(bundle).values().stream()
                .filter(change -> change.sequence() > s0 && change.sequence() <= proof.prefixThrough())
                .toList();
    }

    // ======================================================================== research-local definitions

    /** {@code active-job-ticks[M,a,b)}: the integral over the interval of the jobs active on one resource. */
    static Outcome<Long> activeJobTicks(Bundle bundle, MachineId machine, long a, long b) {
        List<String> reasons = new ArrayList<>();
        Optional<Proof> proof = prove(bundle, a, b, reasons);
        if (proof.isEmpty()) {
            return new Outcome.Refused<>(String.join("; ", reasons));
        }
        return new Outcome.Value<>(integrateActive(bundle.basis(), prefix(bundle, proof.get()), machine, a, b),
                proof.get().description());
    }

    /** The same integral with no sufficiency check: an unfinished dispatch is taken to run until {@code b}. */
    static long naiveActiveJobTicks(Bundle bundle, MachineId machine, long a, long b) {
        long s0 = bundle.basis().metadata().latestEventSequence();
        return integrateActive(bundle.basis(), held(bundle).values().stream()
                .filter(change -> change.sequence() > s0).toList(), machine, a, b);
    }

    private static long integrateActive(RuntimeObservation basis, List<RuntimeEventEnvelope> changes, MachineId machine,
            long a, long b) {
        long count = resource(basis, machine).activeJobIds().size();
        long cursorTime = a;
        long total = 0;
        for (RuntimeEventEnvelope change : changes) {
            long time = change.simulationTime().value();
            if (time >= b) {
                break;
            }
            if (time > cursorTime) {
                total += count * (time - cursorTime);
                cursorTime = time;
            }
            switch (change.payload()) {
                case RuntimeEventPayload.JobDispatched dispatched when dispatched.machineId().equals(machine) -> count++;
                case RuntimeEventPayload.JobStepCompleted completed when completed.machineId().equals(machine) -> count--;
                default -> { }
            }
            assertTrue(count >= 0, "active count cannot go negative");
        }
        return total + count * (b - cursorTime);
    }

    /** Completion-credited processing within {@code [a,b)}: durations of steps whose completion lies in it. */
    static Outcome<Long> completionCreditedTicks(Bundle bundle, FactoryModel model, MachineId machine, long a, long b) {
        List<String> reasons = new ArrayList<>();
        Optional<Proof> proof = prove(bundle, a, b, reasons);
        if (proof.isEmpty()) {
            return new Outcome.Refused<>(String.join("; ", reasons));
        }
        List<OperationStepDefinition> routing = model.operations().getFirst().steps();
        long total = 0;
        for (RuntimeEventEnvelope change : prefix(bundle, proof.get())) {
            long time = change.simulationTime().value();
            if (time >= a && time < b && change.payload() instanceof RuntimeEventPayload.JobStepCompleted completed
                    && completed.machineId().equals(machine)) {
                total += routing.get(completed.stepIndex()).duration();
            }
        }
        return new Outcome.Value<>(total, proof.get().description());
    }

    /** Exact per-slot utilization pair: working slot-ticks over online slot-ticks, offline time excluded. */
    record SlotTicks(long working, long available) {}

    static Outcome<SlotTicks> perSlotUtilization(Bundle bundle, MachineId machine, long a, long b) {
        List<String> reasons = new ArrayList<>();
        Optional<Proof> proof = prove(bundle, a, b, reasons);
        if (proof.isEmpty()) {
            return new Outcome.Refused<>(String.join("; ", reasons));
        }
        List<RuntimeEventEnvelope> changes = prefix(bundle, proof.get());
        long working = integrateActive(bundle.basis(), changes, machine, a, b);
        long online = onlineTicks(bundle.basis(), changes, machine, a, b);
        return new Outcome.Value<>(new SlotTicks(working, resource(bundle.basis(), machine).concurrency() * online),
                proof.get().description());
    }

    /** Period rate: completed orders whose completion lies in {@code [a,b)}, per tick of the period. */
    record PeriodRate(long completedOrders, long periodTicks) {}

    static Outcome<PeriodRate> completedOrdersPerPeriodTick(Bundle bundle, long a, long b) {
        List<String> reasons = new ArrayList<>();
        Optional<Proof> proof = prove(bundle, a, b, reasons);
        if (proof.isEmpty()) {
            return new Outcome.Refused<>(String.join("; ", reasons));
        }
        long completed = prefix(bundle, proof.get()).stream()
                .filter(change -> change.payload() instanceof RuntimeEventPayload.OrderCompleted)
                .filter(change -> change.simulationTime().value() >= a && change.simulationTime().value() < b)
                .count();
        return new Outcome.Value<>(new PeriodRate(completed, b - a), proof.get().description());
    }

    private static long onlineTicks(RuntimeObservation basis, List<RuntimeEventEnvelope> changes, MachineId machine,
            long a, long b) {
        boolean online = resource(basis, machine).state() != MachineState.Offline;
        long cursorTime = a;
        long total = 0;
        for (RuntimeEventEnvelope change : changes) {
            long time = change.simulationTime().value();
            if (time >= b) {
                break;
            }
            if (time > cursorTime) {
                total += online ? time - cursorTime : 0;
                cursorTime = time;
            }
            if (change.payload() instanceof RuntimeEventPayload.MachineAvailabilityChanged changed
                    && changed.machineId().equals(machine)) {
                online = changed.online();
            }
        }
        return total + (online ? b - cursorTime : 0);
    }

    /** Idle-slot flow characterization aggregated over {@code [a,b)}, in slot-ticks. */
    record Flow(long working, long idleWithEligibleWorkQueued, long starvedUncapped, long starvedCapped, long noWorkLeft) {}

    private enum JobState { PLACING, WAITING, RUNNING, DONE }

    private static final class JobTrack {
        JobState state = JobState.PLACING;
        int step;
        int lastCompleted = -1;
        MachineId machine;
        Set<MachineId> eligible = Set.of();
    }

    /**
     * Folds supported job placement facts into slot-tick categories for one resource. Step position
     * comes only from supported dispatch, waiting and completion facts plus the published routing;
     * the basis must hold no jobs, and any offline time in the interval refuses (no offline treatment
     * is declared for this characterization).
     */
    static Outcome<Flow> flow(Bundle bundle, FactoryModel model, MachineId machine, long a, long b) {
        List<String> reasons = new ArrayList<>();
        Optional<Proof> proof = prove(bundle, a, b, reasons);
        if (proof.isEmpty()) {
            return new Outcome.Refused<>(String.join("; ", reasons));
        }
        if (!bundle.basis().jobs().isEmpty()) {
            return new Outcome.Refused<>("this research-local fold needs a basis with no jobs");
        }
        List<RuntimeEventEnvelope> changes = prefix(bundle, proof.get());
        if (onlineTicks(bundle.basis(), changes, machine, a, b) != b - a) {
            return new Outcome.Refused<>("the resource was offline inside the interval; no offline treatment is declared");
        }
        List<OperationStepDefinition> routing = model.operations().getFirst().steps();
        int concurrency = resource(bundle.basis(), machine).concurrency();
        Map<JobId, JobTrack> jobs = new LinkedHashMap<>();
        long[] totals = new long[5];
        long cursorTime = a;
        for (RuntimeEventEnvelope change : changes) {
            long time = change.simulationTime().value();
            if (time >= b) {
                break;
            }
            if (time > cursorTime) {
                accumulate(totals, jobs, routing, machine, concurrency, time - cursorTime);
                cursorTime = time;
            }
            switch (change.payload()) {
                case RuntimeEventPayload.OrderAccepted accepted ->
                    accepted.jobIds().forEach(job -> jobs.put(job, new JobTrack()));
                case RuntimeEventPayload.JobDispatched dispatched -> {
                    JobTrack track = jobs.get(dispatched.jobId());
                    track.state = JobState.RUNNING;
                    track.step = dispatched.stepIndex();
                    track.machine = dispatched.machineId();
                }
                case RuntimeEventPayload.JobWaiting waiting -> {
                    JobTrack track = jobs.get(waiting.jobId());
                    track.state = JobState.WAITING;
                    track.step = track.lastCompleted + 1;
                    track.eligible = Set.copyOf(waiting.eligibleMachines());
                }
                case RuntimeEventPayload.JobStepCompleted completed -> {
                    JobTrack track = jobs.get(completed.jobId());
                    track.lastCompleted = completed.stepIndex();
                    track.state = completed.jobComplete() ? JobState.DONE : JobState.PLACING;
                }
                default -> { }
            }
        }
        if (b > cursorTime) {
            accumulate(totals, jobs, routing, machine, concurrency, b - cursorTime);
        }
        return new Outcome.Value<>(new Flow(totals[0], totals[1], totals[2], totals[3], totals[4]),
                proof.get().description());
    }

    private static void accumulate(long[] totals, Map<JobId, JobTrack> jobs, List<OperationStepDefinition> routing,
            MachineId machine, int concurrency, long ticks) {
        long active = jobs.values().stream()
                .filter(track -> track.state == JobState.RUNNING && machine.equals(track.machine))
                .count();
        totals[0] += active * ticks;
        long idle = concurrency - active;
        if (idle <= 0) {
            return;
        }
        long queued = jobs.values().stream()
                .filter(track -> track.state == JobState.WAITING && track.eligible.contains(machine))
                .count();
        if (queued > 0) {
            totals[1] += idle * ticks;
            return;
        }
        long needing = jobs.values().stream().filter(track -> {
            int from = switch (track.state) {
                case RUNNING -> track.step + 1;
                case WAITING -> track.step;
                case PLACING -> track.lastCompleted + 1;
                case DONE -> routing.size();
            };
            for (int step = from; step < routing.size(); step++) {
                if (routing.get(step).eligibleResources().contains(machine)) {
                    return true;
                }
            }
            return false;
        }).count();
        if (needing > 0) {
            totals[2] += idle * ticks;
            totals[3] += Math.min(idle, needing) * ticks;
        } else {
            totals[4] += idle * ticks;
        }
    }

    /** Current slot state at one observation: working and idle slots, read directly; no history. */
    record SlotState(long working, long idle) {}

    static SlotState slotState(RuntimeObservation observation, MachineId machine) {
        ResourceObservation resource = resource(observation, machine);
        return new SlotState(resource.activeJobIds().size(), resource.concurrency() - resource.activeJobIds().size());
    }

    // ======================================================================== the driver

    /** Owns one runtime, drives it only through supported calls and records its own call outcomes. */
    static final class Driver {
        final FactoryRuntime runtime;
        final FactoryModel model;
        final List<Call> calls = new ArrayList<>();
        final List<RuntimeEventEnvelope> everything = new ArrayList<>();

        Driver(LinearRoutingFamily family) {
            this.model = family.model();
            this.runtime = FactoryRuntime.forModel(FactoryModelPublisher.publish(model));
        }

        RuntimeObservation observe() {
            return runtime.observe();
        }

        void submit(long quantity) {
            assertInstanceOf(CommandResult.Accepted.class,
                    runtime.submitWorkload(LinearRoutingFamily.PRODUCT, quantity, LinearRoutingFamily.UNIT_PRICE));
        }

        void availability(MachineId machine, boolean online) {
            CommandResult<?> result = runtime.setMachineAvailability(machine, online);
            assertInstanceOf(CommandResult.Accepted.class, result);
        }

        void advance(long target, long budget) {
            try {
                List<?> processedEvents = runtime.advanceUntil(SimTime.of(target), budget);
                long processed = processedEvents.size();
                calls.add(new Call(target, budget, processed, false));
            } catch (SimError fault) {
                calls.add(new Call(target, budget, -1, true));
                throw fault;
            }
        }

        /** Drains newly published changes; the driver itself always keeps them. */
        List<RuntimeEventEnvelope> drain() {
            List<RuntimeEventEnvelope> drained = runtime.drainSupportedEvents();
            everything.addAll(drained);
            return drained;
        }

        Closure closure(String scope, long inputExcludedThrough) {
            return new Closure("driver", scope, List.copyOf(calls), inputExcludedThrough, Optional.of(observe()));
        }
    }

    // ======================================================================== P1 stale frontier

    private static final LinearRoutingFamily ONE_FIVE_TICK_STEP =
            new LinearRoutingFamily(List.of(new Step("WORK", 5)), List.of(Resource.of("M", 1, "WORK")));

    @Test
    void p1StaleFrontierFiveVersusOneHundred() {
        MachineId m = ONE_FIVE_TICK_STEP.resourceId("M");
        Driver driver = new Driver(ONE_FIVE_TICK_STEP);
        RunId run = driver.runtime.runId();
        RuntimeObservation basis = driver.observe();
        assertEquals(0, basis.metadata().latestEventSequence());
        driver.submit(1);
        List<RuntimeEventEnvelope> staleChanges = driver.drain();
        RuntimeObservation staleObservation = driver.observe();
        assertEquals(List.of(1L, 2L), sequences(staleChanges));
        assertEquals(List.of(RuntimeEventType.ORDER_ACCEPTED, RuntimeEventType.JOB_DISPATCHED), types(staleChanges));
        driver.advance(100, 1);
        assertEquals(4, driver.observe().metadata().latestEventSequence(), "a processed step publishes at its own time");
        driver.advance(100, 1);
        driver.drain();
        RuntimeObservation post = driver.observe();
        assertEquals(4, post.metadata().latestEventSequence());
        assertEquals(5, post.metadata().currentTime().value());
        assertEquals(RuntimeRunState.QUIESCENT, post.metadata().runState());
        assertEquals(List.of(new Call(100, 1, 1, false), new Call(100, 1, 0, false)), driver.calls);

        Closure bound = driver.closure("closed script", 100);
        Bundle complete = new Bundle(run, basis, driver.everything, List.of(basis, staleObservation, post), Optional.of(bound));
        Bundle stale = new Bundle(run, basis, staleChanges, List.of(basis, staleObservation), Optional.of(bound));
        Closure unbound = new Closure("driver", "closed script", bound.calls(), 100, Optional.empty());

        long p11 = value(activeJobTicks(complete, m, 0, 100));
        String p12 = refused(activeJobTicks(stale, m, 0, 100));
        String p13 = refused(activeJobTicks(stale.withClosure(unbound), m, 0, 100));
        String p14 = refused(activeJobTicks(stale.withoutClosure(), m, 0, 100));
        long p15 = naiveActiveJobTicks(stale, m, 0, 100);
        String p16 = refused(activeJobTicks(complete.withoutClosure(), m, 0, 100));
        long p17 = value(activeJobTicks(complete.withoutClosure(), m, 0, 5));
        String p18 = refused(activeJobTicks(stale.withoutClosure(), m, 0, 5));
        assertEquals(5, p11);
        assertTrue(p12.contains("3..4"), p12);
        assertTrue(p13.contains("not bound"), p13);
        assertTrue(p14.contains("no controller closure"), p14);
        assertEquals(100, p15, "the naive fold reports the stale tail");
        assertTrue(p16.contains("no controller closure") && p16.contains("no held change at time >= 100"), p16);
        assertEquals(5, p17);
        assertTrue(p18.contains("no held change at time >= 5"), p18);
        assertTrue(proofOf(activeJobTicks(complete.withoutClosure(), m, 0, 5)).contains("change 3 at time 5"));

        // P1.9: one budget-1 call only -- the budget is spent, so exhaustion is not demonstrated.
        Driver spent = new Driver(ONE_FIVE_TICK_STEP);
        RuntimeObservation spentBasis = spent.observe();
        spent.submit(1);
        spent.advance(100, 1);
        spent.drain();
        Bundle spentBundle = new Bundle(spent.runtime.runId(), spentBasis, spent.everything, List.of(spentBasis),
                Optional.of(spent.closure("closed script", 100)));
        String p19 = refused(activeJobTicks(spentBundle, m, 0, 100));
        assertTrue(p19.contains("does not demonstrate successful exhaustion"), p19);

        // P1.10: one unbounded call to a target below b.
        Driver short50 = new Driver(ONE_FIVE_TICK_STEP);
        RuntimeObservation shortBasis = short50.observe();
        short50.submit(1);
        short50.advance(50, UNBOUNDED);
        short50.drain();
        Bundle shortBundle = new Bundle(short50.runtime.runId(), shortBasis, short50.everything, List.of(shortBasis),
                Optional.of(short50.closure("closed script", 50)));
        String p110a = refused(activeJobTicks(shortBundle, m, 0, 100));
        long p110b = value(activeJobTicks(shortBundle, m, 0, 50));
        assertTrue(p110a.contains("only through 50"), p110a);
        assertEquals(5, p110b);

        System.out.println(LOG + "P1: complete+closure [0,100)=" + p11 + "; stale+bound closure refuses (" + p12
                + "); unbound refuses; stale without closure refuses; naive stale fold=" + p15
                + "; complete without closure [0,100) refuses; [0,5) by witness=" + p17 + "; stale [0,5) refuses;"
                + " spent budget refuses (" + p19 + "); target 50: [0,100) refuses, [0,50)=" + p110b);
    }

    // ======================================================================== P2 earlier prefix

    @Test
    void p2AnEarlierProvedPrefixSurvivesALaterGap() {
        MachineId m = ONE_FIVE_TICK_STEP.resourceId("M");
        Driver driver = new Driver(ONE_FIVE_TICK_STEP);
        RunId run = driver.runtime.runId();
        RuntimeObservation basis = driver.observe();
        driver.submit(2);
        driver.advance(5, UNBOUNDED);
        List<RuntimeEventEnvelope> kept = driver.drain();
        RuntimeObservation atFive = driver.observe();
        driver.advance(100, UNBOUNDED);
        List<RuntimeEventEnvelope> lost = driver.drain();
        RuntimeObservation post = driver.observe();
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), sequences(kept));
        assertEquals(List.of(RuntimeEventType.ORDER_ACCEPTED, RuntimeEventType.JOB_DISPATCHED, RuntimeEventType.JOB_WAITING,
                RuntimeEventType.JOB_STEP_COMPLETED, RuntimeEventType.JOB_DISPATCHED), types(kept));
        assertEquals(List.of(6L, 7L), sequences(lost));
        assertEquals(List.of(10L, 10L), times(lost));
        assertEquals(7, post.metadata().latestEventSequence());
        assertEquals(10, post.metadata().currentTime().value());

        Bundle gap = new Bundle(run, basis, kept, List.of(basis, atFive, post), Optional.empty());
        Bundle complete = new Bundle(run, basis, driver.everything, List.of(basis, atFive, post), Optional.empty());
        Bundle witnessOnly = new Bundle(run, basis, kept.subList(0, 4), List.of(basis), Optional.empty());

        long p21 = value(activeJobTicks(gap, m, 0, 5));
        String p22 = refused(activeJobTicks(gap, m, 0, 10));
        long p23 = value(activeJobTicks(complete, m, 0, 10));
        long p24 = value(activeJobTicks(witnessOnly, m, 0, 5));
        String p25a = refused(activeJobTicks(gap, m, 5, 10));
        long p25b = value(activeJobTicks(complete, m, 5, 10));
        assertEquals(5, p21);
        assertTrue(p22.contains("cursor 7") && p22.contains("6..7"), p22);
        assertEquals(10, p23);
        assertEquals(5, p24);
        assertTrue(proofOf(activeJobTicks(witnessOnly, m, 0, 5)).contains("change 4 at time 5"));
        assertTrue(p25a.contains("6..7"), p25a);
        assertEquals(5, p25b);

        System.out.println(LOG + "P2: gap holder [0,5)=" + p21 + "; [0,10) refuses (" + p22 + "); complete [0,10)="
                + p23 + "; witness-only [0,5)=" + p24 + "; gap [5,10) refuses; complete [5,10)=" + p25b);
    }

    // ======================================================================== P3 late join

    @Test
    void p3ALateJoinerKeepsBoundedReasoningFromItsBasisButNoPreJoinHistory() {
        assertEquals(List.of("jobId", "orderId", "ordinalWithinOrder", "productId", "status", "currentStep", "totalSteps",
                "currentMachineId", "createdAt", "completedAt"), components(JobObservation.class));
        assertEquals(List.of("machineId", "name", "state", "concurrency", "activeJobIds", "queueDepth", "capacityLiters",
                "setupTime"), components(ResourceObservation.class));
        assertEquals(List.of("backlog", "completedOrders", "completedSalesValue", "averageLeadTime", "throughputPerTick"),
                components(RuntimePerformanceObservation.class));

        MachineId m = ONE_FIVE_TICK_STEP.resourceId("M");
        Driver driver = new Driver(ONE_FIVE_TICK_STEP);
        RunId run = driver.runtime.runId();
        driver.submit(2);
        driver.advance(7, UNBOUNDED);
        driver.drain(); // delivered to someone else before the late joiner arrives
        RuntimeObservation joined = driver.observe();
        assertEquals(5, joined.metadata().latestEventSequence());
        assertEquals(5, joined.metadata().currentTime().value());
        assertEquals(1, resource(joined, m).activeJobIds().size());
        driver.advance(100, UNBOUNDED);
        List<RuntimeEventEnvelope> afterJoin = driver.drain();
        Bundle late = new Bundle(run, joined, afterJoin, List.of(joined, driver.observe()), Optional.empty());

        long p33a = value(activeJobTicks(late, m, 5, 10));
        String p33b = refused(activeJobTicks(late, m, 0, 10));
        assertEquals(5, p33a);
        assertTrue(p33b.contains("pre-join history is unavailable"), p33b);
        System.out.println(LOG + "P3: observation components carry no step start time and no processing total; late"
                + " joiner [5,10)=" + p33a + "; [0,10) refuses (" + p33b + ")");
    }

    // ======================================================================== P4 game period and flow

    private static final LinearRoutingFamily TWO_SLOT_OVEN =
            new LinearRoutingFamily(List.of(new Step("BAKE", 4)), List.of(Resource.of("Oven", 2, "BAKE")));

    private static final LinearRoutingFamily MIX_THEN_WIDE_PACK = new LinearRoutingFamily(
            List.of(new Step("MIX", 4), new Step("PACK", 1)),
            List.of(Resource.of("Mixer", 1, "MIX"), Resource.of("Packer", 2, "PACK")));

    /** P4.1's run: offline, submit 4, online at 0, then exhausted through 20 under a closed script. */
    private static Bundle toggledOven() {
        MachineId oven = TWO_SLOT_OVEN.resourceId("Oven");
        Driver driver = new Driver(TWO_SLOT_OVEN);
        RuntimeObservation basis = driver.observe();
        driver.availability(oven, false);
        driver.submit(4);
        driver.availability(oven, true);
        driver.advance(20, UNBOUNDED);
        driver.drain();
        RuntimeObservation post = driver.observe();
        assertEquals(16, post.metadata().latestEventSequence());
        assertEquals(16, post.metadata().currentTime().value());
        return new Bundle(driver.runtime.runId(), basis, driver.everything, List.of(basis, post),
                Optional.of(driver.closure("closed script", 20)));
    }

    @Test
    void p4GameDefinedPeriodAvailabilityTreatmentAndFlow() {
        MachineId oven = TWO_SLOT_OVEN.resourceId("Oven");
        FactoryModel ovenModel = TWO_SLOT_OVEN.model();
        Bundle toggled = toggledOven();
        assertEquals(List.of(RuntimeEventType.MACHINE_AVAILABILITY_CHANGED, RuntimeEventType.ORDER_ACCEPTED,
                RuntimeEventType.JOB_WAITING, RuntimeEventType.JOB_WAITING, RuntimeEventType.JOB_WAITING,
                RuntimeEventType.JOB_WAITING, RuntimeEventType.MACHINE_AVAILABILITY_CHANGED,
                RuntimeEventType.JOB_DISPATCHED), types(toggled.changes().subList(0, 8)));

        SlotTicks u16 = value(perSlotUtilization(toggled, oven, 0, 16));
        String u20refused = refused(perSlotUtilization(toggled.withoutClosure(), oven, 0, 20));
        SlotTicks u20 = value(perSlotUtilization(toggled, oven, 0, 20));
        Flow f16 = value(flow(toggled, ovenModel, oven, 0, 16));
        Flow f20 = value(flow(toggled, ovenModel, oven, 0, 20));
        assertEquals(new SlotTicks(16, 32), u16);
        assertTrue(u20refused.contains("no controller closure"), u20refused);
        assertEquals(new SlotTicks(16, 40), u20);
        assertEquals(new Flow(16, 12, 0, 0, 4), f16);
        assertEquals(new Flow(16, 12, 0, 0, 12), f20);

        // The tracked continuously-online occupancy oracle refuses the same run.
        ExperimentEvidence substrate = ExperimentRunner.run(new ExperimentFixture("oven-back-online", TWO_SLOT_OVEN.model(),
                List.of(ExperimentStep.setAvailability(oven, false),
                        ExperimentStep.submit(LinearRoutingFamily.PRODUCT, 4, LinearRoutingFamily.UNIT_PRICE),
                        ExperimentStep.setAvailability(oven, true), ExperimentStep.advanceToQuiescence(100),
                        ExperimentStep.captureEvents("whole-run")),
                ExperimentFixture.WindowIntent.COMPLETE_RUN, List.of()));
        String oracle = String.join(" ", OutcomeAssertions.assertUnderdetermined(
                new ProcessingOccupancyOracle(ExperimentEvidence.CLOSING_LABEL).evaluateOn(substrate)).reasons());
        assertTrue(oracle.contains("changed availability"), oracle);

        // P4.2: starved accounting depends on a declared cap.
        MachineId packer = MIX_THEN_WIDE_PACK.resourceId("Packer");
        FactoryModel lineModel = MIX_THEN_WIDE_PACK.model();
        Driver closed = new Driver(MIX_THEN_WIDE_PACK);
        RuntimeObservation closedBasis = closed.observe();
        closed.submit(1);
        closed.advance(100, UNBOUNDED);
        closed.drain();
        RuntimeObservation atFive = closed.observe();
        assertEquals(6, atFive.metadata().latestEventSequence());
        assertEquals(5, atFive.metadata().currentTime().value());
        assertEquals(RuntimeRunState.QUIESCENT, atFive.metadata().runState());
        Bundle closedBundle = new Bundle(closed.runtime.runId(), closedBasis, closed.everything, List.of(closedBasis, atFive),
                Optional.empty());
        Flow f42 = value(flow(closedBundle, lineModel, packer, 0, 5));
        assertEquals(new Flow(1, 0, 8, 4, 1), f42);

        // P4.3: the known-work horizon. A closed and an open run share every supported fact through 5.
        closed.advance(9, UNBOUNDED);
        Bundle closedWithClosure = closedBundle.withClosure(closed.closure("closed script", 9));
        Driver open = new Driver(MIX_THEN_WIDE_PACK);
        RuntimeObservation openBasis = open.observe();
        open.submit(1);
        open.advance(100, UNBOUNDED);
        open.drain();
        RuntimeObservation openAtFive = open.observe();
        open.submit(1);
        open.advance(100, UNBOUNDED);
        open.drain();
        assertEquals(normalized(closed.everything), normalized(open.everything.subList(0, 6)),
                "changes 1..6 are identical in both runs");
        assertEquals(normalized(atFive), normalized(openAtFive), "so is the observation at 5");
        assertEquals(List.of(5L, 5L, 9L, 9L, 10L, 10L), times(open.everything.subList(6, 12)));
        Bundle openBundle = new Bundle(open.runtime.runId(), openBasis, open.everything,
                List.of(openBasis, openAtFive, open.observe()), Optional.empty());

        Flow f43closed = value(flow(closedWithClosure, lineModel, packer, 5, 9));
        String f43refused = refused(flow(closedBundle, lineModel, packer, 5, 9));
        Flow f43open = value(flow(openBundle, lineModel, packer, 5, 9));
        assertEquals(new Flow(0, 0, 0, 0, 8), f43closed);
        assertTrue(f43refused.contains("no controller closure"), f43refused);
        assertEquals(new Flow(0, 0, 8, 4, 0), f43open);

        System.out.println(LOG + "P4: toggled Oven per-slot [0,16)=" + u16 + ", [0,20) refuses without closure, with closure "
                + u20 + "; flow [0,16)=" + f16 + ", [0,20)=" + f20 + "; continuously-online oracle refuses; Packer [0,5)="
                + f42 + "; [5,9) closed+closure=" + f43closed + ", closed without closure refuses, open=" + f43open);
    }

    // ======================================================================== P5 cross-boundary work

    private static final LinearRoutingFamily ONE_TEN_TICK_STEP =
            new LinearRoutingFamily(List.of(new Step("WORK", 10)), List.of(Resource.of("M", 1, "WORK")));

    @Test
    void p5UnfinishedWorkCrossingTheUpperBoundary() {
        MachineId m = ONE_TEN_TICK_STEP.resourceId("M");
        FactoryModel model = ONE_TEN_TICK_STEP.model();
        Driver driver = new Driver(ONE_TEN_TICK_STEP);
        RuntimeObservation basis = driver.observe();
        driver.submit(1);
        driver.advance(100, UNBOUNDED);
        driver.drain();
        RuntimeObservation post = driver.observe();
        assertEquals(List.of(0L, 0L, 10L, 10L), times(driver.everything));
        Bundle complete = new Bundle(driver.runtime.runId(), basis, driver.everything, List.of(basis, post), Optional.empty());

        long occ06 = value(activeJobTicks(complete, m, 0, 6));
        long credited06 = value(completionCreditedTicks(complete, model, m, 0, 6));
        long credited010 = value(completionCreditedTicks(complete, model, m, 0, 10));
        long occ010 = value(activeJobTicks(complete, m, 0, 10));
        long occ610 = value(activeJobTicks(complete, m, 6, 10));
        String occ612refused = refused(activeJobTicks(complete, m, 6, 12));

        driver.advance(12, UNBOUNDED);
        long occ612 = value(activeJobTicks(complete.withClosure(driver.closure("closed script", 12)), m, 6, 12));

        Driver stopsAtSix = new Driver(ONE_TEN_TICK_STEP);
        RuntimeObservation sixBasis = stopsAtSix.observe();
        stopsAtSix.submit(1);
        stopsAtSix.advance(6, UNBOUNDED);
        stopsAtSix.drain();
        assertEquals(new Call(6, UNBOUNDED, 0, false), stopsAtSix.calls.getLast());
        RuntimeObservation atSix = stopsAtSix.observe();
        assertEquals(2, atSix.metadata().latestEventSequence());
        assertEquals(0, atSix.metadata().currentTime().value(), "a target time is not a clock advance");
        long occ06controller = value(activeJobTicks(new Bundle(stopsAtSix.runtime.runId(), sixBasis, stopsAtSix.everything,
                List.of(sixBasis, atSix), Optional.of(stopsAtSix.closure("closed script", 6))), m, 0, 6));

        assertEquals(6, occ06);
        assertEquals(6, occ06controller);
        assertEquals(0, credited06);
        assertEquals(0, credited010);
        assertEquals(10, occ010);
        assertEquals(4, occ610);
        assertTrue(occ612refused.contains("no controller closure"), occ612refused);
        assertEquals(4, occ612);
        System.out.println(LOG + "P5: running occupancy [0,6)=" + occ06 + " by witness and " + occ06controller
                + " by closure; completion-credited [0,6)=" + credited06 + ", [0,10)=" + credited010 + "; occupancy [0,10)="
                + occ010 + ", [6,10)=" + occ610 + ", [6,12) refuses without closure, " + occ612 + " with closure");
    }

    // ======================================================================== P6 custody separation

    /** A stand-in for a game-local call site of a named definition. */
    private static Outcome<SlotTicks> gameLocal(Bundle bundle, MachineId machine, long a, long b) {
        return perSlotUtilization(bundle, machine, a, b);
    }

    /** A stand-in for a shared-method candidate's call site of the same named definition. */
    private static Outcome<SlotTicks> sharedCandidate(Bundle bundle, MachineId machine, long a, long b) {
        return perSlotUtilization(bundle, machine, a, b);
    }

    @Test
    void p6OneHolderSuppliesTwoCallSitesWithoutTransferringCustody() {
        MachineId oven = TWO_SLOT_OVEN.resourceId("Oven");
        FactoryModel ovenModel = TWO_SLOT_OVEN.model();
        Bundle bundle = toggledOven();
        Bundle stripped = bundle.withoutClosure();
        RuntimeObservation post = bundle.observations().getLast();

        assertEquals(gameLocal(bundle, oven, 0, 16), sharedCandidate(bundle, oven, 0, 16));
        assertEquals(gameLocal(bundle, oven, 0, 20), sharedCandidate(bundle, oven, 0, 20));
        assertEquals(new SlotTicks(16, 32), value(gameLocal(bundle, oven, 0, 16)));
        assertEquals(new SlotTicks(16, 40), value(sharedCandidate(bundle, oven, 0, 20)));
        String trust = proofOf(sharedCandidate(bundle, oven, 0, 20));
        assertTrue(trust.contains("controller closure by driver (closed script)"), trust);

        assertEquals(new SlotTicks(16, 32), value(sharedCandidate(stripped, oven, 0, 16)));
        String strippedRefusal = refused(sharedCandidate(stripped, oven, 0, 20));
        String strippedFlow = refused(flow(stripped, ovenModel, oven, 16, 20));
        assertEquals(new SlotState(0, 2), slotState(post, oven));
        Bundle observationOnly = new Bundle(bundle.runId(), post, List.of(), List.of(post), Optional.empty());
        String observationOnlyRefusal = refused(sharedCandidate(observationOnly, oven, 0, 16));
        assertTrue(strippedRefusal.contains("no controller closure"), strippedRefusal);
        assertTrue(strippedFlow.contains("no controller closure"), strippedFlow);
        assertTrue(observationOnlyRefusal.contains("pre-join history is unavailable"), observationOnlyRefusal);
        System.out.println(LOG + "P6: both call sites agree, [0,16)=" + value(gameLocal(bundle, oven, 0, 16)) + ", [0,20)="
                + value(gameLocal(bundle, oven, 0, 20)) + " under '" + trust + "'; stripped: [0,16) unchanged, [0,20)"
                + " refuses, flow [16,20) refuses, slot state " + slotState(post, oven) + "; observation only refuses [0,16)");
    }

    // ======================================================================== P7 command time and clock

    @Test
    void p7CommandTimeAndTheObservationClock() {
        // A closed script: one unit, then no further input through 100.
        Driver closedScript = new Driver(ONE_FIVE_TICK_STEP);
        RunId run = closedScript.runtime.runId();
        RuntimeObservation basis = closedScript.observe();
        closedScript.submit(1);
        closedScript.advance(100, UNBOUNDED);
        closedScript.drain();
        RuntimeObservation afterFirst = closedScript.observe();
        assertEquals(4, afterFirst.metadata().latestEventSequence());
        assertEquals(5, afterFirst.metadata().currentTime().value(), "a requested target is not a clock advance");
        assertEquals(0.2, afterFirst.performance().throughputPerTick(), 0.0);
        assertEquals(5.0, afterFirst.performance().averageLeadTime(), 0.0);
        Bundle bundle = new Bundle(run, basis, closedScript.everything, List.of(basis, afterFirst),
                Optional.of(closedScript.closure("closed script", 100)));
        PeriodRate period = value(completedOrdersPerPeriodTick(bundle, 0, 100));
        String periodRefused = refused(completedOrdersPerPeriodTick(bundle.withoutClosure(), 0, 100));
        assertEquals(new PeriodRate(1, 100), period);
        assertTrue(periodRefused.contains("no controller closure"), periodRefused);

        // The same prefix, then a second unit submitted after the same call: it applies at the observed time.
        Driver interleaved = new Driver(ONE_FIVE_TICK_STEP);
        interleaved.submit(1);
        interleaved.advance(100, UNBOUNDED);
        interleaved.drain();
        interleaved.submit(1);
        List<RuntimeEventEnvelope> submitted = interleaved.drain();
        assertEquals(List.of(5L, 6L), sequences(submitted));
        assertEquals(List.of(5L, 5L), times(submitted), "the command applies at the observed time, not the target");
        interleaved.advance(200, UNBOUNDED);
        interleaved.drain();
        RuntimeObservation end = interleaved.observe();
        assertEquals(8, end.metadata().latestEventSequence());
        assertEquals(10, end.metadata().currentTime().value());
        assertEquals(2, end.performance().completedOrders());
        assertEquals(5.0, end.performance().averageLeadTime(), 0.0);
        assertEquals(0.2, end.performance().throughputPerTick(), 0.0);
        System.out.println(LOG + "P7: after advanceUntil(100) observation (4,5), throughputPerTick "
                + afterFirst.performance().throughputPerTick() + "; period rate [0,100)=" + period
                + " with closure, refused without; a second submission is accepted at " + submitted.getFirst().simulationTime()
                + "; end (8,10), mean lead " + end.performance().averageLeadTime() + ", throughput "
                + end.performance().throughputPerTick());
    }

    // ======================================================================== P8 fault provenance

    @Test
    void p8CompleteSupportedChangesDoNotShowAFault() {
        LinearRoutingFamily overflowing = new LinearRoutingFamily(
                List.of(new Step("CUT", 5), new Step("HOLD", Long.MAX_VALUE)),
                List.of(Resource.of("Cutter", 1, "CUT"), Resource.of("Holder", 1, "HOLD")));
        MachineId cutter = overflowing.resourceId("Cutter");
        MachineId holder = overflowing.resourceId("Holder");
        Driver driver = new Driver(overflowing);
        RunId run = driver.runtime.runId();
        RuntimeObservation basis = driver.observe();
        driver.submit(1);
        assertThrows(SimError.EventOrderingViolation.class, () -> driver.advance(100, UNBOUNDED));
        driver.drain();
        RuntimeObservation post = driver.observe();
        assertEquals(List.of(1L, 2L, 3L, 4L), sequences(driver.everything));
        assertEquals(List.of(RuntimeEventType.ORDER_ACCEPTED, RuntimeEventType.JOB_DISPATCHED,
                RuntimeEventType.JOB_STEP_COMPLETED, RuntimeEventType.JOB_DISPATCHED), types(driver.everything));
        assertEquals(List.of("ORDER_ACCEPTED", "JOB_DISPATCHED", "JOB_WAITING", "JOB_STEP_COMPLETED", "ORDER_COMPLETED",
                "MACHINE_AVAILABILITY_CHANGED"), Arrays.stream(RuntimeEventType.values()).map(Enum::name).toList());
        assertEquals(4, post.metadata().latestEventSequence());
        assertEquals(5, post.metadata().currentTime().value());
        assertEquals(1, resource(post, holder).activeJobIds().size());
        assertEquals(RuntimeRunState.QUIESCENT, post.metadata().runState());

        Closure honest = driver.closure("closed script", 100);
        Closure hiding = new Closure("driver", "closed script", List.of(new Call(100, UNBOUNDED, 2, false)), 100,
                Optional.of(post));
        Bundle bundle = new Bundle(run, basis, driver.everything, List.of(basis, post), Optional.of(honest));
        String honestRefusal = refused(activeJobTicks(bundle, holder, 0, 100));
        long hidden = value(activeJobTicks(bundle.withClosure(hiding), holder, 0, 100));
        long cutterFive = value(activeJobTicks(bundle.withoutClosure(), cutter, 0, 5));
        long holderFive = value(activeJobTicks(bundle.withoutClosure(), holder, 0, 5));
        assertTrue(honestRefusal.contains("does not demonstrate successful exhaustion"), honestRefusal);
        assertEquals(95, hidden);
        assertEquals(5, cutterFive);
        assertEquals(0, holderFive);
        System.out.println(LOG + "P8: faulted run publishes " + types(driver.everything) + "; observation (4,5) "
                + post.metadata().runState() + " with Holder active " + resource(post, holder).activeJobIds().size()
                + "; honest closure refuses; fault-hiding closure gives " + hidden + "; [0,5) Cutter " + cutterFive
                + ", Holder " + holderFive);
    }

    // ======================================================================== helpers

    @SuppressWarnings("unchecked")
    private static <T> T value(Outcome<T> outcome) {
        return ((Outcome.Value<T>) assertInstanceOf(Outcome.Value.class, outcome,
                () -> "expected a value, got " + outcome)).value();
    }

    private static String proofOf(Outcome<?> outcome) {
        return assertInstanceOf(Outcome.Value.class, outcome, () -> "expected a value, got " + outcome).proof();
    }

    private static String refused(Outcome<?> outcome) {
        return assertInstanceOf(Outcome.Refused.class, outcome, () -> "expected a refusal, got " + outcome).reason();
    }

    private static ResourceObservation resource(RuntimeObservation observation, MachineId machine) {
        return observation.resources().stream().filter(r -> r.machineId().equals(machine)).findFirst().orElseThrow();
    }

    private static List<Long> sequences(List<RuntimeEventEnvelope> changes) {
        return changes.stream().map(RuntimeEventEnvelope::sequence).toList();
    }

    private static List<Long> times(List<RuntimeEventEnvelope> changes) {
        return changes.stream().map(change -> change.simulationTime().value()).toList();
    }

    private static List<RuntimeEventType> types(List<RuntimeEventEnvelope> changes) {
        return changes.stream().map(RuntimeEventEnvelope::eventType).toList();
    }

    private static List<String> components(Class<? extends Record> type) {
        return Arrays.stream(type.getRecordComponents()).map(RecordComponent::getName).toList();
    }

    private static List<RuntimeEventEnvelope> normalized(List<RuntimeEventEnvelope> changes) {
        return changes.stream()
                .map(change -> new RuntimeEventEnvelope(ExperimentEvidence.NORMALIZED_RUN_ID, change.sequence(),
                        change.simulationTime(), change.eventType(), change.modelFingerprint(),
                        change.controlledRevisionId(), change.affectedEntityRefs(), change.payload()))
                .toList();
    }

    private static RuntimeObservation normalized(RuntimeObservation observation) {
        RuntimeObservationMetadata metadata = observation.metadata();
        return new RuntimeObservation(new RuntimeObservationMetadata(ExperimentEvidence.NORMALIZED_RUN_ID,
                metadata.modelFingerprint(), metadata.currentTime(), metadata.runState(), metadata.latestEventSequence()),
                observation.resources(), observation.orders(), observation.jobs(), observation.pendingWork(),
                observation.performance());
    }
}
