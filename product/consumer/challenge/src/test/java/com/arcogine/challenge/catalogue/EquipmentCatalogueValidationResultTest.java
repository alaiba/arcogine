package com.arcogine.challenge.catalogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class EquipmentCatalogueValidationResultTest {

    @Test
    void validFactoryProducesAnEmptyValidResult() {
        EquipmentCatalogueValidationResult result = EquipmentCatalogueValidationResult.valid();

        assertTrue(result.isValid());
        assertEquals(List.of(), result.issues());
    }

    @Test
    void nullIssuesDefaultsToEmpty() {
        EquipmentCatalogueValidationResult result = new EquipmentCatalogueValidationResult(null);

        assertEquals(List.of(), result.issues());
    }
}
