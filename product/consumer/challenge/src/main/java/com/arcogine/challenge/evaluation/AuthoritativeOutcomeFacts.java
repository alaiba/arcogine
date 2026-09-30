package com.arcogine.challenge.evaluation;

/**
 * Narrow, consumer-neutral production facts supplied by an authoritative producer.
 *
 * <p>This value contains no queue, dispatch, transfer, or runtime-state detail. It records only
 * the authoritative producer's fixed-contract completion fact and completion tick; tests may
 * construct synthetic facts directly.
 */
public record AuthoritativeOutcomeFacts(boolean contractCompleted, Long completionTick) {

    public AuthoritativeOutcomeFacts {
        if (contractCompleted && completionTick == null) {
            throw new IllegalArgumentException("completed contract requires completionTick");
        }
        if (!contractCompleted && completionTick != null) {
            throw new IllegalArgumentException("incomplete contract must not have completionTick");
        }
        if (completionTick != null && completionTick < 0) {
            throw new IllegalArgumentException("completionTick must be non-negative");
        }
    }
}
