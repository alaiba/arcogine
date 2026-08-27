package com.arcogine.factory.process;

import com.arcogine.core.event.Event;
import com.arcogine.factory.model.FactoryModelVersion;
import com.arcogine.types.SimError;
import java.util.List;

/**
 * The definite result every externally initiated {@link FactoryRuntime} command returns, per
 * docs/planning/factory-simulation-engine-readiness.md §7.2: accepted/rejected status; a stable
 * result/rejection code; an understandable diagnostic; affected-entity identifiers when
 * applicable; session/model provenance; and every event scheduled as a direct effect of the
 * command (empty for a rejection, since a rejected command must never leave partial mutation and
 * therefore never schedules anything).
 *
 * <p>This is one narrow, reused shape rather than a per-command bespoke type: {@code T} is
 * whatever value identifies what the command actually did on acceptance (e.g. the new {@code
 * OrderId} for {@link FactoryRuntime#submitWorkload}, or {@link
 * com.arcogine.core.event.EventPayload.MachineAvailabilityChange} for {@link
 * FactoryRuntime#setMachineAvailability}). Rejection is represented by wrapping the original,
 * already-structured, sealed {@link SimError} the command would otherwise have thrown -- its
 * concrete subtype is the stable rejection code, and its own typed accessors (e.g. {@code
 * field()}, {@code id()}) remain the affected-entity/diagnostic detail, rather than duplicating
 * that detail into a second, parallel shape.
 */
public sealed interface CommandResult<T> {

    /** {@code "ACCEPTED"} for an accepted result, or the rejecting {@link SimError} subtype's simple name. */
    String code();

    /** A human-readable diagnostic: {@code "accepted"} for success, or {@link SimError#getMessage()}. */
    String diagnostic();

    /** The published model version the session -- and therefore this result -- belongs to. */
    FactoryModelVersion modelVersion();

    /** Every {@link Event} scheduled as a direct, synchronous effect of this command, in scheduling order. */
    List<Event> scheduledEvents();

    /** Returns the accepted value, or rethrows the original rejection {@link SimError}. */
    T orElseThrow();

    record Accepted<T>(T value, FactoryModelVersion modelVersion, List<Event> scheduledEvents)
            implements CommandResult<T> {

        public Accepted {
            scheduledEvents = List.copyOf(scheduledEvents);
        }

        @Override
        public String code() {
            return "ACCEPTED";
        }

        @Override
        public String diagnostic() {
            return "accepted";
        }

        @Override
        public T orElseThrow() {
            return value;
        }
    }

    record Rejected<T>(SimError error, FactoryModelVersion modelVersion) implements CommandResult<T> {

        @Override
        public String code() {
            return error.getClass().getSimpleName();
        }

        @Override
        public String diagnostic() {
            return error.getMessage();
        }

        @Override
        public List<Event> scheduledEvents() {
            return List.of();
        }

        @Override
        public T orElseThrow() {
            throw error;
        }
    }
}
