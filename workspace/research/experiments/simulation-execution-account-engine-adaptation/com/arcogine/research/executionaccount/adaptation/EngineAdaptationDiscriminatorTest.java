package com.arcogine.research.executionaccount.adaptation;

import static org.junit.jupiter.api.Assertions.*;

import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.process.CommandResult;
import com.arcogine.factory.process.FactoryRuntime;
import com.arcogine.factory.process.JobObservation;
import com.arcogine.factory.process.OrderObservation;
import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeEventType;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.factory.process.RuntimeRunState;
import com.arcogine.research.experiment.ExperimentEvidence;
import com.arcogine.research.experiment.LinearRoutingFamily;
import com.arcogine.research.experiment.LinearRoutingFamily.Resource;
import com.arcogine.research.experiment.LinearRoutingFamily.Step;
import com.arcogine.research.experiment.SequenceRange;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Discriminators between controller-managed closure and candidate Engine adaptations. Every
 * expected value was derived by hand, before execution, in the engine-adaptation framing
 * checkpoint. A passing negative probe demonstrates the stated limitation; it is not a repair.
 * Proposed Engine behavior is not executed here: it cannot be without changing production code.
 */
class EngineAdaptationDiscriminatorTest {

    private static final long MAX = Long.MAX_VALUE;
    private static final MachineId M1 = new MachineId(1);
    private static final MachineId CUTTER = new MachineId(1);
    private static final MachineId SECOND = new MachineId(2);

    /** One five-tick step on one concurrency-1 machine. */
    private static final LinearRoutingFamily S = new LinearRoutingFamily(
            List.of(new Step("PROCESS", 5)), List.of(Resource.of("M1", 1, "PROCESS")));

    /** The original report's model A: CUT 3 then ASSEMBLE 5. */
    private static final LinearRoutingFamily A2 = new LinearRoutingFamily(
            List.of(new Step("CUT", 3), new Step("ASSEMBLE", 5)),
            List.of(Resource.of("Cutter", 1, "CUT"), Resource.of("Assembler", 1, "ASSEMBLE")));

    /** A second step whose completion time overflows when it is dispatched. */
    private static final LinearRoutingFamily F = new LinearRoutingFamily(
            List.of(new Step("CUT", 5), new Step("HOLD", Long.MAX_VALUE)),
            List.of(Resource.of("Cutter", 1, "CUT"), Resource.of("Holder", 1, "HOLD")));

    private static FactoryRuntime runtime(LinearRoutingFamily family) {
        return FactoryRuntime.forModel(FactoryModelPublisher.publish(family.model()));
    }

    private static void submit(FactoryRuntime runtime, long quantity) {
        runtime.submitWorkload(LinearRoutingFamily.PRODUCT, quantity, LinearRoutingFamily.UNIT_PRICE).orElseThrow();
    }

    private static List<Long> sequences(List<RuntimeEventEnvelope> events) {
        return events.stream().map(RuntimeEventEnvelope::sequence).toList();
    }

    private static List<RuntimeEventType> types(List<RuntimeEventEnvelope> events) {
        return events.stream().map(RuntimeEventEnvelope::eventType).toList();
    }

    private static List<Long> times(List<RuntimeEventEnvelope> events) {
        return events.stream().map(event -> event.simulationTime().value()).toList();
    }

    private static long cursor(RuntimeObservation observation) {
        return observation.metadata().latestEventSequence();
    }

