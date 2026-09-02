package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.model.ResourceDefinition;
import com.arcogine.types.JobId;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Headless Gate 4-C closure evidence: the remaining acceptance facts that G4-A's observation
 * boundary ({@link Gate4RuntimeObservationAcceptanceTest}) and G4-B's supported event contract
 * ({@link Gate4BRuntimeEventAcceptanceTest}) do not already prove on their own --
 *
 * <ul>
 *   <li>a consumer joining an already-progressed runtime can reconstruct the complete current
 *       supported view from one fresh {@link FactoryRuntime#observe()} alone, with no retained or
 *       replayed runtime events, no internal scheduler/{@code EventLog} replay, no {@code
 *       FactoryHandler}/mutable-store access, and no API/Spring/frontend DTOs;
 *   <li>supported observations and supported runtime events close over the same authoritative
 *       transitions: an observation at sequence {@code S}, plus the supported events emitted after
 *       it, accounts for the state a later observation reports;
 *   <li>the supported observation is sufficient to identify the active production bottleneck.
 * </ul>
 *
 * <p>The seventh Gate 4 acceptance criterion (API/UI DTOs never re-enter domain decision paths) is
 * structural rather than behavioural and is enforced by {@code ArchitectureTest
 * .api_dtos_must_not_reenter_domain_decision_paths} in {@code interfaces/api}, the only module
 * whose test classpath can see both sides of that boundary.
 *
 * <p>Everything here is driven purely through {@link FactoryRuntime}'s supported surface, matching
 * the conventions of the two tests above.
 */
class Gate4CHeadlessClosureAcceptanceTest {

    private static final double UNIT_PRICE = 4.25;

    /**
     * A deliberately unbalanced two-step routing: every unit is prepared quickly on {@code M1} and
     * then finished slowly on {@code M2}, so {@code M2} becomes the unambiguous active production
     * bottleneck while {@code M1} runs dry.
     */
    private static FactoryModelVersion unbalancedTwoStageModel() {
        return FactoryModelPublisher.publish(new FactoryModel(
                List.of(
                        new ResourceDefinition(new MachineId(1), "Prep", 1, null, 0),
                        new ResourceDefinition(new MachineId(2), "Finish", 1, null, 0)),
                List.of(new OperationDefinition(
                        1,
                        "Make",
                        List.of(
                                new OperationStepDefinition(1, "PREP", Set.of(new MachineId(1)), 1),
                                new OperationStepDefinition(2, "FINISH", Set.of(new MachineId(2)), 10)))),
                List.of(new ProductDefinition(new ProductId(1), "Widget", 1))));
    }

    /**
     * The complete supported consumer view, rebuilt from nothing but one {@link RuntimeObservation}
     * -- deliberately a pure function of its single argument, so a passing assertion is itself the
     * evidence that no retained event history, internal store, or scheduler replay was consulted.
     */
    private record ConsumerView(
            long latestEventSequence,
            SimTime currentTime,
            RuntimeRunState runState,
            Map<OrderId, Long> completedQuantityByOrder,
            Set<OrderId> completedOrders,
            Map<JobStatus, List<JobId>> jobIdsByStatus,
            Map<JobId, MachineId> assignedMachineByJob,
            Set<JobId> pendingMultiEligibleJobs,
            long backlog) {

        static ConsumerView reconstruct(RuntimeObservation observation) {
            return new ConsumerView(
                    observation.metadata().latestEventSequence(),
                    observation.metadata().currentTime(),
                    observation.metadata().runState(),
                    observation.orders().stream()
                            .collect(Collectors.toMap(
                                    OrderObservation::orderId, OrderObservation::completedQuantity)),
                    observation.orders().stream()
                            .filter(OrderObservation::complete)
                            .map(OrderObservation::orderId)
                            .collect(Collectors.toCollection(java.util.LinkedHashSet::new)),
                    observation.jobs().stream()
                            .collect(Collectors.groupingBy(
                                    JobObservation::status,
                                    Collectors.mapping(JobObservation::jobId, Collectors.toList()))),
                    observation.resources().stream()
                            .flatMap(resource -> resource.activeJobIds().stream()
                                    .map(jobId -> Map.entry(jobId, resource.machineId())))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                    observation.pendingWork().stream()
                            .map(PendingWorkObservation::jobId)
                            .collect(Collectors.toCollection(java.util.LinkedHashSet::new)),
                    observation.performance().backlog());
        }
    }

    /**
     * Two fully interchangeable single-step machines: submitting three units starts two of them
     * immediately and leaves the third in the cross-machine multi-eligible backlog, so the first
     * {@code TaskEnd} frees a machine that must then pick that waiting job up. This is the shape
     * that exposes a {@code TaskEnd} dispatch cascade missing from the supported event stream.
     */
    private static FactoryModelVersion twoInterchangeableMachinesModel() {
        return FactoryModelPublisher.publish(new FactoryModel(
                List.of(
                        new ResourceDefinition(new MachineId(1), "Cell A", 1, null, 0),
                        new ResourceDefinition(new MachineId(2), "Cell B", 1, null, 0)),
                List.of(new OperationDefinition(
                        1,
                        "Make",
                        List.of(new OperationStepDefinition(
                                1, "MAKE", Set.of(new MachineId(1), new MachineId(2)), 5)))),
                List.of(new ProductDefinition(new ProductId(1), "Widget", 1))));
    }

    /**
     * The placement half of the supported consumer view -- what every job is doing, where, and what
     * is still in the cross-machine multi-eligible backlog -- with a transition function that
     * consumes nothing but supported {@link RuntimeEventEnvelope}s.
     *
     * <p>This is the closure evidence the G4-C criterion actually requires: {@code
     * of(earlier).applyAll(delta)} equalling {@code of(later)} proves a consumer can derive the
     * later placement state from an earlier observation using only the supported event stream,
     * without re-observing or reproducing internal dispatch logic.
     */
    private record PlacementView(
            Map<JobId, JobStatus> statusByJob,
            Map<JobId, MachineId> assignedMachineByJob,
            Set<JobId> pendingMultiEligibleJobs) {

        static PlacementView of(RuntimeObservation observation) {
            Map<JobId, JobStatus> status = new LinkedHashMap<>();
            observation.jobs().forEach(job -> status.put(job.jobId(), job.status()));
            Map<JobId, MachineId> assigned = new LinkedHashMap<>();
            observation.resources().forEach(resource ->
                    resource.activeJobIds().forEach(jobId -> assigned.put(jobId, resource.machineId())));
            return new PlacementView(
                    status,
                    assigned,
                    observation.pendingWork().stream()
                            .map(PendingWorkObservation::jobId)
                            .collect(Collectors.toCollection(LinkedHashSet::new)));
        }

        PlacementView applyAll(List<RuntimeEventEnvelope> events) {
            Map<JobId, JobStatus> status = new LinkedHashMap<>(statusByJob);
            Map<JobId, MachineId> assigned = new LinkedHashMap<>(assignedMachineByJob);
            Set<JobId> pending = new LinkedHashSet<>(pendingMultiEligibleJobs);
            for (RuntimeEventEnvelope event : events) {
                switch (event.payload()) {
                    case RuntimeEventPayload.OrderAccepted accepted ->
                        accepted.jobIds().forEach(jobId -> status.put(jobId, JobStatus.Queued));
                    case RuntimeEventPayload.JobDispatched dispatched -> {
                        status.put(dispatched.jobId(), JobStatus.InProgress);
                        assigned.put(dispatched.jobId(), dispatched.machineId());
                        pending.remove(dispatched.jobId());
                    }
                    case RuntimeEventPayload.JobWaiting waiting -> {
                        status.put(waiting.jobId(), JobStatus.Queued);
                        assigned.remove(waiting.jobId());
                        if (waiting.eligibleMachines().size() > 1) {
                            pending.add(waiting.jobId());
                        } else {
                            pending.remove(waiting.jobId());
                        }
                    }
                    case RuntimeEventPayload.JobStepCompleted completed -> {
                        status.put(
                                completed.jobId(),
                                completed.jobComplete() ? JobStatus.Completed : JobStatus.Queued);
                        assigned.remove(completed.jobId());
                        if (completed.jobComplete()) {
                            pending.remove(completed.jobId());
                        }
                    }
                    case RuntimeEventPayload.OrderCompleted ignored -> { }
                    case RuntimeEventPayload.MachineAvailabilityChanged ignored -> { }
                }
            }
            return new PlacementView(status, assigned, pending);
        }
    }

    /**
     * Drives a runtime to a non-trivial mid-flight state -- one order fully complete, one order
     * still mixing active and waiting child work -- and returns the second order's id.
     */
    private static OrderId progressToMixedState(FactoryRuntime runtime) throws SimError {
        runtime.submitWorkload(new ProductId(1), 1, UNIT_PRICE).orElseThrow();
        while (runtime.advance().isPresent()) {}
        OrderId second = runtime.submitWorkload(new ProductId(1), 3, UNIT_PRICE).orElseThrow();
        runtime.advanceUntil(new SimTime(runtime.observe().metadata().currentTime().value() + 5), 100);
        return second;
    }

    @Test
    void freshObservationReconstructsCurrentConsumerViewWithoutReplay() throws SimError {
        FactoryModelVersion version = unbalancedTwoStageModel();
        FactoryRuntime runtime = FactoryRuntime.forModel(version);
        OrderId inFlight = progressToMixedState(runtime);

        // A late-joining consumer has no retained runtime events: everything emitted so far is
        // drained and discarded here, exactly as if it had been delivered to somebody else.
        List<RuntimeEventEnvelope> discarded = runtime.drainSupportedEvents();
        assertFalse(discarded.isEmpty(), "the scenario must have emitted supported events before the join");
        assertTrue(runtime.drainSupportedEvents().isEmpty(), "drain must not retain a replayable journal");

        RuntimeObservation fresh = runtime.observe();
        ConsumerView view = ConsumerView.reconstruct(fresh);

        // The cursor survives draining: the fresh observation still tells the consumer exactly
        // where the supported event stream stands, so it can continue from there without replay.
        assertEquals(discarded.getLast().sequence(), view.latestEventSequence());
        assertEquals(version.fingerprint(), fresh.metadata().modelFingerprint());
        assertEquals(runtime.runId(), fresh.metadata().runId());

        // The reconstructed view is genuinely non-trivial: completed, active and waiting work all
        // present at once, across both orders.
        assertEquals(4, view.jobIdsByStatus().values().stream().mapToInt(List::size).sum());
        assertEquals(1, view.completedOrders().size(), "the first order completed before the join");
        assertFalse(view.completedOrders().contains(inFlight));
        assertTrue(view.jobIdsByStatus().getOrDefault(JobStatus.Completed, List.of()).size() >= 1);
        assertTrue(view.jobIdsByStatus().getOrDefault(JobStatus.InProgress, List.of()).size() >= 1);
        assertTrue(view.jobIdsByStatus().getOrDefault(JobStatus.Queued, List.of()).size() >= 1);
        assertEquals(RuntimeRunState.ACTIVE, view.runState());
        assertEquals(1, view.backlog(), "one order is still open");

        // Every job the consumer believes is running is attributed to the resource actually
        // running it -- the assignment fact a consumer would otherwise have to replay events for.
        for (JobId active : view.jobIdsByStatus().getOrDefault(JobStatus.InProgress, List.of())) {
            assertTrue(view.assignedMachineByJob().containsKey(active));
        }

        // Reconstructing again from an independently taken fresh observation yields the same view:
        // the observation, not any accumulated consumer history, is the authority.
        assertEquals(view, ConsumerView.reconstruct(runtime.observe()));

        // And the same view is reachable at any later join point from a fresh observation alone --
        // advancing further changes what is observed, never whether it is observable.
        runtime.advanceUntil(new SimTime(Long.MAX_VALUE), 3);
        runtime.drainSupportedEvents();
        ConsumerView later = ConsumerView.reconstruct(runtime.observe());
        assertNotEquals(view, later);
        assertTrue(later.latestEventSequence() > view.latestEventSequence());
    }

    @Test
    void observationAndSupportedEventsCloseOverTheSameAuthoritativeTransitions() throws SimError {
        FactoryRuntime runtime = FactoryRuntime.forModel(unbalancedTwoStageModel());
        OrderId orderId = runtime.submitWorkload(new ProductId(1), 3, UNIT_PRICE).orElseThrow();
        runtime.drainSupportedEvents();

        RuntimeObservation before = runtime.observe();
        ConsumerView beforeView = ConsumerView.reconstruct(before);
        assertFalse(beforeView.completedOrders().contains(orderId));

        while (runtime.advance().isPresent()) {}
        List<RuntimeEventEnvelope> delta = runtime.drainSupportedEvents();
        RuntimeObservation after = runtime.observe();

        // 1. The delta is exactly the events that follow the earlier observation's cursor, with no
        //    gap and no overlap -- so observation and event stream agree about how much
        //    authoritative change has happened.
        assertEquals(
                before.metadata().latestEventSequence() + delta.size(),
                after.metadata().latestEventSequence());
        assertEquals(before.metadata().latestEventSequence() + 1, delta.getFirst().sequence());
        assertEquals(after.metadata().latestEventSequence(), delta.getLast().sequence());

        // 2. Every authoritative transition the delta reports is accounted for by the later
        //    observation's state -- no event claims something the observation contradicts.
        Map<JobId, JobObservation> jobsAfter = after.jobs().stream()
                .collect(Collectors.toMap(JobObservation::jobId, Function.identity()));
        for (RuntimeEventEnvelope event : delta) {
            switch (event.payload()) {
                case RuntimeEventPayload.JobStepCompleted completed -> {
                    JobObservation job = jobsAfter.get(completed.jobId());
                    assertTrue(job.currentStep() > completed.stepIndex()
                            || job.status() == JobStatus.Completed);
                    assertEquals(orderId, job.orderId());
                }
                case RuntimeEventPayload.OrderCompleted completed ->
                    assertTrue(after.orders().stream()
                            .anyMatch(o -> o.orderId().equals(completed.orderId()) && o.complete()));
                case RuntimeEventPayload.JobDispatched dispatched ->
                    assertEquals(orderId, jobsAfter.get(dispatched.jobId()).orderId());
                case RuntimeEventPayload.JobWaiting waiting ->
                    assertEquals(orderId, jobsAfter.get(waiting.jobId()).orderId());
                default -> throw new AssertionError("unexpected supported payload " + event.payload());
            }
        }

        // 2b. The delta is not merely consistent with the later observation, it is sufficient for
        //     it: replaying only these supported events onto the earlier observation's placement
        //     state reproduces the later observation's placement state exactly.
        assertEquals(PlacementView.of(after), PlacementView.of(before).applyAll(delta));

        // 3. Conversely, the state the later observation reports that the earlier one did not is
        //    entirely explained by the delta: the order became complete, and the delta says so.
        ConsumerView afterView = ConsumerView.reconstruct(after);
        assertTrue(afterView.completedOrders().contains(orderId));
        assertTrue(delta.stream()
                .anyMatch(e -> e.eventType() == RuntimeEventType.ORDER_COMPLETED
                        && ((RuntimeEventPayload.OrderCompleted) e.payload()).orderId().equals(orderId)));
        assertEquals(3, afterView.completedQuantityByOrder().get(orderId));
    }

    /**
     * REV-002 regression evidence: a {@code TaskEnd} authoritatively re-places work beyond the job
     * whose step ended -- the freed machine immediately picks up backlog work -- and the supported
     * delta must say so. Reporting only {@code JOB_STEP_COMPLETED} here would leave a consumer
     * unable to derive the newly dispatched job's status or machine assignment from the event
     * stream, defeating the G4-C closure claim.
     */
    @Test
    void taskEndDispatchCascadeIsReportedByTheSupportedEventStream() throws SimError {
        FactoryRuntime runtime = FactoryRuntime.forModel(twoInterchangeableMachinesModel());
        runtime.submitWorkload(new ProductId(1), 3, UNIT_PRICE).orElseThrow();
        runtime.drainSupportedEvents();

        RuntimeObservation before = runtime.observe();
        PlacementView beforeView = PlacementView.of(before);
        assertEquals(2, beforeView.assignedMachineByJob().size(), "two units start immediately");
        assertEquals(1, beforeView.pendingMultiEligibleJobs().size(), "the third waits on both cells");
        JobId waiting = beforeView.pendingMultiEligibleJobs().iterator().next();

        // Exactly one authoritative transition: the first unit's TaskEnd, which frees its cell.
        runtime.advance().orElseThrow();
        List<RuntimeEventEnvelope> delta = runtime.drainSupportedEvents();
        RuntimeObservation after = runtime.observe();
        PlacementView afterView = PlacementView.of(after);

        // The freed capacity genuinely re-placed the waiting job -- that is the authoritative fact.
        assertEquals(JobStatus.InProgress, afterView.statusByJob().get(waiting));
        assertTrue(afterView.assignedMachineByJob().containsKey(waiting));
        assertTrue(afterView.pendingMultiEligibleJobs().isEmpty());

        // ... and the supported delta identifies it, by job and by the machine it landed on.
        RuntimeEventPayload.JobDispatched dispatched = delta.stream()
                .filter(e -> e.eventType() == RuntimeEventType.JOB_DISPATCHED)
                .map(e -> (RuntimeEventPayload.JobDispatched) e.payload())
                .filter(payload -> payload.jobId().equals(waiting))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "no JOB_DISPATCHED for the job the TaskEnd cascade placed; delta was " + delta));
        assertEquals(afterView.assignedMachineByJob().get(waiting), dispatched.machineId());

        // Closure: the delta alone carries the earlier placement state to the later one.
        assertNotEquals(beforeView, afterView);
        assertEquals(afterView, beforeView.applyAll(delta));
    }

    /**
     * REV-003 regression evidence: the internal scheduler also carries markers {@code
     * FactoryHandler} ignores -- the {@code TaskStart} paired with every dispatched {@code TaskEnd},
     * and the {@code OrderCompleted} a terminal {@code TaskEnd} schedules purely so other internal
     * handlers can observe completion. Processing one is authoritatively a no-op and emits no
     * supported event, so it must not produce a second, different observation at the same {@code
     * latestEventSequence}: otherwise "fresh observation at {@code S} + supported events after
     * {@code S} = current consumer view" would have two contradictory answers at the same {@code
     * S}.
     */
    @Test
    void processingANoOpInternalMarkerLeavesTheSupportedObservationUnchanged() throws SimError {
        FactoryRuntime runtime = FactoryRuntime.forModel(twoInterchangeableMachinesModel());
        runtime.submitWorkload(new ProductId(1), 3, UNIT_PRICE).orElseThrow();
        runtime.drainSupportedEvents();

        int taskStartMarkers = 0;
        int orderCompletedMarkers = 0;
        Optional<Event> processed;
        while ((processed = advanceObservingMarkers(runtime)).isPresent()) {
            EventPayload payload = processed.get().payload();
            if (payload instanceof EventPayload.TaskStart) {
                taskStartMarkers++;
            } else if (payload instanceof EventPayload.OrderCompleted) {
                orderCompletedMarkers++;
            }
        }

        assertTrue(taskStartMarkers > 0, "the dispatch path must have produced TaskStart markers");
        assertTrue(
                orderCompletedMarkers > 0,
                "the terminal TaskEnd must have scheduled an internal OrderCompleted marker");

        // The run genuinely drained, and the drained runtime reports quiescence -- not ACTIVE
        // merely because internal bookkeeping was still queued behind the last supported event.
        RuntimeObservation drained = runtime.observe();
        assertEquals(RuntimeRunState.QUIESCENT, drained.metadata().runState());
        assertTrue(drained.orders().stream().allMatch(OrderObservation::complete));
    }

    /**
     * Advances one event, asserting that if it was a no-op internal marker the supported view did
     * not move at all: no supported event, and an observation identical in every fact -- {@code
     * latestEventSequence}, {@code currentTime}, {@code runState}, resources, orders, jobs, pending
     * work and performance alike.
     */
    private static Optional<Event> advanceObservingMarkers(FactoryRuntime runtime) throws SimError {
        RuntimeObservation before = runtime.observe();
        Optional<Event> processed = runtime.advance();
        List<RuntimeEventEnvelope> delta = runtime.drainSupportedEvents();
        if (processed.isEmpty()) {
            return processed;
        }
        boolean marker = !(processed.get().payload() instanceof EventPayload.TaskEnd)
                && !(processed.get().payload() instanceof EventPayload.OrderCreation)
                && !(processed.get().payload() instanceof EventPayload.MachineAvailabilityChange);
        if (marker) {
            assertTrue(delta.isEmpty(), "a no-op marker must not emit supported events: " + delta);
            assertEquals(
                    before,
                    runtime.observe(),
                    "processing " + processed.get().payload() + " changed the supported observation");
        } else {
            assertFalse(delta.isEmpty(), "an authoritative transition must advance the supported stream");
        }
        return processed;
    }

    @Test
    void supportedObservationIdentifiesTheActiveBottleneckWithoutInternalAccess() throws SimError {
        FactoryRuntime runtime = FactoryRuntime.forModel(unbalancedTwoStageModel());
        runtime.submitWorkload(new ProductId(1), 4, UNIT_PRICE).orElseThrow();
        runtime.advanceUntil(new SimTime(25), 200);
        runtime.drainSupportedEvents();

        RuntimeObservation observation = runtime.observe();
        long elapsed = observation.metadata().currentTime().value();
        assertTrue(elapsed > 0);

        // Consumer-side bottleneck identification, using only supported observation facts: the
        // resource carrying the most work (active plus queued) and, independently, the highest
        // busy-tick utilization. Ordering is broken deterministically by machine id.
        Comparator<ResourceObservation> byLoad = Comparator
                .comparingInt((ResourceObservation r) -> r.activeJobIds().size() + r.queueDepth())
                .thenComparing(r -> r.machineId().value());
        Comparator<ResourceObservation> byUtilization = Comparator
                .comparingDouble((ResourceObservation r) -> (double) r.busyTicks() / elapsed)
                .thenComparing(r -> r.machineId().value());

        ResourceObservation busiest = observation.resources().stream().max(byLoad).orElseThrow();
        ResourceObservation mostUtilized = observation.resources().stream().max(byUtilization).orElseThrow();

        assertEquals(new MachineId(2), busiest.machineId(), "the slow finishing stage is the bottleneck");
        assertEquals(new MachineId(2), mostUtilized.machineId());
        assertEquals("Finish", busiest.name());

        ResourceObservation prep = observation.resources().stream()
                .filter(r -> r.machineId().equals(new MachineId(1)))
                .findFirst()
                .orElseThrow();
        assertTrue(
                busiest.queueDepth() > prep.queueDepth(),
                "work accumulates in front of the bottleneck, not in front of the fast stage");
        assertTrue(
                busiest.busyTicks() > prep.busyTicks(),
                "the bottleneck is also the most utilized resource");
        assertTrue(prep.activeJobIds().isEmpty(), "the non-bottleneck stage has already run dry");

        // The same identification is reproducible from an equivalent fresh run, so a consumer's
        // bottleneck diagnosis is deterministic rather than an artifact of one run's identity.
        FactoryRuntime replay = runtime.reset();
        assertNotEquals(runtime.runId(), replay.runId());
        replay.submitWorkload(new ProductId(1), 4, UNIT_PRICE).orElseThrow();
        replay.advanceUntil(new SimTime(25), 200);
        assertEquals(observation.resources(), replay.observe().resources());
    }
}
