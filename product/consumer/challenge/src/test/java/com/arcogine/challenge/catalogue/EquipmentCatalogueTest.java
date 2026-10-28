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

    @Test
    void findByItemIdRejectsNullItemId() {
        EquipmentCatalogue catalogue = EquipmentCatalogueFixtures.referenceCatalogue();

        assertThrows(NullPointerException.class, () -> catalogue.findByItemId(null));
    }

    @Test
    void toStringIncludesOffers() {
        EquipmentCatalogue catalogue = EquipmentCatalogueFixtures.referenceCatalogue();

        assertTrue(catalogue.toString().contains("EquipmentCatalogue"));
    }

    @Test
    void semanticFingerprintIsStableAcrossOfferOrder() {
        EquipmentOffer cutter = EquipmentOffer.of(
                new EquipmentCatalogueItemId("equipment.cutter"), 5_000L);
        EquipmentOffer inspector = EquipmentOffer.of(
                new EquipmentCatalogueItemId("equipment.inspector"), 3_000L);

        EquipmentCatalogue first = new EquipmentCatalogue(List.of(cutter, inspector));
        EquipmentCatalogue reordered = new EquipmentCatalogue(List.of(inspector, cutter));

        assertEquals(first.semanticFingerprint(), reordered.semanticFingerprint());
    }

    @Test
    void semanticFingerprintIncludesQuantityLimitAndDistinguishesEmptyCatalogue() {
        EquipmentCatalogue unlimited = new EquipmentCatalogue(List.of(
                EquipmentOffer.of(new EquipmentCatalogueItemId("equipment.cutter"), 5_000L)));
        EquipmentCatalogue limited = new EquipmentCatalogue(List.of(
                EquipmentOffer.of(new EquipmentCatalogueItemId("equipment.cutter"), 5_000L, 2)));
        EquipmentCatalogue empty = new EquipmentCatalogue(List.of());

        assertTrue(!unlimited.semanticFingerprint().equals(limited.semanticFingerprint()));
        assertTrue(!empty.semanticFingerprint().equals(unlimited.semanticFingerprint()));
    }

    @Test
    void wellFormedFingerprintsAreUnchangedByStrictEncoding() {
        assertEquals(
                "2faa04e69e94747054b37990e8e191022f4a71a0ddc817e18cf59f1a6cf91f60",
                catalogueOf(offer("?", 1L)).semanticFingerprint());
        assertEquals(
                "a4ac38e191de74a78bbc651500657dfb72301831c910dec169db7b5c220d58f4",
                catalogueOf(offer("😀", 1L)).semanticFingerprint());
        assertEquals(
                "d86014a878c65c4851d7888cdadf039fd1c3727e52e5fadec81e2492ec87e855",
                catalogueOf(
                                offer("equipment.press", 12_000L, 1),
                                offer("equipment.cutter", 5_000L, 2),
                                offer("equipment.inspector", 3_000L))
                        .semanticFingerprint());
    }

    @Test
    void formerlyAliasedCataloguesAreNowInvalidOnTheIllFormedSide() {
        List<EquipmentCatalogue[]> pairs = List.of(
                new EquipmentCatalogue[] {catalogueOf(offer("\uD800", 1L)), catalogueOf(offer("\uD801", 1L))},
                new EquipmentCatalogue[] {catalogueOf(offer("\uDC00", 1L)), catalogueOf(offer("?", 1L))},
                new EquipmentCatalogue[] {catalogueOf(offer("\uD800", 1L)), catalogueOf(offer("?", 1L))},
                new EquipmentCatalogue[] {catalogueOf(offer("a\uD800", 1L)), catalogueOf(offer("a?", 1L))},
                new EquipmentCatalogue[] {
                    catalogueOf(offer("\uD800", 1L), offer("\uDBFF", 9L)),
                    catalogueOf(offer("\uD801", 1L), offer("\uDC00", 9L))
                });

        for (EquipmentCatalogue[] pair : pairs) {
            assertTrue(pair[0].offers().stream().anyMatch(o -> !valid(o)));
            assertTrue(!EquipmentCatalogueValidator.validate(pair[0]).isValid());
        }
        assertTrue(EquipmentCatalogueValidator.validate(pairs.get(1)[1]).isValid());
        assertTrue(EquipmentCatalogueValidator.validate(pairs.get(3)[1]).isValid());
    }

    @Test
    void fingerprintOfIllFormedItemFailsExplicitlyNamingTheItemPath() {
        EquipmentCatalogue catalogue = catalogueOf(offer("equipment.cutter", 1L), offer("a\uD800", 1L));

        IllegalStateException failure =
                assertThrows(IllegalStateException.class, catalogue::semanticFingerprint);

        assertTrue(failure.getMessage().contains("offers[1].itemId"), failure.getMessage());
    }

    private static boolean valid(EquipmentOffer offer) {
        return EquipmentCatalogueValidator.validate(new EquipmentCatalogue(List.of(offer))).isValid();
    }

    private static EquipmentCatalogue catalogueOf(EquipmentOffer... offers) {
        return new EquipmentCatalogue(List.of(offers));
    }

    private static EquipmentOffer offer(String itemId, long cost) {
        return EquipmentOffer.of(new EquipmentCatalogueItemId(itemId), cost);
    }

    private static EquipmentOffer offer(String itemId, long cost, int limit) {
        return EquipmentOffer.of(new EquipmentCatalogueItemId(itemId), cost, limit);
    }

    @Test
    void explicitIdentityIsRetained() {
        EquipmentCatalogueIdentity identity = new EquipmentCatalogueIdentity("catalogue.test", "2");
        EquipmentCatalogue catalogue = new EquipmentCatalogue(identity, List.of());

        assertEquals(identity, catalogue.identity());
    }
}
