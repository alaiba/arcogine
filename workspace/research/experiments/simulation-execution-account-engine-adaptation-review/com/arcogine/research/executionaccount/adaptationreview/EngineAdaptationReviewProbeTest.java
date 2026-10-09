package com.arcogine.research.executionaccount.adaptationreview;

import static org.junit.jupiter.api.Assertions.*;

import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.process.CommandResult;
import com.arcogine.factory.process.FactoryRuntime;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeEventType;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.factory.process.RuntimeRunState;
import com.arcogine.research.experiment.LinearRoutingFamily;
import com.arcogine.research.experiment.LinearRoutingFamily.Resource;
import com.arcogine.research.experiment.LinearRoutingFamily.Step;
import com.arcogine.types.MachineId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Independent adversarial-review probes for the Engine-adaptation follow-up report. Every expected
 * value was derived by hand from {@code FactoryHandler}/{@code FactoryRuntime} before execution.
 * These probes run against the current Engine only: the authoritative-turn unit is <em>emulated</em>
 * by a driver over public session-control calls, which shows what that unit would guarantee, not
 * that a changed Engine behaves this way.
 */
class EngineAdaptationReviewProbeTest {

    private static final long MAX = Long.MAX_VALUE;

    /** One five-tick step on one concurrency-1 machine. */
    private static final LinearRoutingFamily S = new LinearRoutingFamily(
            List.of(new Step("PROCESS", 5)), List.of(Resource.of("M1", 1, "PROCESS")));

    /** A shared-eligibility first step (backlog dispatch path) and a dedicated second step (queue path). */
    private static final LinearRoutingFamily POOL = new LinearRoutingFamily(
            List.of(new Step("FORM", 4), new Step("FINISH", 3)),
            List.of(Resource.of("A", 1, "FORM"), Resource.of("B", 1, "FORM"), Resource.of("C", 1, "FINISH")));

    /** A second step whose completion time overflows when dispatched (fault during advancement). */
    private static final LinearRoutingFamily F = new LinearRoutingFamily(
            List.of(new Step("CUT", 5), new Step("HOLD", Long.MAX_VALUE)),
            List.of(Resource.of("Cutter", 1, "CUT"), Resource.of("Holder", 1, "HOLD")));

    private static FactoryRuntime runtime(LinearRoutingFamily family) {
        return FactoryRuntime.forModel(FactoryModelPublisher.publish(family.model()));
    }

    private static void submit(FactoryRuntime runtime, long quantity) {
        assertInstanceOf(CommandResult.Accepted.class,
                runtime.submitWorkload(LinearRoutingFamily.PRODUCT, quantity, LinearRoutingFamily.UNIT_PRICE));
    }

    private static long cursor(FactoryRuntime runtime) {
        return runtime.observe().metadata().latestEventSequence();
    }

    private static long observedTime(FactoryRuntime runtime) {
        return runtime.observe().metadata().currentTime().value();
    }

    private static void assertSameExceptRunIdentity(RuntimeObservation expected, RuntimeObservation actual) {
        assertEquals(expected.metadata().currentTime(), actual.metadata().currentTime());
        assertEquals(expected.metadata().runState(), actual.metadata().runState());
        assertEquals(expected.metadata().latestEventSequence(), actual.metadata().latestEventSequence());
        assertEquals(expected.metadata().modelFingerprint(), actual.metadata().modelFingerprint());
        assertEquals(expected.resources(), actual.resources());
        assertEquals(expected.orders(), actual.orders());
        assertEquals(expected.jobs(), actual.jobs());
        assertEquals(expected.pendingWork(), actual.pendingWork());
        assertEquals(expected.performance(), actual.performance());
    }

    /** A supported event without its run identity, so independently created runs can be compared. */
    private record Change(long sequence, long time, RuntimeEventType type, RuntimeEventPayload payload) {
        static List<Change> of(List<RuntimeEventEnvelope> events) {
            return events.stream()
                    .map(e -> new Change(e.sequence(), e.simulationTime().value(), e.eventType(), e.payload()))
                    .toList();
        }
    }

    private static long acceptedAt(List<RuntimeEventEnvelope> events) {
        return events.stream()
                .filter(event -> event.eventType() == RuntimeEventType.ORDER_ACCEPTED)
                .reduce((first, second) -> second)
                .orElseThrow()
                .simulationTime()
                .value();
    }

