package com.arcogine.factory.process;

/**
 * The minimum supported, consumer-facing runtime event taxonomy for {@link FactoryRuntime}
 * (Gate 4-B). This is deliberately narrower than the internal scheduler's {@code EventType}: it
 * represents meaningful authoritative state change a consumer-neutral caller can act on, not
 * scheduler implementation detail (e.g. the internal {@code TaskStart} marker event, which never
 * itself changes authoritative state, has no supported counterpart).
 */
public enum RuntimeEventType {
    /** A submitted order was accepted and its execution job(s) created. */
    ORDER_ACCEPTED,
    /** A job's execution advanced past one production step. */
    JOB_STEP_COMPLETED,
    /** An accepted order's full execution aggregate completed. */
    ORDER_COMPLETED,
    /** A machine's online/offline availability changed. */
    MACHINE_AVAILABILITY_CHANGED
}
