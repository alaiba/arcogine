package com.arcogine.governance.conformance;

import com.arcogine.governance.ControlledRevisionAuthority;
import com.arcogine.governance.HistoricalRevision;
import com.arcogine.governance.evidence.EvidenceProvenance;
import com.arcogine.governance.evidence.EvidenceReferenceAuthority;
import com.arcogine.governance.evidence.EvidenceUse;
import com.arcogine.governance.evidence.InMemoryEvidenceReferenceAuthority;
import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
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
    private final EvidenceReferenceAuthority evidenceReferenceAuthority;
    private final Clock clock;
    private final Map<EvaluationOccurrenceId, EvaluationOccurrence> occurrences = new LinkedHashMap<>();

    public InMemoryEvaluationOccurrenceAuthority() {
        this(null, new InMemoryEvidenceReferenceAuthority(), Clock.systemUTC());
    }

    public InMemoryEvaluationOccurrenceAuthority(ControlledRevisionAuthority revisionAuthority) {
        this(revisionAuthority, new InMemoryEvidenceReferenceAuthority(), Clock.systemUTC());
    }

    public InMemoryEvaluationOccurrenceAuthority(EvidenceReferenceAuthority evidenceReferenceAuthority) {
        this(null, evidenceReferenceAuthority, Clock.systemUTC());
    }

    public InMemoryEvaluationOccurrenceAuthority(
            ControlledRevisionAuthority revisionAuthority,
            EvidenceReferenceAuthority evidenceReferenceAuthority) {
        this(revisionAuthority, evidenceReferenceAuthority, Clock.systemUTC());
    }

    InMemoryEvaluationOccurrenceAuthority(
            ControlledRevisionAuthority revisionAuthority,
            EvidenceReferenceAuthority evidenceReferenceAuthority,
            Clock clock) {
        this.revisionAuthority = revisionAuthority;
        this.evidenceReferenceAuthority = Objects.requireNonNull(
                evidenceReferenceAuthority, "evidenceReferenceAuthority");
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
        verifyOutcomeBasis(candidate);
        verifyRelatedOccurrence(candidate);
        verifyEvidenceReferences(candidate);
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

    private void verifyOutcomeBasis(EvaluationOccurrenceDraft candidate) {
        if (candidate.assertion().requiresExternalEvidence()
                && (candidate.evaluation().result() == ConformanceResult.PASS
                        || candidate.evaluation().result() == ConformanceResult.FAIL)
                && candidate.reliedOnUses().stream().noneMatch(EvidenceUse::isReliedOn)) {
            throw new IllegalArgumentException(
                    "an external-evidence PASS or FAIL requires an adequate relied-on evidence use");
        }
    }

    private void verifyRelatedOccurrence(EvaluationOccurrenceDraft candidate) {
        candidate.relatedOccurrenceIdOptional().ifPresent(relatedId -> {
            if (relatedId.equals(candidate.id())) {
                throw new IllegalArgumentException("an occurrence cannot relate to itself");
            }
            if (!occurrences.containsKey(relatedId)) {
                throw new IllegalArgumentException("related occurrence is not accepted: " + relatedId);
            }
        });
    }

    private void verifyEvidenceReferences(EvaluationOccurrenceDraft candidate) {
        List<EvidenceUse> uses = new ArrayList<>();
        uses.addAll(candidate.reliedOnUses());
        uses.addAll(candidate.consideredButExcludedUses());
        for (EvidenceUse use : uses) {
            verifyProducerRevision(use.evidence().provenance());
            evidenceReferenceAuthority.record(use.evidence());
        }
    }

    private void verifyProducerRevision(EvidenceProvenance provenance) {
        if (provenance.producerControlledRevision().isEmpty()) {
            return;
        }
        if (revisionAuthority == null) {
            throw new IllegalArgumentException(
                    "evidence producer revision requires an authority for occurrence acceptance");
        }
        ControlledRevisionId producerRevisionId = provenance.producerControlledRevision().orElseThrow();
        HistoricalRevision historical = revisionAuthority.resolve(producerRevisionId);
        ModelFingerprint producerFingerprint = provenance.producerModelFingerprint().orElseThrow(() ->
                new IllegalArgumentException("evidence producer revision requires a producer fingerprint"));
        if (!historical.artifact().fingerprint().equals(producerFingerprint)) {
            throw new IllegalArgumentException("evidence producer revision is bound to a different model fingerprint");
        }
    }
}
