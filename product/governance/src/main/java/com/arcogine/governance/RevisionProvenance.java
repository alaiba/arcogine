package com.arcogine.governance;

import java.time.Instant;
import java.util.Objects;

/** Immutable provenance for acceptance into controlled revision history. */
public record RevisionProvenance(Instant recordedAt, RevisionRecorder recorder) {

    public RevisionProvenance {
        Objects.requireNonNull(recordedAt, "recordedAt");
        Objects.requireNonNull(recorder, "recorder");
    }
}
