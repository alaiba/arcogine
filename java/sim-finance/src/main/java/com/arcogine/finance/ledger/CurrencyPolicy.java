package com.arcogine.finance.ledger;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The canonical monetary quantization policy for Arcogine's single simulation currency: any
 * amount entering Finance from a {@code double}-based economic/commercial calculation is
 * quantized to this scale, using this rounding mode, at the point it becomes a {@link Posting}
 * amount. This is what "the same amount" means across the double/BigDecimal boundary --
 * converting a double to BigDecimal without a stated scale/rounding policy would just move
 * floating-point artifacts across the boundary instead of resolving them, and two independent
 * conversions of the same economic quantity could round differently and appear to disagree.
 *
 * <p>This does not imply Arcogine models multiple currencies or currency conversion -- there is
 * exactly one simulation currency, and this is its quantization rule.
 */
public final class CurrencyPolicy {

    /** Currency amounts are quantized to 2 decimal places (i.e. cents). */
    public static final int SCALE = 2;

    /** Standard "round half up" -- the conventional rounding for currency amounts. */
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private CurrencyPolicy() {}

    public static BigDecimal quantize(BigDecimal amount) {
        return amount.setScale(SCALE, ROUNDING);
    }
}
