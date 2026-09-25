package com.arcogine.types;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A content digest of canonical semantic content under a named canonicalization policy.
 *
 * <p>{@code policy} names the definition the digest was computed under. A work-in-progress
 * definition is named by a mutable development marker such as {@code wip}: equal fingerprints then
 * mean equal content under the current definition only, never across development revisions.
 */
public record ModelFingerprint(String namespace, String policy, String algorithm, String digest) {

    private static final Pattern SHA256_DIGEST = Pattern.compile("[0-9a-f]{64}");

    public ModelFingerprint {
        requireText(namespace, "namespace");
        requireText(policy, "policy");
        requireText(algorithm, "algorithm");
        requireText(digest, "digest");
        if ("sha256".equals(algorithm) && !SHA256_DIGEST.matcher(digest).matches()) {
            throw new IllegalArgumentException("sha256 digest must be 64 lowercase hexadecimal characters");
        }
    }

    private static void requireText(String value, String field) {
        if (Objects.requireNonNull(value, field).isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    @Override
    public String toString() {
        return namespace + ":" + policy + ":" + algorithm + ":" + digest;
    }
}
