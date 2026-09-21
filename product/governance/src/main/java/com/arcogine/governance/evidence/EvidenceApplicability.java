package com.arcogine.governance.evidence;

import java.util.Objects;

/** An attributable use-level applicability/reliance determination and its interpretation. */
public record EvidenceApplicability(
        EvidenceApplicabilityStatus status, String explanation, String interpretationRules) {

    public EvidenceApplicability {
        Objects.requireNonNull(status, "status");
        explanation = requireText(explanation, "explanation");
        interpretationRules = requireText(interpretationRules, "interpretationRules");
    }

    public boolean isAdequate() {
        return status == EvidenceApplicabilityStatus.APPLICABLE;
    }

    private static String requireText(String value, String field) {
        if (Objects.requireNonNull(value, field).isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
