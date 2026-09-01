package com.arcogine.types;

import java.util.UUID;

/** Opaque correlation identity for one fresh runtime session. */
public record RunId(UUID value) {

    public RunId {
        if (value == null) {
            throw new NullPointerException("value");
        }
    }

    /** Creates a fresh identity that is deliberately independent of simulation semantics. */
    public static RunId create() {
        return new RunId(UUID.randomUUID());
    }
}
