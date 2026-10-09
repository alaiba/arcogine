package com.arcogine.research.executionaccount.adaptation;

import com.arcogine.factory.process.ResourceObservation;
import com.arcogine.factory.process.RuntimeEventEnvelope;
import com.arcogine.factory.process.RuntimeEventPayload;
import com.arcogine.factory.process.RuntimeObservation;
import com.arcogine.research.experiment.SequenceRange;
import com.arcogine.types.JobId;
import com.arcogine.types.MachineId;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.RunId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Research-local capture holder whose interval licences are bound to the evidence that proves them.
 * It is the holder half of the qualified closure protocol and is not a proposed production type.
 *
 * <p>Licensing {@code [a, b)} needs a state basis at or before {@code a} and gap-free coverage up to
 * a <em>proof sequence</em>, which is one of:
 *
 * <ul>
 *   <li><b>producer witness</b> — the first retained supported change with time {@code >= b} (every
 *       change with time below {@code b} precedes it, because supported time never decreases along
 *       sequence and later changes are stamped at or after the command clock), or a frontier
 *       observation whose supported time is {@code >= b};
 *   <li><b>controller attestation</b> — a {@link ControllerClosure.Attestation} of this run whose
 *       bound covers {@code b}; coverage must reach its bound sequence, so a holder that has not yet
 *       received the tail cannot be licensed by it, and the licence records the attestor's trust.
 * </ul>
 *
 * <p>Coverage is evaluated per claim, only up to the claim's proof sequence, so a later capture loss
 * never erases an earlier interval whose proof precedes it.
 */
final class HeldAccount {

    /** A delivered supported change that falls inside an attested interval after its bound. */
    record Contradiction(ControllerClosure.Attestation attestation, String message) {}

    enum Proof {
        PRODUCER_SUPPORTED_TIME,
        CONTROLLER_ATTESTATION
    }

    sealed interface License {

        record Licensed(Proof proof, long proofSequence, Optional<String> trustedAttestor) implements License {}

        record Refused(String reason) implements License {}
    }

    private final RunId runId;
    private final ModelFingerprint fingerprint;
    private final RuntimeObservation basis;
    private final List<RuntimeEventEnvelope> retained = new ArrayList<>();
    private final List<ControllerClosure.Attestation> attestations = new ArrayList<>();
    private final List<Contradiction> contradictions = new ArrayList<>();
    private RuntimeObservation frontier;

    private HeldAccount(RuntimeObservation basis) {
        this.basis = Objects.requireNonNull(basis, "basis");
        this.runId = basis.metadata().runId();
        this.fingerprint = basis.metadata().modelFingerprint();
        this.frontier = basis;
    }

    static HeldAccount from(RuntimeObservation basis) {
        return new HeldAccount(basis);
    }

    RuntimeObservation basis() {
        return basis;
    }

    List<String> contradictions() {
        return contradictions.stream().map(Contradiction::message).toList();
    }

    /** Retains drained events of this run; a delivered change that contradicts an attestation is recorded. */
    void retain(List<RuntimeEventEnvelope> drained) {
        for (RuntimeEventEnvelope event : drained) {
            if (!event.runId().equals(runId) || !event.modelFingerprint().equals(fingerprint)) {
                throw new IllegalArgumentException("event " + event.sequence() + " belongs to another run or model");
            }
            if (event.sequence() <= basisCursor()) {
                continue;
            }
            if (!retained.isEmpty() && event.sequence() <= retained.getLast().sequence()) {
                throw new IllegalArgumentException("event " + event.sequence() + " does not follow "
                        + retained.getLast().sequence());
            }
            for (ControllerClosure.Attestation attestation : attestations) {
                checkAgainst(attestation, event);
            }
            retained.add(event);
        }
    }

    void observeFrontier(RuntimeObservation observation) {
        if (!observation.metadata().runId().equals(runId)) {
            throw new IllegalArgumentException("frontier observation belongs to another run");
        }
        if (observation.metadata().latestEventSequence() < frontier.metadata().latestEventSequence()) {
            throw new IllegalArgumentException("a frontier never moves backwards");
        }
        frontier = observation;
    }

    /** Accepts a controller attestation for this run. An attestation of another run is refused. */
    void accept(ControllerClosure.Attestation attestation) {
        if (!attestation.runId().equals(runId)) {
            throw new IllegalArgumentException("attestation of run " + attestation.runId() + " does not apply to run "
                    + runId);
        }
        attestations.add(attestation);
        retained.forEach(event -> checkAgainst(attestation, event));
    }

