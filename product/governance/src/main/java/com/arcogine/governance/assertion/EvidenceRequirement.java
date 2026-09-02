package com.arcogine.governance.assertion;

/**
 * Whether an {@link Assertion} can be decided from authoritative Arcogine model state alone, or
 * needs an external observation.
 *
 * <p>This is a declaration only. G3 does not implement G5 evidence: no {@code Evidence}/{@code
 * EvidenceUse} type, no telemetry ingestion, no persistence, no freshness semantics. An assertion
 * declaring {@link #EXTERNAL_EVIDENCE_REQUIRED} simply records that later (G5-backed, G4-driven)
 * evaluation will need external evidence to reach a result -- it never ingests or fabricates that
 * evidence itself.
 */
public enum EvidenceRequirement {
    /** Decidable purely from authoritative Arcogine model state. */
    MODEL_STATE_SUFFICIENT,
    /** Cannot be decided without an external observation outside Arcogine's authoritative model. */
    EXTERNAL_EVIDENCE_REQUIRED
}
