package com.arcogine.factory.model;

import com.arcogine.factory.model.validation.FactoryModelValidator;
import com.arcogine.types.ModelFingerprint;

/**
 * An immutable, published identity of a {@link FactoryModel}.
 *
 * <p>{@link #fingerprint()} is the durable semantic identity contract for published factory
 * models. It implements the policy-versioned {@code factory-model:v1} canonical encoding from
 * docs/architecture/factory-model-v1.md and returns a typed {@link ModelFingerprint} suitable for
 * cross-process and
 * cross-language identity under that released policy.
 *
 * <p>Controlled revision identity, lineage, and persistence are separate concerns from the model
 * fingerprint; see docs/architecture/factory-design.md section 11 and
 * docs/architecture/controlled-revisions.md.
 *
 * <p>{@link FactoryModelPublisher#publish(FactoryModel)} is the intended way to obtain an
 * instance, but the invariant that an invalid model can never be published or instantiated
 * is enforced here, in the canonical constructor itself, rather than relied upon merely by
 * convention: constructing a {@code FactoryModelVersion} directly from an invalid model throws
 * {@link com.arcogine.factory.model.validation.FactoryModelValidationException} exactly as
 * {@code publish} does, so there is no construction path -- public API misuse included -- that
 * produces a version wrapping an unvalidated model or a forged identity.
 */
public record FactoryModelVersion(FactoryModel model) {

    public FactoryModelVersion {
        if (model == null) {
            throw new NullPointerException("model");
        }
        FactoryModelValidator.requireValid(model);
    }

    public ModelFingerprint fingerprint() {
        return FactoryModelFingerprintV1.fingerprint(model);
    }
}
