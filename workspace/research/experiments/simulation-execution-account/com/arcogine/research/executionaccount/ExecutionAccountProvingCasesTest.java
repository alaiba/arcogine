package com.arcogine.research.executionaccount;

import static com.arcogine.factory.process.RuntimeEventType.JOB_DISPATCHED;
import static com.arcogine.factory.process.RuntimeEventType.JOB_STEP_COMPLETED;
import static com.arcogine.factory.process.RuntimeEventType.JOB_WAITING;
import static com.arcogine.factory.process.RuntimeEventType.MACHINE_AVAILABILITY_CHANGED;
import static com.arcogine.factory.process.RuntimeEventType.ORDER_ACCEPTED;
import static com.arcogine.factory.process.RuntimeEventType.ORDER_COMPLETED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.change.FactoryModelSemanticComparator;
import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelArtifact;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.spatial.FactoryFloor;
import com.arcogine.factory.model.spatial.ResourceFootprint;
import com.arcogine.factory.model.spatial.ResourceLayout;
import com.arcogine.factory.model.spatial.ResourcePlacement;
import com.arcogine.factory.model.spatial.SpatialRecord;
import com.arcogine.factory.process.CommandResult;
import com.arcogine.factory.process.FactoryRuntime;
import com.arcogine.factory.process.JobObservation;
import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeEventType;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.factory.process.RuntimeRunState;
import com.arcogine.factory.process.UnsupportedModelContentException;
import com.arcogine.governance.SemanticArtifact;
import com.arcogine.governance.change.SemanticChange;
import com.arcogine.governance.change.SemanticChangeKind;
import com.arcogine.research.experiment.ExperimentEvidence;
import com.arcogine.research.experiment.ExperimentFixture;
import com.arcogine.research.experiment.ExperimentRunner;
import com.arcogine.research.experiment.ExperimentStep;
import com.arcogine.research.experiment.LinearRoutingFamily;
import com.arcogine.research.experiment.OracleOutcome;
import com.arcogine.research.experiment.ProcessingOccupancyOracle;
import com.arcogine.research.experiment.SequenceRange;
import com.arcogine.types.JobId;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;

/**
 * Proving cases for the simulation execution-account investigation. Every expected value was
 * derived by hand from the Engine specification and pre-registered before this class ran; see the
 * investigation's independent-reconstruction note.
 *
 * <p>Everything here uses only the supported runtime contract: published models, the supported
 * session-control commands, supported observations and drained supported events.
 */
class ExecutionAccountProvingCasesTest {

    private static final ProductId PRODUCT = LinearRoutingFamily.PRODUCT;
    private static final double PRICE = LinearRoutingFamily.UNIT_PRICE;
    private static final SimTime END = SimTime.of(Long.MAX_VALUE);

    private static final MachineId CUTTER = new MachineId(1);
    private static final MachineId ASSEMBLER = new MachineId(2);
    private static final MachineId ASSEMBLER_TWO = new MachineId(3);
    private static final MachineId OVEN = new MachineId(1);

    /** The event shape the pre-registration states: sequence, time, type. */
    record Shape(long sequence, long time, RuntimeEventType type) {}

    /** An event with its run identity removed, for comparing distinct executions. */
    record Normalized(long sequence, long time, RuntimeEventType type, ModelFingerprint fingerprint,
            Object refs, RuntimeEventPayload payload) {}

    // ---------------------------------------------------------------- models

    /** Model A: CUT 3 then ASSEMBLE 5; one Cutter, one Assembler. */
    private static final LinearRoutingFamily LINE = new LinearRoutingFamily(
            List.of(new LinearRoutingFamily.Step("CUT", 3), new LinearRoutingFamily.Step("ASSEMBLE", 5)),
            List.of(LinearRoutingFamily.Resource.of("Cutter", 1, "CUT"),
                    LinearRoutingFamily.Resource.of("Assembler", 1, "ASSEMBLE")));

    /** Model A': model A plus a second Assembler; one authored change. */
    private static final LinearRoutingFamily LINE_PLUS_ASSEMBLER = new LinearRoutingFamily(
            LINE.steps(),
            List.of(LinearRoutingFamily.Resource.of("Cutter", 1, "CUT"),
                    LinearRoutingFamily.Resource.of("Assembler", 1, "ASSEMBLE"),
                    LinearRoutingFamily.Resource.of("Assembler 2", 1, "ASSEMBLE")));

    /** Model B: BAKE 4 on one Oven of concurrency 2. */
    private static final LinearRoutingFamily OVEN_LINE = new LinearRoutingFamily(
            List.of(new LinearRoutingFamily.Step("BAKE", 4)),
            List.of(LinearRoutingFamily.Resource.of("Oven", 2, "BAKE")));

    /** Model C: PROCESS 5 on one machine of concurrency 1. */
    private static final LinearRoutingFamily SINGLE = new LinearRoutingFamily(
            List.of(new LinearRoutingFamily.Step("PROCESS", 5)),
            List.of(LinearRoutingFamily.Resource.of("Machine", 1, "PROCESS")));

