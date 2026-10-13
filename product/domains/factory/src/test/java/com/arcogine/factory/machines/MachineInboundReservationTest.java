package com.arcogine.factory.machines;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import com.arcogine.types.SimError;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Inbound admission reservation on a destination machine (docs/architecture/engine-semantics.md
 * sections 6, 8 and 9): a reservation consumes admission capacity without being active or queued
 * work, and ends either as active processing or as the tail of the machine's own queue.
 */
class MachineInboundReservationTest {

    private static final MachineId DESTINATION = new MachineId(2);

    private static Machine machine(int concurrency) {
        return new Machine(DESTINATION, "Destination", concurrency, null, 0);
    }

    private static JobId job(long value) {
        return new JobId(value);
    }

    /** Everything a test can observe about a machine, including its reservations. */
    private record Snapshot(
            MachineState state,
            List<JobId> activeJobs,
            List<JobId> queuedJobs,
            int queueDepth,
            List<JobId> inboundReservations,
            boolean canAcceptJob) {

        static Snapshot of(Machine m) {
            return new Snapshot(
                    m.state(),
                    m.activeJobs(),
                    m.queuedJobs(),
                    m.queueDepth(),
                    m.inboundReservations(),
                    m.canAcceptJob());
        }
    }

    private static void assertRefusedUnchanged(
            Machine m, Executable action, String... contextFragments) {
        Snapshot before = Snapshot.of(m);
        SimError.InvalidStateTransition error =
                assertThrows(SimError.InvalidStateTransition.class, action);
        for (String fragment : contextFragments) {
            assertTrue(
                    error.context().contains(fragment),
                    () -> "expected \"" + fragment + "\" in: " + error.context());
        }
        assertEquals(before, Snapshot.of(m), "a refused operation must not change the machine");
    }

    // --- capacity admission ---

    @Test
    void oneReservationFillsAUnaryMachine() {
        Machine m = machine(1);
        m.reserveInboundAdmission(job(1));

        assertFalse(m.canAcceptJob());
        assertRefusedUnchanged(
                m,
                () -> m.startJob(job(2)),
                "machine " + DESTINATION,
                "concurrency",
                "1 reserved for inbound jobs");
        assertRefusedUnchanged(
                m,
                () -> m.reserveInboundAdmission(job(2)),
                "Job(2)",
                DESTINATION.toString(),
                "concurrency");
        assertEquals(List.of(job(1)), m.inboundReservations());
    }

    @Test
    void oneActiveJobAndOneReservationFillConcurrencyTwo() {
        Machine m = machine(2);
        m.startJob(job(1));
        m.reserveInboundAdmission(job(2));

        assertFalse(m.canAcceptJob());
        assertRefusedUnchanged(m, () -> m.startJob(job(3)), "concurrency");
        assertRefusedUnchanged(m, () -> m.reserveInboundAdmission(job(3)), "concurrency");
    }

    @Test
    void oneReservationLeavesExactlyOneSlotOnConcurrencyTwo() {
        Machine m = machine(2);
        m.reserveInboundAdmission(job(1));
        assertTrue(m.canAcceptJob());

        m.startJob(job(2));
        assertFalse(m.canAcceptJob());
        assertRefusedUnchanged(m, () -> m.startJob(job(3)), "concurrency");
    }

    @Test
    void twoReservationsFillConcurrencyTwo() {
        Machine m = machine(2);
        m.reserveInboundAdmission(job(1));
        m.reserveInboundAdmission(job(2));

        assertFalse(m.canAcceptJob());
        assertRefusedUnchanged(
                m, () -> m.startJob(job(3)), "concurrency", "2 reserved for inbound jobs");
        assertRefusedUnchanged(m, () -> m.reserveInboundAdmission(job(3)), "concurrency");
    }

    /**
     * Every interleaving of ordinary starts and reservations, one longer than the machine's
     * concurrency: exactly {@code concurrency} succeed, the next is refused, and admission load
     * never exceeds concurrency.
     */
    @Test
    void anyMixOfStartsAndReservationsIsBoundedByConcurrency() {
        for (int concurrency = 1; concurrency <= 3; concurrency++) {
            int steps = concurrency + 1;
            for (int mask = 0; mask < (1 << steps); mask++) {
                Machine m = machine(concurrency);
                for (int step = 0; step < steps; step++) {
                    JobId jobId = job(step + 1);
                    boolean reserve = (mask & (1 << step)) != 0;
                    Executable action = reserve
                            ? () -> m.reserveInboundAdmission(jobId)
                            : () -> m.startJob(jobId);
                    if (step < concurrency) {
                        assertTrue(m.canAcceptJob());
                        assertDoesNotThrow(action);
                    } else {
                        assertFalse(m.canAcceptJob());
                        assertRefusedUnchanged(m, action, "concurrency");
                    }
                    assertTrue(
                            m.activeJobs().size() + m.inboundReservations().size() <= concurrency);
                }
                assertEquals(
                        concurrency, m.activeJobs().size() + m.inboundReservations().size());
            }
        }
    }

    // --- active and queue separation ---

