package com.arcogine.challenge.economics;

import com.arcogine.challenge.EquipmentCatalogueItemId;
import com.arcogine.challenge.catalogue.EquipmentCatalogueValidationResult;

/**
 * A structured, deterministic reason {@link DraftEconomicsCalculator} could not derive {@link
 * DraftEconomics} for a given input.
 *
 * @param code stable, machine-readable failure code
 * @param message human-readable description of the failure
 */
public record DraftEconomicsFailure(String code, String message) {

    public DraftEconomicsFailure {
        if (code == null) {
            throw new NullPointerException("code");
        }
        if (message == null) {
            throw new NullPointerException("message");
        }
    }

    public static DraftEconomicsFailure invalidCatalogue(EquipmentCatalogueValidationResult validation) {
        if (validation == null) {
            throw new NullPointerException("validation");
        }
        return new DraftEconomicsFailure(
                "catalogue.invalid",
                "catalogue failed internal validation (" + validation.issues().size() + " issue(s)): "
                        + validation.issues().get(0));
    }

    public static DraftEconomicsFailure unknownCatalogueItem(EquipmentCatalogueItemId itemId) {
        return new DraftEconomicsFailure(
                "draft.occurrence.unknown-catalogue-item",
                "no catalogue offer for item id: " + itemId.value());
    }

    public static DraftEconomicsFailure costOverflow() {
        return new DraftEconomicsFailure(
                "draft.cost.overflow", "committed construction cost overflowed a 64-bit total");
    }

    @Override
    public String toString() {
        return "[" + code + "]: " + message;
    }
}
