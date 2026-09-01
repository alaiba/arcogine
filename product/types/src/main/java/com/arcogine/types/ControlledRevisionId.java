package com.arcogine.types;

import java.util.Objects;
import java.util.UUID;

/** Opaque identity for one historical controlled revision. */
public record ControlledRevisionId(UUID value) {

    public ControlledRevisionId {
        Objects.requireNonNull(value, "value");
        if (value.version() != 4) {
            throw new IllegalArgumentException("controlled revision ID must be a UUIDv4");
        }
    }

    public static ControlledRevisionId generate() {
        return new ControlledRevisionId(UUID.randomUUID());
    }

    public static ControlledRevisionId parse(String value) {
        Objects.requireNonNull(value, "value");
        UUID uuid = UUID.fromString(value);
        if (!uuid.toString().equals(value)) {
            throw new IllegalArgumentException("controlled revision ID must use canonical UUID form");
        }
        return new ControlledRevisionId(uuid);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
