package com.arcogine.research.experiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.factory.model.FactoryModel;
import com.arcogine.factory.model.OperationStepDefinition;
import com.arcogine.factory.process.JobObservation;
import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeEventType;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.factory.process.RuntimeObservationMetadata;
import com.arcogine.research.experiment.LinearRoutingFamily.Resource;
import com.arcogine.research.experiment.LinearRoutingFamily.Step;
import com.arcogine.research.experiment.PostExecutionEvidenceDiscriminatorExperiment.Bundle;
import com.arcogine.research.experiment.PostExecutionEvidenceDiscriminatorExperiment.Call;
import com.arcogine.research.experiment.PostExecutionEvidenceDiscriminatorExperiment.Closure;
import com.arcogine.research.experiment.PostExecutionEvidenceDiscriminatorExperiment.Driver;
import com.arcogine.research.experiment.PostExecutionEvidenceDiscriminatorExperiment.Outcome;
import com.arcogine.types.JobStatus;
import com.arcogine.types.MachineId;
import com.arcogine.types.RunId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

/**
 * Reviewer probes for the adversarial review of the analytics ownership Phase 2 post-execution-evidence
 * revision. They call the author's research-local interval helper unchanged and ask whether it can
 * accept an insufficient claim (mixed runs, a contradicted closure), how strict its observation branch
 * is, and whether a one-observation snapshot differs from the interval classification. The reviewer
 * checks below are research-local illustrations, not a production evidence format or analytics API,
 * and evaluate no dispatch, ranking or cascade rule. Every expected value was registered by hand first.
 */
class PostExecutionEvidenceReviewProbe {

    private static final String LOG = "[analytics-post-execution-evidence-review] ";
    private static final long UNBOUNDED = Long.MAX_VALUE;

    private static final LinearRoutingFamily ONE_FIVE_TICK_STEP =
            new LinearRoutingFamily(List.of(new Step("WORK", 5)), List.of(Resource.of("M", 1, "WORK")));

    private static final LinearRoutingFamily MIX_THEN_WIDE_PACK = new LinearRoutingFamily(
            List.of(new Step("MIX", 4), new Step("PACK", 1)),
            List.of(Resource.of("Mixer", 1, "MIX"), Resource.of("Packer", 2, "PACK")));

    // ======================================================================== reviewer-local checks

    /** Every part of a bundle must belong to the bundle's run; equal fingerprints do not merge runs. */
    static Optional<String> sameRun(Bundle bundle) {
        RunId run = bundle.runId();
        if (!bundle.basis().metadata().runId().equals(run)) {
            return Optional.of("basis observation belongs to another run");
        }
        if (bundle.changes().stream().anyMatch(change -> !change.runId().equals(run))) {
            return Optional.of("a held change belongs to another run");
        }
        if (bundle.observations().stream().anyMatch(observation -> !observation.metadata().runId().equals(run))) {
            return Optional.of("a held observation belongs to another run");
        }
        if (bundle.closure().flatMap(Closure::bound).filter(bound -> !bound.metadata().runId().equals(run)).isPresent()) {
            return Optional.of("closure is bound to another run");
        }
        return Optional.empty();
    }

    /** A closure-based proof is refused when the holder holds a change after the binding but before {@code b}. */
    static Optional<String> closureContradiction(Bundle bundle, long b) {
        Optional<RuntimeObservation> bound = bundle.closure().flatMap(Closure::bound);
        if (bound.isEmpty()) {
            return Optional.empty();
        }
        long bindingCursor = bound.get().metadata().latestEventSequence();
        List<Long> contradicting = bundle.changes().stream()
                .filter(change -> change.sequence() > bindingCursor && change.simulationTime().value() < b)
                .map(RuntimeEventEnvelope::sequence)
                .toList();
        return contradicting.isEmpty()
                ? Optional.empty()
                : Optional.of("closure bound to cursor " + bindingCursor + " is contradicted by held changes "
                        + contradicting + " before " + b);
    }

