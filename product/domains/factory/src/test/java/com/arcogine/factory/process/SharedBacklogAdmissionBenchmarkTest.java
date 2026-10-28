package com.arcogine.factory.process;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.arcogine.factory.model.ConfiguredResource;
import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.FactoryModelPublisher;
import com.arcogine.factory.model.OperationDefinition;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.model.ProductDefinition;
import com.arcogine.types.MachineId;
import com.arcogine.types.ProductId;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Non-functional benchmark for admitting a growing flexible (multi-eligible) backlog.
 *
 * <p>Admission ranks every submitted job's eligible machines against the shared pending backlog,
 * so its cost is the observable consequence of how that backlog is indexed. The test records
 * host-dependent admission timings across increasing backlog sizes and asserts only the
 * deterministic structure of the result; it is not a wall-clock performance contract.
 */
class SharedBacklogAdmissionBenchmarkTest {
    private static final int[] BACKLOG_SIZES = {1_000, 2_000, 4_000, 8_000, 16_000};

    private static FactoryRuntime runtime() {
        Set<MachineId> eligible = Set.of(new MachineId(1), new MachineId(2));
        FactoryModel model = new FactoryModel(
                List.of(
                        new ConfiguredResource(new MachineId(1), "M1", 1, null, 0),
                        new ConfiguredResource(new MachineId(2), "M2", 1, null, 0)),
                List.of(new OperationDefinition(
                        1, "Route", List.of(new OperationStepDefinition(1, "FLEX", eligible, 1)))),
                List.of(new ProductDefinition(new ProductId(1), "Widget", 1)));
        return FactoryRuntime.forModel(FactoryModelPublisher.publish(model));
    }

    @Test
    void flexibleBacklogAdmissionIsDeterministicAcrossIncreasingSizes() {
        for (int size : BACKLOG_SIZES) {
            FactoryRuntime runtime = runtime();

            long started = System.nanoTime();
            runtime.submitWorkload(new ProductId(1), size, 1.0).orElseThrow();
            long admissionNanos = System.nanoTime() - started;

            // Two jobs start immediately (one per single-slot machine); every other job waits in
            // the shared backlog rather than being bound to either machine.
            assertEquals(size - 2, runtime.pendingWorkView().size());
            assertEquals(0, runtime.machinesView().stream().mapToLong(m -> m.queueDepth()).sum());

            System.out.printf(
                    Locale.ROOT,
                    "Shared-backlog admission benchmark: flexibleBacklog=%d admissionMs=%.3f%n",
                    size - 2,
                    admissionNanos / 1_000_000.0);
        }
    }
}
