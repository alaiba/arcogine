package com.arcogine.challenge.catalogue;

import java.util.List;

/** Deterministic, structured result of validating an {@link EquipmentCatalogue}. */
public record EquipmentCatalogueValidationResult(List<EquipmentCatalogueIssue> issues) {

    public EquipmentCatalogueValidationResult {
        issues = issues == null ? List.of() : List.copyOf(issues);
    }

    public static EquipmentCatalogueValidationResult valid() {
        return new EquipmentCatalogueValidationResult(List.of());
    }

    public boolean isValid() {
        return issues.isEmpty();
    }
}