    // ---- Emulated authoritative-turn unit (the sketched E-guard) over public calls only ----

    /**
     * One authoritative turn: when authoritative work is pending, process single internal events
     * until the supported sequence advances. Leading markers are absorbed; a marker-only tail is
     * left queued (QUIESCENT means no authoritative event remains). Returns internal events used.
     */
    static int turn(FactoryRuntime runtime, long target) {
        if (runtime.observe().metadata().runState() == RuntimeRunState.QUIESCENT) {
            return 0;
        }
        long before = cursor(runtime);
        int processed = 0;
        while (true) {
            int one = runtime.advanceUntil(SimTime.of(target), 1).size();
            if (one == 0) {
                return processed;
            }
            processed += one;
            if (cursor(runtime) > before) {
                return processed;
            }
        }
    }

    /** One public operation of a replayable script. */
    sealed interface Op {
        void apply(FactoryRuntime runtime);
    }

    record Submit(long quantity) implements Op {
        public void apply(FactoryRuntime runtime) {
            submit(runtime, quantity);
        }
    }

    record Availability(long machine, boolean online) implements Op {
        public void apply(FactoryRuntime runtime) {
            CommandResult<?> result = runtime.setMachineAvailability(new MachineId(machine), online);
            assertFalse(result instanceof CommandResult.Rejected<?>, "probe availability must not be rejected");
        }
    }

    /** Today's unit: at most {@code budget} internal events. A fault is absorbed after publication. */
    record Raw(long target, long budget) implements Op {
        public void apply(FactoryRuntime runtime) {
            try {
                runtime.advanceUntil(SimTime.of(target), budget);
            } catch (SimError fault) {
                // the finally-path publication already happened; the probe continues
            }
        }
    }

    /** The emulated authoritative-turn unit. */
    record Turn(long target) implements Op {
        public void apply(FactoryRuntime runtime) {
            try {
                turn(runtime, target);
            } catch (SimError fault) {
                // as for Raw
            }
        }
    }

    /**
     * The time at which the next command would apply, read without disturbing the probed run: a
     * fresh twin replays the same public script and reports its next order's acceptance time.
     */
    private static long commandTime(LinearRoutingFamily family, List<Op> script) {
        FactoryRuntime twin = runtime(family);
        script.forEach(op -> op.apply(twin));
        twin.drainSupportedEvents();
        submit(twin, 1);
        return acceptedAt(twin.drainSupportedEvents());
    }

    /** {@code commandTime - observedTime} after every prefix of {@code script}. */
    private static List<Long> lagProfile(LinearRoutingFamily family, List<Op> script) {
        FactoryRuntime live = runtime(family);
        List<Op> prefix = new ArrayList<>();
        List<Long> lags = new ArrayList<>();
        for (Op op : script) {
            op.apply(live);
            prefix.add(op);
            lags.add(commandTime(family, prefix) - observedTime(live));
        }
        return lags;
    }

    private static List<Op> repeat(Op first, Op step, int times) {
        List<Op> script = new ArrayList<>();
        script.add(first);
        for (int i = 0; i < times; i++) {
            script.add(step);
        }
        return script;
    }

    // ---- R1: the budget channel alone, with no time movement (an OrderCompleted tail) ----

    @Test
    void aQuiescentMarkerTailMakesTheSameNextCallDoDifferentThingsWithoutAnyTimeSplit() {
        FactoryRuntime p = runtime(S);
        submit(p, 1);
        assertEquals(1, p.advanceUntil(SimTime.of(100), 1).size()); // TaskEnd@5; OrderCompleted@5 stays queued
        FactoryRuntime q = runtime(S);
        submit(q, 1);
        assertEquals(2, q.advanceUntil(SimTime.of(100), 2).size()); // TaskEnd@5 and the marker
        assertEquals(RuntimeRunState.QUIESCENT, p.observe().metadata().runState());
        assertSameExceptRunIdentity(q.observe(), p.observe()); // QUIESCENT, sequence 4, time 5

        submit(p, 1); // both at time 5: no time split exists in either run
        submit(q, 1);
        assertSameExceptRunIdentity(q.observe(), p.observe()); // ACTIVE, sequence 6, time 5
        p.drainSupportedEvents();
        q.drainSupportedEvents();

        // The identical next public call:
        assertEquals(1, p.advanceUntil(SimTime.of(100), 1).size()); // consumes the stale marker
        assertEquals(1, q.advanceUntil(SimTime.of(100), 1).size()); // completes job 2 at 10
        assertTrue(p.drainSupportedEvents().isEmpty());
        assertEquals(List.of(RuntimeEventType.JOB_STEP_COMPLETED, RuntimeEventType.ORDER_COMPLETED),
                q.drainSupportedEvents().stream().map(RuntimeEventEnvelope::eventType).toList());

        submit(p, 1);
        submit(q, 1);
        assertEquals(5, acceptedAt(p.drainSupportedEvents()));
        assertEquals(10, acceptedAt(q.drainSupportedEvents()));
        p.advanceUntil(SimTime.of(100), MAX);
        q.advanceUntil(SimTime.of(100), MAX);
        assertEquals(20.0 / 3.0, p.observe().performance().averageLeadTime(), 1e-12); // leads 5, 5, 10
        assertEquals(5.0, q.observe().performance().averageLeadTime(), 1e-12); // leads 5, 5, 5
    }

