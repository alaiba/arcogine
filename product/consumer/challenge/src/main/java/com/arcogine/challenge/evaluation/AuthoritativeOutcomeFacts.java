package com.arcogine.challenge.evaluation;

/**
 * Narrow, consumer-neutral production facts supplied by an authoritative producer.
 *
 * <p>This value deliberately does not describe queues, dispatch, transfers, or runtime state. A
 * later adapter may obtain these facts from an Arcogine-supported result, while tests may provide
 * synthetic facts directly.
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
