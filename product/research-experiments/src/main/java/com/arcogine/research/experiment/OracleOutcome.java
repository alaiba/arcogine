package com.arcogine.research.experiment;

import java.util.List;
import java.util.Objects;

/**
 * The result of applying a research-local derivation to supported evidence: either a derived value
 * together with the evidence that supports it, or an explicit refusal.
 *
 * <p>Refusal is a first-class outcome. When the supported evidence does not license a claim, the
 * correct result is {@link Underdetermined}, never a best guess.
 */
public sealed interface OracleOutcome<T> {

    /** The definition that produced this outcome. */
    ResearchDefinition definition();

    /** A value the named definition derives from exactly {@code support}. */
    record Derived<T>(ResearchDefinition definition, T value, EvidenceSupport support) implements OracleOutcome<T> {
        public Derived {
            Objects.requireNonNull(definition, "definition");
            Objects.requireNonNull(value, "value");
            Objects.requireNonNull(support, "support");
        }
    }

    /** The supported evidence does not license the claim; {@code reasons} say why. */
    record Underdetermined<T>(ResearchDefinition definition, List<String> reasons) implements OracleOutcome<T> {
        public Underdetermined {
            Objects.requireNonNull(definition, "definition");
            reasons = List.copyOf(Objects.requireNonNull(reasons, "reasons"));
            if (reasons.isEmpty()) {
                throw new IllegalArgumentException("a refusal must state at least one reason");
            }
        }
    }
}
