package com.arcogine.research.experiment;

import static com.arcogine.research.experiment.OutcomeAssertions.assertDerived;
import static com.arcogine.research.experiment.OutcomeAssertions.assertUnderdetermined;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeEventType;
import com.arcogine.research.experiment.DispatchProfileOracle.DispatchProfile;
import com.arcogine.research.experiment.DispatchProfileOracle.ResourceStepDispatches;
import com.arcogine.research.experiment.DispatchProfileOracle.StepWait;
import com.arcogine.research.experiment.ExperimentFixture.WindowIntent;
import com.arcogine.types.ProductId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * The dispatch profile: counts per resource and step and exact waits per step, resolved through
 * supported events and the published model, and refused where they cannot be.
 */
class DispatchProfileOracleTest {

    private static final LinearRoutingFamily FAMILY = CapacityCorpus.SHARED_RESOURCE;

    private final ExperimentEvidence evidence = ExperimentRunner.run(CapacityCorpus.sharedResourceAuthored());

    private static OracleOutcome<DispatchProfile> atClosing(ExperimentEvidence evidence) {
        return new DispatchProfileOracle(ExperimentEvidence.CLOSING_LABEL).evaluateOn(evidence);
    }

    private static List<RuntimeEventEnvelope> without(ExperimentEvidence evidence, RuntimeEventType type, long jobId, int stepIndex) {
        return evidence.retainedEvents().stream()
                .filter(event -> !(event.eventType() == type && matches(event.payload(), jobId, stepIndex)))
                .toList();
    }

    private static boolean matches(RuntimeEventPayload payload, long jobId, int stepIndex) {
        return switch (payload) {
            case RuntimeEventPayload.JobStepCompleted completed ->
                completed.jobId().value() == jobId && completed.stepIndex() == stepIndex;
            case RuntimeEventPayload.OrderAccepted accepted -> accepted.jobIds().stream().anyMatch(job -> job.value() == jobId);
            default -> false;
        };
    }

    @Test
    void countsAndWaitsAreListedInStepOrderThenResourceOrder() {
        OracleOutcome.Derived<DispatchProfile> derived = assertDerived(atClosing(evidence));

        // Hand-derived in CapacityCorpus: unit 2 waits 2 for the cutter, nothing waits to be assembled,
        // and the two inspections on Shared start at 7 and 9 for units ready at 5 and 7.
        assertEquals(
                new DispatchProfile(
                        List.of(
                                new ResourceStepDispatches(FAMILY.resourceId("Cutter"), CapacityCorpus.step(FAMILY, "CUT"), 2),
                                new ResourceStepDispatches(FAMILY.resourceId("Assembler"), CapacityCorpus.step(FAMILY, "ASSEMBLE"), 1),
                                new ResourceStepDispatches(FAMILY.resourceId("Shared"), CapacityCorpus.step(FAMILY, "ASSEMBLE"), 1),
                                new ResourceStepDispatches(FAMILY.resourceId("Shared"), CapacityCorpus.step(FAMILY, "INSPECT"), 2)),
                        List.of(
                                new StepWait(CapacityCorpus.step(FAMILY, "CUT"), 2, 2),
                                new StepWait(CapacityCorpus.step(FAMILY, "ASSEMBLE"), 2, 0),
                                new StepWait(CapacityCorpus.step(FAMILY, "INSPECT"), 2, 4))),
                derived.value());
        assertEquals(
                Set.of(EvidenceInput.PUBLISHED_MODEL, EvidenceInput.OBSERVATIONS, EvidenceInput.SUPPORTED_EVENTS),
                DispatchProfileOracle.DEFINITION.inputs());
        long cursor = evidence.observation(ExperimentEvidence.CLOSING_LABEL).metadata().latestEventSequence();
        assertEquals(
                new EvidenceSupport(List.of(ExperimentEvidence.CLOSING_LABEL), Optional.of(new SequenceRange(1, cursor))),
                derived.support());
    }

    @Test
    void anIncompleteEventWindowIsRefused() {
        ExperimentEvidence joinedLate = ExperimentRunner.run(new ExperimentFixture(
                "partial/shared-resource-joined-late",
                FAMILY.model(),
                List.of(
                        ExperimentStep.submit(LinearRoutingFamily.PRODUCT, CapacityCorpus.PAIR_QUANTITY, LinearRoutingFamily.UNIT_PRICE),
                        ExperimentStep.discardEvents("joined-late"),
                        ExperimentStep.advanceToQuiescence(1_000),
                        ExperimentStep.captureEvents("after-join")),
                WindowIntent.PARTIAL,
                List.of()));

        assertTrue(assertUnderdetermined(atClosing(joinedLate)).reasons().getFirst().contains("not all retained"));
    }

