package com.arcogine.governance.conformance;

import java.util.List;
import java.util.Optional;

/** Acceptance and immutable historical-resolution boundary for evaluation occurrences. */
public interface EvaluationOccurrenceAuthority {

    EvaluationOccurrence accept(EvaluationOccurrenceDraft candidate);

    Optional<EvaluationOccurrence> findById(EvaluationOccurrenceId id);

    EvaluationOccurrence resolve(EvaluationOccurrenceId id);

    List<EvaluationOccurrence> occurrences();
}
