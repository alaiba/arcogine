package com.arcogine.challenge.economics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DraftEconomicsFailureTest {

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
