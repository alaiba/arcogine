package com.arcogine.governance.assertion;

import java.util.Objects;

/**
 * The narrow structural fact an {@link AssertionRule} produces: whether the authoritative state
 * it examined satisfied the rule, and why.
 *
 * <p>This is deliberately not the conformance-evaluation result model. It carries no {@code PASS}/{@code
 * FAIL}/{@code UNKNOWN}/{@code NOT_APPLICABLE} taxonomy, no finding, no severity, no evidence
 * set, and no historical evaluation record -- it exists only so a requirement-scope {@link AssertionRule} has
 * something deterministic to return to prove executable semantics. Conformance evaluation owns turning this (and
 * external-evidence-backed results) into a real conformance evaluation and finding.
 */
public record StructuralAssertionOutcome(boolean satisfied, String explanation) {

    public StructuralAssertionOutcome {
        explanation = explanation == null ? "" : explanation;
    }

    public static StructuralAssertionOutcome satisfied(String explanation) {
        return new StructuralAssertionOutcome(true, explanation);
    }

    public static StructuralAssertionOutcome violated(String explanation) {
        return new StructuralAssertionOutcome(false, explanation);
    }

    @Override
    public String toString() {
        return Objects.toString(satisfied) + (explanation.isBlank() ? "" : ": " + explanation);
    }
}