    /** The author's {@code activeJobTicks}, preceded by the two reviewer consistency checks. */
    static Outcome<Long> checkedActiveJobTicks(Bundle bundle, MachineId machine, long a, long b) {
        Optional<String> mixed = sameRun(bundle);
        if (mixed.isPresent()) {
            return new Outcome.Refused<>(mixed.get());
        }
        Outcome<Long> outcome = PostExecutionEvidenceDiscriminatorExperiment.activeJobTicks(bundle, machine, a, b);
        if (outcome instanceof Outcome.Value<Long> value && value.proof().startsWith("controller closure")) {
            Optional<String> contradiction = closureContradiction(bundle, b);
            if (contradiction.isPresent()) {
                return new Outcome.Refused<>(contradiction.get());
            }
        }
        return outcome;
    }

    /**
     * Observation-time variant: because supported observation time is the time of the change at the
     * observation's cursor, a held prefix through {@code S-1} with every time below {@code b}, plus an
     * observation {@code (S, T >= b)}, proves {@code [a,b)} even when change {@code S} itself is not held.
     */
    static Outcome<Long> observationTimeWitnessActiveJobTicks(Bundle bundle, MachineId machine, long a, long b) {
        Outcome<Long> author = PostExecutionEvidenceDiscriminatorExperiment.activeJobTicks(bundle, machine, a, b);
        if (author instanceof Outcome.Value<Long>) {
            return author;
        }
        long t0 = bundle.basis().metadata().currentTime().value();
        long s0 = bundle.basis().metadata().latestEventSequence();
        if (t0 > a || sameRun(bundle).isPresent()) {
            return author;
        }
        Map<Long, RuntimeEventEnvelope> held = new TreeMap<>();
        bundle.changes().forEach(change -> held.put(change.sequence(), change));
        for (RuntimeObservation observation : bundle.observations()) {
            long cursor = observation.metadata().latestEventSequence();
            if (observation.metadata().currentTime().value() < b || cursor <= s0) {
                continue;
            }
            boolean prefixHeldBelowB = true;
            for (long sequence = s0 + 1; sequence < cursor; sequence++) {
                RuntimeEventEnvelope change = held.get(sequence);
                if (change == null || change.simulationTime().value() >= b) {
                    prefixHeldBelowB = false;
                    break;
                }
            }
            if (prefixHeldBelowB) {
                List<RuntimeEventEnvelope> prefix = held.values().stream()
                        .filter(change -> change.sequence() > s0 && change.sequence() < cursor)
                        .toList();
                return new Outcome.Value<>(integrateActive(bundle.basis(), prefix, machine, a, b),
                        "observation-time witness: cursor " + cursor + " at time "
                                + observation.metadata().currentTime().value() + ", prefix through " + (cursor - 1));
            }
        }
        return author;
    }

    private static long integrateActive(RuntimeObservation basis, List<RuntimeEventEnvelope> changes, MachineId machine,
            long a, long b) {
        long count = resource(basis, machine).activeJobIds().size();
        long cursorTime = a;
        long total = 0;
        for (RuntimeEventEnvelope change : changes) {
            long time = change.simulationTime().value();
            if (time >= b) {
                break;
            }
            if (time > cursorTime) {
                total += count * (time - cursorTime);
                cursorTime = time;
            }
            switch (change.payload()) {
                case RuntimeEventPayload.JobDispatched dispatched when dispatched.machineId().equals(machine) -> count++;
                case RuntimeEventPayload.JobStepCompleted completed when completed.machineId().equals(machine) -> count--;
                default -> { }
            }
        }
        return total + count * (b - cursorTime);
    }

    /** One-observation snapshot of a resource's idle slots: starved (capped by needing jobs) or no known work. */
    record Snapshot(long working, long idle, long needing, long starvedUncapped, long starvedCapped, long noKnownWork) {}

    static Snapshot snapshot(RuntimeObservation observation, FactoryModel model, MachineId machine) {
        ResourceObservation resource = resource(observation, machine);
        long working = resource.activeJobIds().size();
        long idle = resource.concurrency() - working;
        List<OperationStepDefinition> routing = model.operations().getFirst().steps();
        long needing = observation.jobs().stream()
                .filter(job -> job.status() != JobStatus.Completed)
                .filter(job -> {
                    int from = job.status() == JobStatus.InProgress ? job.currentStep() + 1 : job.currentStep();
                    for (int step = from; step < routing.size(); step++) {
                        if (routing.get(step).eligibleResources().contains(machine)) {
                            return true;
                        }
                    }
                    return false;
                })
                .count();
        return needing > 0
                ? new Snapshot(working, idle, needing, idle, Math.min(idle, needing), 0)
                : new Snapshot(working, idle, 0, 0, 0, idle);
    }