    /** Sequences in {@code (basisCursor, through]} this holder does not retain. */
    List<SequenceRange> gapsThrough(long through) {
        List<SequenceRange> gaps = new ArrayList<>();
        long next = basisCursor() + 1;
        for (RuntimeEventEnvelope event : retained) {
            if (event.sequence() > through) {
                break;
            }
            if (event.sequence() > next) {
                gaps.add(new SequenceRange(next, event.sequence() - 1));
            }
            next = event.sequence() + 1;
        }
        if (next <= through) {
            gaps.add(new SequenceRange(next, through));
        }
        return List.copyOf(gaps);
    }

    License license(long a, long b) {
        if (b <= a) {
            throw new IllegalArgumentException("empty interval [" + a + ", " + b + ")");
        }
        long basisTime = basis.metadata().currentTime().value();
        if (basisTime > a) {
            return new License.Refused("no state basis at " + a + ": the basis is at time " + basisTime);
        }
        Optional<RuntimeEventEnvelope> witness = retained.stream()
                .filter(event -> event.simulationTime().value() >= b)
                .findFirst();
        if (witness.isPresent() && gapsThrough(witness.get().sequence() - 1).isEmpty()) {
            return new License.Licensed(Proof.PRODUCER_SUPPORTED_TIME, witness.get().sequence() - 1, Optional.empty());
        }
        long frontierCursor = frontier.metadata().latestEventSequence();
        if (frontier.metadata().currentTime().value() >= b && gapsThrough(frontierCursor).isEmpty()) {
            return new License.Licensed(Proof.PRODUCER_SUPPORTED_TIME, frontierCursor, Optional.empty());
        }
        List<ControllerClosure.Attestation> covering = attestations.stream()
                .filter(attestation -> attestation.closedBefore() >= b)
                .sorted(Comparator.comparingLong(ControllerClosure.Attestation::boundSequence))
                .toList();
        List<String> reasons = new ArrayList<>();
        for (ControllerClosure.Attestation attestation : covering) {
            List<SequenceRange> gaps = gapsThrough(attestation.boundSequence());
            if (!gaps.isEmpty()) {
                reasons.add("attestation by " + attestation.attestor() + " is bound to sequence "
                        + attestation.boundSequence() + " but supported events " + gaps + " are not held");
            } else if (contradictions.stream().anyMatch(c -> c.attestation().equals(attestation))) {
                reasons.add("attestation by " + attestation.attestor() + " is contradicted by a delivered change");
            } else {
                return new License.Licensed(
                        Proof.CONTROLLER_ATTESTATION, attestation.boundSequence(), Optional.of(attestation.attestor()));
            }
        }
        return new License.Refused("interval still open: no held supported change or frontier at time >= " + b
                + " with gap-free coverage" + (reasons.isEmpty() ? ", and no covering attestation" : "; " + reasons)
                + "; gaps through frontier " + gapsThrough(frontierCursor));
    }

    /**
     * {@code active-job-ticks} on {@code machine} over a licensed {@code [a, b)}: the basis's active
     * jobs folded with retained changes up to the licence's proof sequence. A research-local reading.
     */
    long activeJobTicks(MachineId machine, long a, long b) {
        if (!(license(a, b) instanceof License.Licensed licensed)) {
            throw new IllegalStateException("refused: " + license(a, b));
        }
        Set<JobId> active = new HashSet<>();
        basis.resources().stream()
                .filter(resource -> resource.machineId().equals(machine))
                .map(ResourceObservation::activeJobIds)
                .forEach(active::addAll);
        long total = 0;
        long cursor = a;
        for (RuntimeEventEnvelope event : retained) {
            long time = event.simulationTime().value();
            if (event.sequence() > licensed.proofSequence() || time >= b) {
                break;
            }
            long at = Math.max(time, a);
            total += (at - cursor) * active.size();
            cursor = at;
            switch (event.payload()) {
                case RuntimeEventPayload.JobDispatched d when d.machineId().equals(machine) -> active.add(d.jobId());
                case RuntimeEventPayload.JobStepCompleted c when c.machineId().equals(machine) -> active.remove(c.jobId());
                default -> { }
            }
        }
        return total + (b - cursor) * active.size();
    }

    private long basisCursor() {
        return basis.metadata().latestEventSequence();
    }

    private void checkAgainst(ControllerClosure.Attestation attestation, RuntimeEventEnvelope event) {
        if (event.sequence() > attestation.boundSequence()
                && event.simulationTime().value() < attestation.closedBefore()) {
            contradictions.add(new Contradiction(attestation, "event " + event.sequence() + " at time "
                    + event.simulationTime().value() + " follows bound sequence " + attestation.boundSequence()
                    + " inside the attested interval by " + attestation.attestor()));
        }
    }
}