    private static final List<Shape> LINE_SHAPE = List.of(
            new Shape(1, 0, ORDER_ACCEPTED),
            new Shape(2, 0, JOB_DISPATCHED),
            new Shape(3, 0, JOB_WAITING),
            new Shape(4, 3, JOB_STEP_COMPLETED),
            new Shape(5, 3, JOB_DISPATCHED),
            new Shape(6, 3, JOB_DISPATCHED),
            new Shape(7, 6, JOB_STEP_COMPLETED),
            new Shape(8, 6, JOB_WAITING),
            new Shape(9, 8, JOB_STEP_COMPLETED),
            new Shape(10, 8, JOB_DISPATCHED),
            new Shape(11, 13, JOB_STEP_COMPLETED),
            new Shape(12, 13, ORDER_COMPLETED));

    private static FactoryModelVersion publish(LinearRoutingFamily family) {
        return FactoryModelPublisher.publish(family.model());
    }

    // ---------------------------------------------------------------- helpers

    private static List<Shape> shape(List<RuntimeEventEnvelope> events) {
        return events.stream()
                .map(e -> new Shape(e.sequence(), e.simulationTime().value(), e.eventType()))
                .toList();
    }

    private static List<Normalized> normalized(List<RuntimeEventEnvelope> events) {
        return events.stream()
                .map(e -> new Normalized(e.sequence(), e.simulationTime().value(), e.eventType(),
                        e.modelFingerprint(), e.affectedEntityRefs(), e.payload()))
                .toList();
    }

    /** Runs model A's two-unit workload to quiescence as a full capture from establishment. */
    private static CapturedAccount fullLineAccount(FactoryRuntime runtime) {
        CapturedAccount account = CapturedAccount.from(runtime.observe());
        runtime.submitWorkload(PRODUCT, 2, PRICE).orElseThrow();
        runtime.advanceUntil(END, Long.MAX_VALUE);
        account.retain(runtime.drainSupportedEvents());
        account.observeFrontier(runtime.observe());
        return account;
    }

    /**
     * Issues the availability command, held as {@code CommandResult<?>}: its accepted value is an
     * internal scheduler payload, which supported evidence never reads (the runner does the same).
     */
    private static CommandResult<?> availability(FactoryRuntime runtime, MachineId machine, boolean online) {
        CommandResult<?> result = runtime.setMachineAvailability(machine, online);
        return result;
    }

    /** Requires an accepted command without naming the internal payload type its value carries. */
    private static void accepted(CommandResult<?> result) {
        assertInstanceOf(CommandResult.Accepted.class, result, () -> "expected acceptance, got " + result.code());
    }

    private static ResourceObservation resource(RuntimeObservation observation, MachineId machine) {
        return observation.resources().stream().filter(r -> r.machineId().equals(machine)).findFirst().orElseThrow();
    }

    private static long completionTime(CapturedAccount account) {
        return account.retainedEvents().stream()
                .filter(e -> e.eventType() == ORDER_COMPLETED)
                .mapToLong(e -> e.simulationTime().value())
                .max()
                .orElseThrow();
    }

    // ---------------------------------------------------------------- case 1

    @Test
    void case01FullCaptureFromEstablishmentIsAGapFreePrefixNotATerminalRecord() {
        FactoryModelVersion version = publish(LINE);
        FactoryRuntime runtime = FactoryRuntime.forModel(version);

        RuntimeObservation establishment = runtime.observe();
        assertEquals(0, establishment.metadata().latestEventSequence());
        assertEquals(0, establishment.metadata().currentTime().value());
        assertEquals(RuntimeRunState.QUIESCENT, establishment.metadata().runState());
        assertTrue(establishment.orders().isEmpty());
        assertTrue(establishment.jobs().isEmpty());
        assertTrue(establishment.resources().stream()
                .allMatch(r -> r.state() == MachineState.Idle && r.activeJobIds().isEmpty() && r.queueDepth() == 0));

        CapturedAccount account = fullLineAccount(runtime);

        assertEquals(LINE_SHAPE, shape(account.retainedEvents()));
        assertTrue(account.coversFromEstablishment());
        assertTrue(account.retainedEvents().stream().allMatch(e -> e.runId().equals(runtime.runId())));
        assertTrue(account.retainedEvents().stream().allMatch(e -> e.modelFingerprint().equals(version.fingerprint())));

        RuntimeObservation last = account.frontier();
        assertEquals(12, last.metadata().latestEventSequence());
        assertEquals(13, last.metadata().currentTime().value());
        assertEquals(RuntimeRunState.QUIESCENT, last.metadata().runState());
        assertTrue(PlacementFold.from(establishment).applyAll(account.retainedEvents()).agreesWith(last));

        // Quiescence is not an end: the same run accepts more work and continues its sequence.
        runtime.submitWorkload(PRODUCT, 1, PRICE).orElseThrow();
        List<RuntimeEventEnvelope> more = runtime.drainSupportedEvents();
        assertEquals(13, more.getFirst().sequence());
        assertEquals(13, more.getFirst().simulationTime().value());
        assertEquals(runtime.runId(), more.getFirst().runId());
    }

    // ---------------------------------------------------------------- case 2

