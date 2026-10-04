package com.arcogine.research.experiment;

import static com.arcogine.research.experiment.OutcomeAssertions.assertDerived;
import static com.arcogine.research.experiment.OutcomeAssertions.assertUnderdetermined;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.process.OrderObservation;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventType;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.research.experiment.ExperimentFixture.WindowIntent;
import com.arcogine.types.SimTime;
import com.arcogine.types.OrderId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * The single-order completion tick: derived only when the closing observation and the supported
 * {@code ORDER_COMPLETED} event agree, and refused otherwise.
 */
class CompletionTickOracleTest {

    private static final ThreeStepRoutingFamily FAMILY = StarterCorpus.BASELINE_FAMILY;

    private final ExperimentEvidence evidence = ExperimentRunner.run(StarterCorpus.capacityConstrainedBaseline());

    private static ExperimentEvidence run(String id, WindowIntent intent, ExperimentStep... script) {
        return ExperimentRunner.run(new ExperimentFixture(id, FAMILY.model(), List.of(script), intent, List.of()));
    }

    private static ExperimentStep submit() {
        return ExperimentStep.submit(ThreeStepRoutingFamily.PRODUCT, StarterCorpus.BASELINE_QUANTITY, ThreeStepRoutingFamily.UNIT_PRICE);
    }

    @Test
    void theTickIsDerivedFromAgreeingObservationAndEvent() {
        OracleOutcome.Derived<Long> derived = assertDerived(new CompletionTickOracle().evaluateOn(evidence));

        // The baseline's hand-derived makespan (see StarterCorpus).
        assertEquals(29L, derived.value());
        assertEquals(29, CompletionTickOracle.completionTick(evidence));
        long cursor = evidence.observation(ExperimentEvidence.CLOSING_LABEL).metadata().latestEventSequence();
        assertEquals(
                new EvidenceSupport(List.of(ExperimentEvidence.CLOSING_LABEL), Optional.of(new SequenceRange(1, cursor))),
                derived.support());
    }

    @Test
    void aClosingObservationThatDisagreesWithTheEventIsRefusedAndTheAccessorFails() {
        RuntimeObservation closing = evidence.observation(ExperimentEvidence.CLOSING_LABEL);
        OrderObservation order = closing.orders().getFirst();
        OrderObservation earlier = new OrderObservation(
                order.orderId(),
                order.productId(),
                order.requestedQuantity(),
                order.releasedQuantity(),
                order.completedQuantity(),
                order.createdAt(),
                SimTime.of(order.completedAt().value() - 1),
                order.complete());
        ExperimentEvidence disagreeing = TamperedEvidence.withObservation(
                evidence,
                ExperimentEvidence.CLOSING_LABEL,
                new RuntimeObservation(
                        closing.metadata(), closing.resources(), List.of(earlier), closing.jobs(), closing.pendingWork(),
                        closing.performance()));

        OracleOutcome.Underdetermined<Long> refused = assertUnderdetermined(new CompletionTickOracle().evaluateOn(disagreeing));
        assertTrue(refused.reasons().getFirst().contains("at tick 29 but the closing observation reports completedAt 28"),
                refused.toString());

        IllegalStateException failure =
                assertThrows(IllegalStateException.class, () -> CompletionTickOracle.completionTick(disagreeing));
        assertTrue(failure.getMessage().contains("has no single-order completion tick"), failure.getMessage());
    }

    @Test
    void aClosingObservationForAnotherOrderIsRefused() {
        RuntimeObservation closing = evidence.observation(ExperimentEvidence.CLOSING_LABEL);
        OrderObservation order = closing.orders().getFirst();
        OrderObservation other = new OrderObservation(
                new OrderId(order.orderId().value() + 1), order.productId(), order.requestedQuantity(),
                order.releasedQuantity(), order.completedQuantity(), order.createdAt(),
                order.completedAt(), order.complete());
        ExperimentEvidence mismatched = TamperedEvidence.withObservation(
                evidence, ExperimentEvidence.CLOSING_LABEL,
                new RuntimeObservation(closing.metadata(), closing.resources(), List.of(other),
                        closing.jobs(), closing.pendingWork(), closing.performance()));

        OracleOutcome.Underdetermined<Long> refused =
                assertUnderdetermined(new CompletionTickOracle().evaluateOn(mismatched));
        assertTrue(refused.reasons().getFirst().contains("does not report exactly the accepted order"),
                refused.toString());
    }

    @Test
    void aMissingCompletionEventIsRefused() {
        List<RuntimeEventEnvelope> withoutCompletion = evidence.retainedEvents().stream()
                .filter(event -> event.eventType() != RuntimeEventType.ORDER_COMPLETED)
                .toList();

        OracleOutcome.Underdetermined<Long> refused = assertUnderdetermined(
                new CompletionTickOracle().evaluateOn(TamperedEvidence.withEvents(evidence, withoutCompletion)));

        assertTrue(refused.reasons().getFirst().contains("0 ORDER_COMPLETED events"), refused.toString());
    }

    @Test
    void anIncompleteEventWindowIsRefused() {
        ExperimentEvidence joinedLate = run(
                "partial/baseline-joined-late",
                WindowIntent.PARTIAL,
                submit(),
                ExperimentStep.discardEvents("joined-late"),
                ExperimentStep.advanceToQuiescence(1_000),
                ExperimentStep.captureEvents("after-join"));

        OracleOutcome.Underdetermined<Long> refused = assertUnderdetermined(new CompletionTickOracle().evaluateOn(joinedLate));

        assertTrue(refused.reasons().getFirst().contains("not all retained"), refused.toString());
    }

    @Test
    void anOrderThatHasNotCompletedIsRefused() {
        ExperimentEvidence unfinished = run(
                "unfinished/baseline-stopped-early",
                WindowIntent.COMPLETE_RUN,
                submit(),
                ExperimentStep.advanceUntil(10),
                ExperimentStep.captureEvents("so-far"));

        OracleOutcome.Underdetermined<Long> refused = assertUnderdetermined(new CompletionTickOracle().evaluateOn(unfinished));

        assertTrue(refused.reasons().getFirst().contains("is not complete at the closing observation"), refused.toString());
    }

    @Test
    void moreOrFewerThanOneAcceptedOrderIsRefused() {
        ExperimentEvidence twoOrders = run(
                "two-orders/baseline",
                WindowIntent.COMPLETE_RUN,
                submit(),
                submit(),
                ExperimentStep.advanceToQuiescence(1_000),
                ExperimentStep.captureEvents("everything"));
        ExperimentEvidence noOrder = run(
                "no-order/baseline",
                WindowIntent.COMPLETE_RUN,
                ExperimentStep.submit(ThreeStepRoutingFamily.PRODUCT, 0, ThreeStepRoutingFamily.UNIT_PRICE),
                ExperimentStep.captureEvents("nothing"));

        assertTrue(assertUnderdetermined(new CompletionTickOracle().evaluateOn(twoOrders)).reasons().getFirst()
                .contains("2 orders were accepted"));
        assertTrue(assertUnderdetermined(new CompletionTickOracle().evaluateOn(noOrder)).reasons().getFirst()
                .contains("0 orders were accepted"));
    }
}
