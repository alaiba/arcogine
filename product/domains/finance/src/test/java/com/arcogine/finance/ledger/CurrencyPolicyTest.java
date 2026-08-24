package com.arcogine.finance.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.Test;

/** Locks down the canonical monetary quantization policy so it can't drift silently. */
class CurrencyPolicyTest {

    @Test
    void scaleIsTwoDecimalPlaces() {
        assertEquals(2, CurrencyPolicy.SCALE);
    }

    @Test
    void roundingIsHalfUp() {
        assertEquals(RoundingMode.HALF_UP, CurrencyPolicy.ROUNDING);
    }

    @Test
    void quantizesToTwoDecimalPlaces() {
        assertEquals(new BigDecimal("12.35"), CurrencyPolicy.quantize(new BigDecimal("12.345")));
        assertEquals(new BigDecimal("12.30"), CurrencyPolicy.quantize(new BigDecimal("12.3")));
        assertEquals(new BigDecimal("12.00"), CurrencyPolicy.quantize(new BigDecimal("12")));
    }

    @Test
    void roundsHalfUpAtTheMidpoint() {
        // 0.005 rounds up to 0.01 under HALF_UP, not down to 0.00 (HALF_EVEN would go down here).
        assertEquals(new BigDecimal("0.01"), CurrencyPolicy.quantize(new BigDecimal("0.005")));
    }
}
