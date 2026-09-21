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
        Objects.requireNonNull(reference, "reference");
        Key key = new Key(reference.sourceIdentity(), reference.revisionIdentity());
        EvidenceReference existing = references.get(key);
        if (existing != null) {
            if (!existing.provenance().equals(reference.provenance())
                    || !existing.relationOptional().equals(reference.relationOptional())) {
                throw new IllegalArgumentException("evidence reference would be rebound: " + reference);
            }
            return existing;
        }
        references.put(key, reference);
        return reference;
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