    // ---- R2: the emulated authoritative-turn unit keeps the command clock on the supported boundary ----

    @Test
    void todaysUnitSplitsTheCommandClockExactlyAfterALoneStartMarker() {
        List<Long> raw = lagProfile(S, repeat(new Submit(3), new Raw(MAX, 1), 7));
        assertEquals(List.of(0L, 0L, 5L, 0L, 5L, 0L, 0L, 0L), raw);
    }

    @Test
    void theEmulatedTurnUnitNeverSplitsTheCommandClockOnAnyReachablePath() {
        // queue dispatch
        assertTrue(lagProfile(S, repeat(new Submit(3), new Turn(MAX), 6)).stream().allMatch(lag -> lag == 0));
        // shared backlog, dedicated queue, equal-time completions and a final order marker
        assertTrue(lagProfile(POOL, repeat(new Submit(4), new Turn(MAX), 10)).stream().allMatch(lag -> lag == 0));
        // time guard with the next authoritative event beyond the target
        List<Op> guarded = new ArrayList<>(List.of(new Submit(4), new Turn(3), new Turn(4), new Turn(4),
                new Turn(5), new Turn(7), new Turn(8), new Turn(9)));
        for (int i = 0; i < 6; i++) {
            guarded.add(new Turn(MAX));
        }
        assertTrue(lagProfile(POOL, guarded).stream().allMatch(lag -> lag == 0));
        // the availability cascade schedules the start marker inside a command
        List<Op> cascade = new ArrayList<>(List.of(new Availability(1, false), new Submit(2), new Availability(1, true)));
        for (int i = 0; i < 4; i++) {
            cascade.add(new Turn(MAX));
        }
        assertTrue(lagProfile(S, cascade).stream().allMatch(lag -> lag == 0));
        // commands interleaved between turns
        List<Op> mixed = List.of(new Submit(2), new Turn(MAX), new Submit(1), new Turn(MAX), new Turn(MAX),
                new Submit(1), new Turn(MAX), new Turn(MAX), new Turn(MAX), new Turn(MAX));
        assertTrue(lagProfile(S, mixed).stream().allMatch(lag -> lag == 0));
        // a fault during a turn
        assertEquals(List.of(0L, 0L, 0L), lagProfile(F, List.of(new Submit(2), new Turn(MAX), new Turn(MAX))));
    }

    @Test
    void todaysUnitSplitsTheClockOnTheSameMixedScriptsTheTurnUnitKeepsWhole() {
        List<Op> mixed = List.of(new Submit(2), new Raw(MAX, 1), new Submit(1), new Raw(MAX, 1), new Raw(MAX, 1),
                new Submit(1), new Raw(MAX, 1), new Raw(MAX, 1), new Raw(MAX, 1), new Raw(MAX, 1));
        assertTrue(lagProfile(S, mixed).stream().anyMatch(lag -> lag > 0));
        assertTrue(lagProfile(POOL, repeat(new Submit(4), new Raw(MAX, 1), 24)).stream().anyMatch(lag -> lag > 0));
        List<Op> cascade = new ArrayList<>(List.of(new Availability(1, false), new Submit(2), new Availability(1, true)));
        cascade.add(new Raw(MAX, 1));
        assertEquals(5L, lagProfile(S, cascade).getLast()); // the cascade's start marker at 5, alone
        // The fault path schedules no start marker, so even today's unit keeps the clock whole there.
        assertEquals(List.of(0L, 0L, 0L), lagProfile(F, List.of(new Submit(2), new Raw(MAX, 1), new Raw(MAX, 1))));
    }

