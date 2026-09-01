package com.arcogine.factory.model;

import com.arcogine.types.ModelFingerprint;
import java.util.Objects;

/**
 * Public artifact boundary for the released {@code factory-model:v1} semantic encoding.
 *
 * <p>The bytes are the same canonical bytes used by {@link FactoryModelVersion#fingerprint()}.
 * Decoding is strict: malformed or merely decodable-but-noncanonical bytes are rejected.
 */
public final class FactoryModelArtifactV1 {

    private FactoryModelArtifactV1() {}

    public static byte[] encode(FactoryModelVersion version) {
        Objects.requireNonNull(version, "version");
        return FactoryModelFingerprintV1.canonicalBytes(version.model());
    }

    public static FactoryModelVersion decode(byte[] canonicalBytes) {
        return new FactoryModelVersion(FactoryModelFingerprintV1.decodeCanonicalBytes(canonicalBytes));
    }

    public static ModelFingerprint fingerprint(byte[] canonicalBytes) {
        return decode(canonicalBytes).fingerprint();
    }

    public static boolean supports(ModelFingerprint fingerprint) {
        Objects.requireNonNull(fingerprint, "fingerprint");
        return "factory-model".equals(fingerprint.namespace())
                && "v1".equals(fingerprint.policyVersion())
                && "sha256".equals(fingerprint.algorithm());
    }
}
