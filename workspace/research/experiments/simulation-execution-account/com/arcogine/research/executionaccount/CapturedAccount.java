package com.arcogine.research.executionaccount;

import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.research.experiment.SequenceRange;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.RunId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Research-local candidate: one consumer's captured supported evidence about one run, with its
 * coverage stated explicitly. It is an executable statement of the coverage rules the
 * execution-account investigation tests, built only from the supported runtime contract, and it is
 * deliberately not a proposed production type.
 *
 * <p>An account has a state basis (a supported observation at some cursor, which is cursor 0 for a
 * capture from establishment), the supported events it retained after that cursor, a frontier (the
 * latest supported observation it took), and optionally a controller's closure declaration. Its
 * claims are limited to what those establish:
 *
 * <ul>
 *   <li>it accepts evidence of exactly one run and one model fingerprint;
 *   <li>a missing sequence between its basis and its frontier is a gap, and anything spanning a gap
 *       is refused;
 *   <li>a state at time {@code a} is known only when the basis is at or before {@code a};
 *   <li>an interval {@code [a, b)} is closed only when supported time has reached {@code b}, or the
 *       controller declared that nothing further will happen at or before a horizon {@code >= b}.
 * </ul>
 */
final class CapturedAccount {

    /** Whether a claim over an interval is licensed, and if not, why. */
    record Determinacy(boolean determined, String reason) {

        static Determinacy yes() {
            return new Determinacy(true, "determined");
        }

        static Determinacy no(String reason) {
            return new Determinacy(false, reason);
        }
    }

    private final RunId runId;
    private final ModelFingerprint fingerprint;
    private final RuntimeObservation basis;
    private final List<RuntimeEventEnvelope> retained = new ArrayList<>();
    private RuntimeObservation frontier;
    private Long closedThrough;

    private CapturedAccount(RuntimeObservation basis) {
        this.basis = Objects.requireNonNull(basis, "basis");
        this.runId = basis.metadata().runId();
        this.fingerprint = basis.metadata().modelFingerprint();
        this.frontier = basis;
    }

    /** An account whose state basis is {@code basis}; cursor 0 means capture from establishment. */
    static CapturedAccount from(RuntimeObservation basis) {
        return new CapturedAccount(basis);
    }

    RunId runId() {
        return runId;
    }

    RuntimeObservation basis() {
        return basis;
    }

    long basisCursor() {
        return basis.metadata().latestEventSequence();
    }

    /** Retains drained events. Evidence of another run or model is refused, never merged. */
    void retain(List<RuntimeEventEnvelope> drained) {
        for (RuntimeEventEnvelope event : drained) {
            if (!event.runId().equals(runId)) {
                throw new IllegalArgumentException("event " + event.sequence() + " belongs to run " + event.runId()
                        + ", not to this account's run " + runId);
            }
            if (!event.modelFingerprint().equals(fingerprint)) {
                throw new IllegalArgumentException("event " + event.sequence() + " carries another model fingerprint");
            }
            if (event.sequence() <= basisCursor()) {
                continue; // already reflected in the state basis
            }
            if (closedThrough != null && event.simulationTime().value() <= closedThrough) {
                throw new IllegalStateException("closure violated: event " + event.sequence() + " at time "
                        + event.simulationTime().value() + " arrived after the controller closed the run through "
                        + closedThrough);
            }
            if (!retained.isEmpty() && event.sequence() <= retained.getLast().sequence()) {
                throw new IllegalArgumentException("event " + event.sequence() + " does not follow retained event "
                        + retained.getLast().sequence());
            }
            retained.add(event);
        }
    }

    /** Records a later supported observation of the same run as the account's frontier. */
    void observeFrontier(RuntimeObservation observation) {
        if (!observation.metadata().runId().equals(runId)) {
            throw new IllegalArgumentException("frontier observation belongs to another run");
        }
        if (observation.metadata().latestEventSequence() < frontier.metadata().latestEventSequence()) {
            throw new IllegalArgumentException("a frontier never moves backwards");
        }
        frontier = observation;
    }

    /**
     * The controller's declaration that every internal event at or before {@code horizon} has been
     * processed and that it will issue no further command. Only the party driving the runtime can
     * make it; nothing in the supported observation can.
     */
    void declareClosedThrough(long horizon) {
        closedThrough = horizon;
    }

    List<RuntimeEventEnvelope> retainedEvents() {
        return List.copyOf(retained);
    }

    RuntimeObservation frontier() {
        return frontier;
    }

    long frontierCursor() {
        return frontier.metadata().latestEventSequence();
    }

    long frontierTime() {
        return frontier.metadata().currentTime().value();
    }

    /** Sequences in {@code (basisCursor, frontierCursor]} that were not retained. */
    List<SequenceRange> missing() {
        List<SequenceRange> gaps = new ArrayList<>();
        long next = basisCursor() + 1;
        for (RuntimeEventEnvelope event : retained) {
            if (event.sequence() > frontierCursor()) {
                break;
            }
            if (event.sequence() > next) {
                gaps.add(new SequenceRange(next, event.sequence() - 1));
            }
            next = event.sequence() + 1;
        }
        if (next <= frontierCursor()) {
            gaps.add(new SequenceRange(next, frontierCursor()));
        }
        return List.copyOf(gaps);
    }

    /** True only for a gap-free capture whose basis is the run's establishment. */
    boolean coversFromEstablishment() {
        return basisCursor() == 0 && missing().isEmpty();
    }

    /** Whether the account licenses claims over simulation interval {@code [a, b)}. */
    Determinacy intervalDeterminacy(long a, long b) {
        if (b <= a) {
            throw new IllegalArgumentException("empty interval [" + a + ", " + b + ")");
        }
        List<SequenceRange> gaps = missing();
        if (!gaps.isEmpty()) {
            return Determinacy.no("supported events " + gaps + " between the basis and the frontier are missing");
        }
        long basisTime = basis.metadata().currentTime().value();
        if (basisTime > a) {
            return Determinacy.no("no state basis at " + a + ": the account begins at time " + basisTime
                    + " (cursor " + basisCursor() + ")");
        }
        boolean timeReached = frontierTime() >= b;
        boolean closed = closedThrough != null && closedThrough >= b;
        if (!timeReached && !closed) {
            return Determinacy.no("interval still open: supported time is " + frontierTime() + " < " + b
                    + " and no controller closure covers it");
        }
        return Determinacy.yes();
    }

    /** The account's events with simulation time strictly before {@code b}. */
    List<RuntimeEventEnvelope> eventsBefore(long b) {
        return retained.stream().filter(e -> e.simulationTime().value() < b).toList();
    }

    Optional<Long> closedThrough() {
        return Optional.ofNullable(closedThrough);
    }
}
