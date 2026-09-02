package com.arcogine.governance.requirement;

/**
 * Monotonic Arcogine-owned version of one {@link RequirementId} lineage. Independent of
 * {@code ModelFingerprint}, {@code ControlledRevisionId}, and any assertion version -- changing
 * the model being evaluated or the assertion implementation never implies a new requirement
 * version, and evolving Arcogine's interpretation of a requirement never implies a new model or
 * assertion identity.
 */
public record RequirementVersion(int value) implements Comparable<RequirementVersion> {

    public RequirementVersion {
        if (value < 1) {
            throw new IllegalArgumentException("value must be >= 1");
        }
    }

    @Override
    public int compareTo(RequirementVersion other) {
        return Integer.compare(value, other.value);
    }

    @Override
    public String toString() {
        return "v" + value;
    }
}
