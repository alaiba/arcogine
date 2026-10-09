package com.arcogine.research.executionaccount.adaptation;

import com.arcogine.factory.process.FactoryRuntime;
import com.arcogine.types.RunId;
import java.util.Objects;

/**
 * Research-local statement of the qualified controller-managed closure protocol, packaged once so
 * consumers need not reinvent it. Built only from the supported session-control surface; it is not
 * a proposed production type and adds nothing to the Engine.
 *
 * <p>What the protocol establishes itself: successful exhaustion of every pending internal event at
 * or before the bound (never inferred from a used-up budget, never after a fault), and the supported
 * boundary {@code (RunId, sequence, supported time)} observed immediately afterwards by the same
 * exclusive caller. What it cannot establish and only records as the attestor's commitment: that the
 * caller is the runtime's only driver, has stopped admission, and will issue no command whose effect
 * could have a time below the bound. Nothing in the supported contract can check that commitment.
 */
final class ControllerClosure {

    private static final int MAX_CALLS = 1_000_000;

    private ControllerClosure() {}

    /**
     * A controller's attributable statement that no supported change with time below {@code
     * closedBefore} will follow sequence {@code boundSequence} of run {@code runId}. The bound
     * boundary is a runtime fact the controller observed; the "no later input" part is its promise.
     */
    record Attestation(RunId runId, long closedBefore, long boundSequence, long boundSupportedTime, String attestor) {

        Attestation {
            Objects.requireNonNull(runId, "runId");
            Objects.requireNonNull(attestor, "attestor");
        }
    }

    sealed interface Result {

        record Attested(Attestation attestation) implements Result {}

        record Refused(String reason) implements Result {}
    }

    /**
     * Runs the protocol: repeat bounded advancement through {@code closedBefore} until a call proves
     * exhaustion, refuse on a fault, then bind the observed boundary. By calling it the caller asserts
     * the preconditions described in the class documentation.
     */
    static Result closeBefore(FactoryRuntime runtime, long closedBefore, long budget, String attestor) {
        for (int call = 0; call < MAX_CALLS; call++) {
            BoundedAdvance.Outcome outcome = BoundedAdvance.advanceThrough(runtime, closedBefore, budget);
            switch (outcome.stop()) {
                case FAULTED -> {
                    return new Result.Refused("advancement faulted: " + outcome.fault().orElseThrow().getMessage());
                }
                case BUDGET_EXHAUSTED -> {
                    // Repeat: a used-up budget proves nothing about what remains.
                }
                case EXHAUSTED_THROUGH_TARGET -> {
                    return new Result.Attested(new Attestation(
                            outcome.runId(),
                            closedBefore,
                            outcome.sequenceAfter(),
                            outcome.supportedTimeAfter(),
                            attestor));
                }
            }
        }
        return new Result.Refused("exhaustion not reached within " + MAX_CALLS + " calls");
    }
}
