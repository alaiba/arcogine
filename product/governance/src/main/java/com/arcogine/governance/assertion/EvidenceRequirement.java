package com.arcogine.governance.assertion;

/**
 * Whether an {@link Assertion} can be decided from authoritative Arcogine model state alone, or
 * needs an external observation.
 *
 * <p>This is a declaration only; it does not ingest or fabricate external evidence, define
 * freshness policy, or own producer semantics. The headless evidence capability composes explicit
 * {@code EvidenceUse} values with this declaration at a separate evaluation boundary.
 */
public enum EvidenceRequirement {
    /** Decidable purely from authoritative Arcogine model state. */
    MODEL_STATE_SUFFICIENT,
    /** Cannot be decided without an external observation outside Arcogine's authoritative model. */
    EXTERNAL_EVIDENCE_REQUIRED
}
