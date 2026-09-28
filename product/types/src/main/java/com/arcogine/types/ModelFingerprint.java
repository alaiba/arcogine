package com.arcogine.types;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A content digest of canonical semantic content within a domain namespace.
 *
 * <p>The fingerprint identifies content under the canonical form implemented by the producing
 * context. It does not identify that canonical form's exact development revision, maturity, support
 * status, or any human-facing label. Those concerns require their own explicit basis when a concrete
 * consumer needs them.
 */
public record ModelFingerprint(String namespace, String algorithm, String digest) {

    private static final Pattern SHA256_DIGEST = Pattern.compile("[0-9a-f]{64}");

    public ModelFingerprint {
        requireText(namespace, "namespace");
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
        return namespace + ":" + algorithm + ":" + digest;
    }
}
