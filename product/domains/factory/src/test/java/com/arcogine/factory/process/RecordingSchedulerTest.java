package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.types.MachineId;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit coverage for {@link RecordingScheduler}'s capture-window design: independent review of PR
 * #177 found the original always-append design retained every event ever scheduled for the
 * lifetime of the owning {@link FactoryRuntime}, including events scheduled by ordinary {@code
 * advance()}/{@code advanceUntil} processing that had nothing to do with any command capture. This
 * proves the fix: capture only happens between {@link RecordingScheduler#startCapturing} and
 * {@link RecordingScheduler#stopCapturing}, and nothing scheduled outside that window is retained
 * by the scheduler itself anywhere.
 */
class RecordingSchedulerTest {

    private static Event eventAt(long time) {
        return Event.of(SimTime.of(time), new EventPayload.MachineAvailabilityChange(new MachineId(1), true));
    }

    @Test
    void onlyEventsScheduledWhileCapturingIsActiveAreCaptured() {
        RecordingScheduler scheduler = new RecordingScheduler();

        scheduler.schedule(eventAt(1)); // before any capture window: must never be captured

        List<Event> captured = new ArrayList<>();
        scheduler.startCapturing(captured);
        Event duringA = eventAt(2);
        Event duringB = eventAt(3);
        scheduler.schedule(duringA);
        scheduler.schedule(duringB);
        scheduler.stopCapturing();

        scheduler.schedule(eventAt(4)); // after the window closes: must never be captured

        assertEquals(List.of(duringA, duringB), captured);
    }

    @Test
    void ordinaryAdvancementScheduledOutsideACaptureWindowDoesNotLeakIntoALaterCaptureWindow() {
        RecordingScheduler scheduler = new RecordingScheduler();

        // Simulate a long-running session's worth of ordinary scheduling/draining, entirely
        // outside any command's capture window -- this is the shape that grew without bound
        // before the fix (RecordingScheduler.schedule appended to one permanent list forever).
        for (int i = 1; i <= 10_000; i++) {
            scheduler.schedule(eventAt(i));
            scheduler.nextEvent();
        }

        List<Event> captured = new ArrayList<>();
        scheduler.startCapturing(captured);
        Event commandEvent = eventAt(20_000);
        scheduler.schedule(commandEvent);
        scheduler.stopCapturing();

        assertEquals(
                List.of(commandEvent),
                captured,
                "a capture window must report only what it directly observed, regardless of how much "
                        + "ordinary scheduling happened before it opened");
    }

    @Test
    void capturingCanBeReusedAcrossSuccessiveCommandsWithoutAccumulatingPriorWindows() {
        RecordingScheduler scheduler = new RecordingScheduler();

        List<Event> firstCapture = new ArrayList<>();
        scheduler.startCapturing(firstCapture);
        Event first = eventAt(1);
        scheduler.schedule(first);
        scheduler.stopCapturing();

        List<Event> secondCapture = new ArrayList<>();
        scheduler.startCapturing(secondCapture);
        Event second = eventAt(2);
        scheduler.schedule(second);
        scheduler.stopCapturing();

        assertEquals(List.of(first), firstCapture, "the first window's own list must be unaffected by the second");
        assertEquals(
                List.of(second),
                secondCapture,
                "a fresh capture window must not inherit anything from a prior command's window");
        assertFalse(secondCapture.contains(first));
    }
}
