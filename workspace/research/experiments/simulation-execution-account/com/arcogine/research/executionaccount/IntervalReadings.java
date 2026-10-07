package com.arcogine.research.executionaccount;

import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;

/**
 * Research-local readings over an account's interval. Each one is a stated definition applied to
 * the same account, not an Engine fact, a game analytic, or a proposed production formula. They
 * exist to test which evidence an interval claim needs and to show that different definitions over
 * one unchanged account legitimately disagree.
 */
final class IntervalReadings {

    private IntervalReadings() {}

    /**
     * {@code active-job-ticks}: for each tick of {@code [a, b)}, the number of jobs running on
     * {@code machine}, summed. Read from the account's state basis plus its events; refused unless
     * the account determines the interval.
     */
    static long activeJobTicks(CapturedAccount account, MachineId machine, long a, long b) {
        return integrate(account, machine, a, b, count -> count);
    }

    /**
     * {@code busy-ticks (at least one job)}: the number of ticks of {@code [a, b)} during which at
     * least one job ran on {@code machine}. A different, equally legitimate definition.
     */
    static long ticksWithAnyJob(CapturedAccount account, MachineId machine, long a, long b) {
        return integrate(account, machine, a, b, count -> count > 0 ? 1 : 0);
    }

    private static long integrate(CapturedAccount account, MachineId machine, long a, long b, IntUnaryOperator weight) {
        CapturedAccount.Determinacy determinacy = account.intervalDeterminacy(a, b);
        if (!determinacy.determined()) {
            throw new IllegalStateException("refused: " + determinacy.reason());
        }
        PlacementFold fold = PlacementFold.from(account.basis());
        List<RuntimeEventEnvelope> events = account.eventsBefore(b);
        int i = 0;
        while (i < events.size() && events.get(i).simulationTime().value() <= a) {
            fold.apply(events.get(i++));
        }
        long total = 0;
        long previous = a;
        while (i < events.size()) {
            long t = events.get(i).simulationTime().value();
            total += (t - previous) * weight.applyAsInt(fold.activeCount(machine));
            previous = t;
            while (i < events.size() && events.get(i).simulationTime().value() == t) {
                fold.apply(events.get(i++));
            }
        }
        return total + (b - previous) * weight.applyAsInt(fold.activeCount(machine));
    }

    /**
     * A naive consumer's reading that ignores coverage: pair each dispatch with the matching step
     * completion it happens to hold, count an unmatched dispatch until {@code boundary}, and drop an
     * unmatched completion. This is the shape a gap-blind or late-blind consumer would compute.
     */
    static long naivePairedTicks(List<RuntimeEventEnvelope> events, MachineId machine, long boundary) {
        record StepKey(JobId job, int step) {}
        Map<StepKey, Long> open = new HashMap<>();
        long total = 0;
        for (RuntimeEventEnvelope event : events) {
            switch (event.payload()) {
                case RuntimeEventPayload.JobDispatched d when d.machineId().equals(machine) ->
                    open.put(new StepKey(d.jobId(), d.stepIndex()), event.simulationTime().value());
                case RuntimeEventPayload.JobStepCompleted c when c.machineId().equals(machine) -> {
                    Long start = open.remove(new StepKey(c.jobId(), c.stepIndex()));
                    if (start != null) {
                        total += event.simulationTime().value() - start;
                    }
                }
                default -> { }
            }
        }
        for (long start : open.values()) {
            total += boundary - start;
        }
        return total;
    }
}
