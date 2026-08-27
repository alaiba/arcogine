package com.arcogine.challenge.catalogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.challenge.EquipmentCatalogueItemId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EquipmentCatalogueTest {

    @Test
    void findsOfferByItemId() {
        EquipmentCatalogue catalogue = EquipmentCatalogueFixtures.referenceCatalogue();

        Optional<EquipmentOffer> found =
                catalogue.findByItemId(new EquipmentCatalogueItemId("equipment.cutter"));

        assertTrue(found.isPresent());
        assertEquals(EquipmentCatalogueFixtures.CUTTER_COST_CREDITS, found.get().purchaseCostCredits());
    }

    @Test
    void unknownItemIdResolvesToEmpty() {
        EquipmentCatalogue catalogue = EquipmentCatalogueFixtures.referenceCatalogue();

        assertTrue(catalogue
                .findByItemId(new EquipmentCatalogueItemId("equipment.unknown"))
                .isEmpty());
    }

    @Test
    void callerMutationOfSourceListCannotAlterCatalogue() {
        List<EquipmentOffer> source = new ArrayList<>(EquipmentCatalogueFixtures.referenceCatalogue().offers());
        EquipmentCatalogue catalogue = new EquipmentCatalogue(source);

        source.clear();

        assertEquals(3, catalogue.offers().size());
    }

    @Test
    void offersListIsImmutable() {
        EquipmentCatalogue catalogue = EquipmentCatalogueFixtures.referenceCatalogue();

        assertThrows(UnsupportedOperationException.class, () -> catalogue
                .offers()
                .add(EquipmentOffer.of(new EquipmentCatalogueItemId("equipment.extra"), 1L)));
    }

    @Test
    void nullOfferEntryIsRejected() {
        List<EquipmentOffer> withNull = new ArrayList<>();
        withNull.add(null);

        assertThrows(NullPointerException.class, () -> new EquipmentCatalogue(withNull));
    }

    @Test
    void nullOffersDefaultsToEmpty() {
        EquipmentCatalogue catalogue = new EquipmentCatalogue(null);

        assertEquals(List.of(), catalogue.offers());
    }

    @Test
    void equalityIsValueBased() {
        EquipmentCatalogue first = EquipmentCatalogueFixtures.referenceCatalogue();
        EquipmentCatalogue second = EquipmentCatalogueFixtures.referenceCatalogue();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
