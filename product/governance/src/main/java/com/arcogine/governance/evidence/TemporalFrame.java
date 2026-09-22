package com.arcogine.governance.evidence;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/** Optional temporal and knowledge boundary retained when it materially affects interpretation. */
public record TemporalFrame(
        Optional<Instant> effectiveFrom,
        Optional<Instant> effectiveUntil,
        Optional<Instant> knowledgeBoundary) {

    public TemporalFrame {
        effectiveFrom = Objects.requireNonNull(effectiveFrom, "effectiveFrom");
        effectiveUntil = Objects.requireNonNull(effectiveUntil, "effectiveUntil");
        knowledgeBoundary = Objects.requireNonNull(knowledgeBoundary, "knowledgeBoundary");
        if (effectiveFrom.isPresent()
                && effectiveUntil.isPresent()
                && effectiveUntil.get().isBefore(effectiveFrom.get())) {
            throw new IllegalArgumentException("effectiveUntil must not precede effectiveFrom");
        }
    }

    public static TemporalFrame atKnowledgeBoundary(Instant boundary) {
        return new TemporalFrame(Optional.empty(), Optional.empty(), Optional.of(boundary));
    }
}
