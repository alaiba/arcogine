package com.arcogine.factory.model;

import com.arcogine.factory.model.validation.FactoryModelValidator;
import com.arcogine.types.ModelFingerprint;

/**
 * An immutable, published snapshot of a {@link FactoryModel}.
 *
 * <p>{@link #fingerprint()} is the deterministic content fingerprint of the published model under
 * the current canonical form specified by docs/architecture/factory-model.md. For one definition,
 * equal canonical content always has the same fingerprint, across processes and implementation
 * languages. The definition itself may change between development revisions, so the fingerprint is
 * not an exact cross-revision definition reference. Publication proves validity and gives an
 * immutable in-process snapshot; it does not by itself create a stability or support promise.
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
        return FactoryModelCanonicalForm.fingerprint(model);
    }
}
