package com.arcogine.governance.conformance;

import com.arcogine.governance.ControlledRevisionAuthority;
import com.arcogine.governance.HistoricalRevision;
import com.arcogine.governance.evidence.EvidenceUse;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * In-memory occurrence authority for the first headless evidence capability.
 *
 * <p>It provides explicit acceptance and immutable lookup only; it does not claim durable
 * evaluation-history persistence. When supplied, the controlled-revision authority verifies every
 * occurrence revision binding before acceptance.
 */
public final class InMemoryEvaluationOccurrenceAuthority implements EvaluationOccurrenceAuthority {

    private final ControlledRevisionAuthority revisionAuthority;
    private final Clock clock;
    private final Map<EvaluationOccurrenceId, EvaluationOccurrence> occurrences = new LinkedHashMap<>();

    public InMemoryEvaluationOccurrenceAuthority() {
        this(null, Clock.systemUTC());
    }

    public InMemoryEvaluationOccurrenceAuthority(ControlledRevisionAuthority revisionAuthority) {
        this(revisionAuthority, Clock.systemUTC());
    }

    InMemoryEvaluationOccurrenceAuthority(ControlledRevisionAuthority revisionAuthority, Clock clock) {
        this.revisionAuthority = revisionAuthority;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public synchronized EvaluationOccurrence accept(EvaluationOccurrenceDraft candidate) {
        Objects.requireNonNull(candidate, "candidate");
        if (occurrences.containsKey(candidate.id())) {
            throw new IllegalArgumentException("evaluation occurrence ID already accepted: " + candidate.id());
        }
        verifyRevision(candidate);
        verifyUseRevisions(candidate);
        EvaluationOccurrence accepted = new EvaluationOccurrence(candidate, clock.instant());
        occurrences.put(accepted.id(), accepted);
        return accepted;
    }

    @Override
    public synchronized Optional<EvaluationOccurrence> findById(EvaluationOccurrenceId id) {
        return Optional.ofNullable(occurrences.get(Objects.requireNonNull(id, "id")));
    }

    @Override
    public synchronized EvaluationOccurrence resolve(EvaluationOccurrenceId id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException("unknown evaluation occurrence: " + id));
    }

    @Override
    public synchronized List<EvaluationOccurrence> occurrences() {
        return List.copyOf(new ArrayList<>(occurrences.values()));
    }

    private void verifyRevision(EvaluationOccurrenceDraft candidate) {
        if (candidate.controlledRevisionId() == null) {
            return;
        }
        if (revisionAuthority == null) {
            throw new IllegalArgumentException(
                    "a controlled revision requires an authority for occurrence acceptance");
        }
        HistoricalRevision historical = revisionAuthority.resolve(candidate.controlledRevisionId());
        if (!historical.artifact().fingerprint().equals(candidate.modelFingerprint())) {
            throw new IllegalArgumentException("occurrence revision is bound to a different model fingerprint");
        }
    }

    private void verifyUseRevisions(EvaluationOccurrenceDraft candidate) {
        List<EvidenceUse> uses = new ArrayList<>();
        uses.addAll(candidate.reliedOnUses());
        uses.addAll(candidate.consideredButExcludedUses());
        for (EvidenceUse use : uses) {
            if (use.targetControlledRevision() == null) {
                continue;
            }
            if (revisionAuthority == null) {
                throw new IllegalArgumentException(
                        "an evidence-use controlled revision requires an authority for occurrence acceptance");
            }
            HistoricalRevision historical = revisionAuthority.resolve(use.targetControlledRevision());
            if (!historical.artifact().fingerprint().equals(use.targetModelFingerprint())) {
                throw new IllegalArgumentException("evidence-use revision is bound to a different model fingerprint");
            }
        }
    }
}
