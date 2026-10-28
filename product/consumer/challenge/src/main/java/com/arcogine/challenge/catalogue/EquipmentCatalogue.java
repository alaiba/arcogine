package com.arcogine.challenge.catalogue;

import com.arcogine.challenge.EquipmentCatalogueItemId;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
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

    private final EquipmentCatalogueIdentity identity;
    private final List<EquipmentOffer> offers;

    public EquipmentCatalogue(List<EquipmentOffer> offers) {
        this(new EquipmentCatalogueIdentity("catalogue.unversioned", "1"), offers);
    }

    public EquipmentCatalogue(EquipmentCatalogueIdentity identity, List<EquipmentOffer> offers) {
        if (identity == null) {
            throw new NullPointerException("identity");
        }
        this.identity = identity;
        // List.copyOf rejects null elements with its own NullPointerException.
        this.offers = offers == null ? List.of() : List.copyOf(offers);
    }

    /** Identity and semantic version of the catalogue and its economy rules. */
    public EquipmentCatalogueIdentity identity() {
        return identity;
    }

    /** All offers, in declaration order. */
    public List<EquipmentOffer> offers() {
        return offers;
    }

    /**
     * Deterministic fingerprint of the result-affecting catalogue and quantity-limit content.
     *
     * <p>Item identities are encoded as strict UTF-8 and are never character-replaced. This is
     * defined only for item text that passes {@link EquipmentCatalogueValidator#validate}; an
     * item identity containing an unpaired surrogate fails with {@link IllegalStateException}
     * naming the offending {@code offers[i].itemId} path rather than yielding a fingerprint
     * shared with a different catalogue.
     */
    public String semanticFingerprint() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(bytes);
            List<Integer> order = new java.util.ArrayList<>();
            for (int i = 0; i < offers.size(); i++) {
                order.add(i);
            }
            order.sort(Comparator.comparing(i -> offers.get(i).itemId().value()));
            for (int index : order) {
                writeOffer(output, offers.get(index), "offers[" + index + "].itemId");
            }
            output.flush();
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(bytes.toByteArray());
            StringBuilder fingerprint = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                fingerprint.append(Character.forDigit((value >>> 4) & 0x0f, 16));
                fingerprint.append(Character.forDigit(value & 0x0f, 16));
            }
            return fingerprint.toString();
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("unable to fingerprint catalogue", exception);
        }
    }

    private static void writeOffer(DataOutputStream output, EquipmentOffer offer, String itemPath) {
        try {
            byte[] itemId;
            try {
                ByteBuffer encoded = StandardCharsets.UTF_8.newEncoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .encode(CharBuffer.wrap(offer.itemId().value()));
                itemId = new byte[encoded.remaining()];
                encoded.get(itemId);
            } catch (CharacterCodingException exception) {
                throw new IllegalStateException(
                        "catalogue item identity is not well-formed Unicode text at " + itemPath, exception);
            }
            output.writeInt(itemId.length);
            output.write(itemId);
            output.writeLong(offer.purchaseCostCredits());
            output.writeBoolean(offer.quantityLimit().isPresent());
            if (offer.quantityLimit().isPresent()) {
                output.writeInt(offer.quantityLimit().getAsInt());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("unable to encode catalogue", exception);
        }
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
        return obj instanceof EquipmentCatalogue other
            && identity.equals(other.identity) && offers.equals(other.offers);
    }

    @Override
    public int hashCode() {
        return 31 * identity.hashCode() + offers.hashCode();
    }

    @Override
    public String toString() {
        return "EquipmentCatalogue[" + identity + ", " + offers + "]";
    }
}
