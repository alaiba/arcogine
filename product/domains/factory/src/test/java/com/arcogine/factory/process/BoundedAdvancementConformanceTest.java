package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Conformance evidence for the Engine rule that a Factory session schedules only authoritative step
 * completions (docs/architecture/engine-semantics.md section 4 rule 3), and for what that rule
 * makes true of bounded advancement and command time (section 1.2): an event budget is spent only
 * on step completions, each published at its own simulated time, so after any call returns the
 * next command applies at exactly the supported observation's current time, and an advancement
 * budget alone cannot make two runs at an identical supported state diverge.
 *
 * <p>Every expected value is derived by hand from the small models below. The time at which a
 * command would apply is measured without disturbing the run under test: a {@link
 * FactoryRuntime#reset()} twin replays the same public script and reports when one further
 * submission is accepted. Driven entirely through {@link FactoryRuntime}.
 */
class BoundedAdvancementConformanceTest {

    private static final ProductId PRODUCT = new ProductId(1);
    private static final double UNIT_PRICE = 2.0;
    private static final long UNBOUNDED = Long.MAX_VALUE;

    /** One five-tick step on one concurrency-1 machine: further units wait in that machine's queue. */
    private static FactoryModelVersion singleMachine() {
        return publish(
                List.of(new ConfiguredResource(new MachineId(1), "M1", 1, null, 0)),
                List.of(new OperationStepDefinition(1, "PROCESS", Set.of(new MachineId(1)), 5)));
    }

    /**
     * FORM (4 ticks) on either of {@code A}/{@code B} -- the shared multi-eligible backlog -- then
     * FINISH (3 ticks) on the dedicated {@code C}, whose own queue absorbs equal-time arrivals.
     */
    private static FactoryModelVersion sharedThenDedicated() {
        return publish(
                List.of(
                        new ConfiguredResource(new MachineId(1), "A", 1, null, 0),
                        new ConfiguredResource(new MachineId(2), "B", 1, null, 0),
                        new ConfiguredResource(new MachineId(3), "C", 1, null, 0)),
                List.of(
                        new OperationStepDefinition(1, "FORM", Set.of(new MachineId(1), new MachineId(2)), 4),
                        new OperationStepDefinition(2, "FINISH", Set.of(new MachineId(3)), 3)));
    }

    /** CUT (5 ticks) then HOLD ({@code Long.MAX_VALUE} ticks): dispatching HOLD at a positive time faults. */
    private static FactoryModelVersion overflowingSecondStep() {
        return publish(
                List.of(
                        new ConfiguredResource(new MachineId(1), "Cutter", 1, null, 0),
                        new ConfiguredResource(new MachineId(2), "Holder", 1, null, 0)),
                List.of(
                        new OperationStepDefinition(1, "CUT", Set.of(new MachineId(1)), 5),
                        new OperationStepDefinition(2, "HOLD", Set.of(new MachineId(2)), Long.MAX_VALUE)));
    }

    private static FactoryModelVersion publish(List<ConfiguredResource> resources, List<OperationStepDefinition> steps) {
        return FactoryModelPublisher.publish(new FactoryModel(
                resources,
                List.of(new OperationDefinition(1, "Route", steps)),
                List.of(new ProductDefinition(PRODUCT, "Widget", 1))));
    }

    /** One public call of a replayable session script. */
    private sealed interface Op {
        void apply(FactoryRuntime runtime);
    }

    private record Submit(long quantity) implements Op {
        @Override
        public void apply(FactoryRuntime runtime) {
            assertInstanceOf(CommandResult.Accepted.class, runtime.submitWorkload(PRODUCT, quantity, UNIT_PRICE));
        }
    }

    private record Availability(long machine, boolean online) implements Op {
        @Override
        public void apply(FactoryRuntime runtime) {
            assertInstanceOf(
                    CommandResult.Accepted.class, runtime.setMachineAvailability(new MachineId(machine), online));
        }
    }

    /**
     * {@code advanceUntil(target, budget)}. An Engine fault is absorbed so the script can continue:
     * the supported events for the mutations that did occur have already been published.
     */
    private record Step(long target, long budget) implements Op {
        @Override
        public void apply(FactoryRuntime runtime) {
            try {
                runtime.advanceUntil(SimTime.of(target), budget);
            } catch (SimError fault) {
                assertInstanceOf(SimError.EventOrderingViolation.class, fault);
            }
        }
    }

    private static List<Op> script(Op first, Op step, int steps) {
        List<Op> script = new ArrayList<>();
        script.add(first);
        for (int i = 0; i < steps; i++) {
            script.add(step);
        }
        return script;
    }

    private static long observedTime(FactoryRuntime runtime) {
        return runtime.observe().metadata().currentTime().value();
    }

    private static long acceptedAt(List<RuntimeEventEnvelope> events) {
        return events.stream()
                .filter(event -> event.eventType() == RuntimeEventType.ORDER_ACCEPTED)
                .reduce((first, second) -> second)
                .orElseThrow()
                .simulationTime()
                .value();
    }

    private static long lastAcceptedAt(List<Change> changes) {
        return changes.stream()
                .filter(change -> change.type() == RuntimeEventType.ORDER_ACCEPTED)
                .reduce((first, second) -> second)
                .orElseThrow()
                .time();
    }

    /** The time a further command would apply after {@code script}, read from a fresh reset twin. */
    private static long commandTime(FactoryRuntime live, List<Op> script) {
        FactoryRuntime twin = live.reset();
        script.forEach(op -> op.apply(twin));
        twin.drainSupportedEvents();
        new Submit(1).apply(twin);
        return acceptedAt(twin.drainSupportedEvents());
    }

    /**
     * Applies {@code script} call by call, asserting after every call that the next command would
     * apply at exactly the observed current time, and returns that observed time after each call.
     */
    private static List<Long> observedTimesWithCommandsAtObservedTime(FactoryModelVersion model, List<Op> script) {
        FactoryRuntime live = FactoryRuntime.forModel(model);
        assertEquals(0, observedTime(live));
        assertEquals(0, commandTime(live, List.of()), "a fresh or reset session applies commands at zero");
        List<Op> prefix = new ArrayList<>();
        List<Long> observed = new ArrayList<>();
        for (Op op : script) {
            op.apply(live);
            prefix.add(op);
            long seen = observedTime(live);
            assertEquals(seen, commandTime(live, prefix), "after " + prefix);
            observed.add(seen);
        }
        return observed;
    }

    /** Asserts every processed event was a step completion and returns their simulated times. */
    private static List<Long> stepCompletionTimes(List<Event> processed) {
        processed.forEach(event -> assertInstanceOf(EventPayload.TaskEnd.class, event.payload()));
        return processed.stream().map(event -> event.time().value()).toList();
    }

    /** A supported event without run identity, so independently created runs can be compared. */
    private record Change(
            long sequence, long time, RuntimeEventType type, List<AffectedEntityRef> refs, RuntimeEventPayload payload) {
        static List<Change> of(List<RuntimeEventEnvelope> events) {
            return events.stream()
                    .map(e -> new Change(
                            e.sequence(), e.simulationTime().value(), e.eventType(), e.affectedEntityRefs(), e.payload()))
                    .toList();
        }
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

    // ---- Advancement budget: spent only on step completions ----

    /**
     * Completing an order schedules nothing behind its final step completion, so a run stopped by a
     * one-event budget and a run given a larger budget reach the same quiescent state with nothing
     * left queued; the identical next one-event call then completes the next real step in both,
     * and every later command applies at the same time.
     */
    @Test
    void aOneEventBudgetIsSpentOnTheNextStepCompletionWhateverBudgetReachedTheSameState() {
        FactoryRuntime p = FactoryRuntime.forModel(singleMachine());
        FactoryRuntime q = FactoryRuntime.forModel(singleMachine());
        new Submit(1).apply(p);
        new Submit(1).apply(q);

        assertEquals(List.of(5L), stepCompletionTimes(p.advanceUntil(SimTime.of(100), 1)));
        assertEquals(List.of(5L), stepCompletionTimes(q.advanceUntil(SimTime.of(100), 2)),
                "the larger budget finds nothing but the one step completion to spend itself on");
        RuntimeObservation quiescent = p.observe();
        assertEquals(RuntimeRunState.QUIESCENT, quiescent.metadata().runState());
        assertEquals(4, quiescent.metadata().latestEventSequence());
        assertEquals(5, quiescent.metadata().currentTime().value());
        assertSameExceptRunIdentity(quiescent, q.observe());

        new Submit(1).apply(p);
        new Submit(1).apply(q);
        RuntimeObservation active = p.observe();
        assertEquals(RuntimeRunState.ACTIVE, active.metadata().runState());
        assertEquals(6, active.metadata().latestEventSequence());
        assertEquals(5, active.metadata().currentTime().value(), "the new submission applies at the observed time");
        assertSameExceptRunIdentity(active, q.observe());

        assertEquals(List.of(10L), stepCompletionTimes(p.advanceUntil(SimTime.of(100), 1)));
        assertEquals(List.of(10L), stepCompletionTimes(q.advanceUntil(SimTime.of(100), 1)));

        new Submit(1).apply(p);
        new Submit(1).apply(q);
        p.advanceUntil(SimTime.of(100), UNBOUNDED);
        q.advanceUntil(SimTime.of(100), UNBOUNDED);

        List<Change> pChanges = Change.of(p.drainSupportedEvents());
        assertEquals(pChanges, Change.of(q.drainSupportedEvents()), "the same supported changes, in the same order");
        assertEquals(
                List.of(0L, 0L, 5L, 5L, 5L, 5L, 10L, 10L, 10L, 10L, 15L, 15L),
                pChanges.stream().map(Change::time).toList());
        assertEquals(
                List.of(0L, 5L, 10L),
                pChanges.stream().filter(c -> c.type() == RuntimeEventType.ORDER_ACCEPTED).map(Change::time).toList());
        assertEquals(5.0, p.observe().performance().averageLeadTime(), 1e-12); // lead times 5, 5, 5
        assertSameExceptRunIdentity(p.observe(), q.observe());
    }

    // ---- Command time: never ahead of the observed time ----

    @Test
    void queueDispatchKeepsTheNextCommandAtTheObservedTime() {
        // Three units on one machine complete at 5, 10 and 15; later one-event calls find nothing.
        assertEquals(
                List.of(0L, 5L, 10L, 15L, 15L, 15L, 15L, 15L),
                observedTimesWithCommandsAtObservedTime(singleMachine(), script(new Submit(3), new Step(UNBOUNDED, 1), 7)));
    }

    /**
     * Four units: 1 and 2 FORM on A and B and both finish at 4 (equal-time completions). Unit 1
     * then FINISHes on C (7) and frees A for waiting unit 3 (FORM to 8); unit 2 waits in C's queue
     * and frees B for unit 4 (FORM to 8). C then serves units 2, 3 and 4 in arrival order: 10, 13, 16.
     */
    @Test
    void sharedBacklogAndEqualTimeCompletionsKeepTheNextCommandAtTheObservedTime() {
        assertEquals(
                List.of(0L, 4L, 4L, 7L, 8L, 8L, 10L, 13L, 16L, 16L),
                observedTimesWithCommandsAtObservedTime(
                        sharedThenDedicated(), script(new Submit(4), new Step(UNBOUNDED, 1), 9)));
    }

    /**
     * The same four units under time guards and count budgets (completions at 4, 4, 7, 8, 8, 10, 13,
     * 16, as above). A zero budget, or a guard before the next event, processes nothing, and a
     * command then applies at the last processed event's time -- never at the requested target. A
     * count budget exhausted between two completions due at 8 leaves the command at 8, ahead of the
     * second.
     */
    @Test
    void timeGuardsAndCountBudgetsKeepTheNextCommandAtTheObservedTime() {
        List<Op> script = List.of(
                new Submit(4),
                new Step(UNBOUNDED, 0), // a zero budget processes nothing
                new Step(3, UNBOUNDED), // next completion at 4 is beyond the target: nothing processed
                new Step(4, 1), // unit 1 at 4
                new Step(5, UNBOUNDED), // unit 2 at 4; the next completion, at 7, is beyond 5
                new Step(UNBOUNDED, 2), // 7 and the first completion at 8; budget exhausted
                new Step(8, UNBOUNDED), // the second completion at 8; 10 is beyond the target
                new Step(UNBOUNDED, UNBOUNDED)); // 10, 13 and 16
        assertEquals(
                List.of(0L, 0L, 0L, 4L, 4L, 8L, 8L, 16L),
                observedTimesWithCommandsAtObservedTime(sharedThenDedicated(), script));
    }

    /** Bringing the machine back online dispatches the first waiting unit inside the command. */
    @Test
    void availabilityRecoveryKeepsTheNextCommandAtTheObservedTime() {
        List<Op> script = new ArrayList<>(List.of(
                new Availability(1, false), new Submit(2), new Availability(1, true)));
        script.addAll(script(new Step(UNBOUNDED, 1), new Step(UNBOUNDED, 1), 2));
        assertEquals(
                List.of(0L, 0L, 0L, 5L, 10L, 10L),
                observedTimesWithCommandsAtObservedTime(singleMachine(), script));
    }

    /**
     * Order 1 (two units) completes at 5 and 10; order 2, submitted at 5, waits and completes at
     * 15; order 3, submitted at 15 to an idle machine, completes at 20.
     */
    @Test
    void commandsInterleavedWithBoundedCallsApplyAtTheObservedTime() {
        Op step = new Step(UNBOUNDED, 1);
        List<Op> script = List.of(
                new Submit(2), step, new Submit(1), step, step, new Submit(1), step, step);
        assertEquals(
                List.of(0L, 5L, 5L, 10L, 15L, 15L, 20L, 20L),
                observedTimesWithCommandsAtObservedTime(singleMachine(), script));
    }

    /**
     * Unit 1's CUT completes at 5, and dispatching its HOLD step overflows simulated time. The
     * completion and the dispatch that did occur are still published at 5, so the command time
     * stays on the observed time through the fault and afterwards.
     */
    @Test
    void aFaultingStepCompletionKeepsTheNextCommandAtTheObservedTime() {
        assertEquals(
                List.of(0L, 5L, 5L),
                observedTimesWithCommandsAtObservedTime(
                        overflowingSecondStep(), script(new Submit(2), new Step(UNBOUNDED, 1), 2)));

        FactoryRuntime runtime = FactoryRuntime.forModel(overflowingSecondStep());
        new Submit(2).apply(runtime);
        runtime.drainSupportedEvents();
        assertThrows(SimError.EventOrderingViolation.class, () -> runtime.advanceUntil(SimTime.of(UNBOUNDED), 1));
        List<RuntimeEventEnvelope> published = runtime.drainSupportedEvents();
        assertEquals(
                List.of(RuntimeEventType.JOB_STEP_COMPLETED, RuntimeEventType.JOB_DISPATCHED),
                published.stream().map(RuntimeEventEnvelope::eventType).toList());
        assertTrue(published.stream().allMatch(event -> event.simulationTime().value() == 5));
        RuntimeEventPayload.JobStepCompleted completed = (RuntimeEventPayload.JobStepCompleted) published.get(0).payload();
        assertEquals(0, completed.stepIndex());
        assertFalse(completed.jobComplete());
        assertEquals(new MachineId(2), ((RuntimeEventPayload.JobDispatched) published.get(1).payload()).machineId());
        assertEquals(5, observedTime(runtime));
    }

    // ---- Outcome: decided by supported state and commands, not by a budget ----

    /**
     * Two units on one machine; the first completes at 5 and the second at 10. Every way of
     * reaching the decision boundary after the first completion -- a time guard, one {@code
     * advance()}, or a one-event budget -- presents the same supported state, and the same command
     * there has the same outcome. Spending one more budgeted event is not silent: it publishes the
     * second completion, visibly moving the supported state to 10, and the command then applies at
     * 10 with the different outcome that state implies.
     */
    @Test
    void theOutcomeOfACommandFollowsTheSupportedStateItIsIssuedAt() {
        List<FactoryRuntime> atBoundary = new ArrayList<>();
        for (int path = 0; path < 3; path++) {
            FactoryRuntime runtime = FactoryRuntime.forModel(singleMachine());
            new Submit(2).apply(runtime);
            switch (path) {
                case 0 -> runtime.advanceUntil(SimTime.of(9), UNBOUNDED);
                case 1 -> runtime.advance().orElseThrow();
                default -> runtime.advanceUntil(SimTime.of(100), 1);
            }
            atBoundary.add(runtime);
        }
        RuntimeObservation boundary = atBoundary.getFirst().observe();
        assertEquals(RuntimeRunState.ACTIVE, boundary.metadata().runState());
        assertEquals(5, boundary.metadata().latestEventSequence());
        assertEquals(5, boundary.metadata().currentTime().value());

        FactoryRuntime onward = FactoryRuntime.forModel(singleMachine());
        new Submit(2).apply(onward);
        onward.advanceUntil(SimTime.of(9), UNBOUNDED);
        assertSameExceptRunIdentity(boundary, onward.observe());
        assertEquals(List.of(10L), stepCompletionTimes(onward.advanceUntil(SimTime.of(100), 1)));
        RuntimeObservation advanced = onward.observe();
        assertNotEquals(boundary.metadata().currentTime(), advanced.metadata().currentTime());
        assertEquals(7, advanced.metadata().latestEventSequence());
        assertEquals(10, advanced.metadata().currentTime().value());
        assertTrue(advanced.orders().getFirst().complete());

        List<Change> atBoundaryChanges = null;
        for (FactoryRuntime runtime : atBoundary) {
            assertSameExceptRunIdentity(boundary, runtime.observe());
            new Submit(1).apply(runtime);
            runtime.advanceUntil(SimTime.of(100), UNBOUNDED);
            List<Change> changes = Change.of(runtime.drainSupportedEvents());
            assertEquals(5, lastAcceptedAt(changes));
            if (atBoundaryChanges == null) {
                atBoundaryChanges = changes;
            }
            assertEquals(atBoundaryChanges, changes, "the same command at the same supported state");
            assertEquals(10.0, runtime.observe().performance().averageLeadTime(), 1e-12); // leads 10, 10
        }
        assertEquals(
                List.of(0L, 0L, 0L, 5L, 5L, 5L, 5L, 10L, 10L, 10L, 15L, 15L),
                atBoundaryChanges.stream().map(Change::time).toList());

        new Submit(1).apply(onward);
        onward.advanceUntil(SimTime.of(100), UNBOUNDED);
        List<Change> onwardChanges = Change.of(onward.drainSupportedEvents());
        assertEquals(
                List.of(0L, 0L, 0L, 5L, 5L, 10L, 10L, 10L, 10L, 15L, 15L),
                onwardChanges.stream().map(Change::time).toList());
        assertEquals(10, lastAcceptedAt(onwardChanges));
        assertEquals(7.5, onward.observe().performance().averageLeadTime(), 1e-12); // leads 10, 5
    }

    /**
     * A player paces a run with a per-frame event budget, pauses as soon as the supported cursor
     * reaches 5, and submits more work. Two units on one machine complete at 5 and 10: a one-event
     * frame pauses after the first completion (sequence 5, time 5), while larger frames also take
     * the second (sequence 7, time 10). Pacings that pause at the same supported state produce the
     * same outcome; a pacing that pauses at a different one shows the difference in the supported
     * state itself. In every case the command applies at the time the player observed.
     */
    @Test
    void aPlayerCommandAtAPausedSupportedStateAppliesAtTheObservedTimeWhateverTheFrameBudget() {
        record Paused(RuntimeObservation observation, long acceptedAt, List<Change> changes, double meanLeadTime) {}
        List<Paused> byBudget = new ArrayList<>();
        for (long budget : new long[] {1, 2, 3, UNBOUNDED}) {
            FactoryRuntime runtime = FactoryRuntime.forModel(singleMachine());
            new Submit(2).apply(runtime);
            while (runtime.observe().metadata().latestEventSequence() < 5) {
                runtime.advanceUntil(SimTime.of(UNBOUNDED), budget);
            }
            RuntimeObservation paused = runtime.observe();
            new Submit(1).apply(runtime);
            runtime.advanceUntil(SimTime.of(UNBOUNDED), UNBOUNDED);
            List<Change> changes = Change.of(runtime.drainSupportedEvents());
            assertEquals(paused.metadata().currentTime().value(), lastAcceptedAt(changes), "frame budget " + budget);
            byBudget.add(new Paused(
                    paused, lastAcceptedAt(changes), changes, runtime.observe().performance().averageLeadTime()));
        }

        Paused oneEventFrames = byBudget.getFirst();
        assertEquals(5, oneEventFrames.observation().metadata().latestEventSequence());
        assertEquals(5, oneEventFrames.acceptedAt());
        assertEquals(10.0, oneEventFrames.meanLeadTime(), 1e-12); // leads 10, 10

        Paused twoEventFrames = byBudget.get(1);
        for (Paused larger : byBudget.subList(1, byBudget.size())) {
            assertEquals(7, larger.observation().metadata().latestEventSequence());
            assertEquals(10, larger.acceptedAt());
            assertEquals(7.5, larger.meanLeadTime(), 1e-12); // leads 10, 5
            assertSameExceptRunIdentity(twoEventFrames.observation(), larger.observation());
            assertEquals(twoEventFrames.changes(), larger.changes());
        }
    }

    /**
     * Start-fixed inputs: every input is given before advancement, and presentation only chooses
     * how many events each frame may process. Any frame budget, and per-tick pacing, yields the
     * same supported changes as one unbounded call, and no frame that processes an event is silent.
     * Six units FORM in pairs finishing at 4, 8 and 12 and FINISH one at a time on C: 7, 10, 13, 16,
     * 19 and 22.
     */
    @Test
    void presentationPacingOfAStartFixedRunChangesNothingSupported() {
        List<Change> unbounded = paced(UNBOUNDED);
        Change last = unbounded.getLast();
        assertEquals(RuntimeEventType.ORDER_COMPLETED, last.type());
        assertEquals(22, last.time());
        for (long budget : new long[] {1, 2, 3, 7}) {
            assertEquals(unbounded, paced(budget), "frame budget " + budget);
        }

        FactoryRuntime perTick = FactoryRuntime.forModel(sharedThenDedicated());
        new Submit(6).apply(perTick);
        for (long tick = 1; tick <= 30; tick++) {
            perTick.advanceUntil(SimTime.of(tick), UNBOUNDED);
        }
        assertEquals(RuntimeRunState.QUIESCENT, perTick.observe().metadata().runState());
        assertEquals(unbounded, Change.of(perTick.drainSupportedEvents()));
    }

    private static List<Change> paced(long frameBudget) {
        FactoryRuntime runtime = FactoryRuntime.forModel(sharedThenDedicated());
        new Submit(6).apply(runtime);
        long cursor = runtime.observe().metadata().latestEventSequence();
        List<Event> frame;
        while (!(frame = runtime.advanceUntil(SimTime.of(UNBOUNDED), frameBudget)).isEmpty()) {
            long next = runtime.observe().metadata().latestEventSequence();
            assertTrue(next > cursor, "a frame that processed " + frame + " published nothing");
            cursor = next;
        }
        return Change.of(runtime.drainSupportedEvents());
    }
}
