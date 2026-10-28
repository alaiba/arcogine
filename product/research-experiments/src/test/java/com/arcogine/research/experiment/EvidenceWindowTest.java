package com.arcogine.research.experiment;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.research.experiment.ExperimentFixture.WindowIntent;
import com.arcogine.types.RunId;
import java.util.ArrayList;
import java.util.Comparator;
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

    private static final long MAX = Long.MAX_VALUE;

    private static EvidenceWindow window(long start, long end, long runFinal, SequenceRange... gaps) {
        return new EvidenceWindow(
                RunId.create(), WindowIntent.PARTIAL, start, end, runFinal, List.of(gaps), List.of());
    }

    private static void assertMissing(EvidenceWindow window, long after, long through, SequenceRange... expected) {
        assertEquals(List.of(expected), window.missingWithin(after, through), "(" + after + ", " + through + "]");
    }

    @Test
    void queriesAtTheTopOfTheSequenceRangeAreTotal() {
        // (MAX, MAX] is empty at every frontier. The f=0 and f=5 rows are the discriminating cases for a
        // fix that guards only the frontier side: the query-side successor MAX + 1 wraps there.
        assertMissing(window(0, 0, 0), MAX, MAX);
        assertMissing(window(1, 5, 5), MAX, MAX);
        assertMissing(window(1, 5, 5, new SequenceRange(2, 3)), MAX, MAX);
        assertMissing(window(1, MAX, MAX), MAX, MAX);

        // The f=MAX (0, MAX] row is the discriminating case for a fix that guards only the query side:
        // the frontier successor MAX + 1 wraps there and would report the whole range as missing.
        assertMissing(window(1, MAX, MAX), 0, MAX);
        assertMissing(window(1, MAX, MAX), MAX - 1, MAX);
        assertMissing(window(1, MAX - 1, MAX, new SequenceRange(MAX, MAX)), MAX - 1, MAX, new SequenceRange(MAX, MAX));
        assertMissing(window(2, MAX, MAX, new SequenceRange(1, 1)), 0, 1, new SequenceRange(1, 1));
        assertMissing(window(2, MAX, MAX, new SequenceRange(1, 1)), 1, MAX);

        assertMissing(window(1, MAX - 1, MAX - 1), MAX - 1, MAX, new SequenceRange(MAX, MAX));
        assertMissing(window(1, 1, 1), 0, 1);
        assertMissing(window(1, 1, 1), 1, 1);
        assertMissing(window(1, 1, 1), 1, MAX, new SequenceRange(2, MAX));
        assertMissing(window(0, 0, 0), 0, 0);
        assertMissing(window(0, 0, 0), 0, 1, new SequenceRange(1, 1));
        SequenceRange gap = new SequenceRange(2, 3);
        assertMissing(window(1, 5, 5, gap), 0, 5, gap);
        assertMissing(window(1, 5, 5, gap), 2, 5, new SequenceRange(3, 3));
        assertMissing(window(1, 5, 5, gap), 3, 5);
    }

    /** Independent of {@code missingWithin}: open/closed endpoint pairs, no successor of a possible MAX. */
    private static List<long[]> oracle(long after, long through, long runFinal, List<SequenceRange> gaps) {
        List<long[]> result = new ArrayList<>(); // each {exclusiveLow, inclusiveHigh}
        for (SequenceRange gap : gaps) {
            long low = Math.max(after, gap.from() - 1);
            long high = Math.min(through, gap.through());
            if (low < high) {
                result.add(new long[] {low, high});
            }
        }
        long low = Math.max(after, runFinal);
        if (low < through) {
            result.add(new long[] {low, through});
        }
        return normalize(result);
    }

    /** Sorted, with overlapping or touching intervals merged; touching tested without a successor. */
    private static List<long[]> normalize(List<long[]> intervals) {
        List<long[]> sorted = new ArrayList<>(intervals);
        sorted.sort(Comparator.comparingLong(interval -> interval[0]));
        List<long[]> merged = new ArrayList<>();
        for (long[] interval : sorted) {
            long[] last = merged.isEmpty() ? null : merged.getLast();
            if (last != null && interval[0] <= last[1]) {
                last[1] = Math.max(last[1], interval[1]);
            } else {
                merged.add(interval.clone());
            }
        }
        return merged;
    }

    private static List<long[]> asIntervals(List<SequenceRange> ranges) {
        List<long[]> intervals = new ArrayList<>();
        for (SequenceRange range : ranges) {
            intervals.add(new long[] {range.from() - 1, range.through()});
        }
        return normalize(intervals);
    }

    private static void assertSameMembership(List<long[]> expected, List<long[]> actual, String context) {
        assertEquals(expected.size(), actual.size(), context);
        for (int i = 0; i < expected.size(); i++) {
            assertArrayEquals(expected.get(i), actual.get(i), context);
        }
    }

    @Test
    void theIndependentOracleAgreesWithBruteForceMembershipOnSmallValues() {
        List<List<SequenceRange>> gapSets =
                List.of(List.of(), List.of(new SequenceRange(2, 3)), List.of(new SequenceRange(1, 1), new SequenceRange(4, 6)));
        for (List<SequenceRange> gaps : gapSets) {
            for (long runFinal = 0; runFinal <= 8; runFinal++) {
                for (long after = 0; after <= 10; after++) {
                    for (long through = after; through <= 10; through++) {
                        List<long[]> intervals = oracle(after, through, runFinal, gaps);
                        for (long s = 1; s <= 11; s++) {
                            final long sequence = s;
                            boolean expected = sequence > after
                                    && sequence <= through
                                    && (sequence > runFinal
                                            || gaps.stream()
                                                    .anyMatch(g -> g.from() <= sequence && sequence <= g.through()));
                            boolean actual = intervals.stream().anyMatch(i -> i[0] < sequence && sequence <= i[1]);
                            assertEquals(expected, actual, "s=" + s + " (" + after + ", " + through + "] f=" + runFinal);
                        }
                    }
                }
            }
        }
    }

    @Test
    void missingWithinMatchesTheOracleAcrossTheWholeSequenceRange() {
        long[] frontiers = {0, 1, 5, MAX - 1, MAX};
        long[] bounds = {0, 1, MAX - 1, MAX};
        SequenceRange midGap = new SequenceRange(2, 3);
        for (long frontier : frontiers) {
            List<List<SequenceRange>> gapSets = new ArrayList<>();
            gapSets.add(List.of());
            if (frontier >= 5) {
                gapSets.add(List.of(midGap));
            }
            if (frontier == MAX) {
                gapSets.add(List.of(new SequenceRange(MAX, MAX)));
            }
            for (List<SequenceRange> gaps : gapSets) {
                boolean topGap = !gaps.isEmpty() && gaps.getFirst().through() == frontier;
                EvidenceWindow window = window(frontier == 0 ? 0 : 1, topGap ? frontier - 1 : frontier, frontier,
                        gaps.toArray(SequenceRange[]::new));
                for (long after : bounds) {
                    for (long through : bounds) {
                        if (after > through) {
                            continue;
                        }
                        String context = "f=" + frontier + " gaps=" + gaps + " (" + after + ", " + through + "]";
                        List<SequenceRange> raw = window.missingWithin(after, through);
                        assertSameMembership(oracle(after, through, frontier, gaps), asIntervals(raw), context);
                        List<SequenceRange> byStart = new ArrayList<>(raw);
                        byStart.sort(Comparator.comparingLong(SequenceRange::from));
                        for (int i = 1; i < byStart.size(); i++) {
                            assertTrue(byStart.get(i).from() > byStart.get(i - 1).through(),
                                    "no duplicate or overlapping ranges: " + context + " " + raw);
                        }
                    }
                }
            }
        }
    }

    @Test
    void invalidBoundsAndRangesAreRejected() {
        EvidenceWindow window = windowMissingTwoGaps();

        assertThrows(IllegalArgumentException.class, () -> new SequenceRange(0, 3));
        assertThrows(IllegalArgumentException.class, () -> new SequenceRange(5, 4));
        assertEquals(3, new SequenceRange(5, 7).size());
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
        assertThrows(
                IllegalArgumentException.class,
                () -> new EvidenceWindow(
                        RunId.create(), WindowIntent.PARTIAL, -1, 0, 0, List.of(), List.of()));
    }
}
