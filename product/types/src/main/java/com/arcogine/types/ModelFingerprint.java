package com.arcogine.types;

import java.util.Objects;
import java.util.regex.Pattern;

/** A durable semantic identity under a named and versioned fingerprint policy. */
public record ModelFingerprint(String namespace, String policyVersion, String algorithm, String digest) {

    private static final Pattern SHA256_DIGEST = Pattern.compile("[0-9a-f]{64}");

    public ModelFingerprint {
        requireText(namespace, "namespace");
        requireText(policyVersion, "policyVersion");
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
        return namespace + ":" + policyVersion + ":" + algorithm + ":" + digest;
    }
}
