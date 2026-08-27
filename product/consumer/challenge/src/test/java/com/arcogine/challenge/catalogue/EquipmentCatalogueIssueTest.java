package com.arcogine.challenge.catalogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EquipmentCatalogueIssueTest {

    @Test
    void nullCodeIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new EquipmentCatalogueIssue(null, "path", "message"));
    }

    @Test
    void nullPathIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new EquipmentCatalogueIssue("code", null, "message"));
    }

    @Test
    void nullMessageIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new EquipmentCatalogueIssue("code", "path", null));
    }

    @Test
    void toStringIncludesPathCodeAndMessage() {
        EquipmentCatalogueIssue issue = new EquipmentCatalogueIssue("code", "path", "message");

        assertEquals("path [code]: message", issue.toString());
    }
}
