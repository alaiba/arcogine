package com.arcogine.types;

import java.util.Objects;

/**
 * Names the complete result-affecting Engine interpretation a run uses.
 *
 * <p>This is independent of authored model identity, runtime correlation identity, and
 * software/build identity. While Engine semantics are work in progress the only value is the
 * mutable development marker {@code engine-semantics:wip}: it names whichever definition the
 * current build implements, is never an identity spanning development revisions, and must not be
 * read as exact provenance for a result produced by another revision. The current implementation
 * executes exactly one interpretation and refuses every other name.
 */
public record EngineSemantics(String value) {

    /** The only Engine interpretation executable by the current implementation. */
    public static final EngineSemantics CURRENT = new EngineSemantics("engine-semantics:wip");

    public EngineSemantics {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }

    /** Returns whether this implementation can execute {@code semantics}. */
    public static boolean isSupported(EngineSemantics semantics) {
        return CURRENT.equals(Objects.requireNonNull(semantics, "semantics"));
    }

    /**
     * Verifies that {@code semantics} is executable by this implementation.
     *
     * @throws IllegalArgumentException when the interpretation is not supported
     */
    public static EngineSemantics requireSupported(EngineSemantics semantics) {
        Objects.requireNonNull(semantics, "semantics");
        if (!isSupported(semantics)) {
            throw new IllegalArgumentException("unsupported engine semantics: " + semantics);
        }
        return semantics;
    }

    @Override
    public String toString() {
        return value;
    }
}
