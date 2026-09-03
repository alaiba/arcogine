package com.arcogine.governance.conformance;

/**
 * The G4 result taxonomy for one {@code Requirement}/{@code Assertion} evaluation against a
 * particular model fingerprint (and, when available, controlled revision).
 *
 * <p>Per the governance-conformance architecture (§7, §11), {@code UNKNOWN} is a first-class
 * result: absence of evidence must never silently collapse into {@link #PASS} or {@link #FAIL}.
 * {@link #NOT_APPLICABLE} is likewise distinct from both a passing and a failing result -- it
 * means the requirement's declared scope simply did not apply to what was evaluated, which is not
 * the same claim as "evaluated and satisfied."
 */
public enum ConformanceResult {
    /** The assertion was evaluated against authoritative state and was satisfied. */
    PASS,
    /** The assertion was evaluated against authoritative state and was violated. */
    FAIL,
    /**
     * The assertion could not be decided -- typically because it declares {@code
     * EXTERNAL_EVIDENCE_REQUIRED} and G4 does not implement G5 evidence, or because the
     * authoritative state needed to decide a {@code MODEL_STATE_SUFFICIENT} assertion was not
     * supplied.
     */
    UNKNOWN,
    /** The requirement's declared scope did not intersect what was evaluated. */
    NOT_APPLICABLE
}
