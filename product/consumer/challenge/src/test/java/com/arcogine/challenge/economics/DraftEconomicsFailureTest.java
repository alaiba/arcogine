package com.arcogine.challenge.economics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcogine.challenge.catalogue.EquipmentCatalogueIssue;
import com.arcogine.challenge.catalogue.EquipmentCatalogueValidationResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class DraftEconomicsFailureTest {

    @Test
    void invalidCatalogueRejectsNullValidation() {
        assertThrows(NullPointerException.class, () -> DraftEconomicsFailure.invalidCatalogue(null));
    }

    @Test
    void invalidCatalogueSummarizesTheFirstIssue() {
        EquipmentCatalogueValidationResult validation = new EquipmentCatalogueValidationResult(
                List.of(new EquipmentCatalogueIssue("code", "path", "message")));

        DraftEconomicsFailure failure = DraftEconomicsFailure.invalidCatalogue(validation);

        assertEquals("catalogue.invalid", failure.code());
        assertTrue(failure.message().contains("1 issue"));
        assertTrue(failure.message().contains("path [code]: message"));
    }

    @Test
    void nullCodeIsRejected() {
        assertThrows(NullPointerException.class, () -> new DraftEconomicsFailure(null, "message"));
    }

    @Test
    void nullMessageIsRejected() {
        assertThrows(NullPointerException.class, () -> new DraftEconomicsFailure("code", null));
    }

    @Test
    void toStringIncludesCodeAndMessage() {
        DraftEconomicsFailure failure = new DraftEconomicsFailure("code", "message");

        assertEquals("[code]: message", failure.toString());
    }
}
