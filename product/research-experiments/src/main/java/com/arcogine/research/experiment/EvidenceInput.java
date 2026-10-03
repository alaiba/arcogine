package com.arcogine.research.experiment;

/**
 * The kinds of supported evidence a research-local derivation may read.
 *
 * <p>The set is closed on purpose. Scheduler queues, handler and store state, and internal
 * scheduler events are not supported evidence, so no input kind names them and no derivation can
 * declare a dependency on them.
 */
public enum EvidenceInput {
    /** The authored Factory model facts the run was instantiated from. */
    PUBLISHED_MODEL,
    /** Supported runtime observations captured at labeled points. */
    OBSERVATIONS,
    /**
     * Supported runtime events, readable only through a range whose completeness the window has
     * confirmed.
     */
    SUPPORTED_EVENTS
}