    private static long time(RuntimeObservation observation) {
        return observation.metadata().currentTime().value();
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

    private static ControllerClosure.Attestation attest(FactoryRuntime runtime, long closedBefore, String attestor) {
        ControllerClosure.Result result = ControllerClosure.closeBefore(runtime, closedBefore, 1, attestor);
        return assertInstanceOf(ControllerClosure.Result.Attested.class, result).attestation();
    }

    private static void collect(FactoryRuntime runtime, HeldAccount account) {
        account.retain(runtime.drainSupportedEvents());
        account.observeFrontier(runtime.observe());
    }

    // ---- Case 2 and 6: what one budgeted internal event means ----

    @Test
    void oneBudgetedEventIsAnAuthoritativeCompletionOnlyOnSomeInternalDispatchPaths() {
        FactoryRuntime initialPath = runtime(S);
        submit(initialPath, 1);
        initialPath.drainSupportedEvents();
        assertEquals(1, initialPath.advanceUntil(SimTime.of(100), 1).size());
        List<RuntimeEventEnvelope> progressed = initialPath.drainSupportedEvents();
        assertEquals(List.of(3L, 4L), sequences(progressed));
        assertEquals(List.of(RuntimeEventType.JOB_STEP_COMPLETED, RuntimeEventType.ORDER_COMPLETED), types(progressed));
        assertEquals(RuntimeRunState.QUIESCENT, initialPath.observe().metadata().runState());

        FactoryRuntime queuePath = runtime(S);
        submit(queuePath, 2);
        queuePath.advanceUntil(SimTime.of(9), MAX);
        queuePath.drainSupportedEvents();
        RuntimeObservation before = queuePath.observe();
        assertEquals(5, cursor(before));
        assertEquals(5, time(before));
        assertEquals(RuntimeRunState.ACTIVE, before.metadata().runState());
        assertEquals(1, queuePath.advanceUntil(SimTime.of(100), 1).size()); // the start marker at 10
        assertEquals(before, queuePath.observe());
        assertTrue(queuePath.drainSupportedEvents().isEmpty());
    }

    @Test
    void aSupportedInvisibleBudgetedEventChangesTheTimeAndOutcomeOfTheNextCommand() {
        FactoryRuntime y1 = runtime(S);
        submit(y1, 2);
        y1.advanceUntil(SimTime.of(9), MAX);
        RuntimeObservation y1Before = y1.observe();
        assertEquals(1, y1.advanceUntil(SimTime.of(100), 1).size());
        RuntimeObservation y1AtSubmit = y1.observe();
        assertEquals(y1Before, y1AtSubmit); // nothing supported distinguishes the two moments
        submit(y1, 1);
        y1.advanceUntil(SimTime.of(100), MAX);
        List<RuntimeEventEnvelope> y1Events = y1.drainSupportedEvents();

        FactoryRuntime y2 = runtime(S);
        submit(y2, 2);
        y2.advanceUntil(SimTime.of(9), MAX);
        RuntimeObservation y2AtSubmit = y2.observe();
        assertSameExceptRunIdentity(y1AtSubmit, y2AtSubmit);
        submit(y2, 1);
        y2.advanceUntil(SimTime.of(100), MAX);
        List<RuntimeEventEnvelope> y2Events = y2.drainSupportedEvents();

        assertEquals(12, y1Events.size());
        assertEquals(types(y1Events), types(y2Events));
        assertEquals(List.of(0L, 0L, 0L, 5L, 5L, 10L, 10L, 10L, 10L, 10L, 15L, 15L), times(y1Events));
        assertEquals(List.of(0L, 0L, 0L, 5L, 5L, 5L, 5L, 10L, 10L, 10L, 15L, 15L), times(y2Events));

        OrderObservation y1Second = y1.observe().orders().get(1);
        OrderObservation y2Second = y2.observe().orders().get(1);
        assertEquals(10, y1Second.createdAt().value());
        assertEquals(5, y2Second.createdAt().value());
        assertEquals(15, y1Second.completedAt().value());
        assertEquals(15, y2Second.completedAt().value());
        assertEquals(7.5, y1.observe().performance().averageLeadTime());
        assertEquals(10.0, y2.observe().performance().averageLeadTime());
    }

    @Test
    void budgetZeroAndAnEmptyResultDoNotProveExhaustion() {
        FactoryRuntime runtime = runtime(S);
        submit(runtime, 1);
        assertEquals(0, runtime.advanceUntil(SimTime.of(100), 0).size());
        RuntimeObservation after = runtime.observe();
        assertEquals(RuntimeRunState.ACTIVE, after.metadata().runState());
        assertEquals(0, time(after));
        assertEquals(2, cursor(after));
        assertThrows(IllegalArgumentException.class, () -> runtime.advanceUntil(SimTime.of(100), -1));
        assertThrows(IllegalArgumentException.class, () -> BoundedAdvance.advanceThrough(runtime, 100, 0));
    }

    // ---- Candidate B: is an Engine stop reason any fact an exclusive driver lacks? ----

    @Test
    void anExclusiveDriverDerivesEveryStopReasonFromTheSupportedSurface() {
        FactoryRuntime pendingBeyond = runtime(S);
        submit(pendingBeyond, 2);
        BoundedAdvance.Outcome beyond = BoundedAdvance.advanceThrough(pendingBeyond, 7, MAX);
        assertEquals(BoundedAdvance.Stop.EXHAUSTED_THROUGH_TARGET, beyond.stop());
        assertTrue(beyond.authoritativeWorkRemains());
        assertEquals(5, beyond.supportedTimeAfter());
        assertEquals(5, beyond.sequenceAfter());

        FactoryRuntime idle = runtime(S);
        submit(idle, 1);
        BoundedAdvance.Outcome drained = BoundedAdvance.advanceThrough(idle, 100, MAX);
        assertEquals(BoundedAdvance.Stop.EXHAUSTED_THROUGH_TARGET, drained.stop());
        assertFalse(drained.authoritativeWorkRemains());
        assertEquals(5, drained.supportedTimeAfter());
        assertEquals(4, drained.sequenceAfter());

        FactoryRuntime marker = runtime(S);
        submit(marker, 2);
        marker.advanceUntil(SimTime.of(9), MAX);
        BoundedAdvance.Outcome onMarker = BoundedAdvance.advanceThrough(marker, 100, 1);
        assertEquals(BoundedAdvance.Stop.BUDGET_EXHAUSTED, onMarker.stop());
        assertEquals(5, onMarker.supportedTimeAfter());

        FactoryRuntime exact = runtime(S);
        submit(exact, 1);
        BoundedAdvance.Outcome usedUp = BoundedAdvance.advanceThrough(exact, 5, 2);
        assertEquals(BoundedAdvance.Stop.BUDGET_EXHAUSTED, usedUp.stop()); // completion + order marker
        assertFalse(usedUp.authoritativeWorkRemains());
        assertEquals(BoundedAdvance.Stop.EXHAUSTED_THROUGH_TARGET, BoundedAdvance.advanceThrough(exact, 5, 1).stop());

        FactoryRuntime faulting = runtime(F);
        submit(faulting, 1);
        BoundedAdvance.Outcome faulted = BoundedAdvance.advanceThrough(faulting, 100, MAX);
        assertEquals(BoundedAdvance.Stop.FAULTED, faulted.stop());
        assertInstanceOf(SimError.EventOrderingViolation.class, faulted.fault().orElseThrow());
    }

    // ---- Case 10: fault during bounded advancement ----

    @Test
    void faultDuringAdvancementIsUnrecordedSoALaterExhaustionLooksClean() {
        FactoryRuntime runtime = runtime(F);
        submit(runtime, 2);
        runtime.drainSupportedEvents();
        assertThrows(SimError.EventOrderingViolation.class, () -> runtime.advanceUntil(SimTime.of(100), MAX));
        List<RuntimeEventEnvelope> surviving = runtime.drainSupportedEvents();
        assertEquals(List.of(4L, 5L), sequences(surviving));
        RuntimeEventPayload.JobStepCompleted completed =
                assertInstanceOf(RuntimeEventPayload.JobStepCompleted.class, surviving.get(0).payload());
        assertEquals(CUTTER, completed.machineId());
        assertEquals(0, completed.stepIndex());
        assertFalse(completed.jobComplete());
        RuntimeEventPayload.JobDispatched dispatched =
                assertInstanceOf(RuntimeEventPayload.JobDispatched.class, surviving.get(1).payload());
        assertEquals(SECOND, dispatched.machineId());
        assertEquals(1, dispatched.stepIndex());

        RuntimeObservation after = runtime.observe();
        assertEquals(RuntimeRunState.QUIESCENT, after.metadata().runState());
        assertEquals(5, time(after));
        ResourceObservation cutter = after.resources().get(0);
        assertEquals(MachineState.Idle, cutter.state());
        assertTrue(cutter.activeJobIds().isEmpty());
        assertEquals(1, cutter.queueDepth()); // eligible work beside an idle online machine
        JobObservation second = after.jobs().get(1);
        assertEquals(JobStatus.Queued, second.status());

        // A successor driver, or one that lost the exception, sees only a clean exhaustion.
        BoundedAdvance.Outcome later = BoundedAdvance.advanceThrough(runtime, 100, MAX);
        assertEquals(BoundedAdvance.Stop.EXHAUSTED_THROUGH_TARGET, later.stop());
        assertFalse(later.authoritativeWorkRemains());
        ControllerClosure.Attestation attestation = attest(runtime, 100, "successor");
        assertEquals(5, attestation.boundSequence());
    }

    // ---- Case 3: stale capture frontier ----

    @Test
    void boundAttestationPreventsIssuingTheStaleFrontierClaim() {
        FactoryRuntime runtime = runtime(S);
        HeldAccount holder = HeldAccount.from(runtime.observe());
        submit(runtime, 1);
        collect(runtime, holder);
        ControllerClosure.Attestation attestation = attest(runtime, 100, "driver");
        assertEquals(4, attestation.boundSequence());
        assertEquals(5, attestation.boundSupportedTime());
        holder.accept(attestation);
        HeldAccount.License stale = holder.license(0, 100);
        assertInstanceOf(HeldAccount.License.Refused.class, stale);
        assertThrows(IllegalStateException.class, () -> holder.activeJobTicks(M1, 0, 100));

        collect(runtime, holder);
        HeldAccount.License.Licensed licensed =
                assertInstanceOf(HeldAccount.License.Licensed.class, holder.license(0, 100));
        assertEquals(HeldAccount.Proof.CONTROLLER_ATTESTATION, licensed.proof());
        assertEquals("driver", licensed.trustedAttestor().orElseThrow());
        assertEquals(5, holder.activeJobTicks(M1, 0, 100));
        // The producer branch alone already licenses the part of the interval supported time has passed.
        assertEquals(HeldAccount.Proof.PRODUCER_SUPPORTED_TIME,
                assertInstanceOf(HeldAccount.License.Licensed.class, holder.license(0, 5)).proof());
    }

    // ---- Case 4: historical subinterval preservation ----

    @Test
    void aPrefixBoundProofSurvivesLaterCaptureLoss() {
        FactoryRuntime runtime = runtime(S);
        HeldAccount holder = HeldAccount.from(runtime.observe());
        submit(runtime, 2);
        runtime.advanceUntil(SimTime.of(5), MAX);
        collect(runtime, holder);
        HeldAccount.License.Licensed early =
                assertInstanceOf(HeldAccount.License.Licensed.class, holder.license(0, 5));
        assertEquals(3, early.proofSequence()); // sequence 4 is the first change at time >= 5
        assertEquals(5, holder.activeJobTicks(M1, 0, 5));

        runtime.advanceUntil(SimTime.of(100), MAX);
        runtime.drainSupportedEvents(); // lose exactly the changes at time 10
        holder.observeFrontier(runtime.observe());
        assertEquals(List.of(new SequenceRange(6, 7)), holder.gapsThrough(7));
        assertInstanceOf(HeldAccount.License.Licensed.class, holder.license(0, 5));
        assertEquals(5, holder.activeJobTicks(M1, 0, 5));
        assertInstanceOf(HeldAccount.License.Refused.class, holder.license(0, 10));
        assertInstanceOf(HeldAccount.License.Refused.class, holder.license(5, 10));
    }

    // ---- Case 8: delivery and shared consumers ----

    @Test
    void competingDrainersCannotUseTheAttestationButTheirUnionCan() {
        FactoryRuntime runtime = runtime(S);
        RuntimeObservation establishment = runtime.observe();
        HeldAccount first = HeldAccount.from(establishment);
        HeldAccount second = HeldAccount.from(establishment);
        HeldAccount union = HeldAccount.from(establishment);
        submit(runtime, 1);
        List<RuntimeEventEnvelope> early = runtime.drainSupportedEvents();
        first.retain(early);
        ControllerClosure.Attestation attestation = attest(runtime, 100, "driver");
        List<RuntimeEventEnvelope> late = runtime.drainSupportedEvents();
        second.retain(late);
        List<RuntimeEventEnvelope> all = new ArrayList<>(early);
        all.addAll(late);
        union.retain(all);
        for (HeldAccount holder : List.of(first, second, union)) {
            holder.observeFrontier(runtime.observe());
            holder.accept(attestation);
        }
        assertInstanceOf(HeldAccount.License.Refused.class, first.license(0, 100));
        assertInstanceOf(HeldAccount.License.Refused.class, second.license(0, 100));
        assertEquals(5, union.activeJobTicks(M1, 0, 100));
    }

    // ---- Case 9: reset and identity ----

    @Test
    void anAttestationBindsToItsRunAcrossReset() {
        FactoryRuntime original = runtime(S);
        submit(original, 1);
        ControllerClosure.Attestation attestation = attest(original, 100, "driver");
        FactoryRuntime fresh = original.reset();
        assertNotEquals(original.runId(), fresh.runId());
        HeldAccount freshHolder = HeldAccount.from(fresh.observe());
        assertThrows(IllegalArgumentException.class, () -> freshHolder.accept(attestation));
        // The original runtime remains usable; only its driver's promise forbids further input.
        assertInstanceOf(CommandResult.Accepted.class,
                original.submitWorkload(LinearRoutingFamily.PRODUCT, 1, LinearRoutingFamily.UNIT_PRICE));
    }

    // ---- Case 1: idle tail and later input ----

    @Test
    void anIdleTailCommandLandsBeforeTheAdvancementTargetAndContradictsAnyClosure() {
        FactoryRuntime runtime = runtime(A2);
        MachineId assembler = new MachineId(2);
        HeldAccount holder = HeldAccount.from(runtime.observe());
        submit(runtime, 2);
        runtime.advanceUntil(SimTime.of(100), MAX);
        collect(runtime, holder);
        assertEquals(RuntimeRunState.QUIESCENT, runtime.observe().metadata().runState());
        assertEquals(12, cursor(runtime.observe()));
        assertEquals(13, time(runtime.observe()));
        assertInstanceOf(HeldAccount.License.Refused.class, holder.license(0, 100)); // open without a promise
        ControllerClosure.Attestation attestation = attest(runtime, 100, "driver");
        assertEquals(12, attestation.boundSequence());
        holder.accept(attestation);
        assertEquals(10, holder.activeJobTicks(assembler, 0, 100));

        submit(runtime, 1); // the promise is broken; the Engine stamps the order at 13, not 100
        List<RuntimeEventEnvelope> late = runtime.drainSupportedEvents();
        assertEquals(List.of(13L, 14L), sequences(late));
        assertEquals(List.of(13L, 13L), times(late));
        holder.retain(late);
        assertEquals(2, holder.contradictions().size()); // detectable only because they were delivered
        assertInstanceOf(HeldAccount.License.Refused.class, holder.license(0, 100));

        runtime.advanceUntil(SimTime.of(100), MAX);
        collect(runtime, holder);
        assertEquals(18, cursor(runtime.observe()));
        assertEquals(21, time(runtime.observe()));
        holder.accept(attest(runtime, 100, "driver-after-input"));
        HeldAccount.License.Licensed licensed =
                assertInstanceOf(HeldAccount.License.Licensed.class, holder.license(0, 100));
        assertEquals("driver-after-input", licensed.trustedAttestor().orElseThrow());
        assertEquals(15, holder.activeJobTicks(assembler, 0, 100));
    }

    // ---- Case 12: the implemented driver obtains "no future input" by confinement ----

    @Test
    void theResearchSubstrateConfinesItsRuntime() {
        RecordComponent[] components = ExperimentEvidence.class.getRecordComponents();
        assertTrue(components.length > 0);
        assertTrue(Arrays.stream(components).noneMatch(component -> component.getType() == FactoryRuntime.class));
    }
}
