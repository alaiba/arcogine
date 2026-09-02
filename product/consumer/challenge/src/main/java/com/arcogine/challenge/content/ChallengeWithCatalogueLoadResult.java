package com.arcogine.challenge.content;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.catalogue.EquipmentCatalogue;
import java.util.List;

/**
 * Deterministic, structured result of loading a {@link ChallengeDefinition} and an {@link
 * EquipmentCatalogue} together and confirming that every catalogue reference the challenge
 * declares actually resolves against that catalogue.
 *
 * <p>This is the aggregate outcome of {@link ChallengeContentLoader#loadChallengeWithCatalogue}:
 * decoding both documents ({@link ChallengeContentLoader#load} and {@link
 * ChallengeContentLoader#loadCatalogue}), running {@code ChallengeDefinitionValidator} on the
 * decoded definition, running {@code EquipmentCatalogueValidator#validate} on the decoded
 * catalogue, and running {@code EquipmentCatalogueValidator#validateChallengeResolution} across
 * both. Exactly one of ({@link #definition()} and {@link #catalogue()}) or a non-empty {@link
 * #issues()} is populated -- either every step succeeded, or every reason from every step is
 * reported as a {@link ChallengeContentIssue} rather than as an uncaught exception or a partial
 * result.
 */
public record ChallengeWithCatalogueLoadResult(
        ChallengeDefinition definition, EquipmentCatalogue catalogue, List<ChallengeContentIssue> issues) {

    public ChallengeWithCatalogueLoadResult {
        issues = issues == null ? List.of() : List.copyOf(issues);
        boolean bothPresent = definition != null && catalogue != null;
        boolean bothAbsent = definition == null && catalogue == null;
        if (!bothPresent && !bothAbsent) {
            throw new IllegalArgumentException("definition and catalogue must both be present or both be absent");
        }
        if (bothAbsent && issues.isEmpty()) {
            throw new IllegalArgumentException("either definition/catalogue or issues must be populated");
        }
        if (bothPresent && !issues.isEmpty()) {
            throw new IllegalArgumentException("definition/catalogue and issues are mutually exclusive");
        }
    }

    static ChallengeWithCatalogueLoadResult success(ChallengeDefinition definition, EquipmentCatalogue catalogue) {
        return new ChallengeWithCatalogueLoadResult(definition, catalogue, List.of());
    }

    static ChallengeWithCatalogueLoadResult failure(List<ChallengeContentIssue> issues) {
        return new ChallengeWithCatalogueLoadResult(null, null, issues);
    }

    public boolean isSuccess() {
        return definition != null && catalogue != null;
    }
}
