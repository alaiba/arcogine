package com.arcogine.factory.research;

import com.arcogine.types.RunId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * What a run's captured supported-event evidence actually covers.
 *
 * <p>{@code FactoryRuntime.drainSupportedEvents()} drains: it is not retained replay
 * ({@code docs/architecture/runtime-contract.md}), so an event stream is only as complete as the
 * drains the experiment kept. This record is research metadata around that contract, not a new one.
 * It states what was collected and when, so a partial trace cannot be mistaken for a complete
 * history, and it does not add event sourcing or replay to the runtime.
 *
 * @param runId the run/session identity, exactly as the runtime issued it (before normalization)
 * @param declaredIntent whether the fixture meant to capture the complete run
 * @param startSequence sequence of the first retained event, or {@code 0} when none was retained
 * @param endSequence sequence of the last retained event, or {@code 0} when none was retained
 * @param runFinalSequence the run's event cursor at the closing observation: the last sequence the
 *     runtime had emitted when the experiment ended
 * @param missingSequences every sequence in {@code 1..runFinalSequence} that was not retained
 * @param collectionPoints every collection action, in script order, ending with the closing
 *     observation
 */
public record EvidenceWindow(
        RunId runId,
        ExperimentFixture.WindowIntent declaredIntent,
        long startSequence,
        long endSequence,
        long runFinalSequence,
        List<SequenceRange> missingSequences,
        List<CollectionPoint> collectionPoints) {

    /** One collection action and the run's event cursor when it happened. */
    public record CollectionPoint(int stepIndex, String label, Kind kind, long runCursor) {

        public enum Kind {
            OBSERVATION,
            EVENTS_RETAINED,
            EVENTS_DISCARDED,
            /** The observation the runner always takes last, so the run's final cursor is known. */
            CLOSING_OBSERVATION
        }

        public CollectionPoint {
            Objects.requireNonNull(label, "label");
            Objects.requireNonNull(kind, "kind");
        }
    }

    public EvidenceWindow {
        Objects.requireNonNull(runId, "runId");
        Objects.requireNonNull(declaredIntent, "declaredIntent");
        missingSequences = List.copyOf(Objects.requireNonNull(missingSequences, "missingSequences"));
        collectionPoints = List.copyOf(Objects.requireNonNull(collectionPoints, "collectionPoints"));
        if (startSequence < 0 || endSequence < startSequence || runFinalSequence < endSequence) {
            throw new IllegalArgumentException("inconsistent evidence window bounds: start=" + startSequence
                    + " end=" + endSequence + " runFinal=" + runFinalSequence);
        }
    }

    /**
     * True when every supported event of the run, from the first through the closing observation,
     * was retained.
     */
    public boolean isComplete() {
        return missingSequences.isEmpty();
    }

    /**
     * The sequences in {@code (afterSequence, throughSequence]} that were not retained. A sequence
     * beyond the run's final cursor was never emitted, so it cannot have been retained and counts as
     * missing. An empty result means the whole range is available.
     */
    public List<SequenceRange> missingWithin(long afterSequence, long throughSequence) {
        if (afterSequence < 0 || throughSequence < afterSequence) {
            throw new IllegalArgumentException(
                    "invalid range (" + afterSequence + ", " + throughSequence + "]");
        }
        long from = afterSequence + 1;
        List<SequenceRange> missing = new ArrayList<>();
        for (SequenceRange gap : missingSequences) {
            long low = Math.max(gap.from(), from);
            long high = Math.min(gap.through(), throughSequence);
            if (low <= high) {
                missing.add(new SequenceRange(low, high));
            }
        }
        long beyondFrom = Math.max(from, runFinalSequence + 1);
        if (beyondFrom <= throughSequence) {
            missing.add(new SequenceRange(beyondFrom, throughSequence));
        }
        return List.copyOf(missing);
    }

    EvidenceWindow withRunId(RunId replacement) {
        return new EvidenceWindow(
                replacement,
                declaredIntent,
                startSequence,
                endSequence,
                runFinalSequence,
                missingSequences,
                collectionPoints);
    }
}
