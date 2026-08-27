package com.arcogine.challenge.catalogue;

/**
 * A single deterministic diagnostic produced by {@link EquipmentCatalogueValidator}.
 *
 * @param code stable, machine-readable issue code (e.g. {@code "offers.purchaseCost.negative"})
 * @param path field path the issue applies to (e.g. {@code "offers[0].purchaseCostCredits"})
 * @param message human-readable description of the issue
 */
public record EquipmentCatalogueIssue(String code, String path, String message) {

    public EquipmentCatalogueIssue {
        if (code == null) {
            throw new NullPointerException("code");
        }
        if (path == null) {
            throw new NullPointerException("path");
        }
        if (message == null) {
            throw new NullPointerException("message");
        }
    }

    @Override
    public String toString() {
        return path + " [" + code + "]: " + message;
    }
}
