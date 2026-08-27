package com.arcogine.challenge.economics;

import com.arcogine.challenge.EquipmentCatalogueItemId;

/**
 * A single occurrence of a catalogue item in a draft, carrying only what draft economics needs to
 * price it.
 *
 * <p>This deliberately carries no position, orientation, footprint, routing, operation
 * assignment, machine capability, canonical resource id, or runtime state -- those belong to a
 * later projection/placement slice, not to construction-cost calculation.
 */
public record DraftEquipmentOccurrence(EquipmentCatalogueItemId itemId) {

    public DraftEquipmentOccurrence {
        if (itemId == null) {
            throw new NullPointerException("itemId");
        }
    }
}
