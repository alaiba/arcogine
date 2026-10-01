package com.arcogine.research.experiment;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * A named, research-local analytical definition: the method behind a derived value, and the kinds
 * of supported evidence that method is allowed to read.
 *
 * <p>This is provenance for a fixture's ground truth, not an Arcogine semantic. A definition that
 * appears here is not an Engine fact, not a game-owned analytic, and not a public API, and it is
 * not the producer-owned analytical-definition provenance that Governance evidence may record.
 * Changing what a definition computes is a new definition with a new name, so results produced
 * under different derivations are never silently treated as comparable.
 *
 * @param name stable name a report can cite
 * @param statement the exact method, stated so it can be reconstructed from {@code inputs} alone
 * @param inputs the supported evidence kinds the method may read, and nothing else
 */
public record ResearchDefinition(String name, String statement, Set<EvidenceInput> inputs) {

    public ResearchDefinition {
        requireText(name, "name");
        requireText(statement, "statement");
        Objects.requireNonNull(inputs, "inputs");
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("a definition must declare at least one supported input");
        }
        // EnumSet keeps the declared enum order, so nothing downstream depends on hash iteration.
        inputs = Collections.unmodifiableSet(EnumSet.copyOf(inputs));
    }

    private static void requireText(String value, String field) {
        if (Objects.requireNonNull(value, field).isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
