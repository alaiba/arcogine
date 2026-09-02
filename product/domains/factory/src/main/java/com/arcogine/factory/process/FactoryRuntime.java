package com.arcogine.factory.process;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.core.queue.Scheduler;
import com.arcogine.factory.jobs.JobView;
import com.arcogine.factory.machines.MachineView;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.factory.model.FactoryRuntimeAssembler;
import com.arcogine.factory.orders.Order;
import com.arcogine.factory.orders.OrderExecutionView;
import com.arcogine.types.JobId;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.RunId;
import com.arcogine.types.SimError;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The supported, consumer-neutral entry point for submitting explicit production workload,
 * independent of the economy/pricing/demand/agent loop.
 *
 * <p>{@link FactoryHandler#submitOrder} takes a caller-supplied {@link Scheduler} and simulation
 * time -- necessary plumbing for the event-driven path, but not something a workload-submission
 * caller should have to own or get right. {@link FactoryRuntime} owns that scheduler/time context
 * internally instead, so a caller only ever supplies commercial/product intent.
 *
 * <p>A {@link FactoryRuntime} always owns the exclusive {@link FactoryHandler}/{@link Scheduler}
 * pair it advances: it is only constructed from a published {@link FactoryModelVersion} via {@link
 * #forModel}, never wrapped around an already-live {@code FactoryHandler} that some other scheduler
 * might also be driving. Without that, two schedulers could race to mutate the same factory state
 * and event ordering would stop being globally authoritative. For the same reason, the mutable
 * {@code FactoryHandler} is not exposed directly; observation happens through this type's own
 * read-only projections.
 *
 * <p>This class implements Gate 3 (docs/planning/factory-simulation-engine-readiness.md §7):
 * consumer-neutral session/control semantics layered onto the exclusive {@link FactoryHandler}/
 * {@link Scheduler} pair from Gate 1/2. {@link #modelVersion()} identifies the published model the
 * session was instantiated from throughout its lifetime; {@link #advance()} and {@link
 * #advanceUntil} give a caller both one-event-at-a-time and bounded-simulated-time control; {@link
 * #reset()} gives a caller a fresh session over the same published model; {@link #submitWorkload}
 * and {@link #setMachineAvailability} return a definite {@link CommandResult} rather than throwing
 * or returning {@code void}; {@link #pendingWorkView()} exposes the cross-machine multi-eligible
 * backlog that no single machine's queue depth reflects. It deliberately does not attempt a generic
 * simulation-session framework. Gate 4-A adds the opaque run identity and current-state
 * observation; Gate 4-B adds the supported {@link RuntimeEventEnvelope} contract -- {@link
 * #drainSupportedEvents()} -- published only after the authoritative transition each event
 * describes has already succeeded, with {@link RuntimeObservationMetadata#latestEventSequence()}
 * advancing in lockstep. Retained, cursor-replayable supported-event history is deliberately not
 * this type's responsibility (ADR-0011 §8, DH-E): {@link #drainSupportedEvents()} returns and
 * clears only the events accumulated since it was last called, so a caller wanting durable replay
 * owns that retention itself. Persistence/recovery/checkpoint/replay semantics and consumer
 * (SSE/frontend) migration remain later Gate 4 work.
 */
public class FactoryRuntime {

    private final FactoryHandler factory;
    private final RecordingScheduler scheduler;
    private final FactoryModelVersion modelVersion;
    private final RunId runId;
    private final List<RuntimeEventEnvelope> pendingSupportedEvents = new ArrayList<>();
    private long eventSequence;

    /**
     * The simulated time {@link #observe()} reports: the time as of the most recent supported
     * boundary (session construction, or the last emitted {@link RuntimeEventEnvelope}), not
     * whatever the internal scheduler's cursor currently says (ADR-0011 REV-003).
     *
     * <p>{@link Scheduler#nextEvent()} advances its cursor for every event it hands out, including
     * internal markers {@link FactoryHandler#handleEvent} ignores. Reading it directly would let a
     * no-op marker move observed {@code currentTime} -- and the time-derived throughput -- while
     * {@link RuntimeObservationMetadata#latestEventSequence()} stood still, so two observations at
     * the same sequence {@code S} could disagree and break "fresh observation at {@code S} +
     * supported events after {@code S} = current consumer view".
     */
    private SimTime observedTime = SimTime.ZERO;

    private FactoryRuntime(FactoryHandler factory, FactoryModelVersion modelVersion) {
        this.factory = factory;
        this.scheduler = new RecordingScheduler();
        this.modelVersion = modelVersion;
        this.runId = RunId.create();
    }

    /**
     * Assembles a fresh factory runtime from a published model and returns a {@link FactoryRuntime}
     * that exclusively owns it, so its explicit-workload scheduler is the only authority over the
     * factory's event ordering.
     */
    public static FactoryRuntime forModel(FactoryModelVersion version) {
        FactoryRuntimeAssembler.Assembled assembled = FactoryRuntimeAssembler.assemble(version);
        return new FactoryRuntime(assembled.factory(), version);
    }

    /**
     * The published model version this session was instantiated from, retained for the lifetime of
     * the session so a caller can identify its source model without tracking it separately (Gate 3
     * acceptance criterion 7).
     */
    public FactoryModelVersion modelVersion() {
        return modelVersion;
    }

    /** Opaque correlation identity for this fresh runtime session. */
    public RunId runId() {
        return runId;
    }

    /**
     * Constructs a fresh {@link FactoryRuntime} over the same {@link #modelVersion()}, with none of
     * this session's submitted workload or dispatch state carried over (Gate 3 acceptance criterion
     * 6: reset and reproduce the same result). This session itself is left untouched -- reset is
     * fresh construction, not in-place mutation, matching {@code FactoryRuntime}'s existing
     * immutable-identity/exclusive-ownership shape: replaying the same command sequence against the
     * returned session reproduces the same result as replaying it against any other fresh session
     * built from this same model version.
     */
    public FactoryRuntime reset() {
        return forModel(modelVersion);
    }

    /**
     * Submits one explicit production order and creates its execution job, under the same
     * acceptance/routing/dispatch semantics as any other accepted order, and returns a definite
     * {@link CommandResult} per docs/planning/factory-simulation-engine-readiness.md §7.2.
     *
     * <p>On acceptance, {@link CommandResult.Accepted#value()} is the new {@link OrderId}. On
     * rejection (e.g. {@link SimError.OutOfRange} for an invalid quantity, {@link
     * SimError.UnknownId} for a product with no published routing), {@link
     * CommandResult.Rejected#error()} is the original {@link SimError} -- a rejected call never
     * leaves partial mutation, since every current rejection path is checked before any {@code
     * Order}/{@code Job} is created.
     */
    public CommandResult<OrderId> submitWorkload(ProductId productId, long quantity, double unitPrice) {
        List<Event> scheduled = new ArrayList<>();
        scheduler.startCapturing(scheduled);
        SimTime submittedAt = scheduler.currentTime();
        try {
            OrderId orderId =
                    factory.submitOrder(productId, quantity, unitPrice, scheduler.currentTime(), scheduler);
            List<JobId> jobIds = factory.jobsView()
                    .filter(j -> j.orderId().equals(orderId))
                    .sorted(Comparator.comparingLong(JobView::ordinalWithinOrder))
                    .map(JobView::id)
                    .toList();
            emit(
                    RuntimeEventType.ORDER_ACCEPTED,
                    submittedAt,
                    new RuntimeEventPayload.OrderAccepted(orderId, productId, quantity, unitPrice, jobIds),
                    List.of(new AffectedEntityRef.OrderRef(orderId)));
            emitJobPlacementEvents(orderId, jobIds, submittedAt);
            return new CommandResult.Accepted<>(orderId, modelVersion, scheduled);
        } catch (SimError e) {
            return new CommandResult.Rejected<>(e, modelVersion);
        } finally {
            scheduler.stopCapturing();
        }
    }

    /**
     * Brings a machine online or takes it offline, under the same dispatch semantics as the
     * economy/scenario-driven {@link EventPayload.MachineAvailabilityChange} event, and returns a
     * definite {@link CommandResult}. Taking a machine offline never affects work already active on
     * it; bringing an eligible machine back online can immediately pick up work that was waiting
     * because no other eligible machine was available -- any such immediately-dispatched {@code
     * TaskStart}/{@code TaskEnd} events are included in {@link CommandResult#scheduledEvents()}.
     *
     * <p>Both rejection paths this method can produce ({@link SimError.UnknownId} for an unknown
     * {@code machineId}; {@link SimError.InvalidStateTransition} for taking a machine with active
     * jobs offline) are verified from this method's own read-only {@link #machinesView()} before
     * calling into {@link FactoryHandler}, so a returned {@link CommandResult.Rejected} is always
     * genuinely pre-mutation. A machine coming online can trigger a cascade of dispatching
     * previously waiting work; a failure surfacing from deep in that cascade (a genuine engine
     * fault, not a rejectable input this method could have pre-verified) is reported as {@link
     * CommandResult.Faulted} rather than {@link CommandResult.Rejected} -- the requested {@link
     * EventPayload.MachineAvailabilityChange} genuinely was applied by that point (both pre-checks
     * above having passed guarantees {@code Machine#setAvailability} itself cannot fail), so {@link
     * CommandResult.Faulted#value()} still carries it, unlike a rejection, which never has one.
     * This method never lets such a failure propagate past its own boundary as a bare exception: it
     * always returns a definite {@link CommandResult}, per
     * docs/planning/factory-simulation-engine-readiness.md §7.2.
     */
    public CommandResult<EventPayload.MachineAvailabilityChange> setMachineAvailability(
            MachineId machineId, boolean online) {
        Optional<MachineView> machine =
                machinesView().stream().filter(m -> m.id().equals(machineId)).findFirst();
        if (machine.isEmpty()) {
            return new CommandResult.Rejected<>(
                    new SimError.UnknownId("machine", machineId.value()), modelVersion);
        }
        if (!online && !machine.get().activeJobs().isEmpty()) {
            return new CommandResult.Rejected<>(
                    new SimError.InvalidStateTransition("cannot take machine " + machineId + " offline while "
                            + machine.get().activeJobs().size() + " jobs are active"),
                    modelVersion);
        }

        // REV-001: Machine#setAvailability is a no-op when the machine is already in the
        // requested online/offline state (e.g. bringing an already-idle machine online again).
        // Only genuinely applied transitions -- online while previously Offline, or offline while
        // previously not Offline -- may emit MACHINE_AVAILABILITY_CHANGED and advance the
        // sequence; see the wasOffline/transitioned computation below.
        boolean wasOffline = machine.get().state() == MachineState.Offline;
        boolean transitioned = online == wasOffline;

        // REV-002: snapshot every not-yet-dispatched job before mutating, so a genuine dispatch
        // cascade triggered by this transition can be reported as JOB_DISPATCHED events derived
        // from the resulting authoritative state, not copied from internal scheduler machinery.
        List<JobView> waitingBefore = transitioned
                ? factory.jobsView().filter(j -> j.status() == JobStatus.Queued).toList()
                : List.of();

        List<Event> scheduled = new ArrayList<>();
        scheduler.startCapturing(scheduled);
        EventPayload.MachineAvailabilityChange requested = new EventPayload.MachineAvailabilityChange(machineId, online);
        SimTime changedAt = scheduler.currentTime();
        try {
            factory.handleMachineAvailability(machineId, online, scheduler, scheduler.currentTime());
            if (transitioned) {
                emit(
                        RuntimeEventType.MACHINE_AVAILABILITY_CHANGED,
                        changedAt,
                        new RuntimeEventPayload.MachineAvailabilityChanged(machineId, online),
                        List.of(new AffectedEntityRef.MachineRef(machineId)));
                emitNewlyDispatchedJobs(waitingBefore, changedAt);
            }
            return new CommandResult.Accepted<>(requested, modelVersion, scheduled);
        } catch (SimError e) {
            // The availability change itself (Machine#setAvailability) is the first thing
            // handleMachineAvailability does and always succeeds once the pre-checks above have
            // passed -- only the subsequent dispatch cascade can fault. So `requested` genuinely
            // was applied by the time any SimError reaches here, and belongs on Faulted too; the
            // supported MACHINE_AVAILABILITY_CHANGED event is emitted for the same reason -- that
            // authoritative change genuinely occurred even though the later cascade did not. Since
            // `transitioned` was computed from pre-mutation state, it still accurately reports
            // whether this genuinely was a transition.
            if (transitioned) {
                emit(
                        RuntimeEventType.MACHINE_AVAILABILITY_CHANGED,
                        changedAt,
                        new RuntimeEventPayload.MachineAvailabilityChanged(machineId, online),
                        List.of(new AffectedEntityRef.MachineRef(machineId)));
                emitNewlyDispatchedJobs(waitingBefore, changedAt);
            }
            return new CommandResult.Faulted<>(requested, e, modelVersion, scheduled);
        } finally {
            scheduler.stopCapturing();
        }
    }

    /**
     * Emits {@link RuntimeEventType#JOB_DISPATCHED} for every job in {@code waitingBefore} that
     * has since transitioned to {@link JobStatus#InProgress} -- the genuine dispatch-cascade
     * outcome of a machine coming online, derived by diffing authoritative job state rather than
     * inspecting internal scheduler machinery (ADR-0011 REV-002). Ordered deterministically by
     * order id then ordinal so repeated runs of the same scenario produce identical event streams.
     */
    private void emitNewlyDispatchedJobs(List<JobView> waitingBefore, SimTime time) {
        waitingBefore.stream()
                .map(JobView::id)
                .map(factory::job)
                .filter(job -> job.status() == JobStatus.InProgress)
                .sorted(Comparator.comparing((JobView view) -> view.orderId().value())
                        .thenComparingLong(JobView::ordinalWithinOrder))
                .forEach(job -> emit(
                        RuntimeEventType.JOB_DISPATCHED,
                        time,
                        new RuntimeEventPayload.JobDispatched(
                                job.id(), job.orderId(), job.currentMachine(), job.currentStep()),
                        List.of(new AffectedEntityRef.JobRef(job.id()), new AffectedEntityRef.OrderRef(job.orderId()))));
    }

    /**
     * Emits, for every job in {@code jobIds} (assumed just-created, in ordinal order), either
     * {@link RuntimeEventType#JOB_DISPATCHED} if {@link FactoryHandler#submitOrder} immediately
     * placed it on a machine, or {@link RuntimeEventType#JOB_WAITING} with the machine(s) it is
     * now waiting on -- the cross-machine multi-eligible backlog ({@link #pendingWorkView()}) when
     * more than one machine is eligible for its current step, or the single eligible machine's own
     * queue otherwise. Together with the enriched {@link RuntimeEventPayload.OrderAccepted}, this
     * lets a consumer reconstruct the job creation/assignment/pending-work deltas {@link
     * FactoryHandler#submitOrder} can produce (ADR-0011 REV-002).
     */
    private void emitJobPlacementEvents(OrderId orderId, List<JobId> jobIds, SimTime time) {
        Map<JobId, Set<MachineId>> pendingEligibility = pendingWorkView().stream()
                .collect(Collectors.toMap(PendingWorkView::jobId, PendingWorkView::eligibleMachines));
        for (JobId jobId : jobIds) {
            JobView job = factory.job(jobId);
            if (job.status() == JobStatus.InProgress) {
                emit(
                        RuntimeEventType.JOB_DISPATCHED,
                        time,
                        new RuntimeEventPayload.JobDispatched(
                                jobId, orderId, job.currentMachine(), job.currentStep()),
                        List.of(new AffectedEntityRef.JobRef(jobId), new AffectedEntityRef.OrderRef(orderId)));
            } else {
                emit(
                        RuntimeEventType.JOB_WAITING,
                        time,
                        new RuntimeEventPayload.JobWaiting(jobId, orderId, waitingOn(jobId, pendingEligibility)),
                        List.of(new AffectedEntityRef.JobRef(jobId), new AffectedEntityRef.OrderRef(orderId)));
            }
        }
    }

    /**
     * The machine(s) {@code jobId} is currently waiting on: its cross-machine multi-eligible
     * backlog entry when it has one, otherwise the single machine whose own queue holds it.
     */
    private Set<MachineId> waitingOn(JobId jobId, Map<JobId, Set<MachineId>> pendingEligibility) {
        Set<MachineId> eligible = pendingEligibility.get(jobId);
        if (eligible != null) {
            return eligible;
        }
        return machinesView().stream()
                .filter(m -> m.queuedJobs().contains(jobId))
                .map(MachineView::id)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * The authoritative placement of one job: what it is doing, where, and for which step. Any
     * difference between two snapshots of this triple is an authoritative placement change a
     * consumer must be told about through the supported event stream (ADR-0011 REV-002) -- the step
     * index is part of it because a job can legitimately be re-dispatched onto the same machine for
     * its next routing step.
     */
    private record JobPlacement(JobStatus status, MachineId machine, int step) {
        static JobPlacement of(JobView job) {
            return new JobPlacement(job.status(), job.currentMachine(), job.currentStep());
        }
    }

    /**
     * Snapshots the placement of every job currently occupying machine capacity. Deliberately not a
     * snapshot of every job: only work that is (or is about to be) machine-assigned can change
     * placement during a {@code TaskEnd}, and the number of concurrently active jobs is bounded by
     * published machine concurrency, so this stays proportional to capacity rather than to backlog
     * size -- a full per-job snapshot would make each of a large order's completions cost O(jobs).
     */
    private Map<JobId, JobPlacement> activePlacements() {
        Map<JobId, JobPlacement> snapshot = new LinkedHashMap<>();
        for (MachineView machine : machinesView()) {
            for (JobId jobId : machine.activeJobs()) {
                snapshot.put(jobId, JobPlacement.of(factory.job(jobId)));
            }
        }
        return snapshot;
    }

    /**
     * Emits {@link RuntimeEventType#JOB_DISPATCHED}/{@link RuntimeEventType#JOB_WAITING} for every
     * placement change a just-processed {@code TaskEnd} authoritatively caused -- the same
     * snapshot/diff derivation {@link #emitNewlyDispatchedJobs} already uses for the
     * machine-availability cascade, widened to cover both directions of placement change.
     *
     * <p>This is what closes the {@code TaskEnd} path (ADR-0011 REV-002). Completing a step frees
     * capacity, and {@code FactoryHandler} re-places not only the completing job onto its next
     * routing step but also whatever queued or multi-eligible backlog work that freed machine can
     * now accept; reporting only {@code JOB_STEP_COMPLETED} would leave a consumer unable to derive
     * those jobs' status or machine assignment from the supported event stream.
     *
     * <p>The two directions are found differently because the transition can only move work one
     * way. Anything newly occupying capacity -- including a job re-dispatched onto the very same
     * machine for its next step, which is why {@link JobPlacement} carries the step index -- shows
     * up as a difference against {@code activeBefore}. The only job that can newly stop occupying
     * capacity without completing is the one whose step just ended, so that is the single {@code
     * JOB_WAITING} candidate. Dispatches are ordered deterministically by order id then ordinal, so
     * repeated runs of the same scenario emit identical event streams.
     */
    private void emitPlacementChanges(Map<JobId, JobPlacement> activeBefore, JobId completedJobId, SimTime time) {
        Map<JobId, JobPlacement> activeAfter = activePlacements();
        activeAfter.entrySet().stream()
                .filter(entry -> !entry.getValue().equals(activeBefore.get(entry.getKey())))
                .map(entry -> factory.job(entry.getKey()))
                .sorted(Comparator.comparing((JobView view) -> view.orderId().value())
                        .thenComparingLong(JobView::ordinalWithinOrder))
                .forEach(job -> emit(
                        RuntimeEventType.JOB_DISPATCHED,
                        time,
                        new RuntimeEventPayload.JobDispatched(
                                job.id(), job.orderId(), job.currentMachine(), job.currentStep()),
                        List.of(new AffectedEntityRef.JobRef(job.id()), new AffectedEntityRef.OrderRef(job.orderId()))));

        JobView completed = factory.job(completedJobId);
        if (completed.status() != JobStatus.Completed && !activeAfter.containsKey(completedJobId)) {
            Map<JobId, Set<MachineId>> pendingEligibility = pendingWorkView().stream()
                    .collect(Collectors.toMap(PendingWorkView::jobId, PendingWorkView::eligibleMachines));
            emit(
                    RuntimeEventType.JOB_WAITING,
                    time,
                    new RuntimeEventPayload.JobWaiting(
                            completed.id(), completed.orderId(), waitingOn(completed.id(), pendingEligibility)),
                    List.of(
                            new AffectedEntityRef.JobRef(completed.id()),
                            new AffectedEntityRef.OrderRef(completed.orderId())));
        }
    }

    /**
     * Read-only view of work waiting for one of several eligible machines to free up (Gate 2's
     * cross-machine multi-eligible backlog) -- not reflected in any single machine's {@link
     * MachineView#queueDepth()}. See {@link PendingWorkView} for why this is a necessary, separate
     * projection (Gate 3 acceptance criterion 5).
     */
    public List<PendingWorkView> pendingWorkView() {
        return factory.pendingWorkView();
    }

    /** Processes exactly one pending event, if any, and returns it. */
    public Optional<Event> advance() throws SimError {
        Optional<Event> next = scheduler.nextEvent();
        if (next.isPresent()) {
            Event event = next.get();
            List<Event> triggered = new ArrayList<>();
            // Placement is snapshotted before the mutation, so the dispatch cascade a TaskEnd can
            // trigger (next-step placement plus whatever the freed machine picks up from its queue
            // or the multi-eligible backlog) is derivable by diffing authoritative state afterwards.
            Map<JobId, JobPlacement> activeBefore =
                    event.payload() instanceof EventPayload.TaskEnd ? activePlacements() : Map.of();
            scheduler.startCapturing(triggered);
            try {
                factory.handleEvent(event, scheduler);
            } finally {
                scheduler.stopCapturing();
                // Post-authoritative derivation happens even on the exception path: whatever
                // authoritative state change already occurred before a fault (e.g. a job
                // completing its step before a later dispatch cascade fails) must still be
                // reported -- see requirement 1 (post-authoritative publication) and the
                // faultReportsOnlyAuthoritativeChangesThatActuallyOccurred acceptance evidence.
                recordSupportedEventsFor(event, triggered, activeBefore);
            }
        }
        return next;
    }

    /**
     * Derives and appends the supported runtime event(s) implied by having just processed {@code
     * trigger}, if any -- never before {@link FactoryHandler#handleEvent} has returned (successfully
     * or not) for it, since a supported event must never claim a transition occurred before the
     * authoritative state actually reflects it. {@code triggeredInternalEvents} is whatever the
     * internal scheduler additionally scheduled while processing {@code trigger}, inspected only for
     * evidence of a further authoritative fact (order completion) that already happened -- never
     * itself re-exposed as the supported payload. {@code activeBefore} is the pre-mutation
     * machine-assignment snapshot {@link #advance()} took, diffed here so every placement change the
     * transition authoritatively caused is reported too (ADR-0011 REV-002).
     */
    private void recordSupportedEventsFor(
            Event trigger, List<Event> triggeredInternalEvents, Map<JobId, JobPlacement> activeBefore) {
        if (!(trigger.payload() instanceof EventPayload.TaskEnd taskEnd)) {
            // Internal scheduler machinery this runtime never itself schedules through its own
            // supported API surface (OrderCreation/MachineAvailabilityChange arrive only via
            // submitWorkload/setMachineAvailability directly, never via the queue) and the
            // TaskStart timing marker, which never itself changes authoritative state.
            return;
        }
        JobView job = factory.job(taskEnd.jobId());
        emit(
                RuntimeEventType.JOB_STEP_COMPLETED,
                trigger.time(),
                new RuntimeEventPayload.JobStepCompleted(
                        taskEnd.jobId(), job.orderId(), taskEnd.machineId(), taskEnd.stepIndex(), job.isComplete()),
                List.of(new AffectedEntityRef.JobRef(taskEnd.jobId()), new AffectedEntityRef.OrderRef(job.orderId())));

        for (Event internal : triggeredInternalEvents) {
            if (internal.payload() instanceof EventPayload.OrderCompleted oc) {
                emit(
                        RuntimeEventType.ORDER_COMPLETED,
                        trigger.time(),
                        new RuntimeEventPayload.OrderCompleted(
                                oc.orderId(), oc.jobId(), oc.productId(), oc.quantity(), oc.unitPrice()),
                        List.of(
                                new AffectedEntityRef.OrderRef(oc.orderId()),
                                new AffectedEntityRef.JobRef(oc.jobId())));
            }
        }

        emitPlacementChanges(activeBefore, taskEnd.jobId(), trigger.time());
    }

    /**
     * Allocates the next strictly monotonic, run-scoped sequence number and appends the resulting
     * {@link RuntimeEventEnvelope} to this session's supported event log. Package-private emission
     * point: this is the only place a {@link RuntimeEventEnvelope} is constructed, always after the
     * authoritative transition it describes has already succeeded.
     */
    private void emit(
            RuntimeEventType eventType, SimTime time, RuntimeEventPayload payload, List<AffectedEntityRef> refs) {
        eventSequence++;
        // The supported boundary moves as one: sequence and observed time advance together, so
        // every observation-visible metadata/performance fact stays coherent with the sequence a
        // consumer cursors from (ADR-0011 REV-003).
        observedTime = time;
        pendingSupportedEvents.add(new RuntimeEventEnvelope(
                runId, eventSequence, time, eventType, modelVersion.fingerprint(), Optional.empty(), refs, payload));
    }

    /**
     * Returns every supported runtime event accumulated since the last call to this method (or
     * since session construction, for the first call), in emission (sequence) order, and clears
     * them from this session. Consumer-neutral supplement to {@link #observe()}: distinct from, and
     * never wraps, the internal scheduler {@link Event} returned by {@link #advance()}/{@link
     * #advanceUntil}.
     *
     * <p>Deliberately draining rather than retained/replayable-by-cursor: {@link #eventSequence}
     * still advances monotonically and independently of draining (so {@link
     * RuntimeObservationMetadata#latestEventSequence()} is unaffected by when a caller drains), but
     * this type does not itself keep an unbounded, cursor-addressable event history -- that is a
     * separately-named responsibility for later distribution hardening (ADR-0011 §8, DH-E), not
     * part of the Gate 4 semantic contract. A caller that needs durable replay must retain the
     * drained events itself.
     */
    public List<RuntimeEventEnvelope> drainSupportedEvents() {
        List<RuntimeEventEnvelope> drained = List.copyOf(pendingSupportedEvents);
        pendingSupportedEvents.clear();
        return drained;
    }

    /**
     * Processes pending events one at a time, in the same order {@link #advance()} would, stopping
     * as soon as either bound is reached: the next pending event's {@link SimTime} would exceed
     * {@code targetTime}, or {@code maxEvents} events have already been processed by this call.
     * Returns every event actually processed, in processing order (empty if none were).
     *
     * <p>This is the bounded-advancement primitive Gate 3 requires (acceptance criterion 4):
     * interactive consumers use it for pause/resume and normal/accelerated presentation speeds
     * without wall-clock sleeping in the simulation core, and headless consumers use it as
     * protection against unbounded work monopolizing a call. It is provably equivalent to looping
     * {@link #advance()} one event at a time under the same two stopping conditions -- it does not
     * reorder, skip, merge, or otherwise reinterpret events -- so a caller can freely mix the two
     * without affecting determinism; {@code Gate3SessionControlAcceptanceTest} proves the two
     * approaches converge to identical ordered event streams and identical terminal state for the
     * same model/workload.
     *
     * @param targetTime the simulated time not to advance past; an event scheduled after this time
     *     is left pending rather than processed
     * @param maxEvents the maximum number of events this call may process; must not be negative
     */
    public List<Event> advanceUntil(SimTime targetTime, long maxEvents) throws SimError {
        if (maxEvents < 0) {
            throw new IllegalArgumentException("maxEvents must not be negative, got " + maxEvents);
        }
        List<Event> processed = new ArrayList<>();
        while (processed.size() < maxEvents) {
            Optional<SimTime> nextTime = scheduler.peekTime();
            if (nextTime.isEmpty() || nextTime.get().compareTo(targetTime) > 0) {
                break;
            }
            advance().ifPresent(processed::add);
        }
        return processed;
    }

    /** Read-only view of every accepted order. */
    public Stream<Order> ordersView() {
        return factory.ordersView();
    }
    public OrderExecutionView orderExecution(OrderId id) { return factory.orderExecution(id); }
    public Stream<OrderExecutionView> orderExecutionsView() { return factory.orderExecutionsView(); }

    /** Read-only view of every execution job. */
    public Stream<JobView> jobsView() {
        return factory.jobsView();
    }

    /** Read-only lookup for a single job. */
    public JobView job(JobId id) {
        return factory.job(id);
    }

    /** Read-only view of every machine. */
    public List<MachineView> machinesView() {
        return factory.machinesView();
    }

    public long backlog() {
        return factory.backlog();
    }

    public double avgLeadTime() {
        return factory.avgLeadTime();
    }

    public double throughput(long elapsedTicks) {
        return factory.throughput(elapsedTicks);
    }

    public double completedSalesValue() {
        return factory.completedSalesValue();
    }

    public long completedSales() {
        return factory.completedSales();
    }

    /**
     * Returns a coherent, immutable, consumer-neutral projection of current authoritative state.
     * This does not expose internal scheduler events; see {@link #drainSupportedEvents()} for the
     * supported runtime events this observation's {@code latestEventSequence} cursors.
     *
     * <p>Every fact reported here is coherent with one supported boundary (ADR-0011 REV-003):
     * metadata time and the time-derived throughput come from {@link #observedTime}, and {@link
     * RuntimeRunState} from pending <em>authoritative</em> work, so processing an internal no-op
     * marker cannot produce a second, different observation at the same {@code
     * latestEventSequence}.
     */
    public RuntimeObservation observe() {
        List<ResourceObservation> resources = machinesView().stream()
                .sorted(Comparator.comparing(view -> view.id().value()))
                .map(view -> new ResourceObservation(
                        view.id(),
                        view.name(),
                        view.state(),
                        view.concurrency(),
                        view.activeJobs().stream().sorted().toList(),
                        view.queueDepth(),
                        view.capacityLiters(),
                        view.setupTime(),
                        view.busyTicks()))
                .toList();
        List<OrderObservation> orders = ordersView()
                .sorted(Comparator.comparing(order -> order.id().value()))
                .map(order -> {
                    OrderExecutionView execution = orderExecution(order.id());
                    return new OrderObservation(
                            order.id(),
                            order.productId(),
                            execution.requestedQuantity(),
                            execution.releasedQuantity(),
                            execution.completedQuantity(),
                            order.createdAt(),
                            execution.completedAt(),
                            execution.complete());
                })
                .toList();
        List<JobObservation> jobs = jobsView()
                .sorted(Comparator.comparing((JobView view) -> view.orderId().value())
                        .thenComparingLong(JobView::ordinalWithinOrder)
                        .thenComparing(view -> view.id().value()))
                .map(view -> new JobObservation(
                        view.id(),
                        view.orderId(),
                        view.ordinalWithinOrder(),
                        view.productId(),
                        view.status(),
                        view.currentStep(),
                        view.totalSteps(),
                        view.currentMachine(),
                        view.createdAt(),
                        view.completedAt()))
                .toList();
        List<PendingWorkObservation> pending = pendingWorkView().stream()
                .sorted(Comparator.comparing(view -> view.jobId().value()))
                .map(view -> new PendingWorkObservation(
                        view.jobId(), view.eligibleMachines().stream().sorted().toList()))
                .toList();
        long elapsedTicks = observedTime.value();
        return new RuntimeObservation(
                new RuntimeObservationMetadata(
                        runId,
                        modelVersion.fingerprint(),
                        observedTime,
                        scheduler.hasPendingAuthoritativeWork()
                                ? RuntimeRunState.ACTIVE
                                : RuntimeRunState.QUIESCENT,
                        eventSequence),
                resources,
                orders,
                jobs,
                pending,
                new RuntimePerformanceObservation(
                        backlog(), completedSales(), completedSalesValue(), avgLeadTime(), throughput(elapsedTicks)));
    }
}
