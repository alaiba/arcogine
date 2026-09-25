package com.arcogine.factory.process;

import java.io.Serial;
import java.util.Objects;

/**
 * Thrown when a valid published model represents authored content the current Engine
 * interpretation does not execute.
 *
 * <p>The refusal happens before any runtime state exists, so no partially assembled runtime can
 * observe, ignore, or default the unsupported content. It is distinct from
 * {@link com.arcogine.factory.model.validation.FactoryModelValidationException} (content that can
 * never be published) and from an unsupported {@link com.arcogine.types.EngineSemantics} name:
 * model validity and Engine executability are separate predicates.
 */
public final class UnsupportedModelContentException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String content;

    public UnsupportedModelContentException(String content, String message) {
        super(Objects.requireNonNull(message, "message"));
        this.content = Objects.requireNonNull(content, "content");
    }

    /** The represented content the current Engine interpretation refused, for example {@code spatial}. */
    public String content() {
        return content;
    }
}
