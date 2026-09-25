package com.arcogine.factory.model;

import com.arcogine.types.ModelFingerprint;
import java.util.Objects;

/**
 * Public artifact boundary for the current {@code factory-model:wip} canonical form.
 *
 * <p>The bytes are the same canonical bytes {@link FactoryModelVersion#fingerprint()} digests.
 * Decoding is strict: malformed or merely decodable-but-noncanonical bytes are rejected with
 * {@link IllegalArgumentException}, and content that decodes but violates a publication predicate
 * is rejected with
 * {@link com.arcogine.factory.model.validation.FactoryModelValidationException} because it could
 * never have been published.
 *
 * <p>Only the current development definition is understood. Artifacts or fingerprints under any
 * other policy -- including the discarded ordinal policies -- are unsupported and are never
 * reinterpreted as current content. A {@code factory-model:wip} artifact is verified only against
 * the current definition; it is development evidence, not a durable historical record.
 */
public final class FactoryModelArtifact {

    private FactoryModelArtifact() {}

    public static byte[] encode(FactoryModelVersion version) {
        Objects.requireNonNull(version, "version");
        return FactoryModelCanonicalForm.canonicalBytes(version.model());
    }

    public static FactoryModelVersion decode(byte[] canonicalBytes) {
        return new FactoryModelVersion(FactoryModelCanonicalForm.decode(canonicalBytes));
    }

    public static ModelFingerprint fingerprint(byte[] canonicalBytes) {
        return decode(canonicalBytes).fingerprint();
    }

    public static boolean supports(ModelFingerprint fingerprint) {
        return FactoryModelCanonicalForm.identifies(Objects.requireNonNull(fingerprint, "fingerprint"));
    }
}
