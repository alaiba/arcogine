package com.arcogine.challenge.catalogue;

import com.arcogine.challenge.EquipmentCatalogueItemId;
import java.util.List;

/**
 * A reference catalogue matching {@code ChallengeFixtures.referenceChallenge()}'s
 * available-equipment identities, with simple fixed test prices.
 */
public final class EquipmentCatalogueFixtures {

    public static final long CUTTER_COST_CREDITS = 5_000L;
    public static final long ASSEMBLY_STATION_COST_CREDITS = 8_000L;
    public static final long INSPECTOR_COST_CREDITS = 3_000L;

    private EquipmentCatalogueFixtures() {}

    public static EquipmentCatalogue referenceCatalogue() {
                return new EquipmentCatalogue(new EquipmentCatalogueIdentity("catalogue.challenge.factory-basics", "1"), List.of(
                EquipmentOffer.of(
                        new EquipmentCatalogueItemId("equipment.cutter"), CUTTER_COST_CREDITS),
                EquipmentOffer.of(
                        new EquipmentCatalogueItemId("equipment.assembly-station"),
                        ASSEMBLY_STATION_COST_CREDITS),
                EquipmentOffer.of(
                        new EquipmentCatalogueItemId("equipment.inspector"),
                        INSPECTOR_COST_CREDITS)));
    }
}
