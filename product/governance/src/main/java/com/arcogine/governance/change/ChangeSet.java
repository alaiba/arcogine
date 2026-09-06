package com.arcogine.governance.change;

import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable governance record of the semantic transition between an authoritative base state and
 * a candidate state.
 *
 * <p>A {@code ChangeSet} is not revision identity, authorization, approval, deployment,
 * conformance, evidence, a workflow object, a generic source-control commit, or a JSON patch. It
 * associates:
 *
 * <ul>
 *   <li>the base's revision identity and semantic-content identity ({@link #baseRevisionId()} /
 *       {@link #baseFingerprint()});
 *   <li>the candidate's semantic-content identity ({@link #candidateFingerprint()}), which may or
 *       may not yet correspond to an authoritative controlled revision;
 *   <li>the resulting controlled revision, once the candidate has actually been persisted through
 *       the persistence-acceptance boundary ({@link #resultingRevisionId()} -- absent for a
 *       not-yet-accepted candidate snapshot, per ADR-0008: a {@code ChangeSet} never
 *       fabricates a synthetic {@link ControlledRevisionId} for an unaccepted candidate);
 *   <li>the classified {@link SemanticChange}s and the derived {@link ImpactScope};
 *   <li>{@link ChangeProvenance} (source, reason, optional external change-request reference).
 * </ul>
 *
 * <p>Semantic changes are stored in a deterministic order (by entity identity, then kind, then
 * detail) regardless of the order supplied at construction, so equivalent comparisons always
 * produce equal {@code ChangeSet} content.
 */
public record ChangeSet(
        ControlledRevisionId baseRevisionId,
        ModelFingerprint baseFingerprint,
        ModelFingerprint candidateFingerprint,
        ControlledRevisionId resultingRevisionId,
        List<SemanticChange> semanticChanges,
        ImpactScope impactScope,
        ChangeProvenance provenance) {

    public ChangeSet {
        Objects.requireNonNull(baseRevisionId, "baseRevisionId");
        Objects.requireNonNull(baseFingerprint, "baseFingerprint");
        Objects.requireNonNull(candidateFingerprint, "candidateFingerprint");
        Objects.requireNonNull(semanticChanges, "semanticChanges");
        Objects.requireNonNull(provenance, "provenance");

        List<SemanticChange> ordered = new ArrayList<>(semanticChanges);
        ordered.sort(
                Comparator.<SemanticChange, ChangedEntityRef>comparing(
                                SemanticChange::entity, ChangedEntityRef.identityComparator())
                        .thenComparing(c -> c.kind().name())
                        .thenComparing(SemanticChange::detail));
        semanticChanges = List.copyOf(ordered);
        impactScope = ImpactScope.of(semanticChanges);
    }

    /** The controlled revision the candidate resolved from, once persisted; empty otherwise. */
    public Optional<ControlledRevisionId> resultingRevisionIdOptional() {
        return Optional.ofNullable(resultingRevisionId);
    }

    /** Whether this transition is semantically a no-op (e.g. rollback to an earlier fingerprint). */
    public boolean isSemanticNoOp() {
        return semanticChanges.isEmpty();
    }
}
