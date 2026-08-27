package com.arcogine.challenge.validation;

import java.util.List;

/** Deterministic, structured result of validating a {@code ChallengeDefinition}. */
public record ChallengeDefinitionValidationResult(List<ChallengeDefinitionIssue> issues) {

    public ChallengeDefinitionValidationResult {
        issues = issues == null ? List.of() : List.copyOf(issues);
    }

    public static ChallengeDefinitionValidationResult valid() {
        return new ChallengeDefinitionValidationResult(List.of());
    }

    public boolean isValid() {
        return issues.isEmpty();
    }
}
