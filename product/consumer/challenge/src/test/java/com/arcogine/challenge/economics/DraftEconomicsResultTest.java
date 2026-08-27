package com.arcogine.challenge.economics;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DraftEconomicsResultTest {

    @Test
    void bothEconomicsAndFailurePresentIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DraftEconomicsResult(
                        new DraftEconomics(0L, 0L, 0L), new DraftEconomicsFailure("code", "message")));
    }

    @Test
    void neitherEconomicsNorFailurePresentIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new DraftEconomicsResult(null, null));
    }
}
