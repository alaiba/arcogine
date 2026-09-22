package com.arcogine.governance.evidence;

import com.arcogine.governance.ControlledRevision;
import java.util.Objects;
import java.util.Optional;

/**
 * An immutable reference to one attributable source or result revision.
 *
 * <p>The source and revision strings are opaque producer-owned handles. They are not a universal
 * URI or identifier grammar. Equality uses only that complete source/revision identity; payload,
 * subject, provenance annotations, and use context are not equality.
 */
public final class EvidenceReference {

    private final String sourceIdentity;
    private final String revisionIdentity;
    private final EvidenceProvenance provenance;
    private final Optional<EvidenceRelation> relation;

    public EvidenceReference(
            String sourceIdentity, String revisionIdentity, EvidenceProvenance provenance) {
        this(sourceIdentity, revisionIdentity, provenance, Optional.empty());
    }

    public EvidenceReference(
            String sourceIdentity,
            String revisionIdentity,
            EvidenceProvenance provenance,
            Optional<EvidenceRelation> relation) {
        this.sourceIdentity = requireText(sourceIdentity, "sourceIdentity");
        this.revisionIdentity = requireText(revisionIdentity, "revisionIdentity");
        this.provenance = Objects.requireNonNull(provenance, "provenance");
        this.relation = Objects.requireNonNull(relation, "relation");
    }

    public String sourceIdentity() {
        return sourceIdentity;
    }

    public String revisionIdentity() {
        return revisionIdentity;
    }

    public EvidenceProvenance provenance() {
        return provenance;
    }

    public Optional<EvidenceRelation> relationOptional() {
        return relation;
    }

    public Optional<EvidenceRelation> relation() {
        return relation;
    }

    /** Uses the producer-owned controlled-revision identity as the evidence reference. */
    public static EvidenceReference forControlledRevision(ControlledRevision revision) {
        Objects.requireNonNull(revision, "revision");
        return new EvidenceReference(
                "Arcogine controlled revision",
                revision.id().toString(),
                EvidenceProvenance.structural(revision.modelFingerprint(), revision.id()));
    }

    /** Returns a new attributable revision related to an earlier reference. */
    public static EvidenceReference relatedRevision(
            String sourceIdentity,
            String revisionIdentity,
            EvidenceProvenance provenance,
            EvidenceRelationKind relationKind,
            EvidenceReference earlier,
            String explanation) {
        return new EvidenceReference(
                sourceIdentity,
                revisionIdentity,
                provenance,
                Optional.of(
                        new EvidenceRelation(
                                relationKind,
                                Objects.requireNonNull(earlier, "earlier"),
                                explanation)));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EvidenceReference reference
                && sourceIdentity.equals(reference.sourceIdentity)
                && revisionIdentity.equals(reference.revisionIdentity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceIdentity, revisionIdentity);
    }

    @Override
    public String toString() {
        return sourceIdentity + "@" + revisionIdentity;
    }

    private static String requireText(String value, String field) {
        if (Objects.requireNonNull(value, field).isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
