package com.arcogine.challenge.catalogue;

/** Stable identity and semantic version of game-owned catalogue and economy rules. */
public record EquipmentCatalogueIdentity(String id, String version) {

    public EquipmentCatalogueIdentity {
        if (id == null) {
            throw new NullPointerException("id");
        }
        if (version == null) {
            throw new NullPointerException("version");
        }
    }
}