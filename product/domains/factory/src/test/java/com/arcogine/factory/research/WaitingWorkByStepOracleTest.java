package com.arcogine.factory.research;

import static com.arcogine.factory.research.OutcomeAssertions.assertDerived;
import static com.arcogine.factory.research.OutcomeAssertions.assertUnderdetermined;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.process.JobObservation;
import com.arcogine.factory.process.PendingWorkObservation;
import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeEventType;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.factory.research.WaitingWorkByStepOracle.Attribution;
import com.arcogine.factory.research.WaitingWorkByStepOracle.WaitingAtStep;
import com.arcogine.types.JobId;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * The multi-eligible waiting counterexample: work eligible for several resources waits in a shared
 * backlog that per-resource queue depth cannot see, while supported pending-work evidence can.
 */
class WaitingWorkByStepOracleTest {

    private static final ThreeStepRoutingFamily FAMILY = StarterCorpus.MULTI_ELIGIBLE_FAMILY;
    private static final Set<MachineId> ASSEMBLERS = Set.copyOf(FAMILY.assembleResources());

    private final ExperimentEvidence evidence = ExperimentRunner.run(StarterCorpus.multiEligibleWaiting());

    private static long perResourceQueueDepth(RuntimeObservation observation) {
        return observation.resources().stream().mapToLong(ResourceObservation::queueDepth).sum();
    }

    @Test
    void perResourceQueueDepthAloneMissesMultiEligibleWaitingThatSupportedEvidenceRepresents() {
        RuntimeObservation midRun = evidence.observation("mid-run");

        // The naive reconstruction -- waiting work is whatever sits in per-resource queues -- finds nothing.
        assertEquals(0, perResourceQueueDepth(midRun), "every resource's own queue is empty");

        // Yet three units wait, and the supported observation says so through pending multi-eligible work,
        // for either assembler and not for any one of them.
        Set<JobId> waitingJobs = Set.of(new JobId(3), new JobId(4), new JobId(5));
        assertEquals(
                waitingJobs,
                midRun.pendingWork().stream().map(PendingWorkObservation::jobId).collect(Collectors.toSet()));
        assertTrue(midRun.pendingWork().stream()
                .allMatch(pending -> Set.copyOf(pending.eligibleMachineIds()).equals(ASSEMBLERS)));
        assertEquals(
                waitingJobs,
                midRun.jobs().stream()
                        .filter(job -> job.status() == JobStatus.Queued)
                        .map(JobObservation::jobId)
                        .collect(Collectors.toSet()));

        // The supported waiting events carry the same shared eligible set; none names a single machine.
        List<RuntimeEventPayload.JobWaiting> sharedWaits = evidence.retainedEvents().stream()
                .filter(event -> event.eventType() == RuntimeEventType.JOB_WAITING)
                .map(RuntimeEventEnvelope::payload)
                .map(RuntimeEventPayload.JobWaiting.class::cast)
                .filter(waiting -> waiting.eligibleMachines().size() > 1)
                .toList();
        assertEquals(
                waitingJobs,
                sharedWaits.stream().map(RuntimeEventPayload.JobWaiting::jobId).collect(Collectors.toSet()));
        assertTrue(sharedWaits.stream().allMatch(waiting -> waiting.eligibleMachines().equals(ASSEMBLERS)));
    }

    @Test
    void theDerivationRepresentsTheSharedWaitingWorkAndNamesNoSinglePhysicalQueue() {
        RuntimeObservation midRun = evidence.observation("mid-run");

        OracleOutcome.Derived<List<WaitingAtStep>> derived =
                assertDerived(new WaitingWorkByStepOracle("mid-run").evaluateOn(evidence));

        assertEquals(
                List.of(new WaitingAtStep(
                        ThreeStepRoutingFamily.OPERATION_ID,
                        ThreeStepRoutingFamily.ASSEMBLE_STEP_ID,
                        ThreeStepRoutingFamily.ASSEMBLE,
                        3,
                        new Attribution.SharedEligibleSet(ASSEMBLERS))),
                derived.value());
        // Ground truth from the authored design and workload disagrees with the per-resource reconstruction.
        assertNotEquals(
                perResourceQueueDepth(midRun),
                derived.value().stream().mapToLong(WaitingAtStep::waitingJobs).sum());
    }