    @Test
    void case02LateJoinReconstructsTheCurrentViewButCannotClaimEarlierHistory() {
        FactoryRuntime runtime = FactoryRuntime.forModel(publish(LINE));
        CapturedAccount full = CapturedAccount.from(runtime.observe());
        runtime.submitWorkload(PRODUCT, 2, PRICE).orElseThrow();
        runtime.advanceUntil(SimTime.of(4), Long.MAX_VALUE);
        full.retain(runtime.drainSupportedEvents());

        RuntimeObservation join = runtime.observe();
        assertEquals(6, join.metadata().latestEventSequence());
        assertEquals(3, join.metadata().currentTime().value());
        CapturedAccount late = CapturedAccount.from(join);

        runtime.advanceUntil(END, Long.MAX_VALUE);
        List<RuntimeEventEnvelope> after = runtime.drainSupportedEvents();
        full.retain(after);
        late.retain(after);
        RuntimeObservation last = runtime.observe();
        full.observeFrontier(last);
        late.observeFrontier(last);

        // Current view: the join observation plus later events is enough.
        assertTrue(late.missing().isEmpty());
        assertTrue(PlacementFold.from(join).applyAll(late.retainedEvents()).agreesWith(last));

        // History: the late account cannot claim the run from its establishment.
        assertTrue(full.coversFromEstablishment());
        assertFalse(late.coversFromEstablishment());
        assertFalse(late.intervalDeterminacy(0, 13).determined());
        assertTrue(late.intervalDeterminacy(0, 13).reason().contains("no state basis at 0"));

        // An interval from the join onward is licensed, and both accounts agree on it.
        assertTrue(late.intervalDeterminacy(3, 13).determined());
        assertEquals(10, IntervalReadings.activeJobTicks(late, ASSEMBLER, 3, 13));
        assertEquals(10, IntervalReadings.activeJobTicks(full, ASSEMBLER, 3, 13));
        assertEquals(3, IntervalReadings.activeJobTicks(late, CUTTER, 3, 13));
        assertEquals(3, IntervalReadings.activeJobTicks(full, CUTTER, 3, 13));

        // A late joiner that treats its capture as the whole run reads the Cutter as never used.
        assertEquals(6, IntervalReadings.activeJobTicks(full, CUTTER, 0, 13));
        assertEquals(0, IntervalReadings.naivePairedTicks(late.retainedEvents(), CUTTER, 13));
    }

    // ---------------------------------------------------------------- case 3

    @Test
    void case03AGapIsDetectedByArithmeticAndAGapBlindReadingIsWrong() {
        FactoryRuntime runtime = FactoryRuntime.forModel(publish(LINE));
        RuntimeObservation establishment = runtime.observe();
        CapturedAccount full = fullLineAccount(runtime);

        CapturedAccount gapped = CapturedAccount.from(establishment);
        gapped.retain(full.retainedEvents().stream().filter(e -> e.sequence() != 7).toList());
        gapped.observeFrontier(full.frontier());

        assertEquals(List.of(new SequenceRange(7, 7)), gapped.missing());
        assertFalse(gapped.coversFromEstablishment());
        assertFalse(gapped.intervalDeterminacy(0, 13).determined());
        assertThrows(IllegalStateException.class, () -> IntervalReadings.activeJobTicks(gapped, CUTTER, 0, 13));

        assertEquals(6, IntervalReadings.activeJobTicks(full, CUTTER, 0, 13));
        assertEquals(13, IntervalReadings.naivePairedTicks(gapped.retainedEvents(), CUTTER, 13));
    }

    // ---------------------------------------------------------------- case 4

    @Test
    void case04ResetStartsANewExecutionAndLeavesTheOriginalRunUsable() {
        FactoryModelVersion version = publish(LINE);
        FactoryRuntime original = FactoryRuntime.forModel(version);
        CapturedAccount first = fullLineAccount(original);

        FactoryRuntime fresh = original.reset();
        RuntimeObservation freshStart = fresh.observe();
        assertNotEquals(original.runId(), fresh.runId());
        assertEquals(fresh.runId(), freshStart.metadata().runId());
        assertEquals(0, freshStart.metadata().latestEventSequence());
        assertEquals(0, freshStart.metadata().currentTime().value());
        assertTrue(freshStart.orders().isEmpty());
        assertTrue(freshStart.jobs().isEmpty());
        assertEquals(version.fingerprint(), freshStart.metadata().modelFingerprint());
        assertEquals(original.modelVersion(), fresh.modelVersion());

        CapturedAccount second = fullLineAccount(fresh);
        assertEquals(normalized(first.retainedEvents()), normalized(second.retainedEvents()));

        // Evidence of the new run cannot be merged into the old run's account.
        assertThrows(IllegalArgumentException.class, () -> first.retain(second.retainedEvents()));

        // The original run did not end: it continues its own sequence under its own identity.
        original.submitWorkload(PRODUCT, 1, PRICE).orElseThrow();
        List<RuntimeEventEnvelope> continued = original.drainSupportedEvents();
        assertEquals(13, continued.getFirst().sequence());
        assertEquals(original.runId(), continued.getFirst().runId());
        assertEquals(12, fresh.observe().metadata().latestEventSequence());
    }

    // ---------------------------------------------------------------- case 5

