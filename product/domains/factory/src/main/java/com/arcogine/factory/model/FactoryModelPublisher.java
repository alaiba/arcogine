package com.arcogine.factory.model;

import com.arcogine.factory.model.validation.FactoryModelValidator;

/**
 * Publishes a validated {@link FactoryModel} as an immutable {@link FactoryModelVersion}.
 *
 * <p>Publication validates the model first (see {@link FactoryModelValidator}) and never returns
 * a version for a structurally invalid model, so a runtime is never constructed from a partially
 * valid design. {@link FactoryModelVersion} itself derives its content hash from the model rather
 * than accepting one, so publication cannot produce a version with a mismatched identity either.
 */
public final class FactoryModelPublisher {

    private FactoryModelPublisher() {}

    public static FactoryModelVersion publish(FactoryModel model) {
        FactoryModelValidator.requireValid(model);
        return new FactoryModelVersion(model);
    }
}
