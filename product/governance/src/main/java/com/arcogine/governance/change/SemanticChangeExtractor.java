package com.arcogine.governance.change;

import com.arcogine.governance.SemanticArtifact;
import com.arcogine.types.ModelFingerprint;
import java.util.List;

/**
 * Domain adapter SPI that produces domain-attributed {@link SemanticChange}s between two exact
 * semantic artifacts of the same fingerprint policy.
 *
 * <p>This is the seam that keeps Governance generic: {@code :governance} depends only on this
 * interface, never on a specific domain model. A domain module (e.g. {@code :factory}) implements
 * it and decides its own stable entity identity, taxonomy mapping, and comparison semantics --
 * mirroring the existing {@link com.arcogine.governance.SemanticArtifactVerifier} boundary.
 *
 * <p>Implementations must be deterministic: given the same two artifacts, {@link #compare} must
 * always return semantic changes describing the same content (order is normalized downstream by
 * {@link ChangeSet}, so implementations need not sort, but must not vary content run-to-run).
 */
public interface SemanticChangeExtractor {

    /** Whether this extractor understands artifacts encoded under the given fingerprint policy. */
    boolean supports(ModelFingerprint fingerprint);

    /**
     * Computes the semantic changes needed to transition from {@code base} to {@code candidate}.
     * Both artifacts must satisfy {@link #supports}. Semantically equal artifacts (equal
     * fingerprint) must yield an empty list.
     */
    List<SemanticChange> compare(SemanticArtifact base, SemanticArtifact candidate);
}
