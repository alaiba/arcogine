package com.arcogine.factory.process;

import com.arcogine.types.ModelFingerprint;
import com.arcogine.types.RunId;
import com.arcogine.types.SimTime;

/** Immutable provenance and cursor metadata for one supported runtime observation. */
public record RuntimeObservationMetadata(
        RunId runId,
        ModelFingerprint modelFingerprint,
        SimTime currentTime,
        RuntimeRunState runState,
        long latestEventSequence) {

    public RuntimeObservationMetadata {
        if (runId == null || modelFingerprint == null || currentTime == null || runState == null) {
            throw new NullPointerException("runtime observation metadata values must not be null");
        }
        if (latestEventSequence < 0) {
            throw new IllegalArgumentException("latestEventSequence must not be negative");
        }
    }
}
