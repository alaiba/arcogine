package com.arcogine.factory.model;

import com.arcogine.factory.model.validation.FactoryModelValidator;

/**
 * An immutable, published identity of a {@link FactoryModel}.
 *
 * <p>{@code contentHash} is a deterministic digest of the model's semantic content: two
 * publications of an equal model produce the same hash. This is an internal, in-memory identity
 * policy sufficient to let a runtime/result attribute itself to the exact model it was
 * instantiated from -- it is not a persisted, public, or cross-process compatibility guarantee.
 * A durable model repository/lineage/versioning scheme is out of scope for this milestone; see
 * ADR-0003.
 *
 * <p>{@link FactoryModelPublisher#publish(FactoryModel)} is the intended way to obtain an
 * instance, but the D2/D4 invariant that an invalid model can never be published or instantiated
 * is enforced here, in the canonical constructor itself, rather than relied upon merely by
 * convention: constructing a {@code FactoryModelVersion} directly from an invalid model throws
 * {@link com.arcogine.factory.model.validation.FactoryModelValidationException} exactly as
 * {@code publish} does, so there is no construction path -- public API misuse included -- that
 * produces a version wrapping an unvalidated model.
 */
public record FactoryModelVersion(FactoryModel model, String contentHash) {

    public FactoryModelVersion {
        if (model == null) {
            throw new NullPointerException("model");
        }
        if (contentHash == null) {
            throw new NullPointerException("contentHash");
        }
        FactoryModelValidator.requireValid(model);
    }
}
