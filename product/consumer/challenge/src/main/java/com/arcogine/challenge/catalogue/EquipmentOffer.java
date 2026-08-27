package com.arcogine.challenge.catalogue;

import com.arcogine.challenge.EquipmentCatalogueItemId;
import java.util.OptionalInt;

/**
 * An immutable, game-owned equipment offer: a purchasable catalogue entry, not a canonical
 * Arcogine production resource.
 *
 * <p>{@code EquipmentOffer} carries only what the game needs to price and (optionally) cap a
 * catalogue item -- it does not carry capability, executability, or runtime meaning. Construction
 * only establishes an immutable value; it does not decide whether the offer is part of a valid
 * catalogue. Use {@link EquipmentCatalogueValidator} for deterministic structured diagnostics.
 *
 * @param itemId game-owned identity of the catalogue item this offer represents
 * @param purchaseCostCredits non-negative game-owned purchase cost, in credits
 * @param quantityLimit optional positive limit on how many occurrences of this item a challenge
 *     may allow; empty means unlimited
 */
public record EquipmentOffer(
        EquipmentCatalogueItemId itemId, long purchaseCostCredits, OptionalInt quantityLimit) {

    public EquipmentOffer {
        if (itemId == null) {
            throw new NullPointerException("itemId");
        }
        if (quantityLimit == null) {
            throw new NullPointerException("quantityLimit");
        }
    }

    public static EquipmentOffer of(EquipmentCatalogueItemId itemId, long purchaseCostCredits) {
        return new EquipmentOffer(itemId, purchaseCostCredits, OptionalInt.empty());
    }

    public static EquipmentOffer of(
            EquipmentCatalogueItemId itemId, long purchaseCostCredits, int quantityLimit) {
        return new EquipmentOffer(itemId, purchaseCostCredits, OptionalInt.of(quantityLimit));
    }
}
