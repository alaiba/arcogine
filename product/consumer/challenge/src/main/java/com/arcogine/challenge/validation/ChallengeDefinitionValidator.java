package com.arcogine.challenge.validation;

import com.arcogine.challenge.ChallengeDefinition;
import com.arcogine.challenge.EquipmentCatalogueItemId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Deterministic validation of whether a {@link ChallengeDefinition}'s content is internally
 * valid.
 *
 * <p>This answers a game-owned question -- "is this challenge content internally valid?" -- and
 * is a distinct validation domain from Arcogine's {@code FactoryModelValidator}, which answers
 * "is this projected production system executable by Arcogine?" This validator does not call,
 * extend, or reuse {@code FactoryModelValidator}, and it does not decide whether the challenge's
 * catalogue identities resolve to real equipment offers, whether its budget affords anything, or
 * whether its deadline is achievable -- those remain later Challenge Readiness slices or
 * Arcogine production semantics.
 *
 * <p>Validation never mutates the definition, never consults wall-clock time, random state, or
 * any runtime/session lookup, and iterates the definition's own declared collections in their
 * existing order, so repeated validation of an identical definition always produces an equal
 * result with equal issue ordering.
 */
public final class ChallengeDefinitionValidator {

    private ChallengeDefinitionValidator() {}

    public static ChallengeDefinitionValidationResult validate(ChallengeDefinition definition) {
        List<ChallengeDefinitionIssue> issues = new ArrayList<>();

        if (isBlank(definition.identity().id())) {
            issues.add(new ChallengeDefinitionIssue(
                    "identity.id.blank", "identity.id", "must be present and non-blank"));
        }
        if (isBlank(definition.identity().version())) {
            issues.add(new ChallengeDefinitionIssue(
                    "identity.version.blank", "identity.version", "must be present and non-blank"));
        }

        if (definition.floor().width() <= 0) {
            issues.add(new ChallengeDefinitionIssue(
                    "floor.width.not-positive", "floor.width", "must be > 0"));
        }
        if (definition.floor().height() <= 0) {
            issues.add(new ChallengeDefinitionIssue(
                    "floor.height.not-positive", "floor.height", "must be > 0"));
        }

        if (definition.startingBudget() < 0) {
            issues.add(new ChallengeDefinitionIssue(
                    "startingBudget.negative", "startingBudget", "must be >= 0"));
        }

        if (isBlank(definition.workload().productReference())) {
            issues.add(new ChallengeDefinitionIssue(
                    "workload.productReference.blank",
                    "workload.productReference",
                    "must be present and non-blank"));
        }
        if (definition.workload().requiredQuantity() <= 0) {
            issues.add(new ChallengeDefinitionIssue(
                    "workload.requiredQuantity.not-positive",
                    "workload.requiredQuantity",
                    "must be > 0"));
        }

        Set<EquipmentCatalogueItemId> seenEquipment = new HashSet<>();
        List<EquipmentCatalogueItemId> availableEquipment = definition.availableEquipment();
        for (int i = 0; i < availableEquipment.size(); i++) {
            EquipmentCatalogueItemId item = availableEquipment.get(i);
            String path = "availableEquipment[" + i + "]";
            if (isBlank(item.value())) {
                issues.add(new ChallengeDefinitionIssue(
                        "availableEquipment.id.blank", path, "must be present and non-blank"));
                continue;
            }
            if (!seenEquipment.add(item)) {
                issues.add(new ChallengeDefinitionIssue(
                        "availableEquipment.id.duplicate",
                        path,
                        "duplicate catalogue item id: " + item.value()));
            }
        }

        if (definition.deadline() <= 0) {
            issues.add(new ChallengeDefinitionIssue(
                    "deadline.not-positive", "deadline", "must be > 0"));
        }

        if (isBlank(definition.evaluationPolicy().id())) {
            issues.add(new ChallengeDefinitionIssue(
                    "evaluationPolicy.id.blank",
                    "evaluationPolicy.id",
                    "must be present and non-blank"));
        }
        if (isBlank(definition.evaluationPolicy().version())) {
            issues.add(new ChallengeDefinitionIssue(
                    "evaluationPolicy.version.blank",
                    "evaluationPolicy.version",
                    "must be present and non-blank"));
        }

        return new ChallengeDefinitionValidationResult(issues);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