    @Test
    void case05EqualTimeChangesAreOrderedBySequenceNotByTimestamp() {
        FactoryRuntime runtime = FactoryRuntime.forModel(publish(LINE));
        RuntimeObservation establishment = runtime.observe();
        CapturedAccount account = fullLineAccount(runtime);
        List<RuntimeEventEnvelope> events = account.retainedEvents();

        Map<Long, Long> perTime = events.stream()
                .collect(Collectors.groupingBy(e -> e.simulationTime().value(), TreeMap::new, Collectors.counting()));
        assertEquals(Map.of(0L, 3L, 3L, 3L, 6L, 2L, 8L, 2L, 13L, 2L), perTime);
        for (int i = 1; i < events.size(); i++) {
            assertTrue(events.get(i).simulationTime().compareTo(events.get(i - 1).simulationTime()) >= 0,
                    "simulation time never decreases along the sequence");
        }

        assertTrue(PlacementFold.from(establishment).applyAll(events).agreesWith(account.frontier()));

        // Same events, same timestamps, equal-time groups reversed: an impossible intermediate state.
        List<RuntimeEventEnvelope> reversedWithinTime = new ArrayList<>();
        new TreeMap<>(events.stream().collect(Collectors.groupingBy(e -> e.simulationTime().value())))
                .values()
                .forEach(group -> reversedWithinTime.addAll(group.reversed()));
        assertThrows(IllegalStateException.class, () -> PlacementFold.from(establishment).applyAll(reversedWithinTime));

        // Reversing only the two changes at t = 8 puts both jobs on the concurrency-1 Assembler.
        List<RuntimeEventEnvelope> swappedAtEight = new ArrayList<>(events);
        swappedAtEight.set(8, events.get(9));
        swappedAtEight.set(9, events.get(8));
        assertEquals(8, swappedAtEight.get(8).simulationTime().value());
        assertEquals(8, swappedAtEight.get(9).simulationTime().value());
        IllegalStateException overCapacity = assertThrows(IllegalStateException.class,
                () -> PlacementFold.from(establishment).applyAll(swappedAtEight));
        assertTrue(overCapacity.getMessage().contains("would run"), overCapacity.getMessage());
    }

    // ---------------------------------------------------------------- case 6

    @Test
    void case06WorkCrossingAnIntervalNeedsAStateBasisAndClosureNotItsStartOrEndEvent() {
        FactoryRuntime runtime = FactoryRuntime.forModel(publish(LINE));
        runtime.submitWorkload(PRODUCT, 2, PRICE).orElseThrow();
        runtime.advanceUntil(SimTime.of(4), Long.MAX_VALUE);
        runtime.drainSupportedEvents(); // the window consumer holds no earlier events

        RuntimeObservation basis = runtime.observe();
        assertEquals(6, basis.metadata().latestEventSequence());
        assertEquals(List.of(basis.jobs().getFirst().jobId()), resource(basis, ASSEMBLER).activeJobIds());
        CapturedAccount window = CapturedAccount.from(basis);

        runtime.advanceUntil(SimTime.of(7), Long.MAX_VALUE);
        window.retain(runtime.drainSupportedEvents());
        RuntimeObservation atSix = runtime.observe();
        window.observeFrontier(atSix);
        assertEquals(6, atSix.metadata().currentTime().value(), "advancing to 7 does not move supported time to 7");

        // Every internal event up to 7 has been processed, yet a command now would land at 6, inside [4, 7).
        assertFalse(window.intervalDeterminacy(4, 7).determined());

        runtime.advanceUntil(END, Long.MAX_VALUE);
        window.retain(runtime.drainSupportedEvents());
        window.observeFrontier(runtime.observe());
        assertTrue(window.intervalDeterminacy(4, 7).determined());

        // The running step started before the window and ends after it; neither event is used.
        JobId first = basis.jobs().getFirst().jobId();
        assertTrue(window.retainedEvents().stream().noneMatch(e -> e.sequence() <= 6));
        assertTrue(window.eventsBefore(7).stream()
                .noneMatch(e -> e.payload() instanceof RuntimeEventPayload.JobStepCompleted c && c.jobId().equals(first)));
        assertEquals(3, IntervalReadings.activeJobTicks(window, ASSEMBLER, 4, 7));

        // Completion-credited busy ticks see nothing of that work inside the window.
        assertEquals(0, resource(atSix, ASSEMBLER).busyTicks() - resource(basis, ASSEMBLER).busyTicks());
    }

    // ---------------------------------------------------------------- closure

    @Test
    void case06bClosingAnIntervalNeedsSupportedTimeOrAControllerCommitment() {
        FactoryRuntime runtime = FactoryRuntime.forModel(publish(LINE));
        CapturedAccount account = CapturedAccount.from(runtime.observe());
        runtime.submitWorkload(PRODUCT, 2, PRICE).orElseThrow();
        runtime.advanceUntil(SimTime.of(100), Long.MAX_VALUE);
        account.retain(runtime.drainSupportedEvents());
        RuntimeObservation afterHundred = runtime.observe();
        account.observeFrontier(afterHundred);

        assertEquals(13, afterHundred.metadata().currentTime().value());
        assertEquals(RuntimeRunState.QUIESCENT, afterHundred.metadata().runState());
        assertTrue(account.intervalDeterminacy(0, 13).determined());
        assertEquals(10, IntervalReadings.activeJobTicks(account, ASSEMBLER, 0, 13));
        assertFalse(account.intervalDeterminacy(0, 100).determined(), "quiescent at 13 is not closed through 100");

        // The controller, believing the run is at 100, submits more work: it is accepted at 13.
        runtime.submitWorkload(PRODUCT, 1, PRICE).orElseThrow();
        runtime.advanceUntil(END, Long.MAX_VALUE);
        List<RuntimeEventEnvelope> later = runtime.drainSupportedEvents();
        assertEquals(13, later.getFirst().simulationTime().value());
        account.retain(later);
        account.observeFrontier(runtime.observe());
        assertEquals(21, account.frontierTime());

        // Only now does the controller commit to nothing further; [0, 100) is then 15, not 10.
        account.declareClosedThrough(Long.MAX_VALUE);
        assertTrue(account.intervalDeterminacy(0, 100).determined());
        assertEquals(15, IntervalReadings.activeJobTicks(account, ASSEMBLER, 0, 100));

        // A premature commitment is contradicted by the later command, and the contradiction is detected.
        FactoryRuntime other = FactoryRuntime.forModel(publish(LINE));
        CapturedAccount premature = CapturedAccount.from(other.observe());
        other.submitWorkload(PRODUCT, 2, PRICE).orElseThrow();
        other.advanceUntil(SimTime.of(100), Long.MAX_VALUE);
        premature.retain(other.drainSupportedEvents());
        premature.observeFrontier(other.observe());
        premature.declareClosedThrough(100);
        other.submitWorkload(PRODUCT, 1, PRICE).orElseThrow();
        assertThrows(IllegalStateException.class, () -> premature.retain(other.drainSupportedEvents()));
    }

