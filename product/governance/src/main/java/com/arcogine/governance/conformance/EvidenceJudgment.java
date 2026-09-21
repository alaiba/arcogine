package com.arcogine.governance.conformance;

import java.util.Objects;

/**
 * Consumer-owned conclusion over an evidence basis. The evidence evaluator verifies that PASS or
 * FAIL has an adequate relied-on use; it does not invent producer or claim semantics.
 */
public record EvidenceJudgment(ConformanceResult result, String explanation) {

    public EvidenceJudgment {
        Objects.requireNonNull(result, "result");
        if (result == ConformanceResult.NOT_APPLICABLE) {
            throw new IllegalArgumentException("evidence cannot establish NOT_APPLICABLE");
        }
        explanation = Objects.requireNonNull(explanation, "explanation");
    }
}
