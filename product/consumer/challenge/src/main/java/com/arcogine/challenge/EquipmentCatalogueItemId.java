package com.arcogine.challenge;

/**
 * Game-owned identity of an item in the challenge's equipment catalogue.
 *
 * <p>This is deliberately distinct from Arcogine's canonical {@code ResourceDefinition}: a
 * catalogue item is a game-owned purchasable equipment offer, not a canonical production
 * resource. This type does not resolve this identity against any catalogue -- it only carries it.
 */
public record EquipmentCatalogueItemId(String value) {

    public EquipmentCatalogueItemId {
        if (value == null) {
            throw new NullPointerException("value");
        }
    }
}