    // ---------------------------------------------------------------- case 7

    @Test
    void case07AnAvailabilityChangeLeavesAnIdleSlotBesideWaitingWorkAndTheAccountRecordsOnlyFacts() {
        FactoryRuntime runtime = FactoryRuntime.forModel(publish(OVEN_LINE));
        CapturedAccount account = CapturedAccount.from(runtime.observe());
        accepted(availability(runtime, OVEN, false));
        runtime.submitWorkload(PRODUCT, 4, PRICE).orElseThrow();
        accepted(availability(runtime, OVEN, true));
        RuntimeObservation online = runtime.observe();
        runtime.advanceUntil(END, Long.MAX_VALUE);
        account.retain(runtime.drainSupportedEvents());
        account.observeFrontier(runtime.observe());

        ResourceObservation oven = resource(online, OVEN);
        assertEquals(MachineState.Busy, oven.state());
        assertEquals(2, oven.concurrency());
        assertEquals(1, oven.activeJobIds().size());
        assertEquals(3, oven.queueDepth());
        assertTrue(online.pendingWork().isEmpty());

        assertEquals(List.of(
                new Shape(1, 0, MACHINE_AVAILABILITY_CHANGED),
                new Shape(2, 0, ORDER_ACCEPTED),
                new Shape(3, 0, JOB_WAITING),
                new Shape(4, 0, JOB_WAITING),
                new Shape(5, 0, JOB_WAITING),
                new Shape(6, 0, JOB_WAITING),
                new Shape(7, 0, MACHINE_AVAILABILITY_CHANGED),
                new Shape(8, 0, JOB_DISPATCHED),
                new Shape(9, 4, JOB_STEP_COMPLETED),
                new Shape(10, 4, JOB_DISPATCHED),
                new Shape(11, 8, JOB_STEP_COMPLETED),
                new Shape(12, 8, JOB_DISPATCHED),
                new Shape(13, 12, JOB_STEP_COMPLETED),
                new Shape(14, 12, JOB_DISPATCHED),
                new Shape(15, 16, JOB_STEP_COMPLETED),
                new Shape(16, 16, ORDER_COMPLETED)), shape(account.retainedEvents()));
        assertTrue(account.coversFromEstablishment());
        assertEquals(16, completionTime(account));

        // Facts the account preserves: after each time's last change, one of two slots runs, and the
        // queued work beside the idle slot is 3, 2, 1, then 0. Whether that idle slot is "starved",
        // has "no work left", or neither is a characterization, and the account does not encode one.
        PlacementFold fold = PlacementFold.from(account.basis());
        Map<Long, List<Long>> afterEachTime = new TreeMap<>();
        for (RuntimeEventEnvelope event : account.retainedEvents()) {
            fold.apply(event);
            long t = event.simulationTime().value();
            boolean lastAtThisTime = account.retainedEvents().stream()
                    .filter(e -> e.simulationTime().value() == t)
                    .mapToLong(RuntimeEventEnvelope::sequence)
                    .max()
                    .orElseThrow() == event.sequence();
            if (lastAtThisTime) {
                afterEachTime.put(t, List.of((long) fold.activeCount(OVEN), fold.queuedCount()));
            }
        }
        assertEquals(Map.of(
                0L, List.of(1L, 3L),
                4L, List.of(1L, 2L),
                8L, List.of(1L, 1L),
                12L, List.of(1L, 0L),
                16L, List.of(0L, 0L)), afterEachTime);
        assertEquals(16, IntervalReadings.activeJobTicks(account, OVEN, 0, 16));
    }

    // ---------------------------------------------------------------- case 8

