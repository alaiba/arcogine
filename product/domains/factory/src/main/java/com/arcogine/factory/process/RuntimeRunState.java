package com.arcogine.factory.process;

/**
 * Current advancement state of a {@link FactoryRuntime}.
 *
 * <p>{@link #ACTIVE} means the runtime has pending authoritative work and can advance. {@link
 * #QUIESCENT} means no pending work remains that could authoritatively change state.
 *
 * <p>"Authoritative" is load-bearing (ADR-0011): the internal scheduler queue can still
 * hold no-op markers ({@code TaskStart}, or the {@code OrderCompleted} a terminal {@code TaskEnd}
 * schedules for other internal handlers) that {@code FactoryHandler} ignores. Those change nothing
 * a consumer can observe and produce no supported event, so they never make a runtime {@code
 * ACTIVE}; otherwise a runtime whose work has genuinely drained would report {@code ACTIVE} and
 * then {@code QUIESCENT} at the same {@code latestEventSequence}.
 *
 * <p>The current session-control runtime has no pause, cancellation, or
 * terminal-session lifecycle, so quiescence deliberately covers both a fresh runtime and one whose
 * submitted work has drained rather than inventing a terminal state.
 */
public enum RuntimeRunState {
    ACTIVE,
    QUIESCENT
}
