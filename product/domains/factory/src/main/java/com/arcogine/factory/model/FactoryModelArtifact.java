package com.arcogine.factory.model;

import com.arcogine.governance.SemanticArtifactVerifier;
import com.arcogine.types.ModelFingerprint;
import java.util.Objects;

/**
 * Public artifact boundary for the current Factory canonical form.
 *
 * <p>The bytes are the same canonical bytes {@link FactoryModelVersion#fingerprint()} digests.
 * Decoding is strict: malformed or merely decodable-but-noncanonical bytes are rejected with
 * {@link IllegalArgumentException}, and content that decodes but violates a publication predicate
 * is rejected with
 * {@link com.arcogine.factory.model.validation.FactoryModelValidationException} because it could
 * never have been published.
 *
 * <p>Only the current development definition is understood. Artifacts using discarded ordinal
 * prefixes are unsupported and are never reinterpreted as current content. The fingerprint carries
 * no maturity label or exact-definition identifier; current artifacts are verified against the
 * current definition and are development evidence, not durable historical records.
 */
public final class FactoryModelArtifact {

    private static final SemanticArtifactVerifier VERIFIER = new Verifier();

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

    /**
     * The Governance verifier for current Factory artifacts. Its definition binding names the exact
     * build of the current definition, so a proving store written under an earlier development
     * revision is refused rather than read under this one. The binding is separate because a
     * content fingerprint does not identify the exact definition build that produced it.
     */
    public static SemanticArtifactVerifier verifier() {
        return VERIFIER;
    }

    private static final class Verifier implements SemanticArtifactVerifier {

        @Override
        public boolean supports(ModelFingerprint fingerprint) {
            return FactoryModelArtifact.supports(fingerprint);
        }

        @Override
        public ModelFingerprint fingerprint(byte[] canonicalBytes) {
            return FactoryModelArtifact.fingerprint(canonicalBytes);
        }

        @Override
        public String definitionBinding() {
            return FactoryModelCanonicalForm.definitionBinding();
        }
    }
}