    @Test
    void case08TwoDefinitionsOverOneUnchangedAccountLegitimatelyDisagree() {
        ExperimentFixture fixture = new ExperimentFixture(
                "execution-account-oven-availability",
                OVEN_LINE.model(),
                List.of(
                        ExperimentStep.observe("establishment"),
                        ExperimentStep.setAvailability(OVEN, false),
                        ExperimentStep.submit(PRODUCT, 4, PRICE),
                        ExperimentStep.setAvailability(OVEN, true),
                        ExperimentStep.advanceToQuiescence(100),
                        ExperimentStep.captureEvents("all")),
                ExperimentFixture.WindowIntent.COMPLETE_RUN,
                List.of());
        ExperimentEvidence evidence = ExperimentRunner.runAndReplay(fixture);
        assertTrue(evidence.window().isComplete());

        CapturedAccount account = CapturedAccount.from(evidence.observation("establishment"));
        account.retain(evidence.retainedEvents());
        account.observeFrontier(evidence.observation(ExperimentEvidence.CLOSING_LABEL));
        List<RuntimeEventEnvelope> before = account.retainedEvents();
        RuntimeObservation basisBefore = account.basis();

        long activeJobTicks = IntervalReadings.activeJobTicks(account, OVEN, 0, 16);
        long ticksWithAnyJob = IntervalReadings.ticksWithAnyJob(account, OVEN, 0, 16);
        long slotTicks = (long) resource(account.basis(), OVEN).concurrency() * 16;
        // Slot utilization: 16 / (2 x 16) = 1/2. Machine busy fraction: 16 / 16 = 1.
        assertEquals(16, activeJobTicks);
        assertEquals(32, slotTicks);
        assertEquals(16, ticksWithAnyJob);
        System.out.println("[case08] slot utilization " + activeJobTicks + "/" + slotTicks
                + "; machine busy fraction " + ticksWithAnyJob + "/16");

        // The substrate's whole-run occupancy definition refuses this same complete account outright.
        OracleOutcome<List<ProcessingOccupancyOracle.ResourceOccupancy>> substrate =
                new ProcessingOccupancyOracle(ExperimentEvidence.CLOSING_LABEL).evaluateOn(evidence);
        OracleOutcome.Underdetermined<?> refused = assertInstanceOf(OracleOutcome.Underdetermined.class, substrate);
        assertTrue(refused.reasons().getFirst().contains("changed availability"), refused.reasons().toString());

        assertEquals(before, account.retainedEvents());
        assertEquals(basisBefore, account.basis());
    }

    // ---------------------------------------------------------------- case 9

    @Test
    void case09AControlledComparisonReferencesTwoExecutionsAndNeverMergesThem() {
        FactoryModelVersion base = publish(LINE);
        FactoryModelVersion changed = publish(LINE_PLUS_ASSEMBLER);
        FactoryRuntime baseRun = FactoryRuntime.forModel(base);
        FactoryRuntime changedRun = FactoryRuntime.forModel(changed);
        CapturedAccount baseAccount = fullLineAccount(baseRun);
        CapturedAccount changedAccount = fullLineAccount(changedRun);

        assertTrue(baseAccount.coversFromEstablishment());
        assertTrue(changedAccount.coversFromEstablishment());
        assertNotEquals(baseAccount.runId(), changedAccount.runId());
        assertEquals(13, completionTime(baseAccount));
        assertEquals(11, completionTime(changedAccount));
        assertEquals(List.of(
                new Shape(1, 0, ORDER_ACCEPTED),
                new Shape(2, 0, JOB_DISPATCHED),
                new Shape(3, 0, JOB_WAITING),
                new Shape(4, 3, JOB_STEP_COMPLETED),
                new Shape(5, 3, JOB_DISPATCHED),
                new Shape(6, 3, JOB_DISPATCHED),
                new Shape(7, 6, JOB_STEP_COMPLETED),
                new Shape(8, 6, JOB_DISPATCHED),
                new Shape(9, 8, JOB_STEP_COMPLETED),
                new Shape(10, 11, JOB_STEP_COMPLETED),
                new Shape(11, 11, ORDER_COMPLETED)), shape(changedAccount.retainedEvents()));

        // The same sequence number names different facts in the two runs: identity is (run, sequence).
        assertEquals(JOB_WAITING, baseAccount.retainedEvents().get(7).eventType());
        assertEquals(JOB_DISPATCHED, changedAccount.retainedEvents().get(7).eventType());
        RuntimeEventPayload.JobDispatched toSecond =
                (RuntimeEventPayload.JobDispatched) changedAccount.retainedEvents().get(7).payload();
        assertEquals(ASSEMBLER_TWO, toSecond.machineId());

        // The authored change comes from Factory's semantic comparison, not from the event streams.
        List<SemanticChange> changes = new FactoryModelSemanticComparator().compare(
                new SemanticArtifact(base.fingerprint(), FactoryModelArtifact.encode(base)),
                new SemanticArtifact(changed.fingerprint(), FactoryModelArtifact.encode(changed)));
        assertTrue(changes.stream().anyMatch(c -> c.kind() == SemanticChangeKind.ENTITY_ADDED
                        && c.entity().entityType().equals("factory.resource")
                        && c.entity().entityId().equals("3")),
                changes.toString());
        System.out.println("[case09] authored changes: " + changes);
    }

    // ---------------------------------------------------------------- case 10

