package com.arcogine.types;

import java.util.Objects;

/**
 * Semantic identity of the complete result-affecting Engine interpretation used by a run.
 *
 * <p>This identity is independent of authored model identity, runtime correlation identity, and
 * software/build identity. The current implementation executes exactly one supported version.
 */
public record EngineSemanticsVersion(String value) {

    /** The only Engine semantics version executable by the current implementation. */
    public static final EngineSemanticsVersion CURRENT = new EngineSemanticsVersion("engine-semantics:v1");

    public EngineSemanticsVersion {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }

    /** Returns whether this implementation can execute {@code version}. */
    public static boolean isSupported(EngineSemanticsVersion version) {
        return CURRENT.equals(Objects.requireNonNull(version, "version"));
    }

    /**
     * Verifies that {@code version} is executable by this implementation.
     *
     * @throws IllegalArgumentException when the version is not supported
     */
    public static EngineSemanticsVersion requireSupported(EngineSemanticsVersion version) {
        Objects.requireNonNull(version, "version");
        if (!isSupported(version)) {
            throw new IllegalArgumentException("unsupported engine semantics version: " + version);
        }
        return version;
    }

    @Override
    public String toString() {
        return value;
    }
}
