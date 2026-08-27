package com.arcogine.challenge.catalogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.challenge.EquipmentCatalogueItemId;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class EquipmentOfferTest {

    @Test
    void nullItemIdIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new EquipmentOffer(null, 1L, OptionalInt.empty()));
    }

    @Test
    void nullQuantityLimitIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new EquipmentOffer(new EquipmentCatalogueItemId("equipment.cutter"), 1L, null));
    }

    @Test
    void factoryWithoutLimitLeavesQuantityLimitEmpty() {
        EquipmentOffer offer =
                EquipmentOffer.of(new EquipmentCatalogueItemId("equipment.cutter"), 5_000L);

        assertTrue(offer.quantityLimit().isEmpty());
    }

    @Test
    void factoryWithLimitCarriesIt() {
        EquipmentOffer offer =
                EquipmentOffer.of(new EquipmentCatalogueItemId("equipment.cutter"), 5_000L, 3);

        assertEquals(OptionalInt.of(3), offer.quantityLimit());
    }

    @Test
    void equalityIsValueBased() {
        EquipmentOffer first = EquipmentOffer.of(new EquipmentCatalogueItemId("equipment.cutter"), 5_000L, 3);
        EquipmentOffer second = EquipmentOffer.of(new EquipmentCatalogueItemId("equipment.cutter"), 5_000L, 3);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
