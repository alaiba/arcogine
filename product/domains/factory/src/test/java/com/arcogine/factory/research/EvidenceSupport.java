package com.arcogine.factory.research;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The exact supported evidence a derived value was computed from: which labeled observations were
 * read, and the interval of supported events that was read (none when the derivation read no
 * events). This is what makes a derived claim traceable to an event and observation interval
 * instead of to "the run".
 */
public record EvidenceSupport(List<String> observations, Optional<SequenceRange> events) {

    public EvidenceSupport {
        observations = List.copyOf(Objects.requireNonNull(observations, "observations"));
        Objects.requireNonNull(events, "events");
    }
}
