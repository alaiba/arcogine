package com.arcogine.factory.process;

/**
 * Current advancement state of a {@link FactoryRuntime}.
 *
 * <p>{@link #ACTIVE} means the runtime has pending authoritative work and can advance. {@link
 * #QUIESCENT} means no pending work remains that could authoritatively change state.
 *
 * <p>"Authoritative" is load-bearing (docs/architecture/runtime-contract.md): a runtime whose work
 * has genuinely drained must not report {@code ACTIVE} and then {@code QUIESCENT} at the same
 * {@code latestEventSequence}. A Factory session queues only step completions, each an
 * authoritative transition that publishes a supported event when processed
 * (docs/architecture/engine-semantics.md §4), so a non-empty internal queue is exactly pending
 * authoritative work.
 *
 * <p>The current session-control runtime has no pause, cancellation, or
 * terminal-session lifecycle, so quiescence deliberately covers both a fresh runtime and one whose
 * submitted work has drained rather than inventing a terminal state.
 */
public enum RuntimeRunState {
    ACTIVE,
    QUIESCENT
}