    // ======================================================================== R1 basis from another run

    @Test
    void r1ABasisFromAnotherRunIsAcceptedByTheHelper() {
        MachineId m = ONE_FIVE_TICK_STEP.resourceId("M");
        Driver x = new Driver(ONE_FIVE_TICK_STEP);
        x.availability(m, false);
        x.availability(m, true);
        x.drain();
        RuntimeObservation x2 = x.observe();
        assertEquals(2, x2.metadata().latestEventSequence());
        assertEquals(0, resource(x2, m).activeJobIds().size());
        x.submit(1);
        x.advance(100, UNBOUNDED);
        x.drain();
        RuntimeObservation xPost = x.observe();
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L, 6L), sequences(x.everything));
        assertEquals(List.of(0L, 0L, 0L, 0L, 5L, 5L), times(x.everything));

        Driver y = new Driver(ONE_FIVE_TICK_STEP);
        y.submit(1);
        y.drain();
        RuntimeObservation y2 = y.observe();
        assertEquals(2, y2.metadata().latestEventSequence());
        assertEquals(0, y2.metadata().currentTime().value());
        assertEquals(1, resource(y2, m).activeJobIds().size());
        assertEquals(x2.metadata().modelFingerprint(), y2.metadata().modelFingerprint());
        assertTrue(!x2.metadata().runId().equals(y2.metadata().runId()));

        RunId run = x.runtime.runId();
        Bundle own = new Bundle(run, x2, x.everything, List.of(xPost), Optional.empty());
        Bundle foreignBasis = new Bundle(run, y2, x.everything, List.of(xPost), Optional.empty());

        long r11 = value(PostExecutionEvidenceDiscriminatorExperiment.activeJobTicks(own, m, 0, 5));
        Outcome<Long> r12 = PostExecutionEvidenceDiscriminatorExperiment.activeJobTicks(foreignBasis, m, 0, 5);
        String r13 = refused(checkedActiveJobTicks(foreignBasis, m, 0, 5));
        long r13own = value(checkedActiveJobTicks(own, m, 0, 5));

        assertEquals(5, r11);
        assertEquals(10, value(r12), "the helper folds X's changes onto Y's basis");
        assertTrue(proof(r12).contains("producer witness: change 5"), proof(r12));
        assertTrue(r13.contains("basis observation belongs to another run"), r13);
        assertEquals(5, r13own);
        System.out.println(LOG + "R1: own basis [0,5)=" + r11 + "; foreign-run basis accepted=" + value(r12) + " under '"
                + proof(r12) + "'; same-run check refuses (" + r13 + ") and keeps " + r13own);
    }

    // ======================================================================== R2 contradicted closure

    @Test
    void r2AClosureContradictedByHeldChangesIsAcceptedByTheHelper() {
        MachineId m = ONE_FIVE_TICK_STEP.resourceId("M");
        Driver driver = new Driver(ONE_FIVE_TICK_STEP);
        RuntimeObservation basis = driver.observe();
        driver.submit(1);
        driver.drain();
        RuntimeObservation stale = driver.observe();
        driver.advance(100, UNBOUNDED);
        driver.drain();
        RuntimeObservation post = driver.observe();
        assertEquals(2, stale.metadata().latestEventSequence());
        assertEquals(4, post.metadata().latestEventSequence());
        assertEquals(5, post.metadata().currentTime().value());
        assertEquals(List.of(new Call(100, UNBOUNDED, 1, false)), driver.calls);

        Closure bound = driver.closure("closed script", 100);
        Closure misbound = new Closure("driver", "closed script", List.copyOf(driver.calls), 100, Optional.of(stale));
        Bundle bundle = new Bundle(driver.runtime.runId(), basis, driver.everything, List.of(basis, stale, post),
                Optional.of(bound));

        Outcome<Long> r21 = PostExecutionEvidenceDiscriminatorExperiment.activeJobTicks(bundle, m, 0, 100);
        Outcome<Long> r22 = PostExecutionEvidenceDiscriminatorExperiment.activeJobTicks(bundle.withClosure(misbound), m, 0, 100);
        String r23 = refused(checkedActiveJobTicks(bundle.withClosure(misbound), m, 0, 100));
        long r23bound = value(checkedActiveJobTicks(bundle, m, 0, 100));

        assertEquals(5, value(r21));
        assertTrue(proof(r21).contains("bound to (4,5)"), proof(r21));
        assertEquals(100, value(r22), "the helper ignores the held completion after the binding");
        assertTrue(proof(r22).contains("bound to (2,0)"), proof(r22));
        assertTrue(r23.contains("contradicted by held changes [3, 4]"), r23);
        assertEquals(5, r23bound);
        System.out.println(LOG + "R2: post-bound closure [0,100)=" + value(r21) + "; stale-bound closure accepted="
                + value(r22) + " under '" + proof(r22) + "'; contradiction check refuses (" + r23 + ") and keeps "
                + r23bound);
    }

    // ======================================================================== R3 observation-branch strictness

    @Test
    void r3TheObservationBranchIsConservativeByExactlyTheCursorChange() {
        MachineId mixer = MIX_THEN_WIDE_PACK.resourceId("Mixer");
        MachineId packer = MIX_THEN_WIDE_PACK.resourceId("Packer");
        Driver driver = new Driver(MIX_THEN_WIDE_PACK);
        RuntimeObservation basis = driver.observe();
        driver.submit(2);
        driver.advance(4, UNBOUNDED);
        List<RuntimeEventEnvelope> held = driver.drain();
        driver.advance(5, UNBOUNDED);
        List<RuntimeEventEnvelope> lost = driver.drain();
        RuntimeObservation atFive = driver.observe();
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L, 6L), sequences(held));
        assertEquals(List.of(0L, 0L, 0L, 4L, 4L, 4L), times(held));
        assertEquals(List.of(7L), sequences(lost));
        assertEquals(List.of(RuntimeEventType.JOB_STEP_COMPLETED), types(lost));
        assertEquals(List.of(5L), times(lost));
        assertEquals(7, atFive.metadata().latestEventSequence());
        assertEquals(5, atFive.metadata().currentTime().value());

        RunId run = driver.runtime.runId();
        Bundle holder = new Bundle(run, basis, held, List.of(basis, atFive), Optional.empty());
        Bundle complete = new Bundle(run, basis, driver.everything, List.of(basis, atFive), Optional.empty());

        String r31 = refused(PostExecutionEvidenceDiscriminatorExperiment.activeJobTicks(holder, mixer, 0, 5));
        String r32 = refused(PostExecutionEvidenceDiscriminatorExperiment.activeJobTicks(holder, packer, 0, 5));
        Outcome<Long> r33mixer = observationTimeWitnessActiveJobTicks(holder, mixer, 0, 5);
        Outcome<Long> r33packer = observationTimeWitnessActiveJobTicks(holder, packer, 0, 5);
        Outcome<Long> r34mixer = PostExecutionEvidenceDiscriminatorExperiment.activeJobTicks(complete, mixer, 0, 5);
        Outcome<Long> r34packer = PostExecutionEvidenceDiscriminatorExperiment.activeJobTicks(complete, packer, 0, 5);

        assertTrue(r31.contains("cursor 7") && r31.contains("[7]"), r31);
        assertTrue(r32.contains("cursor 7") && r32.contains("[7]"), r32);
        assertEquals(5, value(r33mixer));
        assertEquals(1, value(r33packer));
        assertTrue(proof(r33mixer).contains("observation-time witness"), proof(r33mixer));
        assertEquals(5, value(r34mixer));
        assertEquals(1, value(r34packer));
        assertTrue(proof(r34mixer).contains("producer witness: change 7"), proof(r34mixer));
        System.out.println(LOG + "R3: holder 1..6 + observation (7,5) refuses (" + r31 + "); observation-time variant Mixer "
                + value(r33mixer) + ", Packer " + value(r33packer) + "; complete holder Mixer " + value(r34mixer)
                + ", Packer " + value(r34packer));
    }

    // ======================================================================== R4 snapshot versus interval

    @Test
    void r4AOneObservationSnapshotIsNotTheIntervalClassification() {
        MachineId packer = MIX_THEN_WIDE_PACK.resourceId("Packer");
        MachineId mixer = MIX_THEN_WIDE_PACK.resourceId("Mixer");
        FactoryModel model = MIX_THEN_WIDE_PACK.model();

        Driver closed = new Driver(MIX_THEN_WIDE_PACK);
        closed.submit(1);
        closed.advance(100, UNBOUNDED);
        closed.drain();
        RuntimeObservation closedAtFive = closed.observe();

        Driver open = new Driver(MIX_THEN_WIDE_PACK);
        open.submit(1);
        open.advance(100, UNBOUNDED);
        open.drain();
        RuntimeObservation openAtFive = open.observe();
        open.submit(1);
        List<RuntimeEventEnvelope> added = open.drain();
        RuntimeObservation openAfter = open.observe();

        assertEquals(6, closedAtFive.metadata().latestEventSequence());
        assertEquals(5, closedAtFive.metadata().currentTime().value());
        assertEquals(normalized(closedAtFive), normalized(openAtFive));
        assertEquals(List.of(7L, 8L), sequences(added));
        assertEquals(List.of(5L, 5L), times(added));
        assertEquals(8, openAfter.metadata().latestEventSequence());
        JobObservation second = openAfter.jobs().stream()
                .filter(job -> job.status() != JobStatus.Completed)
                .findFirst()
                .orElseThrow();
        assertEquals(JobStatus.InProgress, second.status());
        assertEquals(0, second.currentStep());
        assertEquals(mixer, second.currentMachineId());

        Snapshot r41closed = snapshot(closedAtFive, model, packer);
        Snapshot r41open = snapshot(openAtFive, model, packer);
        Snapshot r42 = snapshot(openAfter, model, packer);
        assertEquals(new Snapshot(0, 2, 0, 0, 0, 2), r41closed);
        assertEquals(r41closed, r41open);
        assertEquals(new Snapshot(0, 2, 1, 2, 1, 0), r42);
        System.out.println(LOG + "R4: snapshot at (6,5) closed=" + r41closed + ", open=" + r41open
                + "; open after second submission (8,5)=" + r42);
    }

    // ======================================================================== helpers

    @SuppressWarnings("unchecked")
    private static <T> T value(Outcome<T> outcome) {
        return ((Outcome.Value<T>) assertInstanceOf(Outcome.Value.class, outcome,
                () -> "expected a value, got " + outcome)).value();
    }

    private static String proof(Outcome<?> outcome) {
        return assertInstanceOf(Outcome.Value.class, outcome, () -> "expected a value, got " + outcome).proof();
    }

    private static String refused(Outcome<?> outcome) {
        return assertInstanceOf(Outcome.Refused.class, outcome, () -> "expected a refusal, got " + outcome).reason();
    }

    private static ResourceObservation resource(RuntimeObservation observation, MachineId machine) {
        return observation.resources().stream().filter(r -> r.machineId().equals(machine)).findFirst().orElseThrow();
    }

    private static List<Long> sequences(List<RuntimeEventEnvelope> changes) {
        return changes.stream().map(RuntimeEventEnvelope::sequence).toList();
    }

    private static List<Long> times(List<RuntimeEventEnvelope> changes) {
        return changes.stream().map(change -> change.simulationTime().value()).toList();
    }

    private static List<RuntimeEventType> types(List<RuntimeEventEnvelope> changes) {
        return changes.stream().map(RuntimeEventEnvelope::eventType).toList();
    }

    private static RuntimeObservation normalized(RuntimeObservation observation) {
        RuntimeObservationMetadata metadata = observation.metadata();
        return new RuntimeObservation(new RuntimeObservationMetadata(ExperimentEvidence.NORMALIZED_RUN_ID,
                metadata.modelFingerprint(), metadata.currentTime(), metadata.runState(), metadata.latestEventSequence()),
                observation.resources(), observation.orders(), observation.jobs(), observation.pendingWork(),
                observation.performance());
    }
}
