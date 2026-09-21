package com.arcogine.governance.evidence;

import java.util.Objects;

/** An immutable attributable relationship from one evidence revision to another. */
public record EvidenceRelation(
        EvidenceRelationKind kind, EvidenceReference earlier, String explanation) {

    public EvidenceRelation {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(earlier, "earlier");
        explanation = explanation == null ? "" : explanation;
    }
}