    @Test
    void aDispatchWhoseReadinessIsNotInTheEventsIsRefused() {
        // Without unit 1's CUT completion, its ASSEMBLE dispatch has no moment at which it became ready.
        ExperimentEvidence noCompletion = TamperedEvidence.withEvents(
                evidence, without(evidence, RuntimeEventType.JOB_STEP_COMPLETED, 1, 0));
        // Without the order's acceptance, no dispatch can be resolved to a product.
        ExperimentEvidence noAcceptance = TamperedEvidence.withEvents(
                evidence, without(evidence, RuntimeEventType.ORDER_ACCEPTED, 1, 0));

        for (ExperimentEvidence tampered : List.of(noCompletion, noAcceptance)) {
            OracleOutcome.Underdetermined<DispatchProfile> refused = assertUnderdetermined(atClosing(tampered));
            assertTrue(refused.reasons().getFirst().contains("cannot be resolved"), refused.toString());
        }
    }

    @Test
    void aDispatchThePublishedModelCannotResolveIsRefused() {
        FactoryModel model = evidence.publishedModel();
        FactoryModel withoutProducts = new FactoryModel(model.resources(), model.operations(), List.of());

        OracleOutcome.Underdetermined<DispatchProfile> refused =
                assertUnderdetermined(atClosing(TamperedEvidence.withPublishedModel(evidence, withoutProducts)));

        assertTrue(refused.reasons().getFirst().contains("cannot be resolved"), refused.toString());
    }

    @Test
    void invalidRoutingPositionsAndAnUnmatchedOperationAreRefused() {
        FactoryModel model = evidence.publishedModel();
        FactoryModel wrongOperation = new FactoryModel(model.resources(), List.of(), model.products());
        assertTrue(assertUnderdetermined(atClosing(TamperedEvidence.withPublishedModel(evidence, wrongOperation)))
                .reasons().getFirst().contains("cannot be resolved"));

        int dispatchIndex = -1;
        for (int i = 0; i < evidence.retainedEvents().size(); i++) {
            if (evidence.retainedEvents().get(i).payload() instanceof RuntimeEventPayload.JobDispatched) {
                dispatchIndex = i;
                break;
            }
        }
        RuntimeEventPayload.JobDispatched dispatch =
                (RuntimeEventPayload.JobDispatched) evidence.retainedEvents().get(dispatchIndex).payload();
        for (int invalidStep : List.of(-1, model.operations().getFirst().steps().size())) {
            ExperimentEvidence invalid = TamperedEvidence.withPayload(evidence, dispatchIndex,
                    new RuntimeEventPayload.JobDispatched(
                            dispatch.jobId(), dispatch.orderId(), dispatch.machineId(), invalidStep));
            assertTrue(assertUnderdetermined(atClosing(invalid)).reasons().getFirst().contains("cannot be resolved"));
        }
    }

    @Test
    void dispatchesAcrossOperationsFollowPublishedOperationOrder() {
        FactoryModel base = FAMILY.model();
        OperationDefinition first = base.operations().getFirst();
        FactoryModel twoOperations = new FactoryModel(base.resources(),
                List.of(first, new OperationDefinition(2, "second route", first.steps())),
                List.of(base.products().getFirst(), new ProductDefinition(new ProductId(2), "Second widget", 2)));
        ExperimentFixture fixture = new ExperimentFixture("two-operations", twoOperations,
                List.of(
                        ExperimentStep.submit(new ProductId(2), 1, LinearRoutingFamily.UNIT_PRICE),
                        ExperimentStep.submit(LinearRoutingFamily.PRODUCT, 1, LinearRoutingFamily.UNIT_PRICE),
                        ExperimentStep.advanceToQuiescence(1_000),
                        ExperimentStep.captureEvents("everything")), WindowIntent.COMPLETE_RUN, List.of());

        DispatchProfile profile = assertDerived(atClosing(ExperimentRunner.run(fixture))).value();
        assertEquals(List.of(1L, 2L), profile.waits().stream()
                .map(wait -> wait.step().operationId()).distinct().toList());
    }
}
