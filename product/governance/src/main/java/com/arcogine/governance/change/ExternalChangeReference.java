package com.arcogine.governance.change;

import java.util.Objects;

/**
 * Association-only reference to an external change-request/workflow record (e.g. a Jira issue key
 * or GitHub PR URL). This is never revision or ChangeSet identity, and Arcogine does not
 * synchronize with or interpret the referenced system.
 */
public record ExternalChangeReference(String system, String identifier) {

    public ExternalChangeReference {
        Objects.requireNonNull(system, "system");
        Objects.requireNonNull(identifier, "identifier");
        if (system.isBlank()) {
            throw new IllegalArgumentException("system must not be blank");
        }
        if (identifier.isBlank()) {
            throw new IllegalArgumentException("identifier must not be blank");
        }
    }
}