    // ---- R3: Game no-trigger case — all inputs at start, any presentation pacing ----

    private static List<Change> paced(LinearRoutingFamily family, long quantity, long framesBudget) {
        FactoryRuntime runtime = runtime(family);
        submit(runtime, quantity);
        while (runtime.advanceUntil(SimTime.of(MAX), framesBudget).size() > 0) {
            // one presentation frame
        }
        return Change.of(runtime.drainSupportedEvents());
    }

    @Test
    void presentationPacingCannotChangeOutcomesWhenEveryInputIsGivenAtStart() {
        List<Change> unbounded = paced(POOL, 6, MAX);
        for (long budget : new long[] {1, 2, 3, 7}) {
            assertEquals(unbounded, paced(POOL, 6, budget), "frame budget " + budget);
        }
        FactoryRuntime perTick = runtime(POOL);
        submit(perTick, 6);
        for (long tick = 1; tick <= 40; tick++) {
            perTick.advanceUntil(SimTime.of(tick), MAX); // the static prototype's pacing
        }
        assertEquals(unbounded, Change.of(perTick.drainSupportedEvents()));
    }

    @Test
    void markerOnlyFramesAreTheOnlyCosmeticEffectOfSmallFrameBudgets() {
        FactoryRuntime runtime = runtime(S);
        submit(runtime, 3);
        int silentFrames = 0;
        while (true) {
            long before = cursor(runtime);
            if (runtime.advanceUntil(SimTime.of(MAX), 1).isEmpty()) {
                break;
            }
            if (cursor(runtime) == before) {
                silentFrames++;
            }
        }
        assertEquals(3, silentFrames); // TaskStart@10, TaskStart@15, OrderCompleted@15
    }

    // ---- R4: Game trigger case — a player pauses at a supported state and commands ----

    /** Advance {@code perFrame} units per frame until the observed sequence reaches {@code pauseAt}. */
    private static FactoryRuntime pauseAt(long pauseAt, long perFrame, boolean turns) {
        FactoryRuntime runtime = runtime(S);
        submit(runtime, 2);
        while (cursor(runtime) < pauseAt) {
            if (turns) {
                for (int i = 0; i < perFrame; i++) {
                    turn(runtime, MAX);
                }
            } else {
                runtime.advanceUntil(SimTime.of(MAX), perFrame);
            }
        }
        return runtime;
    }

    @Test
    void underTodaysUnitPresentationSpeedChangesTheOutcomeOfAPlayerCommandAtAnIdenticalSupportedState() {
        FactoryRuntime slow = pauseAt(5, 1, false);
        FactoryRuntime fast = pauseAt(5, 2, false);
        assertSameExceptRunIdentity(slow.observe(), fast.observe()); // sequence 5, time 5, ACTIVE
        slow.drainSupportedEvents();
        fast.drainSupportedEvents();
        submit(slow, 1);
        submit(fast, 1);
        assertEquals(5, acceptedAt(slow.drainSupportedEvents()));
        assertEquals(10, acceptedAt(fast.drainSupportedEvents())); // the frame also took job 2's start marker
        slow.advanceUntil(SimTime.of(MAX), MAX);
        fast.advanceUntil(SimTime.of(MAX), MAX);
        assertEquals(10.0, slow.observe().performance().averageLeadTime(), 1e-12);
        assertEquals(7.5, fast.observe().performance().averageLeadTime(), 1e-12);
    }

    @Test
    void underTheTurnUnitASpeedDifferenceIsAVisibleStateDifferenceAndTheCommandLandsAtObservedTime() {
        FactoryRuntime slow = pauseAt(5, 1, true);
        FactoryRuntime fast = pauseAt(5, 2, true);
        assertEquals(5, observedTime(slow));
        assertEquals(10, observedTime(fast)); // the player sees job 2 finished: a different, visible state
        for (FactoryRuntime runtime : List.of(slow, fast)) {
            long seen = observedTime(runtime);
            runtime.drainSupportedEvents();
            submit(runtime, 1);
            assertEquals(seen, acceptedAt(runtime.drainSupportedEvents()));
        }
    }
}
