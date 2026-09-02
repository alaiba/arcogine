package com.arcogine.challenge.content;

import com.arcogine.challenge.ChallengeDefinition;
import java.util.List;

/**
 * Deterministic, structured result of decoding an untrusted external representation into a {@link
 * ChallengeDefinition}.
 *
 * <p>Exactly one of {@link #definition()} or a non-empty {@link #issues()} is populated: either
 * decoding succeeded and produced a structurally complete {@code ChallengeDefinition} (content
 * validity of that definition is a separate, later step -- see {@code
 * com.arcogine.challenge.validation.ChallengeDefinitionValidator}), or it failed and every reason
 * is reported as a {@link ChallengeContentIssue} rather than as an uncaught exception.
 */
public record ChallengeContentLoadResult(ChallengeDefinition definition, List<ChallengeContentIssue> issues) {

    public ChallengeContentLoadResult {
        issues = issues == null ? List.of() : List.copyOf(issues);
        if (definition == null && issues.isEmpty()) {
            throw new IllegalArgumentException("either definition or issues must be populated");
        }
        if (definition != null && !issues.isEmpty()) {
            throw new IllegalArgumentException("definition and issues are mutually exclusive");
        }
    }

    static ChallengeContentLoadResult success(ChallengeDefinition definition) {
        return new ChallengeContentLoadResult(definition, List.of());
    }

    static ChallengeContentLoadResult failure(List<ChallengeContentIssue> issues) {
        return new ChallengeContentLoadResult(null, issues);
    }

    public boolean isSuccess() {
        return definition != null;
    }
}
