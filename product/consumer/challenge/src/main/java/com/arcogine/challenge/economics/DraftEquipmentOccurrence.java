package com.arcogine.challenge.economics;

import com.arcogine.challenge.EquipmentCatalogueItemId;

/**
 * A single occurrence of a catalogue item in a draft, carrying only what draft economics needs to
 * price it.
 *
 * <p>This economics input deliberately carries no placement, routing, operation assignment,
 * machine capability, canonical resource id, or runtime state. The game-owned candidate placement
 * is represented separately by {@link com.arcogine.challenge.admissibility.PlacedEquipment};
 * canonical Factory projection and runtime state are outside this construction-cost value.
 */
public record DraftEquipmentOccurrence(EquipmentCatalogueItemId itemId) {

    public DraftEquipmentOccurrence {
        if (itemId == null) {
            throw new NullPointerException("itemId");
        }
    }
}