    @Test
    void case10AFaultIsVisibleOnlyToTheControllerAndTheSupportedAccountLooksComplete() {
        FactoryModelVersion version = FactoryModelPublisher.publish(new FactoryModel(
                List.of(new ConfiguredResource(new MachineId(1), "Mill A", 1, null, 0),
                        new ConfiguredResource(new MachineId(2), "Mill B", 1, null, 0)),
                List.of(new OperationDefinition(1, "Route A",
                                List.of(new OperationStepDefinition(1, "Op A", Set.of(new MachineId(1)), 5))),
                        new OperationDefinition(2, "Route B",
                                List.of(new OperationStepDefinition(2, "Op B", Set.of(new MachineId(2)), Long.MAX_VALUE)))),
                List.of(new ProductDefinition(new ProductId(1), "Product A", 1),
                        new ProductDefinition(new ProductId(2), "Product B", 2))));

        List<String> outcomes = new ArrayList<>();
        List<List<Normalized>> streams = new ArrayList<>();
        for (int run = 0; run < 2; run++) {
            FactoryRuntime runtime = FactoryRuntime.forModel(version);
            CapturedAccount account = CapturedAccount.from(runtime.observe());
            accepted(availability(runtime, new MachineId(1), false));
            runtime.submitWorkload(new ProductId(1), 1, PRICE).orElseThrow();
            runtime.submitWorkload(new ProductId(2), 1, PRICE).orElseThrow();
            runtime.advanceUntil(END, Long.MAX_VALUE);
            CommandResult<?> result = availability(runtime, new MachineId(1), true);
            outcomes.add(result.getClass().getSimpleName() + ":" + result.code());
            account.retain(runtime.drainSupportedEvents());
            RuntimeObservation after = runtime.observe();
            account.observeFrontier(after);
            streams.add(normalized(account.retainedEvents()));

            assertInstanceOf(CommandResult.Faulted.class, result);
            long max = Long.MAX_VALUE;
            assertEquals(List.of(
                    new Shape(1, 0, MACHINE_AVAILABILITY_CHANGED),
                    new Shape(2, 0, ORDER_ACCEPTED),
                    new Shape(3, 0, JOB_WAITING),
                    new Shape(4, 0, ORDER_ACCEPTED),
                    new Shape(5, 0, JOB_DISPATCHED),
                    new Shape(6, max, JOB_STEP_COMPLETED),
                    new Shape(7, max, ORDER_COMPLETED),
                    new Shape(8, max, MACHINE_AVAILABILITY_CHANGED),
                    new Shape(9, max, JOB_DISPATCHED)), shape(account.retainedEvents()));
            assertTrue(account.coversFromEstablishment(), "the supported account is gap-free");

            // Nothing in the supported boundary says a fault happened.
            assertEquals(EnumSet.of(ORDER_ACCEPTED, JOB_DISPATCHED, JOB_WAITING, JOB_STEP_COMPLETED, ORDER_COMPLETED,
                    MACHINE_AVAILABILITY_CHANGED), EnumSet.allOf(RuntimeEventType.class));
            assertEquals(RuntimeRunState.QUIESCENT, after.metadata().runState());
            JobObservation stranded = after.jobs().stream()
                    .filter(j -> j.productId().equals(new ProductId(1)))
                    .findFirst()
                    .orElseThrow();
            assertEquals(JobStatus.InProgress, stranded.status());
            assertEquals(new MachineId(1), stranded.currentMachineId());
            assertEquals(MachineState.Busy, resource(after, new MachineId(1)).state());
        }
        // The fault is part of the deterministic outcome of the same basis.
        assertEquals(outcomes.get(0), outcomes.get(1));
        assertEquals(streams.get(0), streams.get(1));
        System.out.println("[case10] controller-only outcome: " + outcomes.getFirst());
    }

    // ---------------------------------------------------------------- reproduction

    @Test
    void case12ReproductionNeedsTheControllerScriptNotOnlyTheSupportedHistory() {
        FactoryModelVersion version = publish(SINGLE);

        // Script A: stop after the internal no-op marker at 10, then submit.
        FactoryRuntime scriptA = FactoryRuntime.forModel(version);
        scriptA.submitWorkload(PRODUCT, 2, PRICE).orElseThrow();
        scriptA.advanceUntil(SimTime.of(9), Long.MAX_VALUE);
        scriptA.advanceUntil(SimTime.of(10), 1);
        RuntimeObservation beforeSubmitA = scriptA.observe();
        scriptA.submitWorkload(PRODUCT, 1, PRICE).orElseThrow();
        scriptA.advanceUntil(END, Long.MAX_VALUE);
        List<RuntimeEventEnvelope> historyA = scriptA.drainSupportedEvents();

        // Script B: advance through 10, then submit.
        FactoryRuntime scriptB = FactoryRuntime.forModel(version);
        scriptB.submitWorkload(PRODUCT, 2, PRICE).orElseThrow();
        scriptB.advanceUntil(SimTime.of(10), Long.MAX_VALUE);
        RuntimeObservation beforeSubmitB = scriptB.observe();
        scriptB.submitWorkload(PRODUCT, 1, PRICE).orElseThrow();
        scriptB.advanceUntil(END, Long.MAX_VALUE);
        List<RuntimeEventEnvelope> historyB = scriptB.drainSupportedEvents();

        assertEquals(5, beforeSubmitA.metadata().currentTime().value(), "the observation still says 5");
        assertEquals(10, beforeSubmitB.metadata().currentTime().value());
        assertEquals(List.of(
                new Shape(1, 0, ORDER_ACCEPTED),
                new Shape(2, 0, JOB_DISPATCHED),
                new Shape(3, 0, JOB_WAITING),
                new Shape(4, 5, JOB_STEP_COMPLETED),
                new Shape(5, 5, JOB_DISPATCHED),
                new Shape(6, 10, ORDER_ACCEPTED),
                new Shape(7, 10, JOB_WAITING),
                new Shape(8, 10, JOB_STEP_COMPLETED),
                new Shape(9, 10, ORDER_COMPLETED),
                new Shape(10, 10, JOB_DISPATCHED),
                new Shape(11, 15, JOB_STEP_COMPLETED),
                new Shape(12, 15, ORDER_COMPLETED)), shape(historyA));
        assertEquals(List.of(
                new Shape(1, 0, ORDER_ACCEPTED),
                new Shape(2, 0, JOB_DISPATCHED),
                new Shape(3, 0, JOB_WAITING),
                new Shape(4, 5, JOB_STEP_COMPLETED),
                new Shape(5, 5, JOB_DISPATCHED),
                new Shape(6, 10, JOB_STEP_COMPLETED),
                new Shape(7, 10, ORDER_COMPLETED),
                new Shape(8, 10, ORDER_ACCEPTED),
                new Shape(9, 10, JOB_DISPATCHED),
                new Shape(10, 15, JOB_STEP_COMPLETED),
                new Shape(11, 15, ORDER_COMPLETED)), shape(historyB));

        // Re-driving A's accepted commands at their recorded times through supported control yields B, not A.
        FactoryRuntime redriven = FactoryRuntime.forModel(version);
        for (RuntimeEventEnvelope event : historyA) {
            if (event.payload() instanceof RuntimeEventPayload.OrderAccepted accepted) {
                redriven.advanceUntil(event.simulationTime(), Long.MAX_VALUE);
                redriven.submitWorkload(accepted.productId(), accepted.quantity(), accepted.unitPrice()).orElseThrow();
            }
        }
        redriven.advanceUntil(END, Long.MAX_VALUE);
        List<Normalized> redrivenHistory = normalized(redriven.drainSupportedEvents());
        assertEquals(normalized(historyB), redrivenHistory);
        assertNotEquals(normalized(historyA), redrivenHistory);
    }

