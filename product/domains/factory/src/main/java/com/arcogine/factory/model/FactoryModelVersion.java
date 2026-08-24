package com.arcogine.factory.model;

/**
 * An immutable, published identity of a {@link FactoryModel}.
 *
 * <p>{@code contentHash} is a deterministic digest of the model's semantic content: two
 * publications of an equal model produce the same hash. This is an internal, in-memory identity
 * policy sufficient to let a runtime/result attribute itself to the exact model it was
 * instantiated from -- it is not a persisted, public, or cross-process compatibility guarantee.
 * A durable model repository/lineage/versioning scheme is out of scope for this milestone; see
 * ADR-0003.
 */
public record FactoryModelVersion(FactoryModel model, String contentHash) {

    public FactoryModelVersion {
        if (model == null) {
            throw new NullPointerException("model");
        }
        if (contentHash == null) {
            throw new NullPointerException("contentHash");
        }
    }
}
