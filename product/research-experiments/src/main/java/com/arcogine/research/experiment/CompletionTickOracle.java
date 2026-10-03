package com.arcogine.research.experiment;

import com.arcogine.factory.process.OrderObservation;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.types.OrderId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Research-local derivation: the tick at which a run's single accepted order completed, as of the
 * runner's closing observation. Like every derivation here, it is not an Engine fact, game-owned
 * analytics, or public API.
 *
 * <p>The completion time is reported twice by the supported contract: by the closing observation's
 * order projection and by the order's {@code ORDER_COMPLETED} event. This derivation reads both and
 * derives a tick only when they agree, so a fixture's completion is never taken from one source
 * that the other contradicts. It is defined for a run with exactly one accepted order, and refuses
 * otherwise.
 */
public final class CompletionTickOracle implements Oracle<Long> {

    public static final ResearchDefinition DEFINITION = new ResearchDefinition(
            "single-order-completion-tick",
            "For the closing observation and the supported events 1..latestEventSequence: require exactly one"
                    + " ORDER_ACCEPTED event, for the one order the observation reports; require that order to be"
                    + " complete in the observation and to have exactly one ORDER_COMPLETED event. The completion tick"
                    + " is that event's simulation time, which must equal the observation's completedAt. Refuse when"
                    + " any of those events was not retained or any requirement does not hold.",
            Set.of(EvidenceInput.OBSERVATIONS, EvidenceInput.SUPPORTED_EVENTS));

    /**
     * The completion tick of {@code evidence}'s single accepted order.
     *
     * @throws IllegalStateException when the derivation refuses, with its reasons
     */
    public static long completionTick(ExperimentEvidence evidence) {
        return switch (new CompletionTickOracle().evaluateOn(evidence)) {
            case OracleOutcome.Derived<Long> derived -> derived.value();
            case OracleOutcome.Underdetermined<Long> refused -> throw new IllegalStateException(
                    "fixture '" + evidence.fixtureId() + "' has no single-order completion tick: " + refused.reasons());
        };
    }

    @Override
    public ResearchDefinition definition() {
        return DEFINITION;
    }

    @Override
    public OracleOutcome<Long> evaluate(DeclaredEvidence evidence) {
        RuntimeObservation closing = evidence.observation(ExperimentEvidence.CLOSING_LABEL);
        long cursor = closing.metadata().latestEventSequence();
        Optional<List<RuntimeEventEnvelope>> events = evidence.completeEvents(0, cursor);
        if (events.isEmpty()) {
            return evidence.underdetermined("supported events 1.." + cursor + " are not all retained; missing "
                    + evidence.missingEvents(0, cursor));
        }

        List<OrderId> accepted = events.get().stream()
                .map(RuntimeEventEnvelope::payload)
                .filter(RuntimeEventPayload.OrderAccepted.class::isInstance)
                .map(payload -> ((RuntimeEventPayload.OrderAccepted) payload).orderId())
                .toList();
        if (accepted.size() != 1) {
            return evidence.underdetermined(
                    accepted.size() + " orders were accepted; a completion tick needs exactly one");
        }
        OrderId orderId = accepted.getFirst();
        if (closing.orders().size() != 1 || !closing.orders().getFirst().orderId().equals(orderId)) {
            return evidence.underdetermined(
                    "the closing observation does not report exactly the accepted order " + orderId);
        }
        OrderObservation order = closing.orders().getFirst();
        if (!order.complete() || order.completedAt() == null) {
            return evidence.underdetermined(orderId + " is not complete at the closing observation");
        }

        List<Long> completedAt = events.get().stream()
                .filter(event -> event.payload() instanceof RuntimeEventPayload.OrderCompleted completed
                        && completed.orderId().equals(orderId))
                .map(event -> event.simulationTime().value())
                .toList();
        if (completedAt.size() != 1) {
            return evidence.underdetermined(
                    completedAt.size() + " ORDER_COMPLETED events report " + orderId + "; expected exactly one");
        }
        long eventTick = completedAt.getFirst();
        long observedTick = order.completedAt().value();
        if (eventTick != observedTick) {
            return evidence.underdetermined("ORDER_COMPLETED reports " + orderId + " at tick " + eventTick
                    + " but the closing observation reports completedAt " + observedTick);
        }
        return evidence.derived(eventTick);
    }
}
