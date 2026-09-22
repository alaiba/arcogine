package com.arcogine.governance.evidence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * In-memory evidence-reference authority for fixtures and headless tests.
 *
 * <p>This is not a durable evidence store. Its important boundary is that redelivery resolves to
 * one reference and a same-key reference with changed intrinsic provenance is rejected.
 */
public final class InMemoryEvidenceReferenceAuthority implements EvidenceReferenceAuthority {

    private final Map<Key, EvidenceReference> references = new LinkedHashMap<>();

    @Override
    public synchronized EvidenceReference record(EvidenceReference reference) {
        EvidenceReference canonical = canonicalizeRelation(Objects.requireNonNull(reference, "reference"));
        Key key = new Key(canonical.sourceIdentity(), canonical.revisionIdentity());
        EvidenceReference existing = references.get(key);
        if (existing != null) {
            if (!existing.provenance().equals(canonical.provenance())
                    || !existing.relationOptional().equals(canonical.relationOptional())) {
                throw new IllegalArgumentException("evidence reference would be rebound: " + canonical);
            }
            return existing;
        }
        references.put(key, canonical);
        return canonical;
    }

    private EvidenceReference canonicalizeRelation(EvidenceReference reference) {
        if (reference.relationOptional().isEmpty()) {
            return reference;
        }
        EvidenceRelation relation = reference.relationOptional().orElseThrow();
        EvidenceReference canonicalEarlier = record(relation.earlier());
        if (canonicalEarlier == relation.earlier()) {
            return reference;
        }
        return new EvidenceReference(
                reference.sourceIdentity(),
                reference.revisionIdentity(),
                reference.provenance(),
                Optional.of(new EvidenceRelation(
                        relation.kind(), canonicalEarlier, relation.explanation())));
    }

    @Override
    public synchronized Optional<EvidenceReference> find(String sourceIdentity, String revisionIdentity) {
        return Optional.ofNullable(references.get(new Key(sourceIdentity, revisionIdentity)));
    }

    @Override
    public synchronized List<EvidenceReference> references() {
        return List.copyOf(new ArrayList<>(references.values()));
    }

    private record Key(String sourceIdentity, String revisionIdentity) {
        private Key {
            Objects.requireNonNull(sourceIdentity, "sourceIdentity");
            Objects.requireNonNull(revisionIdentity, "revisionIdentity");
        }
    }
}
