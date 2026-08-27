package com.arcogine.challenge.admissibility;

import com.arcogine.challenge.EquipmentCatalogueItemId;

/** One explicitly identified catalogue item placed at one game-owned floor cell. */
public record PlacedEquipment(
        EquipmentOccurrenceId occurrenceId,
        EquipmentCatalogueItemId itemId,
        GridPlacement placement) {

    public PlacedEquipment {
        if (occurrenceId == null) {
            throw new NullPointerException("occurrenceId");
        }
        if (itemId == null) {
            throw new NullPointerException("itemId");
        }
        if (placement == null) {
            throw new NullPointerException("placement");
        }
    }
}