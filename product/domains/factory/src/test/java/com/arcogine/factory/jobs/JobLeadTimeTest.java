package com.arcogine.factory.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.orders.Order;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.OrderId;
import com.arcogine.types.ProductId;
import com.arcogine.types.SimTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class JobLeadTimeTest {

    @Test
    void leadTimeIsUnavailableUntilTheLastStepCompletes() {
        Order order = new Order(new OrderId(1), new ProductId(1), 1, SimTime.of(3), 10.0);
        Job job = new Job(new JobId(1), order, 1, 2, SimTime.of(3));

        assertTrue(job.leadTime().isEmpty());
        job.start(new MachineId(1));
        job.completeStep(SimTime.of(5));
        assertTrue(job.leadTime().isEmpty());
        job.start(new MachineId(1));
        job.completeStep(SimTime.of(11));
        assertEquals(Optional.of(8L), job.leadTime());
    }
}
