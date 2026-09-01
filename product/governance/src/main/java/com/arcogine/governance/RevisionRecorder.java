package com.arcogine.governance;

import java.util.Objects;

/** Identifies the source and subject that caused a revision to be recorded. */
public record RevisionRecorder(String source, String subject) {

    public RevisionRecorder {
        requireText(source, "source");
        requireText(subject, "subject");
    }

    private static void requireText(String value, String field) {
        if (Objects.requireNonNull(value, field).isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
