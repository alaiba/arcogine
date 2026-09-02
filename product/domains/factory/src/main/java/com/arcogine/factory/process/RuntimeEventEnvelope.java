package com.arcogine.factory.process;

import com.arcogine.types.ControlledRevisionId;
import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.RunId;
import com.arcogine.types.SimTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The supported, consumer-neutral runtime event envelope (Gate 4-B, ADR-0011). This is the
 * externally observable contract a {@link FactoryRuntime} caller correlates against -- distinct
 * from, and never a wrapper around, the internal scheduler's {@code Event}.
 *
 * <p>A {@link RuntimeEventEnvelope} is only ever produced after the authoritative transition it
 * describes has already succeeded (post-authoritative publication): {@link #sequence()} is
 * allocated at emission time, strictly increasing within one {@link #runId()}, and independent of
 * however many internal scheduler events were involved in producing it. Multiple envelopes can
 * share the same {@link #simulationTime()}; {@link #sequence()} is then the tie-break order.
 *
 * @param runId the opaque run this event belongs to (see {@link FactoryRuntime#runId()})
 * @param sequence the strictly monotonic, run-scoped supported-event cursor position (matches
 *     {@code RuntimeObservationMetadata#latestEventSequence()} once applied); always positive
 * @param simulationTime the simulated time at which the described authoritative change occurred
 * @param eventType the supported taxonomy member this event belongs to
 * @param modelFingerprint the durable semantic fingerprint ({@code FactoryModelVersion#fingerprint()})
 *     of the model this run was instantiated from
 * @param controlledRevisionId present only when the runtime is actually authoritatively bound to a
 *     controlled revision through an established contract; G4-B never synthesizes one
 * @param affectedEntityRefs typed, stable correlation to every entity this event concerns
 * @param payload the supported, consumer-neutral payload for {@link #eventType()}
 */
public record RuntimeEventEnvelope(
        RunId runId,
        long sequence,
        SimTime simulationTime,
        RuntimeEventType eventType,
        ModelFingerprint modelFingerprint,
        Optional<ControlledRevisionId> controlledRevisionId,
        List<AffectedEntityRef> affectedEntityRefs,
        RuntimeEventPayload payload) {

    public RuntimeEventEnvelope {
        Objects.requireNonNull(runId, "runId");
        Objects.requireNonNull(simulationTime, "simulationTime");
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(modelFingerprint, "modelFingerprint");
        Objects.requireNonNull(controlledRevisionId, "controlledRevisionId");
        Objects.requireNonNull(affectedEntityRefs, "affectedEntityRefs");
        Objects.requireNonNull(payload, "payload");
        if (sequence <= 0) {
            throw new IllegalArgumentException("sequence must be positive, got " + sequence);
        }
        affectedEntityRefs = List.copyOf(affectedEntityRefs);
    }
}
