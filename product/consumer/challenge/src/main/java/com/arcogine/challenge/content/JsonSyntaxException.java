package com.arcogine.challenge.content;

/**
 * Thrown by {@link Json} when a source string is not well-formed JSON.
 *
 * <p>This is a content-loading concern only -- it never indicates that a structurally valid
 * {@code ChallengeDefinition} has invalid scalar content; that remains {@code
 * ChallengeDefinitionValidator}'s responsibility.
 */
public final class JsonSyntaxException extends Exception {

    private static final long serialVersionUID = 1L;

    public JsonSyntaxException(String message) {
        super(message);
    }
}
