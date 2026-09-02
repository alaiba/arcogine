package com.arcogine.governance.assertion;

/**
 * Monotonic Arcogine-owned version of one {@link AssertionId} lineage, independent of the {@code
 * RequirementVersion} it currently evaluates and of the model/revision it is applied to.
 */
public record AssertionVersion(int value) implements Comparable<AssertionVersion> {

    public AssertionVersion {
        if (value < 1) {
            throw new IllegalArgumentException("value must be >= 1");
        }
    }

    @Override
    public int compareTo(AssertionVersion other) {
        return Integer.compare(value, other.value);
    }

    @Override
    public String toString() {
        return "v" + value;
    }
}
