package com.arcogine.factory.process;

/**
 * Current advancement state of a {@link FactoryRuntime}.
 *
 * <p>{@link #ACTIVE} means the runtime has pending authoritative work and can advance. {@link
 * #QUIESCENT} means no event is pending. The current Gate 3 runtime has no pause, cancellation, or
 * terminal-session lifecycle, so quiescence deliberately covers both a fresh runtime and one whose
 * submitted work has drained rather than inventing a terminal state.
 */
public enum RuntimeRunState {
    ACTIVE,
    QUIESCENT
}