    // ---------------------------------------------------------------- presentations / drainers

    @Test
    void case13TwoDrainersOfOneLiveRunEachHoldAPartialAccountWhileOneCaptureServesTwoViews() {
        FactoryRuntime runtime = FactoryRuntime.forModel(publish(LINE));
        RuntimeObservation establishment = runtime.observe();
        CapturedAccount cliDrainer = CapturedAccount.from(establishment);
        CapturedAccount webDrainer = CapturedAccount.from(establishment);
        CapturedAccount shared = CapturedAccount.from(establishment);

        runtime.submitWorkload(PRODUCT, 2, PRICE).orElseThrow();
        runtime.advanceUntil(SimTime.of(4), Long.MAX_VALUE);
        List<RuntimeEventEnvelope> firstDrain = runtime.drainSupportedEvents();
        cliDrainer.retain(firstDrain);
        shared.retain(firstDrain);
        RuntimeObservation middle = runtime.observe();
        cliDrainer.observeFrontier(middle);
        assertTrue(cliDrainer.coversFromEstablishment(),
                "up to its own frontier the first drainer looks complete; it cannot see what it will miss");

        runtime.advanceUntil(END, Long.MAX_VALUE);
        List<RuntimeEventEnvelope> secondDrain = runtime.drainSupportedEvents();
        webDrainer.retain(secondDrain);
        shared.retain(secondDrain);
        RuntimeObservation last = runtime.observe();
        cliDrainer.observeFrontier(last);
        webDrainer.observeFrontier(last);
        shared.observeFrontier(last);

        Set<Long> cliSequences = firstDrain.stream().map(RuntimeEventEnvelope::sequence).collect(Collectors.toSet());
        Set<Long> webSequences = secondDrain.stream().map(RuntimeEventEnvelope::sequence).collect(Collectors.toSet());
        Set<Long> union = new HashSet<>(cliSequences);
        union.addAll(webSequences);
        assertTrue(cliSequences.stream().noneMatch(webSequences::contains), "draining is single-consumer");
        assertEquals(LongStream.rangeClosed(1, 12).boxed().collect(Collectors.toSet()), union);
        assertEquals(List.of(new SequenceRange(7, 12)), cliDrainer.missing());
        assertEquals(List.of(new SequenceRange(1, 6)), webDrainer.missing());

        // One capture, two presentations: same identity, order and coverage by construction.
        List<String> cliView = shared.retainedEvents().stream()
                .map(e -> e.sequence() + "@" + e.simulationTime().value() + " " + e.eventType())
                .toList();
        Map<RuntimeEventType, Long> webView = shared.retainedEvents().stream()
                .collect(Collectors.groupingBy(RuntimeEventEnvelope::eventType, TreeMap::new, Collectors.counting()));
        assertEquals(12, cliView.size());
        assertEquals(12L, webView.values().stream().mapToLong(Long::longValue).sum());
        assertTrue(shared.coversFromEstablishment());
    }

    // ---------------------------------------------------------------- spatial

    @Test
    void case11PresentSpatialContentIsRefusedBeforeAnyExecutionExists() {
        FactoryModel production = LINE.model();
        FactoryModelVersion spatial = FactoryModelPublisher.publish(new FactoryModel(
                production.resources(),
                production.operations(),
                production.products(),
                Optional.of(new SpatialRecord(new FactoryFloor(4, 1), 1, 0, List.of(
                        new ResourceLayout(CUTTER, new ResourcePlacement(0, 0), new ResourceFootprint(1, 1)),
                        new ResourceLayout(ASSEMBLER, new ResourcePlacement(3, 0), new ResourceFootprint(1, 1)))))));
        assertThrows(UnsupportedModelContentException.class, () -> FactoryRuntime.forModel(spatial));
    }
}
