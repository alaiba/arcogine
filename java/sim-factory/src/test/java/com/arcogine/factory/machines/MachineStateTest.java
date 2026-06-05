package com.arcogine.factory.machines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.MachineState;
import com.arcogine.types.SimError;
import org.junit.jupiter.api.Test;

/** Ported from crates/sim-factory/tests/machine_state.rs. */
class MachineStateTest {

    private static Machine testMachine() {
        return new Machine(new MachineId(1), "TestMill", 1, null, 0);
    }

    @Test
    void newMachineIsIdle() {
        Machine m = testMachine();
        assertEquals(MachineState.Idle, m.state());
        assertTrue(m.activeJobs().isEmpty());
        assertEquals(0, m.queueDepth());
    }

    @Test
    void startJobTransitionsToBusy() {
        Machine m = testMachine();
        m.startJob(new JobId(1));
        assertEquals(MachineState.Busy, m.state());
        assertEquals(1, m.activeJobs().size());
    }

    @Test
    void completeJobTransitionsToIdle() {
        Machine m = testMachine();
        m.startJob(new JobId(1));
        m.completeJob(new JobId(1));
        assertEquals(MachineState.Idle, m.state());
        assertTrue(m.activeJobs().isEmpty());
    }

    @Test
    void cannotStartOnOfflineMachine() {
        Machine m = testMachine();
        m.setAvailability(false);
        assertEquals(MachineState.Offline, m.state());

        SimError.InvalidStateTransition error =
                assertThrows(
                        SimError.InvalidStateTransition.class, () -> m.startJob(new JobId(1)));
        assertTrue(error.context().contains("offline"));
    }

    @Test
    void cannotExceedConcurrency() {
        Machine m = testMachine(); // concurrency = 1
        m.startJob(new JobId(1));

        SimError.InvalidStateTransition error =
                assertThrows(
                        SimError.InvalidStateTransition.class, () -> m.startJob(new JobId(2)));
        assertTrue(error.context().contains("concurrency"));
    }

    @Test
    void concurrentMachineAcceptsMultipleJobs() {
        Machine m = new Machine(new MachineId(1), "ParallelMill", 3, null, 0);
        m.startJob(new JobId(1));
        m.startJob(new JobId(2));
        m.startJob(new JobId(3));
        assertEquals(3, m.activeJobs().size());
        assertEquals(MachineState.Busy, m.state());

        assertThrows(SimError.InvalidStateTransition.class, () -> m.startJob(new JobId(4)));
    }

    @Test
    void completeNonexistentJobReturnsError() {
        Machine m = testMachine();
        m.startJob(new JobId(1));

        SimError.InvalidStateTransition error =
                assertThrows(
                        SimError.InvalidStateTransition.class, () -> m.completeJob(new JobId(999)));
        assertTrue(error.context().contains("999"));
    }

    @Test
    void cannotGoOfflineWithActiveJobs() {
        Machine m = testMachine();
        m.startJob(new JobId(1));

        assertThrows(SimError.InvalidStateTransition.class, () -> m.setAvailability(false));
    }

    @Test
    void queueManagement() {
        Machine m = testMachine();
        m.enqueueJob(new JobId(1));
        m.enqueueJob(new JobId(2));
        assertEquals(2, m.queueDepth());

        JobId j = m.dequeueJob().orElseThrow();
        assertEquals(new JobId(1), j);
        assertEquals(1, m.queueDepth());

        j = m.dequeueJob().orElseThrow();
        assertEquals(new JobId(2), j);
        assertFalse(m.dequeueJob().isPresent());
    }

    @Test
    void onlineOfflineToggle() {
        Machine m = testMachine();
        assertEquals(MachineState.Idle, m.state());

        m.setAvailability(false);
        assertEquals(MachineState.Offline, m.state());

        m.setAvailability(true);
        assertEquals(MachineState.Idle, m.state());

        // Going online when already online is a no-op
        m.setAvailability(true);
        assertEquals(MachineState.Idle, m.state());
    }
}