    @Test
    void singleEligibleWaitingIsAttributableToItsOnlyResourceSoQueueDepthAgreesThere() {
        ExperimentEvidence baseline = ExperimentRunner.run(StarterCorpus.capacityConstrainedBaseline());
        MachineId assembler = StarterCorpus.BASELINE_FAMILY.assembleResources().getFirst();

        List<WaitingAtStep> waiting =
                assertDerived(new WaitingWorkByStepOracle("mid-run").evaluateOn(baseline)).value();

        assertEquals(1, waiting.size());
        assertEquals(new Attribution.SingleResource(assembler), waiting.getFirst().attribution());
        // For a single-eligible step the waiting work really is that resource's own queue.
        RuntimeObservation midRun = baseline.observation("mid-run");
        long assemblerQueue = midRun.resources().stream()
                .filter(resource -> resource.machineId().equals(assembler))
                .mapToLong(ResourceObservation::queueDepth)
                .sum();
        assertEquals(assemblerQueue, waiting.getFirst().waitingJobs());
        assertEquals(perResourceQueueDepth(midRun), waiting.getFirst().waitingJobs());
    }

    @Test
    void atSubmissionEveryQueuedUnitWaitsForTheOnlyCutter() {
        List<WaitingAtStep> waiting =
                assertDerived(new WaitingWorkByStepOracle("after-submission").evaluateOn(evidence)).value();

        assertEquals(
                List.of(new WaitingAtStep(
                        ThreeStepRoutingFamily.OPERATION_ID,
                        ThreeStepRoutingFamily.CUT_STEP_ID,
                        ThreeStepRoutingFamily.CUT,
                        4,
                        new Attribution.SingleResource(FAMILY.cutResources().getFirst()))),
                waiting);
    }

    @Test
    void theDerivationNamesTheObservationItReadAndReadsNoEvents() {
        OracleOutcome.Derived<List<WaitingAtStep>> derived =
                assertDerived(new WaitingWorkByStepOracle("mid-run").evaluateOn(evidence));

        assertEquals(WaitingWorkByStepOracle.DEFINITION, derived.definition());
        assertEquals(new EvidenceSupport(List.of("mid-run"), Optional.empty()), derived.support());
    }

    @Test
    void anObservationWhoseQueuesDoNotAccountForItsQueuedJobsIsRefused() {
        ExperimentEvidence baseline = ExperimentRunner.run(StarterCorpus.capacityConstrainedBaseline());
        RuntimeObservation midRun = baseline.observation("mid-run");
        // The same observation with the assembler's queue emptied while its two waiting jobs remain Queued.
        List<ResourceObservation> emptiedQueues = midRun.resources().stream()
                .map(resource -> new ResourceObservation(
                        resource.machineId(),
                        resource.name(),
                        resource.state(),
                        resource.concurrency(),
                        resource.activeJobIds(),
                        0,
                        resource.capacityLiters(),
                        resource.setupTime(),
                        resource.busyTicks()))
                .toList();
        RuntimeObservation inconsistent = new RuntimeObservation(
                midRun.metadata(),
                emptiedQueues,
                midRun.orders(),
                midRun.jobs(),
                midRun.pendingWork(),
                midRun.performance());

        OracleOutcome.Underdetermined<List<WaitingAtStep>> refused = assertUnderdetermined(
                new WaitingWorkByStepOracle("mid-run")
                        .evaluateOn(TamperedEvidence.withObservation(baseline, "mid-run", inconsistent)));

        assertTrue(refused.reasons().getFirst().contains("2 jobs are Queued"), refused.toString());
    }

    @Test
    void waitingJobsThePublishedModelCannotResolveAreRefusedNotSilentlyDropped() {
        FactoryModel model = evidence.publishedModel();
        FactoryModel withoutProducts = new FactoryModel(model.resources(), model.operations(), List.of());

        OracleOutcome.Underdetermined<List<WaitingAtStep>> refused = assertUnderdetermined(
                new WaitingWorkByStepOracle("mid-run")
                        .evaluateOn(TamperedEvidence.withPublishedModel(evidence, withoutProducts)));

        assertTrue(refused.reasons().getFirst().contains("cannot be resolved"), refused.toString());
    }
}
