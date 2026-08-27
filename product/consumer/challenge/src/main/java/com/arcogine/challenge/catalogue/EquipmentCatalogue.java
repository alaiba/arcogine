package com.arcogine.challenge.catalogue;

import com.arcogine.challenge.EquipmentCatalogueItemId;
import java.util.List;
import java.util.Optional;

/**
 * An immutable, game-owned collection of {@link EquipmentOffer}s available to a content context.
 *
 * <p>A challenge's {@code availableEquipment} is a subset of the identities this catalogue may
 * offer -- this type does not decide which offers a particular challenge allows; that
 * relationship is validated separately (see {@link
 * com.arcogine.challenge.catalogue.EquipmentCatalogueValidator#validateChallengeResolution}).
 *
 * <p>Construction only establishes an immutable value; it does not decide whether the catalogue's
 * content (e.g. duplicate item ids) is valid. Use {@link EquipmentCatalogueValidator} for
 * deterministic structured diagnostics.
 */
public final class EquipmentCatalogue {

    private final List<EquipmentOffer> offers;

    public EquipmentCatalogue(List<EquipmentOffer> offers) {
        // List.copyOf rejects null elements with its own NullPointerException.
        this.offers = offers == null ? List.of() : List.copyOf(offers);
    }

    /** All offers, in declaration order. */
    public List<EquipmentOffer> offers() {
        return offers;
    }

    /**
     * Resolves an offer by item identity.
     *
     * <p>When {@code offers} contains duplicate item ids, this returns the first matching offer in
     * declaration order; {@link EquipmentCatalogueValidator} is the source of truth for rejecting
     * duplicates.
     */
    public Optional<EquipmentOffer> findByItemId(EquipmentCatalogueItemId itemId) {
        if (itemId == null) {
            throw new NullPointerException("itemId");
        }
        for (EquipmentOffer offer : offers) {
            if (offer.itemId().equals(itemId)) {
                return Optional.of(offer);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EquipmentCatalogue other && offers.equals(other.offers);
    }

    @Override
    public int hashCode() {
        return offers.hashCode();
    }

    @Override
    public String toString() {
        return "EquipmentCatalogue" + offers;
    }
}
