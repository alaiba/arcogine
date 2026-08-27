package com.arcogine.challenge.admissibility;

/** An explicit, game-owned identity for one equipment occurrence in a draft snapshot. */
public record EquipmentOccurrenceId(String value) {

    public EquipmentOccurrenceId {
        if (value == null) {
            throw new NullPointerException("value");
        }
    }
}