    @Test
    void reservationsChangeNothingTheMachineReports() {
        Machine reserved = machine(3);
        Machine plain = machine(3);
        for (Machine m : List.of(reserved, plain)) {
            m.startJob(job(4));
            m.enqueueJob(job(6));
            m.enqueueJob(job(5));
        }
        reserved.reserveInboundAdmission(job(1));
        reserved.reserveInboundAdmission(job(2));

        MachineView reservedView = reserved;
        MachineView plainView = plain;
        assertEquals(plainView.activeJobs(), reservedView.activeJobs());
        assertEquals(plainView.queuedJobs(), reservedView.queuedJobs());
        assertEquals(plainView.queueDepth(), reservedView.queueDepth());
        assertEquals(plainView.state(), reservedView.state());
        assertFalse(reserved.activeJobs().contains(job(1)));
        assertFalse(reserved.queuedJobs().contains(job(1)));
    }

    @Test
    void reservationsOnlyMachineIsIdle() {
        Machine m = machine(2);
        m.reserveInboundAdmission(job(1));
        m.reserveInboundAdmission(job(2));

        assertEquals(MachineState.Idle, m.state());
        assertTrue(m.activeJobs().isEmpty());
        assertTrue(m.queuedJobs().isEmpty());
        assertEquals(0, m.queueDepth());
    }

    @Test
    void machineIsBusyOnlyBecauseOfItsActiveJob() {
        Machine m = machine(2);
        m.startJob(job(1));
        m.reserveInboundAdmission(job(2));
        assertEquals(MachineState.Busy, m.state());

        m.completeJob(job(1));
        assertEquals(MachineState.Idle, m.state());
        assertEquals(List.of(job(2)), m.inboundReservations());
        assertTrue(m.canAcceptJob());
    }

    @Test
    void reservationsKeepReservationOrderRatherThanJobIdOrder() {
        Machine m = machine(3);
        m.reserveInboundAdmission(job(5));
        m.reserveInboundAdmission(job(2));
        m.reserveInboundAdmission(job(9));

        assertEquals(List.of(job(5), job(2), job(9)), m.inboundReservations());
    }

    // --- offline behavior ---

    @Test
    void reservationsOnlyMachineCanGoOffline() {
        Machine m = machine(1);
        m.reserveInboundAdmission(job(1));
        m.setAvailability(false);

        assertEquals(MachineState.Offline, m.state());
        assertEquals(List.of(job(1)), m.inboundReservations());

        Machine working = machine(1);
        working.startJob(job(1));
        assertRefusedUnchanged(working, () -> working.setAvailability(false), "jobs are active");
    }

    @Test
    void reservationsSurviveGoingOfflineAndComingBackOnline() {
        Machine m = machine(3);
        m.reserveInboundAdmission(job(1));
        m.reserveInboundAdmission(job(2));

        m.setAvailability(false);
        assertEquals(List.of(job(1), job(2)), m.inboundReservations());
        m.setAvailability(true);

        assertEquals(MachineState.Idle, m.state());
        assertEquals(List.of(job(1), job(2)), m.inboundReservations());
        assertTrue(m.canAcceptJob());
        m.reserveInboundAdmission(job(3));
        assertFalse(m.canAcceptJob());
    }

    @Test
    void offlineMachineCanNeitherAcceptNorReserve() {
        Machine m = machine(2);
        m.reserveInboundAdmission(job(1));
        m.setAvailability(false);

        assertFalse(m.canAcceptJob());
        assertRefusedUnchanged(
                m,
                () -> m.reserveInboundAdmission(job(2)),
                "Job(2)",
                "offline machine " + DESTINATION);
        assertRefusedUnchanged(m, () -> m.startJob(job(2)), "offline");
    }

    // --- conversion into active processing ---

    @Test
    void reservationOnFullUnaryMachineConvertsIntoProcessing() {
        Machine m = machine(1);
        m.reserveInboundAdmission(job(1));

        m.startReservedJob(job(1));

        Machine startedDirectly = machine(1);
        startedDirectly.startJob(job(1));
        assertEquals(Snapshot.of(startedDirectly), Snapshot.of(m));
        assertEquals(MachineState.Busy, m.state());
        assertEquals(List.of(job(1)), m.activeJobs());
        assertTrue(m.inboundReservations().isEmpty());
        assertRefusedUnchanged(
                m, () -> m.startReservedJob(job(1)), "Job(1)", "holds no inbound reservation");

        m.completeJob(job(1));
        assertEquals(MachineState.Idle, m.state());
        assertTrue(m.canAcceptJob());
        m.startJob(job(2));
        assertEquals(List.of(job(2)), m.activeJobs());
    }

    @Test
    void reservationBesideAnActiveJobConvertsWithoutChangingLoad() {
        Machine m = machine(2);
        m.startJob(job(1));
        m.reserveInboundAdmission(job(2));

        m.startReservedJob(job(2));

        Machine startedDirectly = machine(2);
        startedDirectly.startJob(job(1));
        startedDirectly.startJob(job(2));
        assertEquals(Snapshot.of(startedDirectly), Snapshot.of(m));
        assertEquals(List.of(job(1), job(2)), m.activeJobs());
        assertFalse(m.canAcceptJob());
    }

