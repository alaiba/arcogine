package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.core.event.Event;
import com.arcogine.core.event.EventPayload;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit coverage for {@link RecordingScheduler}: its admission of step completions only, and its
 * capture-window design. Independent review of PR #177 found the original always-append design
 * retained every event ever scheduled for the lifetime of the owning {@link FactoryRuntime},
 * including events scheduled by ordinary {@code advance()}/{@code advanceUntil} processing that had
 * nothing to do with any command capture. The capture tests prove the fix: capture only happens
 * between {@link RecordingScheduler#startCapturing} and {@link RecordingScheduler#stopCapturing},
 * and nothing scheduled outside that window is retained by the scheduler itself anywhere.
 */
class RecordingSchedulerTest {

    private static Event eventAt(long time) {
        return Event.of(SimTime.of(time), new EventPayload.TaskEnd(new JobId(1), new MachineId(1), 0));
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

    /**
     * A Factory session schedules step completions and nothing else
     * (docs/architecture/engine-semantics.md section 4). Every other payload -- including the start
     * and order-completion markers the internal vocabulary still defines, and commands the session
     * applies immediately rather than queueing -- is refused before it can be queued or captured, so
     * no event that would consume advancement budget without an authoritative transition can be
     * reintroduced unnoticed.
     */
    @Test
    void refusesEveryEventThatIsNotAStepCompletion() {
        RecordingScheduler scheduler = new RecordingScheduler();
        List<Event> captured = new ArrayList<>();
        scheduler.startCapturing(captured);

        List<EventPayload> refused = List.of(
                new EventPayload.TaskStart(new JobId(1), new MachineId(1), 0),
                new EventPayload.OrderCompleted(new OrderId(1), new JobId(1), new ProductId(1), 1, 1.0),
                new EventPayload.OrderCreation(new ProductId(1), 1, 1.0),
                new EventPayload.MachineAvailabilityChange(new MachineId(1), true));
        for (EventPayload payload : refused) {
            assertThrows(IllegalArgumentException.class, () -> scheduler.schedule(Event.of(SimTime.of(1), payload)));
        }

        assertTrue(scheduler.isEmpty(), "a refused event must not be queued");
        assertTrue(captured.isEmpty(), "a refused event must not be reported as scheduled by a command");
        assertFalse(scheduler.hasPendingAuthoritativeWork());
    }

    @Test
    void everyQueuedStepCompletionIsPendingAuthoritativeWork() {
        RecordingScheduler scheduler = new RecordingScheduler();
        assertFalse(scheduler.hasPendingAuthoritativeWork());

        scheduler.schedule(eventAt(1));
        scheduler.schedule(eventAt(1));
        assertTrue(scheduler.hasPendingAuthoritativeWork());

        scheduler.nextEvent();
        assertTrue(scheduler.hasPendingAuthoritativeWork(), "one step completion is still queued");
        scheduler.nextEvent();
        assertFalse(scheduler.hasPendingAuthoritativeWork());
    }
}
