package com.arcogine.challenge.content;

import com.arcogine.challenge.catalogue.EquipmentCatalogue;
import java.util.List;

/**
 * Deterministic, structured result of decoding an untrusted external representation into an
 * {@link EquipmentCatalogue}.
 *
 * <p>Exactly one of {@link #catalogue()} or a non-empty {@link #issues()} is populated: either
 * decoding (and, once decoded, {@code EquipmentCatalogueValidator} content validation) succeeded,
 * or it failed and every reason is reported as a {@link ChallengeContentIssue} rather than as an
 * uncaught exception. A validation failure is folded into this same result rather than exposed as
 * a separate stage, mirroring {@link ChallengeContentLoadResult}'s decode-only contract but for
 * catalogue content, where {@code EquipmentCatalogueValidator#validate} is cheap and always run.
 */
public record EquipmentCatalogueContentLoadResult(EquipmentCatalogue catalogue, List<ChallengeContentIssue> issues) {

    public EquipmentCatalogueContentLoadResult {
        issues = issues == null ? List.of() : List.copyOf(issues);
        if (catalogue == null && issues.isEmpty()) {
            throw new IllegalArgumentException("either catalogue or issues must be populated");
        }
        if (catalogue != null && !issues.isEmpty()) {
            throw new IllegalArgumentException("catalogue and issues are mutually exclusive");
        }
    }

    static EquipmentCatalogueContentLoadResult success(EquipmentCatalogue catalogue) {
        return new EquipmentCatalogueContentLoadResult(catalogue, List.of());
    }

    static EquipmentCatalogueContentLoadResult failure(List<ChallengeContentIssue> issues) {
        return new EquipmentCatalogueContentLoadResult(null, issues);
    }

    public boolean isSuccess() {
        return catalogue != null;
    }
}