    @Test
    void reservationCannotConvertIntoProcessingOnOfflineMachine() {
        Machine m = machine(1);
        m.reserveInboundAdmission(job(1));
        m.setAvailability(false);

        assertRefusedUnchanged(
                m, () -> m.startReservedJob(job(1)), "Job(1)", "offline machine " + DESTINATION);
        assertEquals(List.of(job(1)), m.inboundReservations());
    }

    // --- conversion into the destination's own queue ---

    @Test
    void reservationOnOfflineMachineConvertsToTheTailOfItsQueue() {
        Machine m = machine(1);
        m.reserveInboundAdmission(job(3));
        m.enqueueJob(job(9));
        m.setAvailability(false);
        m.enqueueJob(job(5));

        m.enqueueReservedJob(job(3));

        assertEquals(List.of(job(9), job(5), job(3)), m.queuedJobs());
        assertEquals(3, m.queueDepth());
        assertTrue(m.inboundReservations().isEmpty());
        assertEquals(1, m.queuedJobs().stream().filter(job(3)::equals).count());
        assertRefusedUnchanged(
                m, () -> m.enqueueReservedJob(job(3)), "Job(3)", "holds no inbound reservation");

        m.setAvailability(true);
        assertEquals(MachineState.Idle, m.state());
        assertTrue(m.canAcceptJob());
        assertEquals(job(9), m.dequeueJob().orElseThrow());
        assertEquals(job(5), m.dequeueJob().orElseThrow());
        assertEquals(job(3), m.dequeueJob().orElseThrow());
    }

    @Test
    void reservationCannotConvertIntoQueueOnOnlineMachine() {
        Machine idle = machine(1);
        idle.reserveInboundAdmission(job(1));
        assertRefusedUnchanged(
                idle,
                () -> idle.enqueueReservedJob(job(1)),
                "Job(1)",
                "online machine " + DESTINATION);

        Machine busy = machine(2);
        busy.startJob(job(1));
        busy.reserveInboundAdmission(job(2));
        assertRefusedUnchanged(busy, () -> busy.enqueueReservedJob(job(2)), "online machine");
    }

    // --- misuse ---

    @Test
    void duplicateReservationIsRefused() {
        Machine m = machine(2);
        m.reserveInboundAdmission(job(1));

        assertRefusedUnchanged(
                m,
                () -> m.reserveInboundAdmission(job(1)),
                "Job(1)",
                "already holds an inbound reservation",
                DESTINATION.toString());
        assertTrue(m.canAcceptJob());
    }

    @Test
    void conversionWithoutAnyReservationIsRefused() {
        Machine online = machine(1);
        assertRefusedUnchanged(
                online,
                () -> online.startReservedJob(job(1)),
                "Job(1)",
                "holds no inbound reservation",
                DESTINATION.toString());

        Machine offline = machine(1);
        offline.setAvailability(false);
        assertRefusedUnchanged(
                offline, () -> offline.enqueueReservedJob(job(1)), "holds no inbound reservation");
    }

    @Test
    void conversionUsingAnotherJobsIdentityIsRefused() {
        Machine m = machine(1);
        m.reserveInboundAdmission(job(1));

        assertRefusedUnchanged(
                m, () -> m.startReservedJob(job(2)), "Job(2)", "holds no inbound reservation");

        m.setAvailability(false);
        assertRefusedUnchanged(
                m, () -> m.enqueueReservedJob(job(2)), "Job(2)", "holds no inbound reservation");
        assertEquals(List.of(job(1)), m.inboundReservations());
    }

    // --- determinism ---

    @Test
    void scriptedReservationHistoryIsReproducible() {
        List<Consumer<Machine>> script = List.of(
                m -> m.reserveInboundAdmission(job(5)),
                m -> m.reserveInboundAdmission(job(2)),
                m -> m.startJob(job(8)),
                m -> m.startReservedJob(job(5)),
                m -> m.completeJob(job(8)),
                m -> m.completeJob(job(5)),
                m -> m.setAvailability(false),
                m -> m.enqueueJob(job(1)),
                m -> m.enqueueReservedJob(job(2)),
                m -> m.setAvailability(true),
                m -> m.reserveInboundAdmission(job(7)),
                m -> m.startJob(m.dequeueJob().orElseThrow()));

        List<List<Snapshot>> runs = new ArrayList<>();
        for (int run = 0; run < 3; run++) {
            Machine m = machine(3);
            List<Snapshot> history = new ArrayList<>();
            for (Consumer<Machine> step : script) {
                step.accept(m);
                history.add(Snapshot.of(m));
            }
            runs.add(history);
        }

        assertEquals(runs.get(0), runs.get(1));
        assertEquals(runs.get(0), runs.get(2));
        assertEquals(
                new Snapshot(
                        MachineState.Busy,
                        List.of(job(1)),
                        List.of(job(2)),
                        1,
                        List.of(job(7)),
                        true),
                runs.get(0).get(script.size() - 1));
    }
}
