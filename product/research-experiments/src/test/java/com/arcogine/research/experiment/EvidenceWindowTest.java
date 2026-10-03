package com.arcogine.research.experiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.research.experiment.ExperimentFixture.WindowIntent;
import com.arcogine.types.RunId;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Range arithmetic of {@link EvidenceWindow}, independent of any run. */
class EvidenceWindowTest {

    /** Events 3-4 and 9-12 of a 15-event run were not retained. */
    private static EvidenceWindow windowMissingTwoGaps() {
        return new EvidenceWindow(
                RunId.create(),
                WindowIntent.PARTIAL,
                1,
                15,
                15,
                List.of(new SequenceRange(3, 4), new SequenceRange(9, 12)),
                List.of());
    }

    @Test
    void missingRangesAreClippedToTheRequestedInterval() {
        EvidenceWindow window = windowMissingTwoGaps();

        assertEquals(List.of(new SequenceRange(3, 4), new SequenceRange(9, 12)), window.missingWithin(0, 15));
        assertEquals(List.of(new SequenceRange(4, 4), new SequenceRange(9, 9)), window.missingWithin(3, 9));
        assertEquals(List.of(new SequenceRange(10, 11)), window.missingWithin(9, 11));
        assertEquals(List.of(), window.missingWithin(4, 8));
        assertEquals(List.of(), window.missingWithin(12, 15));
        assertEquals(List.of(), window.missingWithin(5, 5), "an empty interval is trivially complete");
    }

    @Test
    void sequencesBeyondTheRunFinalCursorWereNeverEmittedSoTheyCountAsMissing() {
        EvidenceWindow window = windowMissingTwoGaps();

        assertEquals(List.of(new SequenceRange(16, 20)), window.missingWithin(15, 20));
        assertEquals(
                List.of(new SequenceRange(11, 12), new SequenceRange(16, 20)), window.missingWithin(10, 20));
    }

    @Test
    void aWindowWithNoMissingSequencesIsCompleteEverywhereWithinTheRun() {
        EvidenceWindow complete =
                new EvidenceWindow(RunId.create(), WindowIntent.COMPLETE_RUN, 1, 15, 15, List.of(), List.of());

        assertTrue(complete.isComplete());
        assertEquals(List.of(), complete.missingWithin(0, 15));
        assertEquals(List.of(new SequenceRange(16, 16)), complete.missingWithin(0, 16));
    }

    @Test
    void aRunThatEmittedNothingHasAnEmptyCompleteWindow() {
        EvidenceWindow empty =
                new EvidenceWindow(RunId.create(), WindowIntent.COMPLETE_RUN, 0, 0, 0, List.of(), List.of());

        assertTrue(empty.isComplete());
        assertEquals(List.of(), empty.missingWithin(0, 0));
    }

    @Test
    void invalidBoundsAndRangesAreRejected() {
        EvidenceWindow window = windowMissingTwoGaps();

        assertThrows(IllegalArgumentException.class, () -> new SequenceRange(0, 3));
        assertThrows(IllegalArgumentException.class, () -> new SequenceRange(5, 4));
        assertThrows(IllegalArgumentException.class, () -> window.missingWithin(-1, 3));
        assertThrows(IllegalArgumentException.class, () -> window.missingWithin(6, 5));
        assertThrows(
                IllegalArgumentException.class,
                () -> new EvidenceWindow(
                        RunId.create(), WindowIntent.PARTIAL, 5, 4, 10, List.of(), List.of()),
                "the last retained sequence cannot precede the first");
        assertThrows(
                IllegalArgumentException.class,
                () -> new EvidenceWindow(
                        RunId.create(), WindowIntent.PARTIAL, 1, 12, 10, List.of(), List.of()),
                "a retained sequence cannot lie beyond the run's final cursor");
    }
}
